package mrpg.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import mrpg.media.Audio;

public class NekoExport extends Export {
	private File dir;
	public NekoExport(File f){dir = f; if(!dir.exists()) dir.mkdir();}
	public void addImage(Graphic b, long i, long modified) throws Exception {
		File f = new File(dir, "A"+IMAGE+Long.toHexString(i)); if(f.exists() && f.lastModified() >= modified) return;
		b.write(f);
	}
	private static byte RIFF[] = null; static{try{RIFF = "RIFF".getBytes("UTF-8");}catch(Exception e){}}
	private static byte WAV[] = null; static{try{WAV = "WAVEfmt ".getBytes("UTF-8");}catch(Exception e){}}
	private static byte DATA[] = null; static{try{DATA = "data".getBytes("UTF-8");}catch(Exception e){}}
	public void addSound(Sound s, long i, long modified) throws Exception {
		boolean mp3 = s.getFormat() == Audio.MP3;
		File f = new File(dir, "A"+SOUND+Long.toHexString(i)+((mp3)?".mp3":".wav")); if(f.exists() && f.lastModified() >= modified) return;
		FileOutputStream out = new FileOutputStream(f);
		if(mp3) out.write(s.getData());
		else {
			ByteBuffer bb = ByteBuffer.allocate(44); bb.order(ByteOrder.LITTLE_ENDIAN); byte[] data = s.getData();
			bb.put(RIFF); bb.putInt(36+data.length); bb.put(WAV); bb.putInt(16); bb.putShort((short)1);
			bb.putShort((short)s.getChannelCount()); bb.putInt(s.getRate()); int ss = s.getChannelCount()*s.getSampleSize();
			bb.putInt(s.getRate()*ss); bb.putShort((short)ss); bb.putShort((short)(s.getSampleSize()*8));
			bb.put(DATA); bb.putInt(data.length); out.write(bb.array()); out.write(data);
		} out.flush(); out.close();
	}
	public void addData(byte[] header, InputStream in, String t, long i, long modified) throws Exception {
		File f = new File(dir, "A"+t+Long.toHexString(i)); if(f.exists() && f.lastModified() >= modified) return;
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f)); if(header != null) out.write(header);
		Export.writeAll(in, out); out.flush(); out.close();
	}
}
