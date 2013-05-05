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
package mrpg.editor.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import mrpg.display.WorldPanel;
import mrpg.editor.Clipboard;
import mrpg.editor.History;
import mrpg.world.Cell;
import mrpg.world.Tile;
import mrpg.world.World;


public class SelectTool implements Tool {
	private final WorldPanel world;
	private final History history; private History.Entry entry; private boolean addedEntry;
	private int sel_x1 = Integer.MIN_VALUE, sel_y1, sel_x2, sel_y2;
	private Tile bg_tiles[] = null;
	public Listener listener;
	public SelectTool(WorldPanel w, History h){world = w; history = h;}
	private static final float[] dashed = new float[]{8.0f};
	public void paint(Graphics g, int stX, int stY, int mouseX, int mouseY){
		if(sel_x1 != Integer.MIN_VALUE){
			Graphics2D g2d = (Graphics2D)g.create();
			int x = Math.min(sel_x1, sel_x2)*Tile.tile_size, y = Math.min(sel_y1, sel_y2)*Tile.tile_size,
			width = (Math.abs(sel_x2-sel_x1)+1)*Tile.tile_size-2, height = (Math.abs(sel_y2-sel_y1)+1)*Tile.tile_size-2;
			g2d.setPaint(Color.black); float f = world.getFrame()*3;
			g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8.0f, dashed, f));
			g2d.drawRect(x, y, width, height);
			g2d.setPaint(Color.white);
			g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8.0f, dashed, 8.0f+f));
			g2d.drawRect(x, y, width, height);
		}
	}
	public void repaint(int stX, int stY, int mouseX, int mouseY){
		double s = world.getScale();
		int x = Math.max(0, (int)Math.floor((Math.min(stX, mouseX)*Tile.tile_size-1)*s-1)),
		y = Math.max(0, (int)Math.floor((Math.min(stY, mouseY)*Tile.tile_size-1)*s-1));
		world.repaint(x, y, (int)Math.ceil(((Math.max(stX, mouseX)+1)*Tile.tile_size+2)*s+2)-x,
				(int)Math.ceil(((Math.max(stY, mouseY)+1)*Tile.tile_size+2)*s+2)-y);
	}
	public void updateSelection(int mouseX, int mouseY, int oldSelWidth, int oldSelHeight){}
	
	public void mouseDragged(int mouseX, int mouseY, int x, int y){
		if(sel_x1 == Integer.MIN_VALUE){
			sel_x1 = mouseX; sel_y1 = mouseY; sel_x2 = mouseX; sel_y2 = mouseY;
			repaint(mouseX, mouseY, mouseX, mouseY);
		}
	}
	private int clamp(int val, int min, int max){return Math.min(max, Math.max(min, val));}
	public void mouseDragged(int stX, int stY, int oldMouseX, int oldMouseY, int mouseX, int mouseY){
		if(bg_tiles != null && oldMouseX <= Math.max(sel_x1, sel_x2) && oldMouseX >= Math.min(sel_x1, sel_x2) &&
				oldMouseY <= Math.max(sel_y1, sel_y2) && oldMouseY >= Math.min(sel_y1, sel_y2)){
			int dx = mouseX-oldMouseX, dy = mouseY-oldMouseY;
			World w = world.getWorld(); int level = world.getEditLevel();
			int _sy = Math.min(sel_y1, sel_y2), _sx = Math.min(sel_x1, sel_x2), width = Math.abs(sel_x1-sel_x2)+1;
			int sy = clamp(Math.min(sel_y1, sel_y2), 0, w.getHeight()-1), ey = clamp(Math.max(sel_y1, sel_y2), 0, w.getHeight()-1),
				sx = clamp(Math.min(sel_x1, sel_x2), 0, w.getWidth()-1), ex = clamp(Math.max(sel_x1, sel_x2), 0, w.getWidth()-1);
			for(int y=sy; y<=ey; y++)
				for(int x=sx; x<=ex; x++){
					Cell c = w.getCell(x, y);
					int i = (y-_sy)*width+(x-_sx);
					if(c != null){
						Tile t = c.getTile(level); entry.changeTile(x, y, t, bg_tiles[i]);
						c.setTile(bg_tiles[i], level); bg_tiles[i] = t;
					}
					else bg_tiles[i] = Tile.empty;
				}
			_sy += dy; _sx += dx;
			sy = clamp(_sy, 0, w.getHeight()-1); ey = clamp(Math.max(sel_y1, sel_y2)+dy, 0, w.getHeight()-1);
			sx = clamp(_sx, 0, w.getWidth()-1); ex = clamp(Math.max(sel_x1, sel_x2)+dx, 0, w.getWidth()-1);
			for(int y=sy; y<=ey; y++)
				for(int x=sx; x<=ex; x++){
					int i = (y-_sy)*width+(x-_sx);
					Tile t = bg_tiles[i];
					Cell c = w.getCell(x, y);
					if(c == null) c = w.addCell(x, y);
					if(c == null) continue;
					bg_tiles[i] = c.getTile(level);
					entry.changeTile(x, y, bg_tiles[i], t);
					c.setTile(t, level);
				}
			repaint(sx-dx-1, sy-dy-1, ex-dx+1, ey-dy+1);
			sel_x1 += dx; sel_y1 += dy; sel_x2 += dx; sel_y2 += dy;
			repaint(sx-1, sy-1, ex+1, ey+1);
			if(!addedEntry) addedEntry = history.addEntry(entry);
		} else {
			sel_x1 = stX; sel_y1 = stY; sel_x2 = mouseX; sel_y2 = mouseY;
			repaint(stX, stY, oldMouseX, oldMouseY); repaint(stX, stY, mouseX, mouseY);
		}
	}
	public void mouseMoved(int oldMouseX, int oldMouseY, int mouseX, int mouseY){
		if(sel_x1 != Integer.MIN_VALUE && mouseX <= Math.max(sel_x1, sel_x2) && mouseX >= Math.min(sel_x1, sel_x2) &&
			mouseY <= Math.max(sel_y1, sel_y2) && mouseY >= Math.min(sel_y1, sel_y2))
			world.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		else world.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	public void mousePressed(int mouseX, int mouseY, int x, int y){
		if(sel_x1 != Integer.MIN_VALUE && mouseX <= Math.max(sel_x1, sel_x2) && mouseX >= Math.min(sel_x1, sel_x2) &&
			mouseY <= Math.max(sel_y1, sel_y2) && mouseY >= Math.min(sel_y1, sel_y2)){
			int level = world.getEditLevel();
			entry = new History.Entry(level); addedEntry = false;
			if(bg_tiles == null){
				bg_tiles = new Tile[(Math.abs(sel_x2-sel_x1)+1)*(Math.abs(sel_y2-sel_y1)+1)];
				for(int i=0; i<bg_tiles.length; i++) bg_tiles[i] = Tile.empty;
			}
		}
		else {repaint(sel_x1, sel_y1, sel_x2, sel_y2); activate();}
	}
	public void mouseReleased(int stX, int stY, int mouseX, int mouseY){
		mouseMoved(0, 0, mouseX, mouseY);
		if(sel_x1 != Integer.MIN_VALUE && listener != null) listener.select();
	}
	
	public void activate(){
		world.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		sel_x1 = Integer.MIN_VALUE; bg_tiles = null; entry = null; addedEntry = false;
		if(listener != null) listener.deselect();
	}
	
	public void selectAll(){
		sel_x1 = 0; sel_x2 = world.getWorld().getWidth()-1;
		sel_y1 = 0; sel_y2 = world.getWorld().getHeight()-1;
		if(listener != null) listener.select();
		world.repaint();
	}
	public boolean hasSelection(){return sel_x1 != Integer.MIN_VALUE;}
	public void deselectAll(){activate(); world.repaint();}
	public void deleteSelection(){
		int level = world.getEditLevel();
		History.Entry entry = new History.Entry(level);
		World w = world.getWorld();
		int _sy = Math.min(sel_y1, sel_y2), _sx = Math.min(sel_x1, sel_x2), width = Math.abs(sel_x1-sel_x2)+1;
		int sy = clamp(Math.min(sel_y1, sel_y2), 0, w.getHeight()-1), ey = clamp(Math.max(sel_y1, sel_y2), 0, w.getHeight()-1),
		sx = clamp(Math.min(sel_x1, sel_x2), 0, w.getWidth()-1), ex = clamp(Math.max(sel_x1, sel_x2), 0, w.getWidth()-1);
		for(int y=sy; y<=ey; y++)
			for(int x=sx; x<=ex; x++){
				Cell c = w.getCell(x, y);
				if(c == null) continue;
				Tile t = (bg_tiles == null)?Tile.empty:bg_tiles[(y-_sy)*width+(x-_sx)];
				entry.changeTile(x, y, c.getTile(level), t);
				c.setTile(t, level);
			}
		history.addEntry(entry);
		deselectAll();
	}
	
	public void copy(Clipboard c){
		World w = world.getWorld(); int level = world.getEditLevel();
		int sy = clamp(Math.min(sel_y1, sel_y2), 0, w.getHeight()-1), ey = clamp(Math.max(sel_y1, sel_y2), 0, w.getHeight()-1),
		sx = clamp(Math.min(sel_x1, sel_x2), 0, w.getWidth()-1), ex = clamp(Math.max(sel_x1, sel_x2), 0, w.getWidth()-1);
		c.set(w, sx, sy, ex-sx+1, ey-sy+1, level);
	}
	public void paste(Clipboard clip){
		if(!clip.hasData()) return;
		int level = world.getEditLevel();
		History.Entry entry = new History.Entry(level);
		activate(); double s = world.getScale();
		Rectangle r = world.getVisibleRect();
		World w = world.getWorld();
		int width = Math.min(w.getWidth(), clip.getWidth()), height = Math.min(w.getHeight(), clip.getHeight());
		sel_x1 = (int)Math.floor((r.x+r.width*0.5)/(Tile.tile_size*s)); sel_y1 = (int)Math.floor((r.y+r.height*0.5)/(Tile.tile_size*s));
		sel_x1 = Math.max(sel_x1-width/2, 0); sel_y1 = Math.max(sel_y1-height/2, 0);
		sel_x2 = sel_x1+width-1; sel_y2 = sel_y1+height-1;
		if(sel_x2 >= w.getWidth()){int dx = w.getWidth()-sel_x2+1; sel_x1 -= dx; sel_x2 -= dx;}
		if(sel_y2 >= w.getHeight()){int dy = w.getHeight()-sel_x2+1; sel_y1 -= dy; sel_y2 -= dy;}
		entry = new History.Entry(level); addedEntry = false;
		bg_tiles = new Tile[width*height];
		for(int y=0; y<height; y++)
			for(int x=0; x<width; x++){
				Cell c = w.getCell(sel_x1+x, sel_y1+y);
				if(c == null) c = w.addCell(sel_x1+x, sel_y1+y);
				bg_tiles[y*width+x] = c.getTile(level); Tile t = clip.getTile(x, y);
				entry.changeTile(sel_x1+x, sel_y1+y, c.getTile(level), t);
				c.setTile(t, level);
			}
		if(listener != null) listener.select();
		history.addEntry(entry);
		world.repaint();
	}
	
	public static interface Listener {
		public void select();
		public void deselect();
	}
}
