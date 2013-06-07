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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mrpg.editor.resource.AutoTile;
import mrpg.editor.resource.Folder;
import mrpg.editor.resource.Image;
import mrpg.editor.resource.Map;
import mrpg.editor.resource.Media;
import mrpg.editor.resource.Modifiable;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.editor.resource.Script;
import mrpg.editor.resource.Tileset;
import mrpg.editor.resource.Workspace;
import mrpg.script.HaxeCompiler;


public class WorkspaceBrowser extends JTree implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -2561363020096125820L;
	public static JFileChooser imgChooser = new JFileChooser(), sndChooser = new JFileChooser();
	public static class ExtFileFilter extends FileFilter {
		private String[] ext; private String name;
		public ExtFileFilter(String n, String[] _ext){
			ext = _ext; StringBuilder b = new StringBuilder();
			b.append(n); b.append(" ("); for(String e : ext){
				b.append("*"); b.append(e); if(e != ext[ext.length-1]) b.append(",");
			} b.append(")"); name = b.toString();
		}
		public boolean accept(File f){
			if(f == null || f.toString() == null) return false; if(f.isDirectory()) return true;
			String s = f.toString(); for(String e : ext){
				if(s.endsWith(e)) return true;
			} return false;
		}
		public String getDescription(){return name;}
	}
	static {
		imgChooser.setAcceptAllFileFilterUsed(false); imgChooser.setFileFilter(new ExtFileFilter("Image Files", new String[]{".png",".jpeg",".jpg",".gif"}));
		imgChooser.setMultiSelectionEnabled(true); imgChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		sndChooser.setAcceptAllFileFilterUsed(false); sndChooser.setFileFilter(new ExtFileFilter("Audio Files", new String[]{".wav",".mp3"}));
		sndChooser.setMultiSelectionEnabled(true); sndChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}
	public static final String ADD_FOLDER_ICON = "add_folder", EDIT_ICON = "edit",
		IMAGE_ICON = "image", MEDIA_ICON = "media", DB_ICON = new String("database"), SCRIPT_ICON = "script", TILESET = new String("database"), AUTOTILE = new String("database");
	private final int NO_DRAG=0, CHECK_DRAG=1, HAS_DRAG=2;
	private final MapEditor editor;
	private final JPopupMenu context_menu; private Resource dragTo = null, dragLine = null;
	private int dragType = NO_DRAG; private int shouldSelect = -1; private Resource clipboard[] = null;
	public WorkspaceBrowser(MapEditor e){
		super(new Workspace(e)); editor = e;
		setCellRenderer(new ProjectTreeRenderer());
		context_menu = new JPopupMenu(); context_menu.setOpaque(true); context_menu.setLightWeightPopupEnabled(true);
		MouseListener m = getMouseListeners()[0];
		addMouseListener(this); addMouseMotionListener(this);
		removeMouseListener(m); addMouseListener(m);
		setFocusable(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		setAutoscrolls(true); setRootVisible(false);
		((BasicTreeUI)getUI()).setExpandedIcon(MapEditor.getIcon("expand"));
		((BasicTreeUI)getUI()).setCollapsedIcon(MapEditor.getIcon("collapse"));
		putClientProperty("JTree.lineStyle", "None");
	}
	public Workspace getWorkspace(){return (Workspace)getModel().getRoot();}
	public void addProject(Project p){
		DefaultTreeModel m = (DefaultTreeModel)getModel();
		Workspace w = (Workspace)m.getRoot();
		m.insertNodeInto(p, w, w.getChildCount());
		TreePath path = new TreePath(new Object[]{w, p});
		expandPath(path);
		setSelectionPath(path);
		scrollPathToVisible(path);
		editor.updateSaveButtons();
	}
	public Project addProject(File f) throws Exception {
		Project p = Project.openProject(editor, (Workspace)getModel().getRoot(), f); addProject(p);
		MapEditor.doDeferredRead(false); return p;
	}
	public static Project getProject(TreePath path){return (path.getPathCount() <= 1)?null:(Project)path.getPathComponent(1);}
	public static Project getProject(Resource node){
		Resource ret = null;
		while(node.getParent() != null){
			ret = node;
			node = node.getParent();
		}
		return (Project)ret;
	}
	public void addResource(Resource r, Resource parent){
		DefaultTreeModel m = (DefaultTreeModel)getModel();
		m.insertNodeInto(r, parent, parent.getChildCount());
		TreePath path = new TreePath(r.getPath());
		setSelectionPath(path);
		scrollPathToVisible(path);
	}
	public void removeResource(Resource r, boolean delete){
		try{r.remove(delete);}catch(Exception ex){}
		((DefaultTreeModel)getModel()).removeNodeFromParent(r);
	}
	private void selectProjectError() throws Exception {
		JOptionPane.showMessageDialog(editor, "Please select a project or folder to import into.", "Unable to Import", JOptionPane.ERROR_MESSAGE);
		throw new Exception();
	}
	private Resource getInsertResource() throws Exception {
		if(isSelectionEmpty() && getRowCount() == 0) selectProjectError();
		TreePath path;
		if(isSelectionEmpty()) path = getPathForRow(0);
		else path = getSelectionPath();
		Resource parent = getResource(path); if(parent == null) selectProjectError();
		while(!parent.canAddChildren()){
			parent = parent.getParent();
			if(parent == null) selectProjectError();
		} return parent;
	}
	private void importImage(Resource parent, File f, Project p, boolean sub){
		String dir = parent.getFile().toString();
		String n = f.getName(); int idx = n.lastIndexOf('.'); if(idx != -1) n = n.substring(0,idx); Resource r;
		try{
			if(f.isDirectory()){
				r = Folder.create(new File(dir,n), editor);
				for(File c : f.listFiles()) if(sub || !c.isDirectory()) importImage(r, c, p, sub);
			} else {
				r = Image.importImage(f, new File(dir,n+"."+Image.EXT), editor, p);
			} addResource(r, parent);
		}catch(Exception e){}
	}
	private void importMedia(Resource parent, File f, Project p, boolean sub){
		String dir = parent.getFile().toString();
		String n = f.getName(); int idx = n.lastIndexOf('.'); if(idx != -1) n = n.substring(0,idx); Resource r;
		try{
			if(f.isDirectory()){
				r = Folder.create(new File(dir,n), editor);
				for(File c : f.listFiles()) if(sub || !c.isDirectory()) importMedia(r, c, p, sub);
			} else {
				r = Media.importMedia(f, new File(dir,n+"."+Media.EXT), editor, p);
			} addResource(r, parent);
		}catch(Exception e){}
	}
	private void importResource(Resource parent, File f, Project p){
		String dir = parent.getFile().toString();
		try{
			if(!f.isDirectory()){
				File f2 = new File(dir,f.getName()); Resource.copyFile(f, f2);
				addResource(Resource.readFile(f2, editor), parent);
			}
		}catch(Exception e){}
	}
	public void addFolder(){
		try{
			Resource parent = getInsertResource();
			String name = (String)JOptionPane.showInputDialog(editor, "Enter a name for the new folder:", "Create New Folder", JOptionPane.PLAIN_MESSAGE, null, null, "New Folder");
			if(name == null) throw new Exception();
			String dir = parent.getFile().toString();
			File f = new File(dir, name);
			if(f.exists()){
				JOptionPane.showMessageDialog(editor, "\'"+name+"\' already exists!", "Create New Folder", JOptionPane.ERROR_MESSAGE);
				throw new Exception();
			} addResource(Folder.create(f, editor), parent);
		}catch(Exception e){}
	}
	public void addMap(){
		try{
			Resource parent = getInsertResource();
			Map m = Map.createMap(parent, editor, getProject(parent)); addResource(m, parent);
			if(editor.getWorld() == null) m.edit();
		}catch(Exception e){}
	}
	public void addScript(){
		try{
			Resource parent = getInsertResource();
			String name = (String)JOptionPane.showInputDialog(editor, "Enter a name for the new script:", "Create Script", JOptionPane.PLAIN_MESSAGE, null, null, "New Script");
			if(name == null) throw new Exception();
			String dir = parent.getFile().toString();
			File f = new File(dir,name+"."+Script.EXT);
			if(f.exists()){
				JOptionPane.showMessageDialog(editor, "\'"+name+"\' already exists!", "Create Script", JOptionPane.ERROR_MESSAGE);
				throw new Exception();
			} addResource(Script.createScript(f, editor, getProject(parent)), parent);
		}catch(Exception e){}
	}
	public void addTileset(){
		try{
			Resource parent = getInsertResource();
			Tileset t = Tileset.createTileset(parent, editor, getProject(parent)); addResource(t, parent);
			if(editor.getTilesetViewer().getTilemap() == null) editor.getTilesetViewer().setTilemap(t.getTilemap(), getProject(t));
		}catch(Exception e){}
	}
	public void addAutoTile(){
		try{
			Resource parent = getInsertResource();
			addResource(AutoTile.createAutoTile(parent, editor, getProject(parent)), parent);
		}catch(Exception e){}
	}
	private static final Border underline=BorderFactory.createMatteBorder(1,0,0,0,Color.black),
	empty=BorderFactory.createEmptyBorder(1,0,0,0);
	private class ProjectTreeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = -6438073546792557714L;
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus){
			selected |= value == dragTo;
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			setIcon(((Resource)value).getIcon());
			if(value == dragLine) setBorder(underline);
			else setBorder(empty);
			return this;
		}
	}
	
	public Resource getSelectedResource(){return getResource(getSelectionPath());}
	public static Resource getResource(TreePath p){return (Resource)p.getLastPathComponent();}
	
	public static Object[] getPathToRoot(Resource r, Object root){
		ArrayList<Object> path = new ArrayList<Object>();
		while(true){
			path.add(r);
			if(r == root) break;
			r = r.getParent();
		}
		Object p[] = new Resource[path.size()];
		for(int i=0; i<path.size(); i++) p[path.size()-1-i] = path.get(i);
		return p;
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command == MapEditor.SAVE) saveSelection();
		else if(command == MapEditor.SAVE_ALL) saveAll();
		else if(command == MapEditor.REVERT) revertSelection();
		else if(command == MapEditor.OPEN){
			try{
				Project p = Project.openProject(editor, (Workspace)getModel().getRoot()); addProject(p);
				MapEditor.doDeferredRead(false); if(!editor.hasMap()) p.getMaps().iterator().next().edit();
			}catch(Exception ex){}
		} else if(command == MapEditor.DELETE){
			deleteSelection();
		} else if(command == MapEditor.REFRESH){
			refreshSelection();
		} else if(command == MapEditor.REMOVE){
			Resource r = getSelectedResource(); if(innerAnyModified(r)){
				int i = JOptionPane.showConfirmDialog(this, "Save all maps before removing project?");
				if(i == JOptionPane.NO_OPTION) removeResource(r, false);
				else if(i == JOptionPane.YES_OPTION){
					innerSaveAll(r); removeResource(r, false);
				}
			} else removeResource(r, false);
		} else if(command == MapEditor.RENAME){
			Resource r = getSelectedResource(); String s = r.getName();
			String name = JOptionPane.showInputDialog(this, "Enter new name for \""+s+"\":", s);
			if(name == null || name.length() == 0) return;
			try{
				r.setName(name);
			}catch(Exception ex){}
		} else if(command == ADD_FOLDER_ICON){addFolder();
		} else if(command == EDIT_ICON){getSelectedResource().edit();
		} else if(command == MapEditor.PROPERTIES){getSelectedResource().properties();
		} else if(command == MapEditor.MAP){addMap();
		} else if(command == Project.PROJECT) try{
			Project p = Project.createProject(editor); addProject(p); MapEditor.doDeferredRead(true);
			if(!editor.hasMap()) p.getMaps().iterator().next().edit();
		}catch(Exception ex){}
		else if(command == IMAGE_ICON){
			if(imgChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				try{
					Resource parent = getInsertResource(); boolean sub = false;
					for(File f : imgChooser.getSelectedFiles()) if(f.isDirectory()) sub = true;
					if(sub) sub = JOptionPane.showConfirmDialog(this, "Include Subdirectories?", "Import Images", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION;
					for(File f : imgChooser.getSelectedFiles()) importImage(parent, f, getProject(parent), sub);
					MapEditor.doDeferredRead(true);
				} catch(Exception ex){}
			}
		} else if(command == MEDIA_ICON){
			if(sndChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				try{
					Resource parent = getInsertResource(); boolean sub = false;
					for(File f : sndChooser.getSelectedFiles()) if(f.isDirectory()) sub = true;
					if(sub) sub = JOptionPane.showConfirmDialog(this, "Include Subdirectories?", "Import Media", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION;
					for(File f : sndChooser.getSelectedFiles()) importMedia(parent, f, getProject(parent), sub);
					MapEditor.doDeferredRead(true);
				} catch(Exception ex){}
			}
		} else if(command == DB_ICON){
			if(Resource.resourceChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				try{
					Resource parent = getInsertResource();
					for(File f : Resource.resourceChooser.getSelectedFiles()) importResource(parent, f, getProject(parent));
					MapEditor.doDeferredRead(true);
				} catch(Exception ex){}
			}
		} else if(command == SCRIPT_ICON){addScript();
		} else if(command == TILESET){addTileset();
		} else if(command == AUTOTILE){addAutoTile();}
		else if(command == MapEditor.BUILD){
			try{
				Project p = getProject(getSelectedResource()); innerSaveAll(p); HaxeCompiler.compile(p);
			}catch(Exception ex){ex.printStackTrace();}
		} else if(command == MapEditor.TEST){
			try{
				Project p = getProject(getSelectedResource()); innerSaveAll(p); HaxeCompiler.compile(p); HaxeCompiler.run(p);
			}catch(Exception ex){ex.printStackTrace();}
		}
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		editor.gainBrowserFocus();
		int r = getRowForLocation(e.getX(), e.getY());
		if(r != -1 && isRowSelected(r)){e.consume(); shouldSelect = r;}
		dragType = CHECK_DRAG;
	}
	public boolean canRefreshSelection(){
		return getSelectionCount() > 0;
	}
	public boolean canDeleteSelection(){
		int ct = getSelectionCount(); if(ct == 0) return false;
		if(ct > 1){
			for(TreePath p : getSelectionPaths())
				if(!getResource(p).canDelete()) return false;
			return true;
		} else return getResource(getSelectionPath()).canDelete();
	}
	public static int trimChildren(JTree t, TreePath[] paths){
		int ret = 0;
		for(int i=0; i<paths.length; i++){
			if(t.isPathSelected(paths[i].getParentPath())) paths[i] = null;
			else ret++;
		}
		return ret;
	}
	public TreePath[] getSelectionPathsTrimChildren(){TreePath[] ret = getSelectionPaths(); trimChildren(this, ret); return ret;}
	public void deleteSelection(){
		int ct = getSelectionCount();
		String del = (ct == 1)?"\""+getResource(getSelectionPath())+"\"?":"the "+ct+" selected resources?";
		if(JOptionPane.showConfirmDialog(this, "Are you sure you wish to permanently delete "+del+"?\nThis operation cannot be undone.") == JOptionPane.YES_OPTION){
			DefaultTreeModel m = (DefaultTreeModel)getModel();
			for(TreePath p : getSelectionPathsTrimChildren()){
				if(p == null) continue;
				Resource r = getResource(p);
				try{
					r.remove(true); m.removeNodeFromParent(r);
				} catch(Exception ex){}
			}
		}
		editor.updateSaveButtons();
	}
	public void refreshSelection(){
		DefaultTreeModel m = (DefaultTreeModel)getModel();
		for(TreePath p : getSelectionPathsTrimChildren()){
			if(p == null) continue;
			Resource r = getResource(p); try{r.refresh(); m.reload(r);}catch(Exception e){}
		} MapEditor.doDeferredRead(false);
	}
	public void revertSelection(){
		DefaultTreeModel m = (DefaultTreeModel)getModel();
		for(TreePath p : getSelectionPathsTrimChildren()){
			if(p == null) continue;
			try{Modifiable r = (Modifiable)getResource(p); if(r.isModified()){r.revert(); m.reload(r);}}catch(Exception e){}
		} MapEditor.doDeferredRead(false);
	}
	public void saveSelection(){
		for(TreePath p : getSelectionPathsTrimChildren()){
			if(p == null) continue;
			try{Modifiable r = (Modifiable)getResource(p); if(r.isModified()) r.save();}catch(Exception e){}
		} MapEditor.doDeferredRead(false);
	}
	
	private void innerSaveAll(Resource r){
		for(int i=0; i<r.getChildCount(); i++){
			Resource c = r.getChild(i); innerSaveAll(c);
			try{Modifiable m = (Modifiable)c; if(m.isModified()) m.save();}catch(Exception e){}
		}
	}
	public void saveAll(){innerSaveAll(getWorkspace());}
	public boolean selectionModified(){
		int ct = getSelectionCount(); if(ct == 0) return false;
		if(ct > 1){
			for(TreePath p : getSelectionPaths()){
				Resource r = getResource(p); if(r instanceof Modifiable && ((Modifiable)r).isModified()) return true;
			} return false;
		} else{
			Resource r = getResource(getSelectionPath()); return r instanceof Modifiable && ((Modifiable)r).isModified();
		}
	}
	private boolean innerAnyModified(Resource r){
		for(int i=0; i<r.getChildCount(); i++){
			Resource c = r.getChild(i); if(innerAnyModified(c)) return true;
			if(c instanceof Modifiable && ((Modifiable)c).isModified()) return true;
		} return false;
	}
	public boolean anyModified(){return innerAnyModified(getWorkspace());}
	public void copy(){
		TreePath paths[] = getSelectionPaths();
		clipboard = new Resource[trimChildren(this, paths)]; int i=0;
		for(TreePath p : paths){
			if(p != null) clipboard[i++] = getResource(p);
		}
	}
	public void paste() throws Exception {
		if(clipboard != null){
			TreePath path = getSelectionPath();
			Resource parent = getInsertResource();
			for(int i=0; i<clipboard.length; i++){
				addResource(Resource.readFile(clipboard[i].copy(parent.getFile(), getProject(parent)),editor), parent);
				if(path == null) clearSelection(); else setSelectionPath(path);
			}
		} MapEditor.doDeferredRead(true);
	}
	public boolean hasClipboardData(){return clipboard != null;}
	public void mouseReleased(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if(dragTo != null || dragLine != null){
			DefaultTreeModel m = (DefaultTreeModel)getModel();
			if(dragTo != null){
				for(TreePath p : getSelectionPathsTrimChildren()){
					if(p == null) continue;
					Resource r = getResource(p);
					try{
						r.rename(r.changeDirectory(dragTo.getFile(),getProject(dragTo),false));
						m.removeNodeFromParent(r); m.insertNodeInto(r, dragTo, dragTo.getChildCount());
					} catch(Exception ex){}
				}
			} else {
				if(!isPathSelected(new TreePath(m.getPathToRoot(dragLine)))){
					Resource parent = dragLine.getParent();
					for(TreePath p : getSelectionPathsTrimChildren()){
						if(p == null) continue;
						Resource r = getResource(p);
						try{
							r.rename(r.changeDirectory(parent.getFile(),getProject(parent),false));
							m.removeNodeFromParent(r); m.insertNodeInto(r, parent, parent.getIndex(dragLine));
						} catch(Exception ex){}
					}
				}
			}
			dragTo = null; dragLine = null; shouldSelect = -1;
			repaint();
		}
		dragType = NO_DRAG;
		if(e.isPopupTrigger()){
			int r = getRowForLocation(e.getX(), e.getY());
			context_menu.removeAll();
			if(r == -1){
				context_menu.add(add_project);
				context_menu.add(open_project);
			} else {
				if(!isRowSelected(r)) setSelectionRow(r);
				int ct = getSelectionCount(); if(ct == 0) return;
				
				if(ct > 1){
					for(TreePath p : getSelectionPaths())
						if(!getResource(p).canDelete()) return;
					context_menu.add(delete_all);
					context_menu.add(refresh_all);
				} else {
					Resource res = getResource(getSelectionPath());
					res.contextMenu(context_menu);
					if(res.canDelete()) context_menu.add(delete);
					else context_menu.add(remove);
					context_menu.add(refresh);
					context_menu.add(rename);
				}
			}
			context_menu.show(this, e.getX(), e.getY());
			shouldSelect = -1;
		}
		if(e.getClickCount() % 2 == 0 && getSelectionCount() == 1){
			int r = getRowForLocation(e.getX(), e.getY());
			if(r != -1){
				if(!getResource(getPathForRow(r)).edit()){
					if(isExpanded(r)) collapseRow(r); else expandRow(r);
				}
			}
		}
		if(shouldSelect != -1){
			if(!e.isShiftDown() && getSelectionCount() != 1) setSelectionRow(shouldSelect);
			shouldSelect = -1;
		}
	}
	public final JMenuItem delete = MapEditor.createMenuItemIcon("Delete", MapEditor.DELETE, this),
		delete_all = MapEditor.createMenuItemIcon("Delete Selected", MapEditor.DELETE, this),
		refresh = MapEditor.createMenuItemIcon("Refresh", MapEditor.REFRESH, this),
		refresh_all = MapEditor.createMenuItemIcon("Refresh Selected", MapEditor.REFRESH, this),
		save = MapEditor.createMenuItemIcon("Save", MapEditor.SAVE, this),
		revert = MapEditor.createMenuItemIcon("Revert", MapEditor.REVERT, this),
		remove = MapEditor.createMenuItemIcon("Remove Project", MapEditor.DELETE, MapEditor.REMOVE, this),
		rename = MapEditor.createMenuItemIcon("Rename", MapEditor.RENAME, this),
		add_folder = MapEditor.createMenuItemIcon("Folder", ADD_FOLDER_ICON, this),
		add_map = MapEditor.createMenuItemIcon("Map", MapEditor.MAP, this),
		add_tileset = MapEditor.createMenuItemIcon("Tileset", TILESET, this),
		add_autotile = MapEditor.createMenuItemIcon("AutoTile", AUTOTILE, this),
		add_resource = MapEditor.createMenuItemIcon("Resource File", DB_ICON, this),
		add_image = MapEditor.createMenuItemIcon("Image File", IMAGE_ICON, this),
		add_media = MapEditor.createMenuItemIcon("Audio File", MEDIA_ICON, this),
		add_script = MapEditor.createMenuItemIcon("Script", SCRIPT_ICON, this),
		edit = MapEditor.createMenuItemIcon("Edit", EDIT_ICON, this),
		properties = MapEditor.createMenuItemIcon("Properties", MapEditor.PROPERTIES, this),
		add_project = MapEditor.createMenuItemIcon("Create New Project", Project.PROJECT, this),
		open_project = MapEditor.createMenuItemIcon("Open Existing Project", MapEditor.OPEN, this);
	private boolean notSelected(TreePath p){
		while(p != null){
			if(isPathSelected(p)) return false;
			p = p.getParentPath();
		}
		return true;
	}
	private boolean canDrag(int close, int r){
		TreePath p = getPathForRow(close);
		if(r == close) return getResource(p).canAddChildren() && notSelected(p);
		else return p.getPathCount() > 1 && ((Resource)(p.getPathComponent(p.getPathCount()-2))).canAddChildren() &&
			notSelected(isRowSelected(close-1)?p:p.getParentPath());
	}
	public void mouseDragged(MouseEvent e) {
		scrollRectToVisible(new Rectangle(e.getX(), e.getY(), 1, 1));
		if(dragType == NO_DRAG) return;
		if(dragType == CHECK_DRAG){
			int r = getRowForLocation(e.getX(), e.getY());
			if(r == -1 || !isRowSelected(r)){dragType = NO_DRAG; return;}
			Resource res = getResource(getPathForRow(r));
			if(!res.canDelete()){dragType = NO_DRAG;}
			else dragType = HAS_DRAG;
			if(dragType == NO_DRAG){setCursor(DragSource.DefaultMoveNoDrop); return;}
		}
		shouldSelect = -1;
        dragTo = null; dragLine = null;
        if(e.getX() > getWidth() || e.getY() > getHeight()){setCursor(DragSource.DefaultMoveNoDrop); repaint(); return;}
		int close = getClosestRowForLocation(e.getX(), e.getY());
		int r = getRowForLocation(e.getX(), e.getY());
		if(canDrag(close, r)){
			setCursor(DragSource.DefaultMoveDrop);
			if(r == close) dragTo = getResource(getPathForRow(close));
			else dragLine = getResource(getPathForRow(close));
		} else setCursor(DragSource.DefaultMoveNoDrop);
		repaint();
	}
	public void mouseMoved(MouseEvent e) {}
}
