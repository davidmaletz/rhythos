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

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import mrpg.editor.MapEditor;
import mrpg.editor.resource.Project;
import mrpg.export.WorldIO;

public class World {
	private ArrayList<Cell> cells;
	private int width, height;
	public boolean wrapX = false, wrapY = false;
	public Image background = null;
	public World(int _width, int _height){
		width = _width; height = _height; cells = new ArrayList<Cell>(width*height);
		cells.addAll(Collections.nCopies(width*height, (Cell)null));
	}
	private World(int _width, int _height, boolean b){width = _width; height = _height; cells = new ArrayList<Cell>(width*height);}
	public World(World w){
		width = w.width; height = w.height; int sz = width*height; cells = new ArrayList<Cell>(sz);
		wrapX = w.wrapX; wrapY = w.wrapY; background = w.background;
		for(int i=0; i<sz; i++){
			Cell c = w.cells.get(i);
			cells.add((c == null)?null:new Cell(c, this));
		}
	}
	public void refresh(Project p){refresh(p, false);}
	public void refresh(Project p, boolean prompt_add){
		PromptAdd prompt = (prompt_add)?new PromptAdd():null;
		boolean u = false; for(Cell c : cells) if(c != null) u |= c.refresh(p, prompt);
		if(u) updateNeighbors();
	}
	public static class PromptAdd {
		private static final int PROMPT=0, DONT_ADD=1, ADD=2;
		private int state = PROMPT;
		public boolean copyTileset(){
			if(state == PROMPT){
				int i = JOptionPane.showConfirmDialog(MapEditor.instance, "The project you are moving this map to is missing tilesets the map needs. Do you wish to copy the tilesets over?", "Moving Map", JOptionPane.YES_NO_OPTION);
				state = (i == JOptionPane.YES_OPTION)?ADD:DONT_ADD;
			} return state == ADD;
		}
	}
	public int getWidth(){return width;} public int getHeight(){return height;}
	private final Cell OOB = new Cell(this, -1, -1);
	public Cell getCell(int x, int y){
		boolean oob = false;
		if(wrapX){x = x%width; if(x < 0) x += width;} else oob = oob || x < 0 || x >= width;
		if(wrapY){y = y%height; if(y < 0) y += height;} else oob = oob || y < 0 || y >= height;
		return (oob)?OOB:cells.get(y*width+x);
	}
	public Cell addCell(int x, int y){
		if(x < 0 || y < 0 || x >= width || y >= height) return null;
		Cell c = new Cell(this, x, y); cells.set(y*width+x, c);
		return c;
	}
	public boolean isNeighbor(int x, int y, Tile tile, int level){
		Cell c = getCell(x, y); return c == OOB || (c != null && c.getTile(level).info.map == tile.info.map);
	}
	public int getNeighbors(int x, int y, Tile tile, int level){
		int neighbors = Direction.NONE;
		if(isNeighbor(x-1, y, tile, level)) neighbors |= Direction.LEFT;
		if(isNeighbor(x+1, y, tile, level)) neighbors |= Direction.RIGHT;
		if(isNeighbor(x, y-1, tile, level)) neighbors |= Direction.UP;
		if(isNeighbor(x, y+1, tile, level)) neighbors |= Direction.DOWN;
		if(isNeighbor(x-1, y-1, tile, level)) neighbors |= Direction.UPPER_LEFT;
		if(isNeighbor(x+1, y-1, tile, level)) neighbors |= Direction.UPPER_RIGHT;
		if(isNeighbor(x-1, y+1, tile, level)) neighbors |= Direction.LOWER_LEFT;
		if(isNeighbor(x+1, y+1, tile, level)) neighbors |= Direction.LOWER_RIGHT;
		return neighbors;
	}
	public void updateNeighbors(){
		for(int _y = 0; _y<height; _y++)
			for(int _x=0; _x<width; _x++){
				Cell c = getCell(_x, _y); if(c == null) continue;
				int level = 0;
				for(Tile t : c){
					if(t == Tile.empty || !t.info.map.indexNeighbors()){level++; continue;}
					int neighbors = getNeighbors(_x, _y, t, level);
					c.tiles.set(level, t.info.map.getTile(neighbors));
					level++;
				}
			}
	}
	public void updateAdjacent(int x, int y, Tile tile, int level){
		for(int _y = y-1; _y<=y+1; _y++)
			for(int _x=x-1; _x<=x+1; _x++){
				Cell c = getCell(_x, _y); if(c == null) continue;
				Tile t = c.getTile(level); if(t == Tile.empty || !t.info.map.indexNeighbors()) continue;
				int neighbors = getNeighbors(_x, _y, t, level);
				c.tiles.set(level, t.info.map.getTile(neighbors));
			}
	}
	public void resize(int _width, int _height){
		ArrayList<Cell> _cells = new ArrayList<Cell>(_width*_height);
		for(int y=0; y<_height; y++)
			for(int x=0; x<_width; x++) _cells.add(getCell(x,y));
		width = _width; height = _height; cells = _cells;
	}
	
	public void write(WorldIO tileIO) throws IOException {
		tileIO.writeShort(width); tileIO.writeShort(height); int wrap = 0; if(wrapX) wrap |= 1; if(wrapY) wrap |= 2; tileIO.write(wrap);
		for(int i=0; i<width*height; i++){
			Cell c = cells.get(i);
			if(c == null) tileIO.write(0);
			else {
				int ct = 0;
				for(Tile t : c) if(t != Tile.empty) ct++;
				tileIO.write(ct); ct = 0;
				for(Tile t : c){
					if(t != Tile.empty){tileIO.write(ct); t.write(tileIO);}
					ct++;
				}
			}
		}
	}
	public static World read(WorldIO tileIO) throws IOException {
		int width = tileIO.readShort(); int height = tileIO.readShort();
		World w = new World(width, height, true);
		int wrap = tileIO.read(); w.wrapX = (wrap & 1) != 0; w.wrapY = (wrap & 2) != 0;
		for(int y=0; y<height; y++)
		for(int x=0; x<width; x++){
			int tiles = tileIO.read();
			if(tiles == 0) w.cells.add(null);
			else{
				Cell c = new Cell(w, x, y); w.cells.add(c);
				for(int t=0; t<tiles; t++){
					int level = tileIO.read();
					c.setTile(Tile.read(tileIO), level, false);
				}
			}
		}
		return w;
	}
}
