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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mrpg.world.Cell;
import mrpg.world.Tile;
import mrpg.world.World;


public class History {
	private final ArrayList<Entry> entries = new ArrayList<Entry>();
	private int redoAt = 0;
	public Listener listener;
	public World world;
	
	public void clearHistory(){
		entries.clear(); redoAt = 0;
		if(listener != null) listener.historyChanged();
	}
	public boolean addEntry(Entry e){
		if(e.active.size() == 0) return false;
		if(entries.size() > 0) entries.get(entries.size()-1).compact();
		entries.subList(redoAt, entries.size()).clear(); entries.add(e); redoAt = entries.size();
		if(listener != null) listener.historyChanged();
		return true;
	}
	public void undo(){
		if(redoAt > 0){
			if(entries.size() > 0) entries.get(entries.size()-1).compact();
			redoAt--; entries.get(redoAt).undo(world);
			if(listener != null) listener.historyChanged();
		}
	}
	public void redo(){
		if(redoAt < entries.size()){
			if(entries.size() > 0) entries.get(entries.size()-1).compact();
			entries.get(redoAt).redo(world); redoAt++;
			if(listener != null) listener.historyChanged();
		}
	}
	
	public boolean hasUndo(){return redoAt > 0;}
	public int redoPos(){return redoAt;}
	public boolean hasRedo(){return redoAt < entries.size();}
	
	public static interface Listener {
		public void historyChanged();
	};
	private static class TileChange {
		public Tile oldTile, newTile;
		public TileChange(Tile old, Tile _new){oldTile = old; newTile = _new;}
	}
	private static class ChangedTile {
		public final Tile oldTile, newTile; public final short x, y;
		public ChangedTile(int _x, int _y, Tile old, Tile _new){x = (short)_x; y = (short)_y; oldTile = old; newTile = _new;}
	}
	public static class Entry {
		private final int level; private HashMap<Point, TileChange> active;
		private ChangedTile[] changed = null;
		public Entry(int _level){level = _level; active = new HashMap<Point, TileChange>();}
		public void changeTile(int x, int y, Tile oldTile, Tile newTile){
			if(oldTile == newTile) return;
			Point p = new Point(x, y);
			TileChange t = active.get(p);
			if(t == null) active.put(p, new TileChange(oldTile, newTile));
			else {
				if(t.oldTile == newTile) active.remove(p); else t.newTile = newTile;
			}
		}
		public void compact(){
			if(active != null){
				changed = new ChangedTile[active.size()];
				int i = 0; for(Map.Entry<Point, TileChange> e : active.entrySet()){
					Point p = e.getKey(); TileChange c = e.getValue();
					changed[i++] = new ChangedTile(p.x, p.y, c.oldTile, c.newTile);
				}
				active = null;
			}
		}
		public void undo(World w){
			for(ChangedTile t : changed){Cell c = w.getCell(t.x, t.y); if(c != null) c.setTile(t.oldTile, level);}
		}
		public void redo(World w){
			for(ChangedTile t : changed) {Cell c = w.getCell(t.x, t.y); if(c != null) c.setTile(t.newTile, level);}
		}
	}
}
