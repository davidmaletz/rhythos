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


public class LMTReader implements LCFInputStream.Handler {
	//These are found in include/lmt_chunks.h
	private static final int NAME=0x01, PARENT=0x02, TYPE=0x04;
	public final ArrayList<Map> maps = new ArrayList<Map>(); private Map cur;
	public LMTReader(File f) throws Exception {
		LCFInputStream in = new LCFInputStream(new BufferedInputStream(new FileInputStream(f)));
		if(!in.readString(in.readInt()).equals("LcfMapTree")) throw new Exception();
		int count = in.readInt(); for(int i=0; i<count; i++){
			int id = in.readInt(); cur = new Map(); in.readChunks(this);
			if(cur.type == 1){
				for(int j=maps.size(); j<=id; j++) maps.add(null); maps.set(id, cur);
				Map parent = maps.get(cur.parent); if(parent != null){
					cur.parentMap = parent; parent.children.add(cur);
				}
			}
		} in.close();
	}
	public boolean handle(LCFInputStream in, int id, int len) throws Exception {
		switch(id){
		case NAME: cur.name = in.readString(len); return true;
		case PARENT: cur.parent = in.readInt(); return true;
		case TYPE: cur.type = in.readInt(); return true;
		default: return false;
		}
	}
	public static class Map {
		public String name; public int parent, type; public Map parentMap; public mrpg.editor.resource.Map resource;
		public final ArrayList<Map> children = new ArrayList<Map>();
		public Map(){name = "Unnamed"; parent = 0; type = 0;}
	}
}
