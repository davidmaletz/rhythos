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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Stack;

import mrpg.display.WorldPanel;
import mrpg.editor.History;
import mrpg.editor.TilesetViewer;
import mrpg.world.Cell;
import mrpg.world.Tile;
import mrpg.world.World;


public class FillTool implements Tool {
	private final WorldPanel world; private final TilesetViewer viewer;
	private final History history;
	private final Stack<Point> stack = new Stack<Point>(); 
	public FillTool(WorldPanel w, TilesetViewer v, History h){world = w; viewer = v; history = h;}
	public void paint(Graphics g, int stX, int stY, int mouseX, int mouseY){
		int selWidth = viewer.getSelectionWidth(), selHeight = viewer.getSelectionHeight();
		Composite c = ((Graphics2D)g).getComposite();
		((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		for(TilesetViewer.SelectionIterator i = viewer.iterator(); i.hasNext(); ){
			int dx = (i.deltaX()+stX-mouseX)%selWidth; if(dx < 0) dx += selWidth;
			int dy = (i.deltaY()+stY-mouseY)%selHeight; if(dy < 0) dy += selHeight;
			int x = mouseX+dx, y = mouseY+dy; Tile tile = i.next();
			tile.paint(g, x*world.tile_size, y*world.tile_size, 0, 0, world.tile_size, world.tile_size, viewer);
		}
		((Graphics2D)g).setComposite(c);
	}
	public void paintTop(Graphics g, double scale, int stX, int stY, int mouseX, int mouseY){
		g.setColor(Color.black); int selWidth = viewer.getSelectionWidth(), selHeight = viewer.getSelectionHeight(), ts = (int)Math.floor(world.tile_size*scale);
		g.drawRect(mouseX*ts, mouseY*ts, selWidth*ts, selHeight*ts);
		g.drawRect(mouseX*ts+3, mouseY*ts+3, selWidth*ts-6, selHeight*ts-6);
		g.setColor(TilesetViewer.selectColor2);
		g.drawRect(mouseX*ts+1, mouseY*ts+1, selWidth*ts-2, selHeight*ts-2);
		g.setColor(TilesetViewer.selectColor1);
		g.drawRect(mouseX*ts+2, mouseY*ts+2, selWidth*ts-4, selHeight*ts-4);
	}
	public void repaint(int stX, int stY, int mouseX, int mouseY){
		double s = world.getScale();
		world.repaint((int)Math.floor((mouseX*world.tile_size-1)*s-1), (int)Math.floor((mouseY*world.tile_size-1)*s-1),
				(int)Math.ceil((viewer.getSelectionWidth()*world.tile_size+2)*s+2),
				(int)Math.ceil((viewer.getSelectionHeight()*world.tile_size+2)*s+2));
	}
	public void updateSelection(int mouseX, int mouseY, int oldSelWidth, int oldSelHeight){
		double s = world.getScale();
		world.repaint((int)Math.floor((mouseX*world.tile_size-1)*s-1), (int)Math.floor((mouseY*world.tile_size-1)*s-1),
				(int)Math.ceil((Math.max(oldSelWidth, viewer.getSelectionWidth())*world.tile_size+2)*s+2),
				(int)Math.ceil((Math.max(oldSelHeight, viewer.getSelectionHeight())*world.tile_size+2)*s+2));
	}
	
	public void mouseDragged(int mouseX, int mouseY, int x, int y){}
	public void mouseDragged(int stX, int stY, int oldMouseX, int oldMouseY, int mouseX, int mouseY){}
	public void mouseMoved(int oldMouseX, int oldMouseY, int mouseX, int mouseY){
		repaint(oldMouseX, oldMouseY, oldMouseX, oldMouseY); repaint(mouseX, mouseY, mouseX, mouseY);
	}
	private boolean checkCell(World w, int x, int y, int level, Tile t){
		Cell c = w.getCell(x, y);
		if(c == null) return t == Tile.empty;
		else{
			Tile o = c.getTile(level);
			return o == t || (o.info.map == t.info.map && o.info.map.indexNeighbors());
		}
	}
	private void floodFill(int x, int y, Tile oldTile, int level){
		if(oldTile == viewer.getSelectedTile() || (oldTile.info.map == viewer.getSelectedTile().info.map && oldTile.info.map.indexNeighbors())) return;
		History.Entry entry = new History.Entry(level);
		int selWidth = viewer.getSelectionWidth(), selHeight = viewer.getSelectionHeight();
		stack.clear();
		int y1; boolean spanLeft, spanRight;
		stack.push(new Point(x, y)); World w = world.getWorld();
		int width = w.getWidth(), height = w.getHeight();
		while(!stack.empty()){
			Point p = stack.pop(); y1 = p.y;
		    while(y1 >= 0 && checkCell(w, p.x, y1, level, oldTile)) y1--;
		    y1++;
		    spanLeft = spanRight = false;
		    while(y1 < height &&  checkCell(w, p.x, y1, level, oldTile)){
		    	int dx = (p.x-x)%selWidth; if(dx < 0) dx += selWidth;
				int dy = (y1-y)%selHeight; if(dy < 0) dy += selHeight;
				Tile tile = viewer.getSelectedTile(dx, dy);
				Cell c = w.getCell(p.x, y1);
				if(c == null) c = w.addCell(p.x, y1);
				if(c != null && c.getTile(level) != tile){
					entry.changeTile(p.x, y1, c.getTile(level), tile);
					c.setTile(tile, level);
				}
		        if(!spanLeft && p.x > 0 && checkCell(w, p.x-1, y1, level, oldTile)){
		            stack.push(new Point(p.x-1, y1));
		            spanLeft = true;
		        }
		        else if(spanLeft && p.x > 0 && !checkCell(w, p.x-1, y1, level, oldTile)) spanLeft = false;
		        if(!spanRight && p.x < width - 1 && checkCell(w, p.x+1, y1, level, oldTile)){
		        	stack.push(new Point(p.x+1, y1));
		            spanRight = true;
		        }
		        else if(spanRight && p.x < width - 1 && !checkCell(w, p.x+1, y1, level, oldTile)) spanRight = false;
		        y1++;
		    }
		}
		history.addEntry(entry);
	}
	public void mousePressed(int mouseX, int mouseY, int x, int y){
		Cell c = world.getWorld().getCell(mouseX, mouseY);
		int level = world.getEditLevel(); floodFill(mouseX, mouseY, (c == null)?Tile.empty:c.getTile(level), level);
		world.repaint();
	}
	public void mouseReleased(int stX, int stY, int mouseX, int mouseY){}
	
	public void activate(){world.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
	
	public String getName(){return "Flood fill tool";}
	public String getIcon(){return "fill";}
}
