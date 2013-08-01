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
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import mrpg.editor.MapEditor;
import mrpg.editor.Menu;

public class Folder extends Resource {
	private static final long serialVersionUID = 4352438677363086255L;
	public static final String FOLDER = "folder", ADD_FOLDER = "add_folder";
	public static final Menu new_options = new Menu("Create New", MapEditor.NEW),
		import_options = new Menu("Import", MapEditor.IMPORT);
	private static final Icon icon = MapEditor.getIcon(FOLDER);
	protected Folder(File f, MapEditor editor){super(f, editor);}
	public boolean canAddChildren(){return true;}
	public void contextMenu(JPopupMenu menu){
		menu.add(new_options.getMenuItem(false));
		menu.add(import_options.getMenuItem(false));
		menu.addSeparator();
	}
	public Resource getChildByName(String n){
		if(children == null) return null;
		for(Object o : children){
			if(o.toString().equals(n)) return (Resource)o;
		} return null;
	}
	public long getId(){return 0;}
	public Icon getIcon(){return icon;}
	public static String OUT_DIR = "__haxe"; 
	protected void read(File f) throws Exception {
		for(File file : f.listFiles())
			if(!file.getName().equals(OUT_DIR)) try{add(Resource.readFile(file, editor));}catch(Exception e){}
	}
	public int getHeaderSize(){return 0;}
	public File copy(File dir, Project p, boolean checkCompatible) throws Exception {
		if(!dir.isDirectory()) throw new Exception(); File f = changeDirectory(dir, p, true, checkCompatible);
		if(!f.mkdir()) throw new Exception(); for(int i=0; i<getChildCount(); i++) getChild(i).copy(f, p, checkCompatible);
		return f;
	}
	public static Folder create(File f, MapEditor e) throws Exception {
		if(f.mkdir()) return new Folder(f,e); else throw new Exception();
	}
	public String getExt(){return null;}
	public static void register(){
		new_options.addItem("Folder", ADD_FOLDER, KeyEvent.VK_F, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, new CreateFolderAction());
	}
	private static class CreateFolderAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addFolder();
		}
	}
}