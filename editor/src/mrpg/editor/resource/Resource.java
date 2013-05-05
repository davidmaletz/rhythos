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

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;


public abstract class Resource extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -3848661454388754599L;
	
	private String name; public final MapEditor editor;
	public Resource(String n, MapEditor e){name = n; editor = e;}
	public void setName(String n){name = n; Project p = WorkspaceBrowser.getProject(this); if(p != null) p.setModified(true);}
	public String getName(){return name;}
	public boolean edit(){return false;}
	public void remove(){for(int i=0; i<getChildCount(); i++) getChild(i).remove();}
	public void properties(){}
	public boolean hasProperties(){return false;}
	public void contextMenu(JPopupMenu menu){}
	public Resource getParent(){return (Resource)super.getParent();}
	public Resource getChild(int i){return (Resource)getChildAt(i);}
	public boolean canDelete(){return true;}
	public boolean canAdd(byte t, Object key){return false;}
	public int indexOf(Resource r){for(int i=0; i<getChildCount(); i++) if(getChild(i) == r) return i; return -1;}
	public boolean contains(Resource r){
		for(int i=0; i<getChildCount(); i++) if(getChild(i) == r) return true;
		return false;
	}
	public byte getType(){return Type.NONE;}
	public Object getTypeKey(){return null;}
	public void setDBType(DBType t){}
	public abstract Icon getIcon();
	protected Resource copyChildren(Resource r){for(int i=0; i<r.getChildCount(); i++) add(r.getChild(i).copy()); return this;}
	public abstract Resource copy();
	public String toString(){return getName();}
}
