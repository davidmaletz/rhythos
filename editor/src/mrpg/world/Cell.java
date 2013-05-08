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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import mrpg.editor.resource.Project;

public class Cell implements Iterable<Tile> {
	private World world; private int x, y;
	ArrayList<Tile> tiles = null;
	public Cell(World _world, int _x, int _y){world = _world; x = _x; y = _y;}
	public Cell(Cell c, World _world){
		world = _world; x = c.x; y = c.y;
		if(c.tiles == null) tiles = null;
		else tiles = new ArrayList<Tile>(c.tiles);
	}
	
	public boolean refresh(Project p){
		if(tiles != null){
			int sz = tiles.size(); boolean u = false; for(int i=0; i<sz; i++){
				Tile t1 = tiles.get(i); Tile t2 = t1.refresh(p); if(t1 != t2){
					tiles.set(i, t2); u = true;
				}
			} return u;
		} return false;
	}
	public Tile getTile(int level){return (tiles == null || level >= tiles.size())?Tile.empty:tiles.get(level);}
	public void setTile(Tile tile, int level){setTile(tile, level, true);}
	public void setTile(Tile tile, int level, boolean updateAdjacent){
		if(tiles == null) tiles = new ArrayList<Tile>(level+1);
		if(level >= tiles.size()){
			tiles.ensureCapacity(level+1);
			tiles.addAll(Collections.nCopies(level-tiles.size(), Tile.empty));
			tiles.add(tile);
		} else tiles.set(level, tile);
		if(updateAdjacent) world.updateAdjacent(x, y, tile, level);
	}
	
	public boolean hasTiles(){return tiles != null;}
	public Iterator<Tile> iterator(){return tiles.iterator();}
}
