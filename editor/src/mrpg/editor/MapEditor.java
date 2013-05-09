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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mrpg.display.WorldPanel;
import mrpg.editor.resource.AutoTile;
import mrpg.editor.resource.Image;
import mrpg.editor.resource.Map;
import mrpg.editor.resource.Media;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.editor.resource.Tileset;
import mrpg.editor.resource.Workspace;
import mrpg.editor.tools.FillTool;
import mrpg.editor.tools.LineTool;
import mrpg.editor.tools.PencilTool;
import mrpg.editor.tools.RectTool;
import mrpg.editor.tools.SelectTool;
import mrpg.editor.tools.Tool;
import mrpg.editor.tools.ZoomTool;
import mrpg.export.SWFTarget;
import mrpg.export.Target;
import mrpg.world.Tile;
import mrpg.world.World;

public class MapEditor extends JFrame implements WindowListener, ActionListener, ChangeListener, TreeSelectionListener, History.Listener, SelectTool.Listener, ZoomTool.Listener, Clipboard.Listener {
	
	private static final long serialVersionUID = 7934438411041874443L;
	
	public static final String NEW = "new", OPEN = "open", SAVE = "save", SAVE_ALL = "save_all",
		IMPORT = "import", TEST = "test", BUILD = "build", SEARCH = "search", HELP = "help", ABOUT = "about";
	public static final String CUT = "cut", COPY = "copy", PASTE = "paste", DELETE = "delete", REFRESH = "refresh", REVERT = "revert", REMOVE = "remove", SEL_ALL = "sel_all",
		DSEL_ALL = "dsel_all", SHOW_ALL = "show_all", SHOW_GRID = "grid", NEXT_LAYER = "next_l", PREV_LAYER = "prev_l";
	public static final String RENAME = "rename";
	public static final String UNDO = "undo", REDO = "redo", SELECT = "select", ZOOM = "zoom", PENCIL = "pencil",
		LINE = "line", RECT = "rect", FILL = "fill", PROPERTIES = "properties", LAYER = "layer", MAP = "map";
	public static final String OK = "ok", CANCEL = "cancel", SET = "set", CLEAR = "clear", M_PLAYER = "media_player"; 
	public static final int MAX_LAYERS = 20;
	private final ButtonGroup group = new ButtonGroup(); private WorldOverlay world_overlay;
	private SelectTool SELECT_TOOL; private Tool ZOOM_TOOL, PENCIL_TOOL, LINE_TOOL, RECT_TOOL, FILL_TOOL;
	private JLabel map_label; private JSpinner zoom_spinner, layer_spinner; private WorkspaceBrowser browser;
	private String map_name = ""; private int map_x = 0, map_y = 0; public boolean browser_focus = false;
	private AbstractButton undo1, undo2, redo1, redo2, cut, copy, paste, delete, dsel, select, showAll, pl1, pl2,
		nl1, nl2, prop, save, save2, save3, saveall, refresh, revert;
	private final ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>(); private Map current_map;
	
	private final History history = new History(); private final Clipboard clipboard = new Clipboard();
	private TilesetViewer tileset_viewer; private MediaPlayer media_player = null;

