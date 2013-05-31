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

import java.awt.Graphics;

public interface Tool {
	public void paint(Graphics g, int stX, int stY, int mouseX, int mouseY);
	public void paintTop(Graphics gold, double scale, int stX, int stY, int mouseX, int mouseY);
	public void repaint(int stX, int stY, int mouseX, int mouseY);
	
	public void updateSelection(int mouseX, int mouseY, int oldSelWidth, int oldSelHeight);
	
	public void mouseDragged(int mouseX, int mouseY, int x, int y);
	public void mouseDragged(int stX, int stY, int oldMouseX, int oldMouseY, int mouseX, int mouseY);
	public void mouseMoved(int oldMouseX, int oldMouseY, int mouseX, int mouseY);
	public void mousePressed(int mouseX, int mouseY, int x, int y);
	public void mouseReleased(int stX, int stY, int mouseX, int mouseY);
	
	public void activate();
}
