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

import mrpg.display.LineRasterizer;
import mrpg.display.WorldPanel;
import mrpg.editor.History;
import mrpg.editor.TilesetViewer;
import mrpg.world.Cell;
import mrpg.world.Tile;
import mrpg.world.World;


public class LineTool implements Tool {
	private final WorldPanel world; private final TilesetViewer viewer;
	private final History history;
	public LineTool(WorldPanel w, TilesetViewer v, History h){world = w; viewer = v; history = h;}
	public void paint(Graphics g, int stX, int stY, int mouseX, int mouseY){
		int selWidth = viewer.getSelectionWidth(), selHeight = viewer.getSelectionHeight();
		Composite c = ((Graphics2D)g).getComposite();
		((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		LineRasterizer lr = new LineRasterizer(stX, stY, mouseX, mouseY, selWidth, selHeight);
		while(lr.hasNext()){
			int x = lr.getX(), y = lr.getY();
			for(TilesetViewer.SelectionIterator i = viewer.iterator(); i.hasNext(); ){
				int dx = (i.deltaX()+stX-x)%selWidth; if(dx < 0) dx += selWidth;
				int dy = (i.deltaY()+stY-y)%selHeight; if(dy < 0) dy += selHeight;
				Tile tile = i.next();
				tile.paint(g, 0, (x+dx)*Tile.tile_size, (y+dy)*Tile.tile_size, 0, 0, Tile.tile_size, Tile.tile_size, world);
			}
			lr.next();
		}
		((Graphics2D)g).setComposite(c);

		g.setColor(Color.black);
		g.drawRect(mouseX*Tile.tile_size, mouseY*Tile.tile_size, selWidth*Tile.tile_size, selHeight*Tile.tile_size);
		g.drawRect(mouseX*Tile.tile_size+3, mouseY*Tile.tile_size+3, selWidth*Tile.tile_size-6, selHeight*Tile.tile_size-6);
		g.setColor(TilesetViewer.selectColor2);
		g.drawRect(mouseX*Tile.tile_size+1, mouseY*Tile.tile_size+1, selWidth*Tile.tile_size-2, selHeight*Tile.tile_size-2);
		g.setColor(TilesetViewer.selectColor1);
		g.drawRect(mouseX*Tile.tile_size+2, mouseY*Tile.tile_size+2, selWidth*Tile.tile_size-4, selHeight*Tile.tile_size-4);
	}
	public void repaint(int stX, int stY, int mouseX, int mouseY){
		double s = world.getScale();
		world.repaint((int)Math.floor((Math.min(stX, mouseX)*Tile.tile_size-1)*s-1), (int)Math.floor((Math.min(stY, mouseY)*Tile.tile_size-1)*s-1),
				(int)Math.ceil(((Math.max(stX, mouseX)+viewer.getSelectionWidth())*Tile.tile_size+2)*s+2),
				(int)Math.ceil(((Math.max(stY, mouseY)+viewer.getSelectionHeight())*Tile.tile_size+2)*s+2));
	}
	public void updateSelection(int mouseX, int mouseY, int oldSelWidth, int oldSelHeight){
		double s = world.getScale();
		world.repaint((int)Math.floor((mouseX*Tile.tile_size-1)*s-1), (int)Math.floor((mouseY*Tile.tile_size-1)*s-1),
				(int)Math.ceil((Math.max(oldSelWidth, viewer.getSelectionWidth())*Tile.tile_size+2)*s+2),
				(int)Math.ceil((Math.max(oldSelHeight, viewer.getSelectionHeight())*Tile.tile_size+2)*s+2));
	}
	
	public void mouseDragged(int mouseX, int mouseY, int x, int y){}
	public void mouseDragged(int stX, int stY, int oldMouseX, int oldMouseY, int mouseX, int mouseY){
		repaint(stX, stY, oldMouseX, oldMouseY); repaint(stX, stY, mouseX, mouseY);
	}
	public void mouseMoved(int oldMouseX, int oldMouseY, int mouseX, int mouseY){
		updateSelection(oldMouseX, oldMouseY, 0, 0); updateSelection(mouseX, mouseY, 0, 0);
	}
	public void mousePressed(int mouseX, int mouseY, int x, int y){}
	public void mouseReleased(int stX, int stY, int mouseX, int mouseY){
		int selWidth = viewer.getSelectionWidth(), selHeight = viewer.getSelectionHeight();
		LineRasterizer lr = new LineRasterizer(stX, stY, mouseX, mouseY, selWidth, selHeight);
		int level = world.getEditLevel(); History.Entry entry = new History.Entry(level);
		while(lr.hasNext()){
			int x = lr.getX(), y = lr.getY();
			for(TilesetViewer.SelectionIterator i = viewer.iterator(); i.hasNext(); ){
				int dx = (i.deltaX()+stX-x)%selWidth; if(dx < 0) dx += selWidth;
				int dy = (i.deltaY()+stY-y)%selHeight; if(dy < 0) dy += selHeight;
				Tile tile = i.next();
				World w = world.getWorld(); Cell c = w.getCell(x+dx, y+dy);
				if(c == null) c = w.addCell(x+dx, y+dy);
				if(c == null) continue;
				if(c.getTile(level) != tile){
					entry.changeTile(x+dx, y+dy, c.getTile(level), tile);
					c.setTile(tile, level);
				}
			}
			lr.next();
		}
		history.addEntry(entry);
		double s = world.getScale();
		world.repaint((int)Math.floor((Math.min(stX, mouseX)-1)*s*Tile.tile_size), (int)Math.floor((Math.min(stY, mouseY)-1)*s*Tile.tile_size),
				(int)Math.ceil((Math.max(stX, mouseX)+viewer.getSelectionWidth()+3)*s*Tile.tile_size),
				(int)Math.ceil((Math.max(stY, mouseY)+viewer.getSelectionHeight()+3)*s*Tile.tile_size));
	}
	
	public void activate(){world.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));}
}
