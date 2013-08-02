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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class LMUReader implements LCFInputStream.Handler {
	//These are found in include/lmu_chunks.h
	private static final int CHIPSET=0x01, WIDTH=0x02, HEIGHT=0x03, LAYER1=0x47, LAYER2=0x48;
	public int width=20, height=15, chipset_id = 1; public LMTReader.Map map; public LDBReader.Chipset chipset;
	public final ArrayList<Short> lower = new ArrayList<Short>(), upper = new ArrayList<Short>();
	public LMUReader(File f) throws Exception {
		LCFInputStream in = new LCFInputStream(new BufferedInputStream(new FileInputStream(f)));
		if(!in.readString(in.readInt()).equals("LcfMapUnit")) throw new Exception();
		try{in.readChunks(this);}catch(Exception e){} in.close();
	}
	public boolean handle(LCFInputStream in, int id, int len) throws Exception {
		switch(id){
		case CHIPSET: chipset_id = in.readInt(); return true;
		case WIDTH: width = in.readInt(); return true;
		case HEIGHT: height = in.readInt(); return true;
		case LAYER1: readLayer(in, width, height, false); return true;
		case LAYER2: readLayer(in, width, height, true); return true;
		default: return false;
		}
	}
	private void readLayer(LCFInputStream in, int w, int h, boolean upper) throws Exception {
		for(int y=0; y<h; y++){
			for(int x=0; x<w; x++){
				int i = in.readShort(), id = -1;
				if(upper){
					id = i-10000;
				} else {
					id = (i <  3000)?  0 + i/1000 :
					(i == 3028)?  3 + 0 :
					(i == 3078)?  3 + 1 :
					(i == 3128)?  3 + 2 :
					(i <  5000)?  6 + (i-4000)/50 :
					(i <  5144)? 18 + i-5000 : 0;
				} int sx=-2, sy=-1;
				if(id >= 0){if(upper){
					sx = 18+(id%6); sy = id/6; if(sy >= 8){sx += 6; sy -= 8;} else sy += 8;
				} else {
					if(id >= 18){
						id -= 18; sx = 12+(id%6); sy = id/6; if(sy >= 16){sy -= 16; sx += 6;}
					} else {
						sx = -1; sy = id;
					}
				}} ArrayList<Short> ar = ((upper)?this.upper:lower); ar.add((short)sx); ar.add((short)sy);
			}
		}
	}
}
