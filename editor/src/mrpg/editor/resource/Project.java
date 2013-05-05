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

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;


public class Project extends Resource {
	private static final long serialVersionUID = -8656579697414666933L;
	public static final String PROJECT = "project", DEFAULT_NAME = "New Project";
	private static final Icon icon = MapEditor.getIcon(PROJECT);
	private boolean modified = false;
	public Project(MapEditor e){this(DEFAULT_NAME, e);}
	public Project(String n, MapEditor e){
		super(n, e);
		Folder.Map m = new Folder.Map("Maps", e);
		m.add(new Map(e)); add(m);
		Resource db = new Folder.Root("Database", e);
		db.add(new Folder.Tileset("Tilesets", e));
		db.add(new Folder.AutoTile("Autotiles", e));
		add(db);
		Resource resources = new Folder.Root("Resources", e);
		Resource images = new Folder.Image("Images", e);
		images.add(new Folder("Sprites", e));
		Resource tilesets = new Folder("Tilesets", e);
		images.add(tilesets);
		Resource autotiles = new Folder("Autotiles", e);
		images.add(autotiles);
		images.add(new Folder("Wall Tiles", e));
		resources.add(images);
		Resource media = new Folder.Media("Audio", e);
		media.add(new Folder("Background Music", e));
		media.add(new Folder("Sound Effects", e));
		resources.add(media);
		add(resources);
		add(new Folder.Script("Scripts", e));
	}
	public Project(Project p){super(p.getName(), p.editor); modified = true; copyChildren(p);}
	private Project(String name, MapEditor e, boolean b){super(name, e);}
	private static boolean addResource(Resource r, Resource parent){
		byte t = r.getType();
		if(parent.canAdd(t, r.getTypeKey())){parent.add(r); return true;}
		for(int i=0; i<parent.getChildCount(); i++)
			if(addResource(r, parent.getChild(i))) return true;
		return false;	
	}
	public void editFirstMap(){
		Resource m = ((Resource)parent.getChildAt(0)).getChild(0); if(m.getChildCount() != 0) m.getChild(0).edit();
	}
	public void addResource(Resource r){addResource(r, this); setModified(true);}
	public boolean isModified(){return modified;}
	public void setModified(boolean m){modified = m; editor.updateSaveButtons();}
	public void properties(){}
	public boolean hasProperties(){return true;}
	public void contextMenu(JPopupMenu menu, WorkspaceBrowser browser){
		menu.add(browser.properties); menu.addSeparator();
	}
	public boolean canAdd(byte t, Object key){return false;}
	public byte getType(){return Type.PROJECT;}
	public Icon getIcon(){return icon;}
	public Resource copy(){return new Project(this);}

	private ArrayList<DBType> db_types = new ArrayList<DBType>();
	public void unregisterAllTypes(){
		db_types.clear(); Resource p = getChild(1); DefaultTreeModel m = (DefaultTreeModel)editor.getBrowser().getModel();
		for(int i=2; i<p.getChildCount(); i++){p.getChild(i).remove(); m.removeNodeFromParent(p.getChild(i));}
	}
	public boolean registerType(DBType type){
		String name = type.getName();
		for(int i=0; i<db_types.size(); i++) if(db_types.get(i).getName().equals(name)){
			db_types.set(i, type);
			Folder.Database folder = (Folder.Database)getChild(1).getChild(i+2);
			folder.setDBType(type);
			return true;
		}
		db_types.add(type);
		DefaultTreeModel m = (DefaultTreeModel)editor.getBrowser().getModel();
		m.insertNodeInto(new Folder.Database(name, editor, type), getChild(1), getChild(1).getChildCount());
		return true;
	}
}
