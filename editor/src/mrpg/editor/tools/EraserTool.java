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


public class EraserTool implements Tool {
	private final WorldPanel world;
	private final History history; private History.Entry entry; private boolean entryAdded;
	public EraserTool(WorldPanel w, TilesetViewer v, History h){world = w;  history = h;}
	public void paint(Graphics g, int stX, int stY, int mouseX, int mouseY){
		Composite c = ((Graphics2D)g).getComposite();
		((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		int x = mouseX, y = mouseY; Tile tile = Tile.empty;
		tile.paint(g, x*world.tile_size, y*world.tile_size, 0, 0, world.tile_size, world.tile_size, world);
		((Graphics2D)g).setComposite(c);
	}
	public void paintTop(Graphics g, double scale, int stX, int stY, int mouseX, int mouseY){
		g.setColor(Color.black); int ts = (int)Math.floor(world.tile_size*scale);
		g.drawRect(mouseX*ts, mouseY*ts, ts, ts);
		g.drawRect(mouseX*ts+3, mouseY*ts+3, ts-6, ts-6);
		g.setColor(TilesetViewer.selectColor2);
		g.drawRect(mouseX*ts+1, mouseY*ts+1, ts-2, ts-2);
		g.setColor(TilesetViewer.selectColor1);
		g.drawRect(mouseX*ts+2, mouseY*ts+2, ts-4, ts-4);
	}
	public void repaint(int stX, int stY, int mouseX, int mouseY){
		double s = world.getScale();
		world.repaint((int)Math.floor((mouseX*world.tile_size-1)*s-1), (int)Math.floor((mouseY*world.tile_size-1)*s-1),
				(int)Math.ceil((world.tile_size+2)*s+2),
				(int)Math.ceil((world.tile_size+2)*s+2));
	}
	public void updateSelection(int mouseX, int mouseY, int oldSelWidth, int oldSelHeight){
		double s = world.getScale();
		world.repaint((int)Math.floor((mouseX*world.tile_size-1)*s-1), (int)Math.floor((mouseY*world.tile_size-1)*s-1),
				(int)Math.ceil((world.tile_size+2)*s+2),
				(int)Math.ceil((world.tile_size+2)*s+2));
	}
	private void setTile(int stX, int stY, int mouseX, int mouseY){
		int level = world.getEditLevel();
		int x = mouseX, y = mouseY; Tile tile = Tile.empty;
		World w = world.getWorld(); Cell c = w.getCell(x, y);
		if(c == null) c = w.addCell(x, y);
		if(c != null && c.getTile(level) != tile){
			entry.changeTile(x, y, c.getTile(level), tile);
			c.setTile(tile, level);
		}
		if(!entryAdded){history.addEntry(entry); entryAdded = true;}
		double s = world.getScale();
		world.repaint((int)Math.floor((mouseX-1)*(s*world.tile_size)), (int)Math.floor((mouseY-1)*(s*world.tile_size)),
				(int)Math.ceil(4*(s*world.tile_size)), (int)Math.ceil(4*(s*world.tile_size)));
	}
	
	public void mouseDragged(int mouseX, int mouseY, int x, int y){}
	public void mouseDragged(int stX, int stY, int oldMouseX, int oldMouseY, int mouseX, int mouseY){
		LineRasterizer r = new LineRasterizer(oldMouseX, oldMouseY, mouseX, mouseY, 1, 1);
		while(r.hasNext()){
			setTile(stX, stY, r.getX(), r.getY());
			r.next();
		}
	}
	public void mouseMoved(int oldMouseX, int oldMouseY, int mouseX, int mouseY){
		repaint(oldMouseX, oldMouseY, oldMouseX, oldMouseY); repaint(mouseX, mouseY, mouseX, mouseY);
	}
	public void mousePressed(int mouseX, int mouseY, int x, int y){
		int level = world.getEditLevel(); entry = new History.Entry(level); entryAdded = false;
		setTile(mouseX, mouseY, mouseX, mouseY);
	}
	public void mouseReleased(int stX, int stY, int mouseX, int mouseY){entry = null;}
	
	public void activate(){world.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));}
	
	public String getName(){return "Eraser tool";}
	public String getIcon(){return "eraser";}
}
