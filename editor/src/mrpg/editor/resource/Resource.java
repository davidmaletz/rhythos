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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser.ExtFileFilter;


public abstract class Resource extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -3848661454388754599L;
	
	private File file; private String name; public final MapEditor editor;
	protected Resource(File f, MapEditor e){editor = e; _setFile(f);}
	protected void _setName(String n){name = n;}
	private void _setFile(File f){
		file = f; if(file == null) name = "";
		else {name = file.getName(); int i = name.lastIndexOf('.'); if(i != -1) name = name.substring(0, i);}
	}
	public File changeName(String n) throws Exception {
		if(name != null && name.equals(n)) throw new Exception(); String ext = file.getName(); int i = ext.lastIndexOf('.');
		 if(i == -1) ext = ""; else ext = ext.substring(i);
		 File f = new File(file.getParent(),n+ext);
		 if(f.exists()){
			 JOptionPane.showMessageDialog(editor, "Unable to rename \'"+getName()+"\'.\n\'"+n+"\' already exists.", "Unable to Rename", JOptionPane.ERROR_MESSAGE);
			 throw new Exception();
		 } return f;
	}
	public void updateName(){((DefaultTreeModel)editor.getBrowser().getModel()).nodeChanged(this);}
	public void rename(File f) throws Exception {
		if(name == null || file.renameTo(f)){
			_setFile(f); updateName();
		} else{
			JOptionPane.showMessageDialog(editor, "Unable to rename \'"+name+"\'.\nPerhaps the file is open in another program?", "Unable to Rename", JOptionPane.ERROR_MESSAGE);
			throw new Exception();
		}
	}
	public abstract long getId();
	public void setName(String n) throws Exception {rename(changeName(n));}
	public String getName(){
		if(name == null){
			String n = file.getName(); int i = n.lastIndexOf('.'); if(i != -1) n = n.substring(0, i); return n;
		} else return name;
	}
	public File getFile(){return file;}
	public boolean edit(){return false;}
	public void remove(boolean delete) throws Exception {
		for(int i=0; i<getChildCount(); i++) getChild(i).remove(delete);
		if(delete && !file.delete()){
			JOptionPane.showMessageDialog(editor, "Unable to delete \'"+name+"\'.\nPerhaps the file is open in another program?", "Unable to Delete", JOptionPane.ERROR_MESSAGE);
			throw new Exception();
		}
	}
	public void properties(){}
	public boolean hasProperties(){return false;}
	public void contextMenu(JPopupMenu menu){}
	public Resource getParent(){return (Resource)super.getParent();}
	public Resource getChild(int i){return (Resource)getChildAt(i);}
	public boolean canDelete(){return true;}
	public boolean canAddChildren(){return false;}
	public int indexOf(Resource r){for(int i=0; i<getChildCount(); i++) if(getChild(i) == r) return i; return -1;}
	public boolean contains(Resource r){
		for(int i=0; i<getChildCount(); i++) if(getChild(i) == r) return true;
		return false;
	}
	public abstract Icon getIcon();
	protected abstract void read(File f) throws Exception ;
	public void deferredRead(File f) throws Exception {}
	public boolean isCompatible(Project p){return true;}
	public File changeDirectory(File dir, Project p, boolean allowRename) throws Exception {
		if((!allowRename && dir.equals(file.getParentFile())) || !isCompatible(p)) throw new Exception();
		String ext = file.getName(); int idx = ext.lastIndexOf('.');
		if(idx == -1) ext = ""; else ext = ext.substring(idx);
		File f = new File(dir.toString(),name+ext);
		if(f.exists()){
			String n = (String)JOptionPane.showInputDialog(editor, "Enter a new name for \'"+name+"\':", "Name Conflict", JOptionPane.PLAIN_MESSAGE, null, null, "Copy Of "+name);
			if(n == null) throw new Exception(); else f = new File(dir.toString(),n+ext);
		} if(f.exists()) throw new Exception(); return f; 
	}
	public static void copyFile(File from, File to) throws Exception {
		FileChannel source = null, destination = null;
		try{
			source = new FileInputStream(from).getChannel();
			destination = new FileOutputStream(to).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {if(source != null) source.close(); if(destination != null) destination.close();}
	}
	public static void copyDir(File from, File to){
		for(File f : from.listFiles()){
			try{
				File n = new File(to, f.getName());
				if(f.isDirectory()){n.mkdir(); copyDir(f, n);}
				else copyFile(f, n);
			}catch(Exception e){}
		}
	}
	public File copy(File dir, Project p) throws Exception {
		if(!dir.isDirectory()) throw new Exception(); File f = changeDirectory(dir, p, true);
		copyFile(file, f); return f;
	}
	public void refresh() throws Exception {
		remove(false); removeAllChildren(); read(file);
	}
	public String toString(){return getName();}
	
	private static final HashMap<String, Class<? extends Resource>> resources = new HashMap<String, Class<? extends Resource>>();
	public static JFileChooser resourceChooser = new JFileChooser();
	static {
		resourceChooser.setAcceptAllFileFilterUsed(false); resourceChooser.setMultiSelectionEnabled(true);
		resourceChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}
	public static void register(String name, String ext, Class<? extends Resource> r){
		resources.put(ext, r); resourceChooser.addChoosableFileFilter(new ExtFileFilter(name, new String[]{ext}));
	}
	public static Resource readFile(File f, MapEditor e) throws Exception {
		if(f.isDirectory()){
			Resource ret = new Folder(f, e); ret.read(f); return ret;
		} else {
			String ext = f.getName(); int idx = ext.lastIndexOf('.');
			if(idx == -1) throw new Exception();
			Class<? extends Resource> r = resources.get(ext.substring(idx+1)); if(r == null) throw new Exception();
			Resource ret = r.getConstructor(File.class, MapEditor.class).newInstance(f, e); ret.read(f); return ret;
		}
	}
}
