/*******************************************************************************
 * Rhythos Editor is a game editor and project management tool for making RPGs on top of the Rhythos Game system.
 * 
 * Copyright (C) 2013  David Maletz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mrpg.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import javazoom.spi.mpeg.sampled.convert.DecodedMpegAudioInputStream;
import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

public class Audio {
	private static final LinkedList<Class<? extends Clip>> clips = new LinkedList<Class<? extends Clip>>();
	
	public static void registerClip(Class<? extends Clip> clip){clips.add(clip);}
	public static void unregisterClip(Class<? extends Clip> clip){clips.remove(clip);}
	
	public static Clip getClip(byte[] bytes) throws Exception {
		try{
		LoopableInputStream in = new LoopableInputStream(new BufferedStream(new ByteArrayInputStream(bytes), bytes.length));
		for(Class<? extends Clip> clip : clips) try{
			in.resetStream();
			return clip.getConstructor(LoopableInputStream.class).newInstance(in);
		} catch(Exception e){}
		in.close();
		} catch(Exception ex){}
		throw new Exception();
	}
	
	public static interface Clip {
		public long length();
		public float framesPerSecond();
		public void start();
		public void stop();
		public void pause();
		public void setVolume(float vol);
		public void playFrame(long frame) throws Exception;
		public Clip clone();
	}
	private static class BufferedStream implements Runnable {
		private InputStream stream; private byte bytes[]; private int read=0, length;
		public BufferedStream(InputStream in, int len){stream = in; length = len; bytes = new byte[len]; new Thread(this).start();}
		
		public void run(){
			int end = length-4096;
			try{
			while(read<end){
				read += stream.read(bytes, read, 4096);
				synchronized(this){notifyAll();}
			}
			while(read < length){
				read += stream.read(bytes, read, length-read);
				synchronized(this){notifyAll();}
			}
			}catch(Exception e){}
			try{stream.close();}catch(Exception e){}
		}
	}
	private static class LoopableInputStream extends InputStream {
		private final BufferedStream stream; private int at = 0, mark = -1;
		public LoopableInputStream(BufferedStream in){stream = in;}
		public int available() throws IOException {return stream.length-at;}
		public int length(){return stream.length;}
		public void close() throws IOException {}
		public synchronized void mark(int readlimit){mark = at;}
		public boolean markSupported() {return true;}
		private void readTo(int pos){while(pos > stream.read) try{synchronized(stream){stream.wait();}}catch(Exception e){}}
		public int read() throws IOException {if(at == stream.length) return -1; readTo(at+1); return stream.bytes[at++] & 0xFF;}
		public int read(byte[] b, int off, int len) throws IOException {
			len = Math.min(stream.length-at, len); readTo(at+len);
			System.arraycopy(stream.bytes, at, b, off, len); at += len;
			return len;
		}
		public int read(byte[] b) throws IOException {return read(b, 0, b.length);}
		public synchronized void reset() throws IOException {if(mark == -1) throw new IOException(); at = mark;}
		public void resetStream(){at = 0;}
		public long skip(long n) throws IOException {n = Math.min(stream.length-at, n); at += n; return n;}
	}
	public static class SampledClip implements Clip {
		private AudioInputStream stream; protected LoopableInputStream input;
		private final SourceDataLine line;
		private final byte buf[];
		private final FloatControl volume;
		private final boolean gain;
		private boolean playing = false;
		private long currentFrame = 0;
		public SampledClip(LoopableInputStream in) throws Exception {
			input = in; stream = getStream(in);
			AudioFormat format = stream.getFormat();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			line = (SourceDataLine)AudioSystem.getLine(info);
			line.open(stream.getFormat());
			if(line.isControlSupported(FloatControl.Type.VOLUME)){gain = false; volume = (FloatControl)line.getControl(FloatControl.Type.VOLUME);}
			else if(line.isControlSupported(FloatControl.Type.MASTER_GAIN)){gain = true; volume = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);}
			else throw new Exception();
			buf = new byte[getFrameSize()];
		}
		AudioInputStream getStream(LoopableInputStream in) throws Exception {return AudioSystem.getAudioInputStream(in);}
		protected void finalize(){
			stop();
			line.close();
			try{stream.close();}catch(Exception e){}
		}
		public long length(){return stream.getFrameLength()/50;}
		public float framesPerSecond(){return stream.getFormat().getFrameRate()/50;}
		public void start(){if(playing) return; playing = true; line.start();}
		public void stop(){if(!playing) return; playing = false; line.drain(); line.stop();}
		public void pause(){if(!playing) return; playing = false; line.stop();}
		public void setVolume(float vol){
			if(vol > 1) vol = 1;
			if(vol < 0) vol = 0;
			if(gain) vol = (float)(Math.log(vol)/Math.log(10)*20);
			volume.setValue(vol);
		}
		void skipFrames(AudioInputStream stream, long f) throws Exception {stream.skip(getFrameSize()*f);}
		int getFrameSize(){return stream.getFormat().getFrameSize()*50;}
		public void playFrame(long frame) throws Exception {
			if(frame < 0) return;
			frame = frame%length();
			if(!playing | frame == currentFrame) return;
			if(frame < currentFrame){currentFrame = 0; input.resetStream(); stream = getStream(input);}
			long delta = frame-currentFrame-1;
			if(delta > 0) skipFrames(stream, delta);
			currentFrame = frame-1;
			int frameSize = getFrameSize();
			int i = 0;
			while(i < frameSize){
				int read = stream.read(buf, i, frameSize-i);
				if(read == -1) break;
				i += read;
			}
			currentFrame = frame;
			if(i == 0) return;
			line.write(buf, 0, i);
		}
		public Clip clone(){try{return new SampledClip(new LoopableInputStream(input.stream));} catch(Exception e){return null;}}
	}
	public static class MP3Clip extends SampledClip {
		private static final MpegFormatConversionProvider provider = new MpegFormatConversionProvider();
		private static final MpegAudioFileReader reader = new MpegAudioFileReader();
		private long length; private float fps; private int frameSize, bytesPerFrame;
		public MP3Clip(LoopableInputStream in) throws Exception {
			super(in);
		}
		void skipFrames(AudioInputStream stream, long f) throws Exception {stream.skip(f*bytesPerFrame);}
		AudioInputStream getStream(LoopableInputStream in) throws Exception {
			AudioInputStream stream = reader.getAudioInputStream(in);
			AudioFormat format = stream.getFormat();
			AudioFormat decoded = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16, format.getChannels(), format.getChannels() * 2, format.getSampleRate(), false);
			DecodedMpegAudioInputStream s = (DecodedMpegAudioInputStream)provider.getAudioInputStream(decoded, stream);
			Map<String, Object> props = reader.getAudioFileFormat(in, in.length()).properties();
			length = ((Number)props.get("mp3.length.frames")).longValue();
			fps = ((Number)props.get("mp3.framerate.fps")).floatValue();
			frameSize = (int)(decoded.getFrameSize()*decoded.getFrameRate()/fps);
			bytesPerFrame = ((Number)props.get("mp3.framesize.bytes")).intValue();
			return s;
		}
		public long length(){return length;}
		public float framesPerSecond(){return fps;}
		int getFrameSize(){return frameSize;}
		public Clip clone(){try{return new MP3Clip(new LoopableInputStream(input.stream));} catch(Exception e){return null;}}
	}
	public static class Player implements Runnable {
		private long frame; private Clip clip = null; private boolean running = false;
		private FrameListener listener;
		public Player(){}
		public synchronized void setClip(Clip c){stop(); clip = c.clone();}
		public synchronized void play(){if(clip != null && !isRunning()){running = true; clip.start(); new Thread(this).start();}}
		public synchronized long getFrame(){return frame;}
		public synchronized void setFrame(long f){frame = f; if(listener != null) listener.playFrame(f);}
		public synchronized void setVolume(float vol){clip.setVolume(vol);}
		public synchronized void pause(){running = false; clip.pause();}
		public synchronized boolean isRunning(){return running;}
		public synchronized void stop(){frame = 0; if(listener != null) listener.playFrame(frame); running = false; if(clip != null) clip.stop();}
		public synchronized void setFrameListener(FrameListener l){listener = l;}
		
		public void run(){
			if(clip == null) return;
			long f; Clip c; synchronized(this){c = clip;}
			while(true){
				synchronized(this){if(!running || c != clip) return; f = frame; frame++; if(listener != null) listener.playFrame(f);}
				try{c.playFrame(f);}catch(Exception e){}
			}
		}
	}
	static{
		Audio.registerClip(Audio.SampledClip.class);
		Audio.registerClip(Audio.MP3Clip.class);
	}
}
