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

import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

public class BasicMenuItem implements MenuItem {
	public String text, icon; public int keyCode, modifiers; public ActionListener listener;
	public BasicMenuItem(String t, String i, int k, int m, ActionListener l){
		text = t; icon = i; keyCode = k; modifiers = m; listener = l;
	}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		if(listener == null) listener = MapEditor.instance.getBrowser();
		if(allowAccelerator) return MapEditor.createMenuItemIcon(text, icon, keyCode, modifiers, listener);
		else return MapEditor.createMenuItemIcon(text, icon, listener);
	}
	public JComponent getToolbarItem(){
		if(listener == null) listener = MapEditor.instance.getBrowser();
		return MapEditor.createToolbarButton(icon, text, listener);
	}
}
