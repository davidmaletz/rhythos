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
package mrpg.editor;

import java.util.ArrayList;

import mrpg.world.Cell;
import mrpg.world.Tile;
import mrpg.world.World;


public class Clipboard {
	private final ArrayList<Tile> tiles = new ArrayList<Tile>(); private int width = 0;
	public Listener listener;
	public void clear(){tiles.clear(); width = 0; if(listener != null) listener.clipboardChanged();}
	public void set(World world, int x, int y, int width, int height, int level){
		tiles.clear(); this.width = width; tiles.ensureCapacity(width*height);
		int ex = x+width, ey = y+height;
		for(; y<ey; y++)
			for(int _x=x; _x<ex; _x++){
				Cell c = world.getCell(_x, y);
				tiles.add((c == null)?Tile.empty:c.getTile(level));
			}
		if(listener != null) listener.clipboardChanged();
	}
	public boolean hasData(){return width != 0;}
	public int getWidth(){return width;}
	public int getHeight(){return tiles.size()/width;}
	public Tile getTile(int x, int y){return tiles.get(y*width+x);}
	
	public static interface Listener {
		public void clipboardChanged();
	}
}
