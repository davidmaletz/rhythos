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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import mrpg.display.Overlay;
import mrpg.display.WorldPanel;
import mrpg.editor.tools.Tool;
import mrpg.world.Tile;


public class WorldOverlay implements MouseListener, MouseMotionListener, Overlay {
	private final WorldPanel world; private final MapEditor editor;
	private int mouseX = 0, mouseY = 0, stX = 0, stY = 0;
	private Tool tool;
	public WorldOverlay(MapEditor e, WorldPanel w){
		editor = e; world = w; world.setAutoscrolls(true); w.addMouseListener(this); w.addMouseMotionListener(this);
	}
	public void paintOverlay(Graphics g, Rectangle r) {
		if(world.getWorld() != null) tool.paint(g, stX, stY, mouseX, mouseY);
	}
	public WorldPanel getPanel(){return world;}
	public MapEditor getEditor(){return editor;}
	public void updateSelection(int oldSelWidth, int oldSelHeight){tool.updateSelection(mouseX, mouseY, oldSelWidth, oldSelHeight);}
	public Tool getTool(){return tool;}
	public void setTool(Tool t){if(tool != t){tool = t; tool.activate(); world.repaint();}}
	public void mouseDragged(MouseEvent e) {
		if(world.getWorld() == null) return;
		world.scrollRectToVisible(new Rectangle(e.getX(), e.getY(), 1, 1));
		tool.mouseDragged(mouseX, mouseY, e.getX(), e.getY());
		double s = world.getScale();
		int x = Math.max(0, Math.min((int)Math.floor(e.getX()/(s*Tile.tile_size)), world.getWorld().getWidth()-1)),
		y = Math.max(0, Math.min((int)Math.floor(e.getY()/(s*Tile.tile_size)), world.getWorld().getHeight()-1));
		if(x == mouseX && y == mouseY) return;
		int ox = mouseX, oy = mouseY;
		mouseX = x; mouseY = y;
		editor.setMapLocation(mouseX, mouseY);
		tool.mouseDragged(stX, stY, ox, oy, mouseX, mouseY);
	}
	public void mouseMoved(MouseEvent e) {
		if(world.getWorld() == null) return;
		double s = world.getScale();
		int x = Math.max(0, Math.min((int)Math.floor(e.getX()/(s*Tile.tile_size)), world.getWorld().getWidth()-1)),
		y = Math.max(0, Math.min((int)Math.floor(e.getY()/(s*Tile.tile_size)), world.getWorld().getHeight()-1));
		if(x == mouseX && y == mouseY) return;
		int ox = mouseX, oy = mouseY;
		stX = x; stY = y; mouseX = x; mouseY = y;
		editor.setMapLocation(mouseX, mouseY);
		tool.mouseMoved(ox, oy, mouseX, mouseY);
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if(world.getWorld() == null) return;
		editor.removeFocus();
		double s = world.getScale();
		int x = Math.max(0, Math.min((int)Math.floor(e.getX()/(s*Tile.tile_size)), world.getWorld().getWidth()-1)),
		y = Math.max(0, Math.min((int)Math.floor(e.getY()/(s*Tile.tile_size)), world.getWorld().getHeight()-1));
		stX = x; stY = y; mouseX = x; mouseY = y;
		tool.mousePressed(mouseX, mouseY, e.getX(), e.getY());
	}
	public void mouseReleased(MouseEvent e) {
		if(world.getWorld() == null) return;
		tool.mouseReleased(stX, stY, mouseX, mouseY);
		stX = mouseX; stY = mouseY;
	}
}
