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
package mrpg.editor.resource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.Icon;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.world.Tilemap;

public abstract class TileResource extends Resource {
	private static final long serialVersionUID = 6072902526037159097L;
	public static final String TILESET = "database";
	private static final Icon icon = MapEditor.getIcon(TILESET);
	protected TileResource(File f, MapEditor e){super(f,e);}
	public Icon getIcon(){return icon;}
	public abstract ImageResource getImage();
	public abstract Tilemap getTilemap();
	public abstract String getType();
	public void writeTileSize(DataOutputStream out) throws Exception {out.writeShort(getProject().tile_size);}
	public void checkTileSize(DataInputStream in) throws Exception {
		if(in.readShort() != WorkspaceBrowser.getProject(this).tile_size) throw new Exception();
	}
	public int getHeaderSize(){return super.getHeaderSize()+2;}
	public static int getSize(TileResource r){return (r==null)?2:10+r.getType().length();}
	public static void write(DataOutputStream out, TileResource r) throws Exception {
		if(r == null) out.writeUTF(""); else {out.writeUTF(r.getType()); out.writeLong(r.getId());}
	}
	public static TileResource read(DataInputStream in, Project p) throws Exception {
		String type = in.readUTF(); if(type.length() == 0) return null;
		long id = in.readLong(); try{return (TileResource)p.getById(type, id);}catch(Exception e){return null;}
	}
	public static boolean isTileResource(Resource r){return r instanceof TileResource;}
	public boolean isCompatible(Project p){return p.tile_size == WorkspaceBrowser.getProject(this).tile_size;}
	public static Tilemap refresh(Tilemap t, Project p) throws Exception {
		return ((TileResource)p.getById(t.getType(), t.getId())).getTilemap();
	}
}
