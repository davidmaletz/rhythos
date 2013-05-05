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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mrpg.editor.resource.AutoTile;
import mrpg.editor.resource.Folder;
import mrpg.editor.resource.Map;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.editor.resource.Tileset;
import mrpg.editor.resource.Type;
import mrpg.editor.resource.Workspace;


public class WorkspaceBrowser extends JTree implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -2561363020096125820L;
	public static final String ADD_FOLDER_ICON = "add_folder", EDIT_ICON = "edit",
		IMAGE_ICON = "image", MEDIA_ICON = "media", SCRIPT_ICON = "script", TILESET = "database", AUTOTILE = new String("database");
	private static final byte CHECK_DRAG = -1; 
	private final MapEditor editor;
	private final JPopupMenu context_menu; private Resource dragTo = null, dragLine = null;
	private byte dragType = Type.NONE; private Object dragKey = null; private int shouldSelect = -1; private Resource clipboard[] = null;
	public WorkspaceBrowser(MapEditor e){
		super(new Workspace("Workspace", e));
		editor = e;
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
		editor.updateSaveButtons(); if(editor.getWorld() == null) p.editFirstMap();
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
		getProject(parent).setModified(true);
		DefaultTreeModel m = (DefaultTreeModel)getModel();
		m.insertNodeInto(r, parent, parent.getChildCount());
		TreePath path = new TreePath(r.getPath());
		setSelectionPath(path);
		scrollPathToVisible(path);
	}
	
	public void addResource(Resource r){
		if(isSelectionEmpty() && getRowCount() == 0){
			Project p = new Project(editor);
			p.addResource(r);
			addProject(p);
			setSelectionPath(new TreePath(r.getPath()));
		} else {
			TreePath path;
			if(isSelectionEmpty()) path = getPathForRow(0);
			else path = getSelectionPath();
			Resource parent = getResource(path);
			if(parent != null && parent.canAdd(r.getType(), r.getTypeKey()))
				addResource(r, parent);
			else {
				Project p = (Project)path.getPathComponent(1);
				p.addResource(r); ((DefaultTreeModel)getModel()).reload(p);
				setSelectionPath(new TreePath(r.getPath()));
			}
		}
	}
	
	private void addResources(TreePath paths[]){
		TreePath path = getSelectionPath();
		TreePath select[] = new TreePath[paths.length]; int i=0;
		for(TreePath p : paths){
			if(p == null) continue;
			addResource(getResource(p).copy());
			select[i++] = getSelectionPath();
			if(path == null) clearSelection(); else setSelectionPath(path);
		}
		clearSelection();
		addSelectionPaths(select);
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
		if(command == MapEditor.OPEN){//editor.openProject();
		} else if(command == MapEditor.DELETE){
			deleteSelection();
		} else if(command == MapEditor.RENAME){
			Resource r = getSelectedResource(); String s = r.getName();
			String name = JOptionPane.showInputDialog(this, "Enter new name for \""+s+"\":", s);
			if(name == null || name.length() == 0) return;
			r.setName(name);
			((DefaultTreeModel)getModel()).nodeChanged(r);
		} else if(command == ADD_FOLDER_ICON){addResource(new Folder(editor), getSelectedResource());
		} else if(command == EDIT_ICON){getSelectedResource().edit();
		} else if(command == MapEditor.PROPERTIES){getSelectedResource().properties();
		} else if(command == MapEditor.MAP){
			Map m = Map.createMap(editor);
			if(m != null){
				addResource(m);
				if(editor.getWorld() == null) m.edit();
			}
		} else if(command == Project.PROJECT) addProject(new Project(editor));
		else if(command == IMAGE_ICON){
			/*TODO: ImageChooser chooser = new ImageChooser(new Folder.Remote("C:/Java/OpenWorlds/FSM", editor, Type.IMAGE), null);
			chooser.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
			chooser.setVisible(true);
			if(chooser.tree.getSelectionCount() != 0){
				TreePath[] paths = chooser.tree.getSelectionPaths(); trimChildren(chooser.tree, paths);
				addResources(paths);
			}*/
		} else if(command == MEDIA_ICON){
			/*TODO: MediaChooser chooser = new MediaChooser(new Folder.Remote("C:/Java/OpenWorlds/FSM", editor, Type.MEDIA), null);
			chooser.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
			chooser.setVisible(true);
			if(chooser.tree.getSelectionCount() != 0){
				TreePath[] paths = chooser.tree.getSelectionPaths(); trimChildren(chooser.tree, paths);
				addResources(paths);
			}*/
		} else if(command == SCRIPT_ICON){/*addResource(new Script(Script.DEFAULT_NAME, editor));*/
		} else if(command == TILESET){
			Tileset t = Tileset.createTileset(editor);
			if(t != null){
				addResource(t);
				if(editor.getTilesetViewer().getTilemap() == null) editor.getTilesetViewer().setTilemap(t.getTilemap());
			}
		} else if(command == AUTOTILE){
			AutoTile t = AutoTile.createAutoTile(editor);
			if(t != null){
				addResource(t);
			}
		}
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		editor.gainBrowserFocus();
		int r = getRowForLocation(e.getX(), e.getY());
		if(r != -1 && isRowSelected(r)){e.consume(); shouldSelect = r;}
		dragType = CHECK_DRAG; dragKey = null;
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
		if(JOptionPane.showConfirmDialog(this, "Are you sure you wish to remove "+del) == JOptionPane.YES_OPTION){
			DefaultTreeModel m = (DefaultTreeModel)getModel();
			for(TreePath p : getSelectionPathsTrimChildren()){
				if(p == null) continue;
				Resource r = getResource(p);
				getProject(r).setModified(true);
				r.remove();
				m.removeNodeFromParent(r);
			}
		}
		editor.updateSaveButtons();
	}
	public void cut(){
		TreePath paths[] = getSelectionPaths();
		clipboard = new Resource[trimChildren(this, paths)]; int i=0;
		DefaultTreeModel m = (DefaultTreeModel)getModel();
		for(TreePath p : paths){
			if(p != null){
				Resource r = getResource(p);
				getProject(r).setModified(true);
				clipboard[i++] = r;
				r.remove();
				m.removeNodeFromParent(r);
			}
		}
	}
	public void copy(){
		TreePath paths[] = getSelectionPaths();
		clipboard = new Resource[trimChildren(this, paths)]; int i=0;
		for(TreePath p : paths){
			if(p != null) clipboard[i++] = getResource(p).copy();
		}
	}
	public void paste(){
		if(clipboard != null){
			TreePath path = getSelectionPath();
			for(int i=0; i<clipboard.length; i++){
				if(clipboard[i] instanceof Project) addProject((Project)clipboard[i].copy());
				else addResource(clipboard[i].copy());
				if(path == null) clearSelection(); else setSelectionPath(path);
			}
		}
	}
	public boolean hasClipboardData(){return clipboard != null;}
	public void mouseReleased(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if(dragTo != null || dragLine != null){
			DefaultTreeModel m = (DefaultTreeModel)getModel();
			if(dragTo != null){
				getProject(dragTo).setModified(true);
				for(TreePath p : getSelectionPathsTrimChildren()){
					if(p == null) continue;
					getProject(p).setModified(true);
					Resource r = getResource(p);
					m.removeNodeFromParent(r); m.insertNodeInto(r, dragTo, dragTo.getChildCount());
				}
			} else {
				if(!isPathSelected(new TreePath(m.getPathToRoot(dragLine)))){
					Resource parent = dragLine.getParent();
					Project pr = getProject(parent);
					if(pr != null) pr.setModified(true);
					for(TreePath p : getSelectionPathsTrimChildren()){
						if(p == null) continue;
						pr = getProject(p.getParentPath());
						if(pr != null) pr.setModified(true);
						Resource r = getResource(p);
						m.removeNodeFromParent(r); m.insertNodeInto(r, parent, parent.getIndex(dragLine));
					}
				}
			}
			dragTo = null; dragLine = null; shouldSelect = -1;
			repaint();
		}
		dragType = Type.NONE; dragKey = null;
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
				} else {
					Resource res = getResource(getSelectionPath());
					res.contextMenu(context_menu);
					if(res.canDelete()) context_menu.add(delete);
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
	public final JMenuItem delete = MapEditor.createMenuItemIcon("Remove", MapEditor.DELETE, this),
		delete_all = MapEditor.createMenuItemIcon("Remove Selected", MapEditor.DELETE, this),
		rename = MapEditor.createMenuItemIcon("Rename", MapEditor.RENAME, this),
		add_folder = MapEditor.createMenuItemIcon("Create New Folder", ADD_FOLDER_ICON, this),
		add_map = MapEditor.createMenuItemIcon("Create New Map", MapEditor.MAP, this),
		add_tileset = MapEditor.createMenuItemIcon("Create New Tileset", TILESET, this),
		add_autotile = MapEditor.createMenuItemIcon("Create New AutoTile", AUTOTILE, this),
		add_image = MapEditor.createMenuItemIcon("Import Image", IMAGE_ICON, this),
		add_media = MapEditor.createMenuItemIcon("Import Audio", MEDIA_ICON, this),
		add_script = MapEditor.createMenuItemIcon("Create New Script", SCRIPT_ICON, this),
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
		if(r == close) return getResource(p).canAdd(dragType, dragKey) && notSelected(p);
		else return p.getPathCount() > 1 && ((Resource)(p.getPathComponent(p.getPathCount()-2))).canAdd(dragType, dragKey) &&
			notSelected(isRowSelected(close-1)?p:p.getParentPath());
	}
	public void mouseDragged(MouseEvent e) {
		scrollRectToVisible(new Rectangle(e.getX(), e.getY(), 1, 1));
		if(dragType == Type.NONE) return;
		if(dragType == CHECK_DRAG){
			int r = getRowForLocation(e.getX(), e.getY());
			if(r == -1 || !isRowSelected(r)){dragType = Type.NONE; return;}
			Resource res = getResource(getPathForRow(r));
			if(!res.canDelete()){dragType = Type.NONE; dragKey = null;}
			else{dragType = res.getType(); dragKey = res.getTypeKey();}
			if(dragType == Type.NONE){setCursor(DragSource.DefaultMoveNoDrop); return;}
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
