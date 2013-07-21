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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import mrpg.display.WorldPanel;
import mrpg.editor.tools.Tool;

public class ToolItem implements MenuItem, ActionListener {
	private Tool tool; private Class<? extends Tool> tool_class; private JMenuItem item; private JComponent toolbar;
	public ToolItem(Class<? extends Tool> tool){tool_class = tool;}
	public Tool getTool(){
		if(tool == null){
			MapEditor e = MapEditor.instance; try{
			tool = tool_class.getConstructor(WorldPanel.class, TilesetViewer.class, History.class).
					newInstance(e.getOverlay().getPanel(), e.getTilesetViewer(), e.getHistory());
			}catch(Exception ex){}
		} return tool;
	}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		if(item == null){
			Tool t = getTool(); item = MapEditor.createMenuItemIcon(t.getName(), t.getIcon(), this);
		} return item;
	}
	public JComponent getToolbarItem(){
		if(toolbar == null){
			Tool t = getTool(); toolbar = MapEditor.createToolbarButton(t.getIcon(), t.getName(), MapEditor.instance.tools, this);
		} return toolbar;
	}
	public void actionPerformed(ActionEvent e){
		Tool t = getTool();
		MapEditor.instance.getOverlay().setTool(t);
	}
}
