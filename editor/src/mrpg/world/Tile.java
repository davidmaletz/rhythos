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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.io.IOException;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.editor.resource.Animation;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.editor.resource.TileResource;
import mrpg.export.WorldIO;

public class Tile {
	public final Image image; public final int x, y; public final Info info;
	public Tile(Image _image, int _x, int _y, Info _info){image = _image; x = _x; y = _y; info = _info;}
	protected static Point frameOffset(Tilemap t, Animation a, int frame){
		frame /= a.speed; frame %= a.numFrames(); int w = a.getWidth(); int f = a.getFrame(frame);
		int ts = t.getTileSize(); int x = (f%w)*ts*t.getTilesX(), y = (f/w)*ts*t.getTilesY();
		return new Point(x,y);
	}
	public void paint(Graphics g, int dx, int dy, int sx, int sy, int w, int h, ImageObserver observer){
		paint(g, null, 0, dx, dy, sx, sy, w, h, observer);
	}
	public void paint(Graphics g, int frame, int dx, int dy, int sx, int sy, int w, int h, ImageObserver observer){
		paint(g, (info.map == null)?null:info.map.getResource().getAnimation(), frame, dx, dy, sx, sy, w, h, observer);
	}
	public void paint(Graphics g, Animation a, int frame, int dx, int dy, int sx, int sy, int w, int h, ImageObserver observer){
		if(image != null){
			if(info.map != null && a != null){Point p = frameOffset(info.map, a, frame); sx += p.x; sy += p.y;}
			g.drawImage(image, dx, dy, dx+w, dy+h, x+sx, y+sy, x+sx+w, y+sy+h, observer);
		}
	}
	public static final Tile empty = new Tile(null, (short)0, (short)0, new Info(null, 0)); 
	
	public Tile refresh(Project p, World.PromptAdd prompt_add){
		if(info.map == null) return this;
		try{
			TileResource r = info.map.getResource(); Project p2 = WorkspaceBrowser.getProject(r);
			if(p2 == null) return Tile.empty; if(p2 != p) try{
					return ((TileResource)p.getById(r.getType(), r.getId())).getTilemap().getTile(info.index);
			} catch(Exception e){
				if(prompt_add.copyTileset()){
					if(!r.isCompatible(p)) r.copyAssets(p);
					r = (TileResource)Resource.readFile(r.copy(p.getFile(), p, false), p.editor);
					p.editor.getBrowser().addResource(r, p); MapEditor.doDeferredRead(true);
				} else return Tile.empty;
			} Tilemap t = r.getTilemap(); if(t == info.map) return this;
			else return t.getTile(info.index);
		}catch(Exception e){return Tile.empty;}
	}
	
	public void write(WorldIO tileIO) throws IOException {tileIO.writeTile(this);}
	public static Tile read(WorldIO tileIO) throws IOException {return tileIO.readTile();}
	
	public static class Info {
		public final Tilemap map; public final short index;
		public Info(Tilemap _map, int i){map = _map; index = (short)i;}
		public byte getWalkable(){return map.getWalkable(index);}
		public void setWalkable(byte w){map.setWalkable(index, w);}
	}
}
