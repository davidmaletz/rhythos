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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import mrpg.media.Audio;

import com.flagstone.transform.sound.DefineSound;
import com.flagstone.transform.sound.SoundFormat;
import com.flagstone.transform.util.sound.SoundFactory;

public class Sound {
	private int fmt, rate, channels, size, count; private byte data[]; private Audio.Clip cache;
	private Sound(DefineSound s) throws Exception {
		rate = s.getRate(); channels = s.getChannelCount(); size = s.getSampleSize();
		count = s.getSampleCount(); fmt = s.getFormat().getValue(); data = s.getSound(); 
	}
	public Sound(DataInputStream in) throws Exception {
		fmt = in.readInt(); rate = in.readInt(); channels = in.readInt(); size = in.readInt();
		count = in.readInt(); int len = in.readInt(); data = new byte[len]; in.read(data);
	}
	public DefineSound getSound(int i){
		return new DefineSound(i, SoundFormat.fromInt(fmt), rate, channels, size, count, data);
	}
	public int getFormat(){return fmt;}
	public int getRate(){return rate;}
	public int getChannelCount(){return channels;}
	public int getSampleSize(){return size;}
	public int getSampleCount(){return count;}
	public byte[] getData(){return data;}
	public void write(File f) throws Exception {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		write(out); out.flush(); out.close();
	}
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(fmt); out.writeInt(rate); out.writeInt(channels); out.writeInt(size);
		out.writeInt(count); out.writeInt(data.length); out.write(data);
	}
	public Sound read(File f) throws Exception {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		Sound ret = new Sound(in); in.close(); return ret;
	}
	public Audio.Clip getClip() throws Exception {
		if(cache == null) cache = Audio.getClip(this); return cache;
	}
	
	private static SoundFactory snd;
	public static Sound decode(File f) throws Exception {
		if(snd == null) snd = new SoundFactory(); snd.read(f);
		return new Sound(snd.defineSound(1));
	}
}
