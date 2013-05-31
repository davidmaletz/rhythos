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

import mrpg.display.WorldPanel;
import mrpg.editor.History;
import mrpg.editor.TilesetViewer;
import mrpg.world.Cell;
import mrpg.world.Tile;
import mrpg.world.World;


public class RectTool implements Tool {
	private final WorldPanel world; private final TilesetViewer viewer;
	private final History history;
	public RectTool(WorldPanel w, TilesetViewer v, History h){world = w; viewer = v; history = h;}
	public void paint(Graphics g, int stX, int stY, int mouseX, int mouseY){
		int selWidth = viewer.getSelectionWidth(), selHeight = viewer.getSelectionHeight();
		Composite c = ((Graphics2D)g).getComposite();
		((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		for(int y = Math.min(stY, mouseY); y < Math.max(stY, mouseY)+selHeight; y++)
			for(int x = Math.min(stX, mouseX); x < Math.max(stX, mouseX)+selWidth; x++){
				int dx = (x-stX)%selWidth; if(dx < 0) dx += selWidth;
				int dy = (y-stY)%selHeight; if(dy < 0) dy += selHeight;
				Tile tile = viewer.getSelectedTile(dx, dy);
				tile.paint(g, 0, x*world.tile_size, y*world.tile_size, 0, 0, world.tile_size, world.tile_size, world);
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
		world.repaint((int)Math.floor((Math.min(stX, mouseX)*world.tile_size-1)*s-1), (int)Math.floor((Math.min(stY, mouseY)*world.tile_size-1)*s-1),
				(int)Math.ceil(((Math.max(stX, mouseX)+viewer.getSelectionWidth())*world.tile_size+2)*s+2),
				(int)Math.ceil(((Math.max(stY, mouseY)+viewer.getSelectionHeight())*world.tile_size+2)*s+2));
	}
	public void updateSelection(int mouseX, int mouseY, int oldSelWidth, int oldSelHeight){
		double s = world.getScale();
		world.repaint((int)Math.floor((mouseX*world.tile_size-1)*s-1), (int)Math.floor((mouseY*world.tile_size-1)*s-1),
				(int)Math.ceil((Math.max(oldSelWidth, viewer.getSelectionWidth())*world.tile_size+2)*s+2),
				(int)Math.ceil((Math.max(oldSelHeight, viewer.getSelectionHeight())*world.tile_size+2)*s+2));
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
		int level = world.getEditLevel(); History.Entry entry = new History.Entry(level);
		for(int y = Math.min(stY, mouseY); y < Math.max(stY, mouseY)+selHeight; y++)
			for(int x = Math.min(stX, mouseX); x < Math.max(stX, mouseX)+selWidth; x++){
				int dx = (x-stX)%selWidth; if(dx < 0) dx += selWidth;
				int dy = (y-stY)%selHeight; if(dy < 0) dy += selHeight;
				Tile tile = viewer.getSelectedTile(dx, dy);
				World w = world.getWorld(); Cell c = w.getCell(x, y);
				if(c == null) c = w.addCell(x, y);
				if(c == null) continue;
				if(c.getTile(level) != tile){
					entry.changeTile(x, y, c.getTile(level), tile);
					c.setTile(tile, level);
				}
			}
		history.addEntry(entry);
		double s = world.getScale();
		world.repaint((int)Math.floor((Math.min(stX, mouseX)-1)*s*world.tile_size), (int)Math.floor((Math.min(stY, mouseY)-1)*s*world.tile_size),
				(int)Math.ceil((Math.max(stX, mouseX)+viewer.getSelectionWidth()+3)*s*world.tile_size),
				(int)Math.ceil((Math.max(stY, mouseY)+viewer.getSelectionHeight()+3)*s*world.tile_size));
	}
	
	public void activate(){world.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));}
}
