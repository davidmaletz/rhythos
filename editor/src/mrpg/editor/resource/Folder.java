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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;


public class Folder extends Resource {
	private static final long serialVersionUID = 4352438677363086255L;
	private static final String FOLDER = "folder", DEFAULT_NAME = "New Folder";
	private static final Icon icon = MapEditor.getIcon(FOLDER); private byte type = Type.NONE;
	private Object typeKey = null; private boolean tk_set = false;
	public Folder(MapEditor editor){this(DEFAULT_NAME, editor);}
	public Folder(String n, MapEditor editor){super(n, editor);}
	public boolean canAdd(byte t, Object key){return t == Type.EMPTY_FOLDER || t == getType();}
	public void setType(byte t){if(t != Type.EMPTY_FOLDER) type = t;}
	public byte getType(){
		if(type != Type.NONE) return type; 
		Resource p = getParent();
		byte t = (p != null)?p.getType():Type.EMPTY_FOLDER;
		setType(t);
		return t;
	}
	public void setDBType(DBType type){typeKey = type.getName(); tk_set = true; for(int i=0; i<getChildCount(); i++) getChild(i).setDBType(type);}
	public Object getTypeKey(){
		if(tk_set) return typeKey;
		Resource p = getParent();
		typeKey = (p != null)?p.getTypeKey():null; tk_set = p != null;
		return typeKey;
	}
	public void contextMenu(JPopupMenu menu){Resource p = getParent(); if(p != null) p.contextMenu(menu);}
	public Icon getIcon(){return icon;}
	
	public Resource copy(){Folder ret = new Folder(getName(), editor); ret.setType(getType()); ret.copyChildren(this); return ret;}
	
	public static class Root extends Folder {
		private static final long serialVersionUID = 8415366441063145256L;
		public Root(String n, MapEditor editor){super(n, editor);}
		public boolean canAdd(Type t, Object key){return false;}
		public boolean canDelete(){return false;}
		public byte getType(){return Type.NONE;}
		public void contextMenu(JPopupMenu menu){}
		public Resource copy(){return new Root(getName(), editor).copyChildren(this);}
	}
	public static class Map extends Folder {
		private static final long serialVersionUID = 4694583041815452379L;
		public Map(String n, MapEditor editor){super(n, editor);}
		public void contextMenu(JPopupMenu menu){contextMenu(editor, menu);}
		public static void contextMenu(MapEditor editor, JPopupMenu menu){
			WorkspaceBrowser browser = editor.getBrowser();
			menu.add(browser.add_folder); menu.add(browser.add_map); menu.addSeparator();
		}
		public boolean canDelete(){return false;}
		public byte getType(){return Type.MAP;}
		public Resource copy(){return new Map(getName(), editor).copyChildren(this);}
	}
	public static class Tileset extends Folder {
		private static final long serialVersionUID = 4694583041815452379L;
		public Tileset(String n, MapEditor editor){super(n, editor);}
		public void contextMenu(JPopupMenu menu){
			WorkspaceBrowser browser = editor.getBrowser();
			menu.add(browser.add_folder); menu.add(browser.add_tileset); menu.addSeparator();
		}
		public boolean canDelete(){return false;}
		public byte getType(){return Type.TILESET;}
		public Resource copy(){return new Map(getName(), editor).copyChildren(this);}
	}
	public static class AutoTile extends Folder {
		private static final long serialVersionUID = 4694583041815452379L;
		public AutoTile(String n, MapEditor editor){super(n, editor);}
		public void contextMenu(JPopupMenu menu){
			WorkspaceBrowser browser = editor.getBrowser();
			menu.add(browser.add_folder); menu.add(browser.add_autotile); menu.addSeparator();
		}
		public boolean canDelete(){return false;}
		public byte getType(){return Type.AUTOTILE;}
		public Resource copy(){return new Map(getName(), editor).copyChildren(this);}	
	}
	public static class Database extends Folder implements ActionListener {
		private static final long serialVersionUID = 4694583041815452379L;
		private DBType type; private final String DATABASE = new String("database");
		public Database(String n, MapEditor editor, DBType t){super(n, editor); type = t;}
		public void contextMenu(JPopupMenu menu){
			WorkspaceBrowser browser = editor.getBrowser();
			menu.add(browser.add_folder); menu.add(MapEditor.createMenuItemIcon("Create New "+type.getDefaultName(), DATABASE, this)); menu.addSeparator();
		}
		public void setDBType(DBType t){type = t; for(int i=0; i<getChildCount(); i++) getChild(i).setDBType(type);}
		public boolean canDelete(){return false;}
		public boolean canAdd(byte t, Object key){return t == Type.EMPTY_FOLDER || (t == getType() && getTypeKey().equals(key));}
		public byte getType(){return Type.DATABASE;}
		public Object getTypeKey(){return type.getName();}
		public Resource copy(){return new Map(getName(), editor).copyChildren(this);}
		public void actionPerformed(ActionEvent e) {editor.getBrowser().addResource(new mrpg.editor.resource.Database(type, editor));}
	}
	public static class Image extends Folder {
		private static final long serialVersionUID = -5297238510422197374L;
		public Image(String n, MapEditor editor){super(n, editor);}
		public void contextMenu(JPopupMenu menu){
			WorkspaceBrowser browser = editor.getBrowser();
			menu.add(browser.add_folder); menu.add(browser.add_image); menu.addSeparator();
		}
		public boolean canDelete(){return false;}
		public byte getType(){return Type.IMAGE;}
		public Resource copy(){return new Image(getName(), editor).copyChildren(this);}
	}
	public static class Media extends Folder {
		private static final long serialVersionUID = 7457418400675383880L;
		public Media(String n, MapEditor editor){super(n, editor);}
		public void contextMenu(JPopupMenu menu){
			WorkspaceBrowser browser = editor.getBrowser();
			menu.add(browser.add_folder); menu.add(browser.add_media); menu.addSeparator();
		}
		public boolean canDelete(){return false;}
		public byte getType(){return Type.MEDIA;}
		public Resource copy(){return new Media(getName(), editor).copyChildren(this);}
	}
	public static class Script extends Folder {
		private static final long serialVersionUID = -3539237184707131742L;
		public Script(String n, MapEditor editor){super(n, editor);}
		public void contextMenu(JPopupMenu menu){
			WorkspaceBrowser browser = editor.getBrowser();
			menu.add(browser.add_folder); menu.add(browser.add_script); menu.addSeparator();
		}
		public boolean canDelete(){return false;}
		public byte getType(){return Type.SCRIPT;}
		public Resource copy(){return new Script(getName(), editor).copyChildren(this);}
	}
}
