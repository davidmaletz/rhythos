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

import java.io.InputStream;
import java.io.OutputStream;

import mrpg.editor.resource.Image;
import mrpg.editor.resource.Media;

public abstract class Export {
	public static String IMAGE = "i", SOUND = "s", TILEMAP = "t", MAP = "m";
	public void addImage(Image i) throws Exception {addImage(i.getGraphic(), i.getId(), i.getFile().lastModified());}
	public abstract void addImage(Graphic b, long i, long modified) throws Exception ;
	public void addMedia(Media m) throws Exception {addSound(m.getSound(), m.getId(), m.getFile().lastModified());}
	public abstract void addSound(Sound s, long i, long modified) throws Exception ;
	public abstract void addData(byte[] header, InputStream in, String t, long i, long modified) throws Exception ;
	public void finish() throws Exception {}
	
	private static byte buf[] = new byte[4096];
	public static int writeAll(InputStream in, OutputStream out) throws Exception {
		int ret = 0; try{
			int n = 0; while(-1 != (n = in.read(buf))){out.write(buf, 0, n); ret += n;}
		}catch(Exception e){} in.close(); return ret;
	}
}
