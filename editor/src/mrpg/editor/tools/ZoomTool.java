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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.MemoryImageSource;

import mrpg.display.WorldPanel;


public class ZoomTool implements Tool {
	private final WorldPanel world; private int pressedY = -1, lastX, lastY;
	private static final Cursor hidden = getHiddenCursor(); private Rectangle rect = null;
	public Listener listener;
	public ZoomTool(WorldPanel w){world = w;}
	public void paint(Graphics g, int stX, int stY, int mouseX, int mouseY){
		if(pressedY != Integer.MIN_VALUE){
			g = g.create();
			AffineTransform t = ((Graphics2D)g).getTransform(); double tx = t.getTranslateX(), ty = t.getTranslateY();
			t.setToTranslation(tx, ty); ((Graphics2D)g).setTransform(t);
			g.setColor(Color.black); g.setXORMode(Color.white);
			g.fillRect(lastX-2, Math.min(lastY, pressedY), 4, Math.abs(lastY-pressedY)+4);
			g.setPaintMode();
		}
		if(rect != null){world.scrollRectToVisible(rect); rect = null;}
	}
	public void paintTop(Graphics g, double scale, int stX, int stY, int mouseX, int mouseY){}
	public void repaint(int stX, int stY, int mouseX, int mouseY){}
	public void updateSelection(int mouseX, int mouseY, int oldSelWidth, int oldSelHeight){}
	
	public void mouseDragged(int mouseX, int mouseY, int x, int y){
		if(pressedY != Integer.MIN_VALUE){
			int sy = Math.min(y, Math.min(lastY, pressedY)),
			height = Math.max(Math.abs(y-pressedY), Math.abs(lastY-pressedY));
			lastY = y;
			int dy = y-pressedY;
			if(dy > 16){
				if(world.getScale() >= 0.125){
					rect = world.getVisibleRect();
					world.setScale(world.getScale()*0.5);
					rect.x = lastX/2-rect.width/2; rect.y = pressedY/2-rect.height/2;
					if(listener != null) listener.zoomChange();
				}
				pressedY = Integer.MIN_VALUE;
				world.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			}
			if(dy < -16){
				if(world.getScale() <= 4.0){
					rect = world.getVisibleRect();
					world.setScale(world.getScale()*2.0);
					rect.x = lastX*2-rect.width/2; rect.y = pressedY*2-rect.height/2;
					if(listener != null) listener.zoomChange();
				}
				pressedY = Integer.MIN_VALUE;
				world.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			}
			world.repaint(lastX-2, sy, 4, height+4);
		}
	}
	public void mouseDragged(int stX, int stY, int oldMouseX, int oldMouseY, int mouseX, int mouseY){}
	public void mouseMoved(int oldMouseX, int oldMouseY, int mouseX, int mouseY){}
	public void mousePressed(int mouseX, int mouseY, int x, int y){
		pressedY = y; lastX = x; lastY = y;
		world.setCursor(hidden);
		world.repaint(lastX-2, Math.min(lastY, pressedY), 4, Math.abs(lastY-pressedY)+4);
	}
	public void mouseReleased(int stX, int stY, int mouseX, int mouseY){
		int y = pressedY; pressedY = Integer.MIN_VALUE;
		world.repaint(lastX-2, Math.min(lastY, y), 4, Math.abs(lastY-y)+4);
		world.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
	}
	
	public void activate(){pressedY = Integer.MIN_VALUE; world.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));}
	
	private static final Cursor getHiddenCursor(){
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
		return Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
	}
	
	public static interface Listener {
		public void zoomChange();
	}
}
