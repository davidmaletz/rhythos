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
package mrpg.editor.resource;

import java.io.File;

import javax.swing.JPopupMenu;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;

public abstract class ModifiableResource extends Resource implements Modifiable {
	private static final long serialVersionUID = 8050024073339824076L;
	private boolean modified = false;
	protected ModifiableResource(File f, MapEditor e){super(f,e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); browser.save.setEnabled(isModified()); menu.add(browser.save);
		browser.revert.setEnabled(isModified()); menu.add(browser.revert); menu.addSeparator();
	}
	public boolean isModified(){return modified;}
	public void setModified(boolean m){if(m != modified){modified = m; updateName();}}
	public String toString(){if(modified) return "*"+super.toString(); else return super.toString();}
	public void refresh() throws Exception {if(!modified) super.refresh();}
	public void revert() throws  Exception {super.refresh();}
	public abstract void save() throws Exception ;
}