	public MapEditor(){
		super("Rhythos! Game Maker");
		InputMap im = (InputMap)UIManager.getDefaults().get("Button.focusInputMap");
        Object pressedAction = im.get(KeyStroke.getKeyStroke("pressed SPACE"));
        Object releasedAction = im.get(KeyStroke.getKeyStroke("released SPACE"));
        im.put(KeyStroke.getKeyStroke("pressed ENTER"), pressedAction);
        im.put(KeyStroke.getKeyStroke("released ENTER"), releasedAction);
        
		history.listener = this; clipboard.listener = this;
		WorldPanel w = new WorldPanel(Tile.tile_size,Tile.tile_size);
		browser = new WorkspaceBrowser(this); browser.addTreeSelectionListener(this);
		setJMenuBar(createMenuBar(browser));
		Container c = getContentPane(); c.setLayout(new BorderLayout());
		c.add(createMainToolbar(), BorderLayout.NORTH);
		JSplitPane frame = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		c.add(frame, BorderLayout.CENTER);
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//ProjectViewer pv = new ProjectViewer(w, tv);
		//pane.add(tv);
		//pane.add(pv);
		pane.setResizeWeight(0.75);
		tileset_viewer = new TilesetViewer();
		world_overlay = new WorldOverlay(this, w); tileset_viewer.overlay = world_overlay;
		w.overlay = tileset_viewer.overlay; w.showLevel(0);
		JPanel tileset = new JPanel(new BorderLayout());
		tileset.add(createTilesetToolbar(), BorderLayout.NORTH);
		tileset.add(new JScrollPane(tileset_viewer), BorderLayout.CENTER);
		pane.add(tileset);
		pane.add(new JScrollPane(browser));
		pane.setDividerLocation(Tile.tile_size*8);
		JScrollPane sp = new JScrollPane(w); Insets i = sp.getInsets(); sp.getViewport().setBackground(new Color(30,30,30));
		int sw = sp.getVerticalScrollBar().getPreferredSize().width+i.left+i.right;
		i = pane.getInsets();
		frame.setDividerLocation(Tile.tile_size*8+1+sw+i.left+i.right+1);
		frame.add(pane);
		sp.setMinimumSize(new Dimension(Tile.tile_size+sw,Tile.tile_size+sp.getHorizontalScrollBar().getPreferredSize().height));
		JPanel edit = new JPanel(new BorderLayout());
		edit.add(sp, BorderLayout.CENTER);
		JPanel options = new JPanel(new BorderLayout());
		JPanel inner = new JPanel();
		JLabel l = new JLabel(getIcon(ZOOM)); l.setToolTipText("Map Scale"); inner.add(l);
		zoom_spinner = new JSpinner(new SpinnerNumberModel(100.0, 6.25, 800.0, 1.0));
		zoom_spinner.addChangeListener(this); inner.add(zoom_spinner);
		inner.add(new JLabel("%"));
		options.add(inner, BorderLayout.WEST);
		map_label = new JLabel("", getIcon(MAP), JLabel.CENTER);
		options.add(map_label, BorderLayout.CENTER);
		inner = new JPanel();
		l = new JLabel(getIcon(LAYER)); l.setToolTipText("Map Editing Layer"); inner.add(l);
		layer_spinner = new JSpinner(new SpinnerNumberModel(0, 0, MAX_LAYERS, 1));
		layer_spinner.addChangeListener(this); inner.add(layer_spinner);
		options.add(inner, BorderLayout.EAST);
		edit.add(options, BorderLayout.SOUTH);
		frame.add(edit);
		
		SELECT_TOOL = new SelectTool(w, history); ZOOM_TOOL = new ZoomTool(w); PENCIL_TOOL = new PencilTool(w, tileset_viewer, history);
		LINE_TOOL = new LineTool(w, tileset_viewer, history); RECT_TOOL = new RectTool(w, tileset_viewer, history); FILL_TOOL = new FillTool(w, tileset_viewer, history);
		SELECT_TOOL.listener = this; ((ZoomTool)ZOOM_TOOL).listener = this;
		world_overlay.setTool(PENCIL_TOOL);
		
		deselect(); historyChanged(); clipboardChanged(); valueChanged(null);
		
		w.startAnim();
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); setSize(800,600); setVisible(true);
	}
	
	private static Hashtable<String, ImageIcon> icons = new Hashtable<String, ImageIcon>();
	public static ImageIcon getIcon(String s){
		if(icons.containsKey(s)) return icons.get(s);
		ImageIcon i = new ImageIcon(MapEditor.class.getResource("/icons/"+s+".png"));
		icons.put(s, i); return i;
	}
	public static JMenuItem createMenuItemIcon(String text, String icon, ActionListener l){
		JMenuItem i = new JMenuItem(text, getIcon(icon)); i.setActionCommand(icon); i.addActionListener(l); return i;
	}
	public static JMenuItem createMenuItemIcon(String text, String icon, int keyCode, int modifiers, ActionListener l){
		JMenuItem i = createMenuItemIcon(text, icon, l); i.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers)); return i;
	}
	public static JMenuItem createMenuItemIcon(String text, String icon, String command, ActionListener l){
		JMenuItem i = new JMenuItem(text, getIcon(icon)); i.setActionCommand(command); i.addActionListener(l); return i;
	}
	public static JMenuItem createMenuItem(String text, String command, ActionListener l){
		JMenuItem i = new JMenuItem(text); i.setActionCommand(command); i.addActionListener(l); return i;
	}
	public static JMenuItem createMenuItem(String text, String command, int keyCode, int modifiers, ActionListener l){
		JMenuItem i = createMenuItem(text, command, l); i.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers)); return i;
	}
	public static JCheckBoxMenuItem createCheckboxMenuItem(String text, String command, ActionListener l){
		JCheckBoxMenuItem i = new JCheckBoxMenuItem(text); i.setActionCommand(command); i.addActionListener(l); return i;
	}
	public static JCheckBoxMenuItem createCheckboxMenuItem(String text, String command, int keyCode, int modifiers, ActionListener l){
		JCheckBoxMenuItem i = createCheckboxMenuItem(text, command, l); i.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers)); return i;
	}
	public static JButton createToolbarButton(String icon, String tooltip, ActionListener l){
		JButton b = new JButton(getIcon(icon)); b.setMargin(new Insets(0,0,0,0)); b.setActionCommand(icon);
		b.addActionListener(l); b.setToolTipText(tooltip); return b;
	}
	public static JToggleButton createToolbarButton(String icon, String tooltip, ButtonGroup group, ActionListener l){return createToolbarButton(icon, tooltip, group, l, false);}
	public static JToggleButton createToolbarButton(String icon, String tooltip, ButtonGroup group, ActionListener l, boolean selected){
		JToggleButton b = new JToggleButton(getIcon(icon)); b.setMargin(new Insets(0,0,0,0)); b.setActionCommand(icon);
		b.setToolTipText(tooltip);  b.addActionListener(l);  b.setSelected(selected); group.add(b); return b;
	}
	
	private JMenuBar createMenuBar(WorkspaceBrowser browser){
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.add(createMenuItemIcon("Create New Project", Project.PROJECT, KeyEvent.VK_N, ActionEvent.CTRL_MASK, browser));
		menu.add(createMenuItemIcon("Open Existing Project", OPEN, KeyEvent.VK_O, ActionEvent.CTRL_MASK, browser));
		JMenu inner = new JMenu("Create New"); inner.setIcon(getIcon(NEW));
		inner.add(createMenuItemIcon("Folder", WorkspaceBrowser.ADD_FOLDER_ICON, KeyEvent.VK_F, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, this));
		inner.add(createMenuItemIcon("Map", MAP, KeyEvent.VK_M, ActionEvent.CTRL_MASK, browser));
		inner.add(createMenuItemIcon("Tileset", WorkspaceBrowser.TILESET, KeyEvent.VK_T, ActionEvent.CTRL_MASK, browser));
		inner.add(createMenuItemIcon("Autotile", WorkspaceBrowser.AUTOTILE, KeyEvent.VK_T, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, browser));
		inner.add(createMenuItemIcon("Script", WorkspaceBrowser.SCRIPT_ICON, KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, browser));
		menu.add(inner);
		menu.addSeparator();
		save3 = createMenuItemIcon("Save Current Map", SAVE, KeyEvent.VK_S, ActionEvent.CTRL_MASK, this); menu.add(save3); save3.setEnabled(false);
		save = createMenuItemIcon("Save Selected", SAVE, browser); menu.add(save); save.setEnabled(false);
		saveall = createMenuItem("Save All", SAVE_ALL, KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, browser); menu.add(saveall); saveall.setEnabled(false);
		revert = createMenuItemIcon("Revert", REVERT, browser); menu.add(revert); revert.setEnabled(false);
		menu.addSeparator();
		inner = new JMenu("Import"); inner.setIcon(getIcon(IMPORT));
		inner.add(createMenuItemIcon("Resource File", WorkspaceBrowser.DB_ICON, KeyEvent.VK_R, ActionEvent.CTRL_MASK, browser));
		inner.add(createMenuItemIcon("Image File", WorkspaceBrowser.IMAGE_ICON, KeyEvent.VK_I, ActionEvent.CTRL_MASK, browser));
		inner.add(createMenuItemIcon("Audio File", WorkspaceBrowser.MEDIA_ICON, KeyEvent.VK_I, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, browser));
		menu.add(inner);
		menu.addSeparator();
		refresh = createMenuItemIcon("Refresh", REFRESH, KeyEvent.VK_F5, 0, browser); menu.add(refresh); refresh.setEnabled(false);
		AbstractButton b = createMenuItemIcon("Rename", RENAME, browser);
		menu.add(b); buttons.add(b);
		b = createMenuItemIcon("Properties", PROPERTIES, KeyEvent.VK_P, ActionEvent.CTRL_MASK, browser);
		menu.add(b); buttons.add(b);
		bar.add(menu);
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		undo1 = createMenuItemIcon("Undo", UNDO, KeyEvent.VK_Z, ActionEvent.CTRL_MASK, this); menu.add(undo1);
		redo1 = createMenuItemIcon("Redo", REDO, KeyEvent.VK_Y, ActionEvent.CTRL_MASK, this); menu.add(redo1);
		menu.addSeparator();
		cut = createMenuItemIcon("Cut", CUT, KeyEvent.VK_X, ActionEvent.CTRL_MASK, this); menu.add(cut);
		copy = createMenuItemIcon("Copy", COPY, KeyEvent.VK_C, ActionEvent.CTRL_MASK, this); menu.add(copy);
		paste = createMenuItemIcon("Paste", PASTE, KeyEvent.VK_V, ActionEvent.CTRL_MASK, this); menu.add(paste);
		delete = createMenuItemIcon("Delete", DELETE, KeyEvent.VK_DELETE, 0, this); menu.add(delete);
		menu.addSeparator();
		menu.add(createMenuItem("Select All", SEL_ALL, KeyEvent.VK_A, ActionEvent.CTRL_MASK, this));
		dsel = createMenuItem("Deselect All", DSEL_ALL, KeyEvent.VK_D, ActionEvent.CTRL_MASK, this); menu.add(dsel);
		bar.add(menu);
		menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		menu.add(createCheckboxMenuItem("Show Grid", SHOW_GRID, KeyEvent.VK_G, ActionEvent.CTRL_MASK, this));
		showAll = createCheckboxMenuItem("Show All Layers", SHOW_ALL, KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, this);
		menu.add(showAll);
		menu.addSeparator();
		pl1 = createMenuItemIcon("Previous Layer", PREV_LAYER, KeyEvent.VK_L, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, this);
		pl1.setEnabled(false); menu.add(pl1);
		nl1 = createMenuItemIcon("Next Layer", NEXT_LAYER, KeyEvent.VK_L, ActionEvent.CTRL_MASK, this); menu.add(nl1);
		bar.add(menu);
		menu = new JMenu("Tools");
		b = createMenuItemIcon("Test Game", TEST, KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK, browser);
		menu.add(b); buttons.add(b);
		menu.addSeparator();
		b = createMenuItemIcon("Build Game", BUILD, KeyEvent.VK_ENTER, ActionEvent.ALT_MASK, browser); menu.add(b); buttons.add(b);
		menu.addSeparator();
		menu.add(createMenuItemIcon("Media Player", M_PLAYER, KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, this));
		menu.add(createMenuItemIcon("Search", SEARCH, KeyEvent.VK_F, ActionEvent.CTRL_MASK, this));
		bar.add(menu);
		menu = new JMenu("Help");
		menu.add(createMenuItemIcon("Contents", HELP, KeyEvent.VK_F1, 0, this));
		menu.addSeparator();
		menu.add(createMenuItemIcon("About", ABOUT, this));
		bar.add(menu);
		return bar;
	}
	private JToolBar createMainToolbar(){
		JToolBar bar = new JToolBar();
		bar.setFloatable(false); bar.setRollover(false);
		bar.add(createToolbarButton(Project.PROJECT, "Create New Project", browser));
		bar.add(createToolbarButton(OPEN, "Open Existing Project", browser));
		bar.addSeparator();
		save2 = createToolbarButton(SAVE, "Save Current Map", this); bar.add(save2); save2.setEnabled(false);
		AbstractButton b = createToolbarButton(RENAME, "Rename Selected Resource", browser);
		bar.add(b); buttons.add(b);
		bar.addSeparator();
		pl2 = createToolbarButton(PREV_LAYER, "Previous Layer", this); pl2.setEnabled(false); bar.add(pl2);
		nl2 = createToolbarButton(NEXT_LAYER, "Next Layer", this); bar.add(nl2);
		bar.addSeparator();
		b = createToolbarButton(TEST, "Test Game", browser); bar.add(b); buttons.add(b);
		b = createToolbarButton(BUILD, "Build Game", browser); bar.add(b); buttons.add(b);
		bar.addSeparator();
		bar.add(createToolbarButton(SEARCH, "Search", this));
		bar.addSeparator();
		bar.add(createToolbarButton(HELP, "Help Contents", this));
		return bar;
	}
	private JToolBar createTilesetToolbar(){
		JToolBar bar = new JToolBar();
		bar.setFloatable(false); bar.setRollover(false);
		undo2 = createToolbarButton(UNDO, "Undo action", this); bar.add(undo2);
		redo2 = createToolbarButton(REDO, "Redo action", this); bar.add(redo2);
		bar.addSeparator();
		select = createToolbarButton(SELECT, "Select tiles", group, this); bar.add(select);
		bar.add(createToolbarButton(ZOOM, "Zoom in/out", group, this));
		bar.add(createToolbarButton(PENCIL, "Freehand drawing tool", group, this, true));
		bar.add(createToolbarButton(LINE, "Line drawing tool", group, this));
		bar.add(createToolbarButton(RECT, "Rectangle drawing tool", group, this));
		bar.add(createToolbarButton(FILL, "Flood fill tool", group, this));
		bar.addSeparator();
		prop = createToolbarButton(PROPERTIES, "Map properties", this); prop.setEnabled(false); bar.add(prop);
		return bar;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command == SAVE){
			if(current_map != null && current_map.isModified()) try{current_map.save();}catch(Exception ex){}
		} else if(command == CUT){if(!browser_focus){SELECT_TOOL.copy(clipboard); SELECT_TOOL.deleteSelection();}
		} else if(command == COPY){if(browser_focus){browser.copy(); paste.setEnabled(browser.hasClipboardData());} else SELECT_TOOL.copy(clipboard);
		} else if(command == PASTE){if(browser_focus) try{browser.paste();}catch(Exception ex){}
			else {world_overlay.setTool(SELECT_TOOL); group.setSelected(select.getModel(), true); SELECT_TOOL.paste(clipboard);
		}} else if(command == DELETE){if(browser_focus) browser.deleteSelection(); else SELECT_TOOL.deleteSelection();
		} else if(command == SEL_ALL){
			if(browser_focus){browser.addSelectionInterval(0, browser.getRowCount()-1);}
			else {
				if(world_overlay.getPanel().getWorld() != null){
				world_overlay.setTool(SELECT_TOOL); group.setSelected(select.getModel(), true); SELECT_TOOL.selectAll();
		} } } else if(command == DSEL_ALL){if(browser_focus) browser.clearSelection(); else SELECT_TOOL.deselectAll();
		} else if(command == SHOW_ALL){
				WorldPanel world = world_overlay.getPanel();
				world.showLevel((showAll.isSelected())?-1:world.getEditLevel());
		} else if(command == SHOW_GRID){world_overlay.getPanel().setShowGrid(((AbstractButton)e.getSource()).isSelected());
		} else if(command == PREV_LAYER){layer_spinner.setValue(Math.max(0, (Integer)layer_spinner.getValue()-1));
		} else if(command == NEXT_LAYER){layer_spinner.setValue(Math.min(MAX_LAYERS, (Integer)layer_spinner.getValue()+1));
		} else if(command == UNDO){
			if(world_overlay.getTool() == SELECT_TOOL) SELECT_TOOL.activate();
			history.undo(); world_overlay.getPanel().repaint();
		} else if(command == REDO){
			if(world_overlay.getTool() == SELECT_TOOL) SELECT_TOOL.activate();
			history.redo(); world_overlay.getPanel().repaint();
		} else if(command == SELECT){world_overlay.setTool(SELECT_TOOL);
		} else if(command == ZOOM){world_overlay.setTool(ZOOM_TOOL);
		} else if(command == PENCIL){world_overlay.setTool(PENCIL_TOOL);
		} else if(command == LINE){world_overlay.setTool(LINE_TOOL);
		} else if(command == RECT){world_overlay.setTool(RECT_TOOL);
		} else if(command == FILL){world_overlay.setTool(FILL_TOOL);
		} else if(command == PROPERTIES){if(current_map != null) current_map.properties();
		} else if(command == WorkspaceBrowser.ADD_FOLDER_ICON){
			browser.addFolder();
		} else if(command == M_PLAYER){
			if(media_player == null) media_player = new MediaPlayer(getBrowser());
			TreePath p = browser.getSelectionPath(); if(p != null) media_player.setProject(WorkspaceBrowser.getProject(p));
			media_player.setVisible(true);
		}
		if(!browser_focus){
			if(world_overlay.getTool() != SELECT_TOOL) deselect();
			removeFocus();
		}
	}
	
	private int lastSave;
	public void historyChanged(){
		boolean hasUndo = history.hasUndo(); undo1.setEnabled(hasUndo); undo2.setEnabled(hasUndo);
		boolean hasRedo = history.hasRedo(); redo1.setEnabled(hasRedo); redo2.setEnabled(hasRedo);
		if(current_map != null){current_map.setModified(lastSave != history.redoPos()); updateSaveButtons();} 
	}
	
	public void select(){
		dsel.setEnabled(true);
		if(browser_focus){delete.setEnabled(browser.canDeleteSelection()); refresh.setEnabled(browser.canRefreshSelection());}
		else{delete.setEnabled(true); refresh.setEnabled(false);}
		cut.setEnabled(!browser_focus); copy.setEnabled(delete.isEnabled());
	}
	public void deselect(){
		delete.setEnabled(false); dsel.setEnabled(false); refresh.setEnabled(false);
		cut.setEnabled(delete.isEnabled()); copy.setEnabled(delete.isEnabled());
	}
	
	public void gainBrowserFocus(){
		browser_focus = true; if(browser.getSelectionCount() > 0) select(); else deselect();
		paste.setEnabled(browser.hasClipboardData());
	}
	public void loseBrowserFocus(){
		browser_focus = false; if(SELECT_TOOL.hasSelection()) select(); else deselect();
		paste.setEnabled(clipboard.hasData());
	}
	private void updateSaveRevert(){
		save.setEnabled(browser.selectionModified());
		save2.setEnabled(current_map != null && current_map.isModified());
		save3.setEnabled(save2.isEnabled()); revert.setEnabled(save.isEnabled());
	}
	public void updateSaveButtons(){
		updateSaveRevert(); saveall.setEnabled(browser.anyModified());
	}
	public void valueChanged(TreeSelectionEvent e) {
		gainBrowserFocus();
		int ct = browser.getSelectionCount();
		for(AbstractButton b : buttons){
			String command = b.getActionCommand();
			if(command == PROPERTIES) b.setEnabled(ct == 1 && WorkspaceBrowser.getResource(browser.getSelectionPath()).hasProperties());
			else if(command == WorkspaceBrowser.IMAGE_ICON) b.setEnabled(ct == 1 && (WorkspaceBrowser.getResource(browser.getSelectionPath()) instanceof Image));
			else if(command == WorkspaceBrowser.MEDIA_ICON) b.setEnabled(ct == 1 && (WorkspaceBrowser.getResource(browser.getSelectionPath()) instanceof Media));
			else if(command == RENAME) b.setEnabled(ct == 1);
			else b.setEnabled(ct > 0);
		}
		if(ct > 0) select(); else deselect();
		updateSaveRevert();
	}
	
	public TilesetViewer getTilesetViewer(){return tileset_viewer;}
	public void refreshTilesets(){
		tileset_viewer.refresh(); if(current_map != null) current_map.getWorld().refresh(WorkspaceBrowser.getProject(current_map));
	}
	public boolean hasMap(){return current_map != null;}
	public World getWorld(){return world_overlay.getPanel().getWorld();}
	public void setMap(Map m){
		World w = m.getWorld();
		WorldPanel p = world_overlay.getPanel(); 
		if(p.getWorld() == w) return;
		current_map = m; lastSave = 0;
		prop.setEnabled(true);
		p.setWorld(w); p.worldSize(w.getWidth()*Tile.tile_size, w.getHeight()*Tile.tile_size);
		history.clearHistory(); history.world = w; setMapName(m.getName());
		Project project = WorkspaceBrowser.getProject(m); tileset_viewer.setProject(project); w.refresh(project);
	}
	public void updateMap(Map m){
		World w = m.getWorld();
		WorldPanel p = world_overlay.getPanel();
		if(p.getWorld() != w) return;
		p.worldSize(w.getWidth()*Tile.tile_size, w.getHeight()*Tile.tile_size);
	}
	public boolean removeMap(Map m){
		World w = m.getWorld();
		WorldPanel p = world_overlay.getPanel();
		if(p.getWorld() != w) return false; current_map = null; lastSave = 0;
		prop.setEnabled(false); p.setWorld(null); p.worldSize(Tile.tile_size, Tile.tile_size);
		history.clearHistory(); history.world = null; map_label.setText("");
		tileset_viewer.setProject(null); return true;
	}
	public void saveMap(Map m){
		if(m == current_map){lastSave = history.redoPos(); updateSaveButtons();}
	}
	
	public void clipboardChanged(){if(!browser_focus) paste.setEnabled(clipboard.hasData());}
	
	private void updateMapName(){map_label.setText(map_name+" ("+map_x+", "+map_y+")");}
	public void setMapName(String name){map_name = name; updateMapName();}
	public void setMapLocation(int x, int y){map_x = x; map_y = y; updateMapName();}

	public void stateChanged(ChangeEvent e) {
		WorldPanel world = world_overlay.getPanel();
		if(e.getSource() == zoom_spinner){
			world.setScale(((Number)zoom_spinner.getValue()).doubleValue()*0.01);
		}
		else if(e.getSource() == layer_spinner){
			int layer = (Integer)layer_spinner.getValue();
			pl1.setEnabled(layer != 0); pl2.setEnabled(layer != 0);
			nl1.setEnabled(layer != MAX_LAYERS); nl2.setEnabled(layer != MAX_LAYERS);
			world.setEditLevel(layer);
			if(!showAll.isSelected()) world.showLevel(world.getEditLevel());
			if(world_overlay.getTool() == SELECT_TOOL) SELECT_TOOL.deselectAll();
		}
		loseBrowserFocus();
		requestFocus();
	}
	
	public void zoomChange(){zoom_spinner.setValue(world_overlay.getPanel().getScale()*100);}
	public void removeFocus(){
		try{zoom_spinner.commitEdit();}catch(Exception e){zoom_spinner.updateUI();}
		try{layer_spinner.commitEdit();}catch(Exception e){layer_spinner.updateUI();}
		loseBrowserFocus();
		requestFocus();
	}

	public WorkspaceBrowser getBrowser(){return browser;}
	
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {
		if(saveall.isEnabled()){
			int i = JOptionPane.showConfirmDialog(this, "Save all maps before closing?");
			if(i == JOptionPane.NO_OPTION){writeWorkspace(); if(media_player != null) media_player.dispose(); dispose(); instance = null;}
			else if(i == JOptionPane.YES_OPTION){
				browser.saveAll(); writeWorkspace(); if(media_player != null) media_player.dispose(); dispose(); instance = null;
			}
		} else{writeWorkspace(); if(media_player != null) media_player.dispose(); dispose(); instance = null;}
	}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	public static MapEditor instance;
	
	public static final int DEF_MEDIA=0, DEF_TILEMAP=1, DEF_MAP=2, N_DEF=3;
	@SuppressWarnings("unchecked")
	private static ArrayList<Resource> deferred[] = new ArrayList[N_DEF];
	static {for(int i=0; i<N_DEF; i++) deferred[i] = new ArrayList<Resource>();}
	public static void deferRead(Resource r, int type){deferred[type].add(r);}
	public static void doDeferredRead(boolean delete){
		for(int i=0; i<N_DEF; i++){
			ArrayList<Resource> ar = deferred[i]; int end = ar.size(); for(int j=0; j<end; j++){
				Resource r = ar.get(j); try{
					r.deferredRead(r.getFile());
				} catch(Exception e){r.editor.browser.removeResource(r,delete);}
			} ar.clear();
		}
	}
	
	private static HashMap<String, Class<? extends Target>> targets = new HashMap<String, Class<? extends Target>>();
	public static void registerTarget(String name, Class<? extends Target> t){targets.put(name, t);}
	public static Object[] getTargetsArray(){return targets.entrySet().toArray();}
	public static Entry<String,Class<? extends Target>> defaultTarget(){return targets.entrySet().iterator().next();}
	
	private static final DocumentBuilderFactory document_factory = DocumentBuilderFactory.newInstance();
	private static final TransformerFactory transformer_factory = TransformerFactory.newInstance();
	private void writeWorkspace(){
		try{
			Document doc = document_factory.newDocumentBuilder().newDocument();
			Element root = doc.createElement("workspace"); doc.appendChild(root);
			Element element = doc.createElement("projectDirectory");
			element.setTextContent(Project.folderChooser.getCurrentDirectory().toString()); root.appendChild(element);
			element = doc.createElement("imageDirectory");
			element.setTextContent(WorkspaceBrowser.imgChooser.getCurrentDirectory().toString()); root.appendChild(element);
			element = doc.createElement("mediaDirectory");
			element.setTextContent(WorkspaceBrowser.sndChooser.getCurrentDirectory().toString()); root.appendChild(element);
			element = doc.createElement("resourceDirectory");
			element.setTextContent(Resource.resourceChooser.getCurrentDirectory().toString()); root.appendChild(element);
			Workspace w = browser.getWorkspace();
			for(int i=0; i<w.getProjectCount(); i++){
				element = doc.createElement("project"); Project p = w.getProject(i);
				Attr attr = doc.createAttribute("directory");
				attr.setValue(p.getFile().toString()); element.setAttributeNode(attr);
				if(current_map != null && WorkspaceBrowser.getProject(current_map) == p){
					Element elem = doc.createElement("map"); elem.setTextContent(Long.toString(current_map.getId()));
					element.appendChild(elem);
				} tileset_viewer.addElements(doc, element, p); root.appendChild(element);
			}
			Transformer transformer = transformer_factory.newTransformer(); transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(new File(".workspace"));
			transformer.transform(new DOMSource(doc), result);
		} catch(Exception e){e.printStackTrace();}
	}
	public static void main(String[] args){
		Resource.register("Image Files", Image.EXT, Image.class);
		Resource.register("Media Files", Media.EXT, Media.class);
		Resource.register("Map Files", Map.EXT, Map.class);
		Resource.register("Tileset Files", Tileset.EXT, Tileset.class);
		Resource.register("Auto Tile Files", AutoTile.EXT, AutoTile.class);
		registerTarget("SWF Version 1.0", SWFTarget.class);
		//TODO: project output file/folder?
		//TODO: finish map loading in haxe, and get export maps working so we can create SWF and see changes.
		//TODO: fix up autotile management to work with the new system. Allow an autotile to be a subset in a tilemap.
		//TODO: register create new file types. (import formats too, for tiled tilemaps?)
		//TODO: register tools to the tool dropdown, which can change when the target changes.
		//TODO: when target changes, script editor has to refresh, as monsters might reference DIFFERENT database entries. Register script quick commands too!
		//TODO: read plugin directory, and install plugins.
		//TODO: new project template
		instance = new MapEditor();
		File f = new File(".workspace");
		if(f.exists()) try{
			Document doc = document_factory.newDocumentBuilder().parse(f);
			Element root = doc.getDocumentElement();
			try{Project.folderChooser.setCurrentDirectory(new File(root.getElementsByTagName("projectDirectory").item(0).getTextContent()));}catch(Exception ex){}
			try{WorkspaceBrowser.imgChooser.setCurrentDirectory(new File(root.getElementsByTagName("imageDirectory").item(0).getTextContent()));}catch(Exception ex){}
			try{WorkspaceBrowser.sndChooser.setCurrentDirectory(new File(root.getElementsByTagName("mediaDirectory").item(0).getTextContent()));}catch(Exception ex){}
			try{Resource.resourceChooser.setCurrentDirectory(new File(root.getElementsByTagName("resourceDirectory").item(0).getTextContent()));}catch(Exception ex){}
			NodeList list = root.getElementsByTagName("project");
			for(int i=0; i<list.getLength(); i++){
				try{
					Element element = (Element)list.item(i);
					Project p = instance.browser.addProject(new File(element.getAttribute("directory")));
					try{p.getMapById(Long.parseLong(element.getElementsByTagName("map").item(0).getTextContent())).edit();}catch(Exception exx){}
					try{p.getTilemapById(Long.parseLong(element.getElementsByTagName("tileset").item(0).getTextContent())).edit();}catch(Exception exx){}
					NodeList n = element.getElementsByTagName("autotile");
					for(int j=0; j<n.getLength(); j++)
						try{p.getTilemapById(Long.parseLong(n.item(j).getTextContent())).edit();}catch(Exception exx){}
				}catch(Exception ex){}
			}			
		} catch(Exception e){e.printStackTrace();}
	}
}
