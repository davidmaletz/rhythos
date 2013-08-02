/*******************************************************************************
 * RPGMaker is a plugin which imports RPGMaker 2000 or 2003 maps into Rhythos.
 * Use at your own risk, this plugin is not feature-complete, and requires some
 * fixing up of maps and project structure after import.
 * 
 * The RPGMaker import code was inspired by EasyRPG
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
package mrpg.plugin.rpgmaker;

import java.io.FilterInputStream;
import java.io.InputStream;


public class LCFInputStream extends FilterInputStream {
	private byte buf[];
	public LCFInputStream(InputStream in){super(in);}
	public int readInt() throws Exception {
		int ret = 0; while(true){
			int tmp = super.read(); if(tmp == -1) throw new Exception(); if(tmp == 0 && ret == 0) return 0;
			ret |= tmp&0x7f;
			if((tmp&0x80) == 0) return ret;
			ret <<= 7;
		}
	}
	public String readString(int len) throws Exception {
		if(buf == null || len > buf.length) buf = new byte[len]; int off = 0;
		while(off < len){
			int l = super.read(buf, off, len-off); if(l == -1) throw new Exception(); off += l;
		} return new String(buf, 0, len, "UTF-8");
	}
    public int readShort() throws Exception {
        int ch1 = super.read(); int ch2 = super.read();
        if((ch1 | ch2) < 0) throw new Exception();
        return (ch2<<8)|(ch1<<0);
    }
    public void readChunks(Handler h) throws Exception {
    	while(true){
			int id = readInt(); if(id == 0) break;
			int len = readInt(); if(len == 0) continue;
			if(!h.handle(this, id, len)){while(len > 0) len -= in.skip(len);}
		}
    }
    public static interface Handler {
    	public boolean handle(LCFInputStream in, int id, int len) throws Exception ;
    }
}
