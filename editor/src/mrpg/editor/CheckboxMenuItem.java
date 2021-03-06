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

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

public class CheckboxMenuItem extends BasicMenuItem {
	private JCheckBoxMenuItem item; private JCheckBox toolbar; private boolean selected;
	public CheckboxMenuItem(String t, String i, ActionListener l){this(t,i,0,0,l);}
	public CheckboxMenuItem(String t, String i, int k, int m, ActionListener l){super(t, i, k, m, l); selected = false;}
	public CheckboxMenuItem(String t, String i, ActionListener l, boolean selected){this(t,i,0,0,l,selected);}
	public CheckboxMenuItem(String t, String i, int k, int m, ActionListener l, boolean selected){
		super(t, i, k, m, l); this.selected = selected; 
	}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		if(item == null){
			if(listener == null) listener = MapEditor.instance.getBrowser();
			if(allowAccelerator) item = MapEditor.createCheckboxMenuItem(text, icon, keyCode, modifiers, listener);
			else item = MapEditor.createCheckboxMenuItem(text, icon, listener);
			item.setSelected(selected);
		} return item;
	}
	public JComponent getToolbarItem(){
		if(toolbar == null){
			if(listener == null) listener = MapEditor.instance.getBrowser();
			toolbar = MapEditor.createToolbarCheckBox(icon, text, listener);
			toolbar.setSelected(selected);
		} return toolbar;
	}
	public void setSelected(boolean s){
		selected = s; if(item != null) item.setSelected(s); if(toolbar != null) toolbar.setSelected(s);
	}
	public boolean isSelected(){
		if(item != null) selected = item.isSelected(); if(toolbar != null) selected = toolbar.isSelected();
		return selected;
	}
}
