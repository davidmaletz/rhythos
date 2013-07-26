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
package mrpg.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import mrpg.media.Audio;

public class NekoExport extends Export {
	private File dir; private ArrayList<File> assets;
	public NekoExport(File f){dir = f; if(!dir.exists()) dir.mkdir(); assets = new ArrayList<File>();}
	private boolean checkAsset(File f, long modified){
		assets.add(f); return f.exists() && f.lastModified() >= modified;
	}
	public void addImage(Graphic b, long i, long modified) throws Exception {
		File f = new File(dir, "A"+IMAGE+Long.toHexString(i)); if(checkAsset(f, modified)) return;
		b.write(f);
	}
	private static byte RIFF[] = null; static{try{RIFF = "RIFF".getBytes("UTF-8");}catch(Exception e){}}
	private static byte WAV[] = null; static{try{WAV = "WAVEfmt ".getBytes("UTF-8");}catch(Exception e){}}
	private static byte DATA[] = null; static{try{DATA = "data".getBytes("UTF-8");}catch(Exception e){}}
	public void addSound(Sound s, long i, long modified) throws Exception {
		boolean mp3 = s.getFormat() == Audio.MP3;
		File f = new File(dir, "A"+SOUND+Long.toHexString(i)+((mp3)?".mp3":".wav")); if(checkAsset(f, modified)) return;
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
	public void addData(InputStream in, String t, long i, long modified) throws Exception {
		File f = new File(dir, "A"+t+Long.toHexString(i)); if(checkAsset(f, modified)) return;
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
		Export.writeAll(in, out); out.flush(); out.close();
	}
	public void finish() throws Exception {
		for(File f : dir.listFiles()) if(!assets.contains(f)) f.delete();
	}
}
