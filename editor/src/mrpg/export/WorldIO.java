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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import mrpg.editor.resource.Project;
import mrpg.world.Tile;

public class WorldIO {
	private final ArrayList<Long> tilemaps; private final ByteArrayOutputStream buf;
	private final DataOutputStream obuf; private final DataInputStream in; private final Project project;
	public WorldIO(Project p, DataInputStream _in) throws IOException {
		in = _in; buf = null; obuf = null; project = p;
		int sz = in.read(); tilemaps = new ArrayList<Long>(sz);
		for(int i=0; i<sz; i++) tilemaps.add(in.readLong());
	}
	public WorldIO(){
		in = null; buf = new ByteArrayOutputStream(); obuf = new DataOutputStream(buf);
		tilemaps = new ArrayList<Long>(255); project = null;
	}
	public Tile readTile() throws IOException {
		int map = in.read(); if(map == 255) return Tile.empty; short id = in.readShort();
		try{
			return project.getTilemapById(tilemaps.get(map)).getTilemap().getTile(id); 
		}catch(Exception e){return Tile.empty;}
	}
	public void writeTile(Tile t) throws IOException {
		if(t == Tile.empty){obuf.write(255); return;}
		long id = t.info.map.getId(); int i = tilemaps.indexOf(id); if(i == -1){
			i = tilemaps.size(); if(i == 255) throw new IOException("Only 255 Tilemaps per Map allowed."); tilemaps.add(id);
		} obuf.write(i); obuf.writeShort(t.info.index);
	}
	public void write(DataOutputStream out) throws IOException {
		int sz = tilemaps.size(); out.write(sz); for(int i=0; i<sz; i++) out.writeLong(tilemaps.get(i));
		out.write(buf.toByteArray());
	}
	public int read() throws IOException {return in.read();}
	public void write(int b) throws IOException {obuf.write(b);}
	public int readShort() throws IOException {return in.readShort();}
	public void writeShort(int b) throws IOException {obuf.writeShort(b);}
}
