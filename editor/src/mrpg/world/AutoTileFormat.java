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
package mrpg.world;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class AutoTileFormat {
	private final byte format[]; public final int tilesX, tilesY;
	public AutoTileFormat(byte fmt[], int tx, int ty) throws Exception {
		if(fmt.length != 256*8) throw new Exception(); format = fmt; tilesX = tx; tilesY = ty;
	}
	public AutoTileFormat(DataInputStream in) throws Exception {
		tilesX = in.read(); tilesY = in.read(); int sz = 256*8; format = new byte[sz]; int off = 0;
		while(off < sz){
			int read = in.read(format, off, sz-off); if(read == -1) throw new Exception(); off += read;
		}
	}
	public void write(DataOutputStream out) throws Exception {
		out.write(tilesX); out.write(tilesY); out.write(format);
	}
	public Coords getCoords(int i){
		return new Coords(format, i<<3);
	}
	public class Coords {
		public final int x1, y1, x2, y2, x3, y3, x4, y4;
		public Coords(byte fmt[], int i){
			x1 = fmt[i++]&0xFF; y1 = fmt[i++]&0xFF; x2 = fmt[i++]&0xFF; y2 = fmt[i++]&0xFF;
			x3 = fmt[i++]&0xFF; y3 = fmt[i++]&0xFF; x4 = fmt[i++]&0xFF; y4 = fmt[i++]&0xFF;
		}
		public int hashCode(){
			int ts = Math.max(tilesX, tilesY)*2;
			return y4*ts*7+x4*ts*6+y3*ts*5+x3*ts*4+y2*ts*3+x2*ts*2+y1*ts+x1;
		}
		public boolean equals(Object o){
			Coords c = (Coords)o;
			return x1 == c.x1 && y1 == c.y1 && x2 == c.x2 && y2 == c.y2 && x3 == c.x3 && y3 == c.y3 && x4 == c.x4 && y4 == c.y4;
		}
	}
}
