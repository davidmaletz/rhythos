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

public class ToggleableMenuItem extends BasicMenuItem {
	private JMenuItem item; private JComponent toolbar; private boolean enabled;
	public ToggleableMenuItem(String t, String i, ActionListener l){this(t,i,0,0,l);}
	public ToggleableMenuItem(String t, String i, int k, int m, ActionListener l){super(t, i, k, m, l); enabled = true;}
	public ToggleableMenuItem(String t, String i, ActionListener l, boolean enabled){this(t,i,0,0,l,enabled);}
	public ToggleableMenuItem(String t, String i, int k, int m, ActionListener l, boolean enabled){
		super(t, i, k, m, l); this.enabled = enabled; 
	}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		if(item == null){
			item = super.getMenuItem(allowAccelerator); item.setEnabled(enabled);
		} return item;
	}
	public JComponent getToolbarItem(){
		if(toolbar == null){
			toolbar = super.getToolbarItem(); toolbar.setEnabled(enabled);
		} return toolbar;
	}
	public void setEnabled(boolean e){
		enabled = e; if(item != null) item.setEnabled(e); if(toolbar != null) toolbar.setEnabled(e);
	}
	public boolean isEnabled(){return enabled;}
}
