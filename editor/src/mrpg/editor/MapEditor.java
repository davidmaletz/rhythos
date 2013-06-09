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
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
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
import mrpg.editor.resource.Folder;
import mrpg.editor.resource.Image;
import mrpg.editor.resource.Map;
import mrpg.editor.resource.Media;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.editor.resource.Script;
import mrpg.editor.resource.Tileset;
import mrpg.editor.resource.Workspace;
import mrpg.editor.tools.EraserTool;
import mrpg.editor.tools.FillTool;
import mrpg.editor.tools.LineTool;
import mrpg.editor.tools.PencilTool;
import mrpg.editor.tools.RectTool;
import mrpg.editor.tools.SelectTool;
import mrpg.editor.tools.ZoomTool;
import mrpg.script.ScriptEditor;
import mrpg.world.World;

public class MapEditor extends JFrame implements Runnable, WindowListener, ActionListener, ChangeListener, TreeSelectionListener, History.Listener, SelectTool.Listener, ZoomTool.Listener, Clipboard.Listener {
	
	private static final long serialVersionUID = 7934438411041874443L;
	
	public static final String NEW = "new", OPEN = "open", SAVE = "save", SAVE_ALL = "save_all", IMPORT = "import",
		EXPORT = "export", TEST = "test", BUILD = "build", SEARCH = "search", HELP = "help", ABOUT = "about";
	public static final String CUT = "cut", COPY = "copy", PASTE = "paste", DELETE = "delete", REFRESH = "refresh", REVERT = "revert", REMOVE = "remove", SEL_ALL = "sel_all",
		DSEL_ALL = "dsel_all", SHOW_ALL = "show_all", SHOW_GRID = "grid", NEXT_LAYER = "next_l", PREV_LAYER = "prev_l";
	public static final String RENAME = "rename";
	public static final String UNDO = "undo", REDO = "redo", PROPERTIES = "properties", LAYER = "layer";
	public static final String OK = "ok", CANCEL = "cancel", SET = "set", CLEAR = "clear", M_PLAYER = "media_player"; 
	public static final int MAX_LAYERS = 20;
	public final ButtonGroup tools = new ButtonGroup(); private WorldOverlay world_overlay;
	
	private static ToolItem SELECT_TOOL, ZOOM_TOOL;
	
	private JLabel map_label; private JSpinner zoom_spinner, layer_spinner; private WorkspaceBrowser browser;
	private String map_name = ""; private int map_x = 0, map_y = 0; public boolean browser_focus = false;
	
	private static ToggleableMenuItem undo, redo, cut, copy, paste, delete, dsel, prev_layer,
		next_layer, prop, save, save_map, saveall, refresh, revert;
	private static CheckboxMenuItem showGrid, showAll;
	private static final ArrayList<ToggleableMenuItem> buttons = new ArrayList<ToggleableMenuItem>();
	private Map current_map;
	
	private final History history = new History(); private final Clipboard clipboard = new Clipboard();
	private TilesetViewer tileset_viewer; private MediaPlayer media_player = null;

	public MapEditor(){
		super("Rhythos! Game Builder");
		InputMap im = (InputMap)UIManager.getDefaults().get("Button.focusInputMap");
        Object pressedAction = im.get(KeyStroke.getKeyStroke("pressed SPACE"));
        Object releasedAction = im.get(KeyStroke.getKeyStroke("released SPACE"));
        im.put(KeyStroke.getKeyStroke("pressed ENTER"), pressedAction);
        im.put(KeyStroke.getKeyStroke("released ENTER"), releasedAction);
        
		history.listener = this; clipboard.listener = this;
		browser = new WorkspaceBrowser(this); browser.addTreeSelectionListener(this);
	}
	
	public void init(){
		WorldPanel w = new WorldPanel(TilesetViewer.TILE_SIZE,TilesetViewer.TILE_SIZE);
		setJMenuBar(menu_bar.getMenuBar());
		Container c = getContentPane(); c.setLayout(new BorderLayout());
		c.add(toolbar.getToolbar(), BorderLayout.NORTH);
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
		SelectTool select = (SelectTool)SELECT_TOOL.getTool(); select.listener = this;
		ZoomTool zoom = (ZoomTool)ZOOM_TOOL.getTool(); zoom.listener = this;
		tileset.add(tile_toolbar.getToolbar(), BorderLayout.NORTH);
		tileset.add(new JScrollPane(tileset_viewer), BorderLayout.CENTER);
		pane.add(tileset);
		pane.add(new JScrollPane(browser));
		pane.setDividerLocation(TilesetViewer.TILE_SIZE*8);
		JScrollPane sp = new JScrollPane(w); Insets i = sp.getInsets(); sp.getViewport().setBackground(new Color(30,30,30));
		int sw = sp.getVerticalScrollBar().getPreferredSize().width+i.left+i.right;
		i = pane.getInsets();
		frame.setDividerLocation(TilesetViewer.TILE_SIZE*8+1+sw+i.left+i.right+1);
		frame.add(pane);
		sp.setMinimumSize(new Dimension(TilesetViewer.TILE_SIZE+sw,TilesetViewer.TILE_SIZE+sp.getHorizontalScrollBar().getPreferredSize().height));
		JPanel edit = new JPanel(new BorderLayout());
		edit.add(sp, BorderLayout.CENTER);
		JPanel options = new JPanel(new BorderLayout());
		JPanel inner = new JPanel();
		JLabel l = new JLabel(getIcon(zoom.getIcon())); l.setToolTipText("Map Scale"); inner.add(l);
		zoom_spinner = new JSpinner(new SpinnerNumberModel(100.0, 6.25, 800.0, 1.0));
		zoom_spinner.addChangeListener(this); inner.add(zoom_spinner);
		inner.add(new JLabel("%"));
		options.add(inner, BorderLayout.WEST);
		map_label = new JLabel("", getIcon(Map.MAP), JLabel.CENTER);
		options.add(map_label, BorderLayout.CENTER);
		inner = new JPanel();
		l = new JLabel(getIcon(LAYER)); l.setToolTipText("Map Editing Layer"); inner.add(l);
		layer_spinner = new JSpinner(new SpinnerNumberModel(0, 0, MAX_LAYERS, 1));
		layer_spinner.addChangeListener(this); inner.add(layer_spinner);
		options.add(inner, BorderLayout.EAST);
		edit.add(options, BorderLayout.SOUTH);
		frame.add(edit);
		
		world_overlay.setTool(select);
		
		deselect(); historyChanged(); clipboardChanged(); valueChanged(null);
		
		w.startAnim();
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); setSize(800,600); setVisible(true);
	}
	
	public WorldOverlay getOverlay(){return world_overlay;}
	public History getHistory(){return history;}
	private static Hashtable<String, ImageIcon> icons = new Hashtable<String, ImageIcon>();
	public static ImageIcon getIcon(String s) {
		if(s == null) return null;
		if(icons.containsKey(s)) return icons.get(s);
		try{
			ImageIcon i = new ImageIcon(MapEditor.class.getResource("/icons/"+s+".png"));
			icons.put(s, i); return i;
		}catch(Exception e){return null;}
	}
	public static JMenuItem createMenuItemIcon(String text, String icon, ActionListener l){
		JMenuItem i = new JMenuItem(text, getIcon(icon)); i.setActionCommand(icon); i.addActionListener(l); return i;
	}
	public static JMenuItem createMenuItemIcon(String text, String icon, int keyCode, int modifiers, ActionListener l){
		JMenuItem i = createMenuItemIcon(text, icon, l);
		if(keyCode != 0) i.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers)); return i;
	}
	public static JMenuItem createMenuItemIcon(String text, String icon, String command, ActionListener l){
		JMenuItem i = new JMenuItem(text, getIcon(icon)); i.setActionCommand(command); i.addActionListener(l); return i;
	}
	public static JMenuItem createMenuItem(String text, String command, ActionListener l){
		JMenuItem i = new JMenuItem(text); i.setActionCommand(command); i.addActionListener(l); return i;
	}
	public static JMenuItem createMenuItem(String text, String command, int keyCode, int modifiers, ActionListener l){
		JMenuItem i = createMenuItem(text, command, l);
		if(keyCode != 0) i.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers)); return i;
	}
	public static JCheckBoxMenuItem createCheckboxMenuItem(String text, String command, ActionListener l){
		JCheckBoxMenuItem i = new JCheckBoxMenuItem(text); i.setActionCommand(command); i.addActionListener(l); return i;
	}
	public static JCheckBoxMenuItem createCheckboxMenuItem(String text, String command, int keyCode, int modifiers, ActionListener l){
		JCheckBoxMenuItem i = createCheckboxMenuItem(text, command, l);
		if(keyCode != 0) i.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers)); return i;
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
	public static JCheckBox createToolbarCheckBox(String icon, String tooltip, ActionListener l){
		JCheckBox b = new JCheckBox(getIcon(icon)); b.setMargin(new Insets(0,0,0,0)); b.setActionCommand(icon);
		b.addActionListener(l); b.setToolTipText(tooltip); return b;
	}
	
	public static final Menu menu_bar = new Menu(), toolbar = new Menu(), tile_toolbar = new Menu(), tile_toolbar_right = new Menu();

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand(); SelectTool select = (SelectTool)SELECT_TOOL.getTool();
		if(command == SAVE){
			if(current_map != null && current_map.isModified()) try{current_map.save(); updateSaveButtons();}catch(Exception ex){}
		} else if(command == CUT){if(!browser_focus){select.copy(clipboard); select.deleteSelection();}
		} else if(command == COPY){if(browser_focus){browser.copy(); paste.setEnabled(browser.hasClipboardData());} else select.copy(clipboard);
		} else if(command == PASTE){if(browser_focus) try{browser.paste();}catch(Exception ex){}
			else {
				world_overlay.setTool(select);
				tools.setSelected(((JToggleButton)SELECT_TOOL.getToolbarItem()).getModel(), true);
				select.paste(clipboard);
			}
		} else if(command == DELETE){if(browser_focus) browser.deleteSelection(); else select.deleteSelection();
		} else if(command == SEL_ALL){
			if(browser_focus){browser.addSelectionInterval(0, browser.getRowCount()-1);}
			else {
				if(world_overlay.getPanel().getWorld() != null){
					world_overlay.setTool(select);
					tools.setSelected(((JToggleButton)SELECT_TOOL.getToolbarItem()).getModel(), true);
					select.selectAll();
				}
			}
		} else if(command == DSEL_ALL){if(browser_focus) browser.clearSelection(); else select.deselectAll();
		} else if(command == SHOW_ALL){
				WorldPanel world = world_overlay.getPanel();
				world.showLevel((showAll.isSelected())?-1:world.getEditLevel());
		} else if(command == SHOW_GRID){world_overlay.getPanel().setShowGrid(((AbstractButton)e.getSource()).isSelected());
		} else if(command == PREV_LAYER){layer_spinner.setValue(Math.max(0, (Integer)layer_spinner.getValue()-1));
		} else if(command == NEXT_LAYER){layer_spinner.setValue(Math.min(MAX_LAYERS, (Integer)layer_spinner.getValue()+1));
		} else if(command == UNDO){
			if(world_overlay.getTool() == select) select.activate();
			history.undo(); world_overlay.getPanel().repaint();
		} else if(command == REDO){
			if(world_overlay.getTool() == select) select.activate();
			history.redo(); world_overlay.getPanel().repaint();
		} else if(command == PROPERTIES){if(current_map != null) current_map.properties();
		} else if(command == Folder.ADD_FOLDER){
			browser.addFolder();
		} else if(command == M_PLAYER){
			if(media_player == null) media_player = new MediaPlayer(getBrowser());
			TreePath p = browser.getSelectionPath(); if(p != null) media_player.setProject(WorkspaceBrowser.getProject(p));
			media_player.setVisible(true);
		}
		if(!browser_focus){
			if(world_overlay.getTool() != select) deselect();
			removeFocus();
		}
	}
	
	private int lastSave;
	public void historyChanged(){
		boolean hasUndo = history.hasUndo(); undo.setEnabled(hasUndo);
		boolean hasRedo = history.hasRedo(); redo.setEnabled(hasRedo);
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
		browser_focus = false; if(((SelectTool)SELECT_TOOL.getTool()).hasSelection()) select(); else deselect();
		paste.setEnabled(clipboard.hasData());
	}
	private void updateSaveRevert(){
		save.setEnabled(browser.selectionModified());
		save_map.setEnabled(current_map != null && current_map.isModified());
		revert.setEnabled(save.isEnabled());
	}
	public void updateSaveButtons(){
		updateSaveRevert(); saveall.setEnabled(browser.anyModified());
	}
	public void valueChanged(TreeSelectionEvent e) {
		gainBrowserFocus();
		int ct = browser.getSelectionCount();
		for(ToggleableMenuItem b : buttons){
			String command = b.getMenuItem(true).getActionCommand();
			if(command == PROPERTIES) b.setEnabled(ct == 1 && WorkspaceBrowser.getResource(browser.getSelectionPath()).hasProperties());
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
		current_map = m; lastSave = (m.isModified())?-1:0;
		prop.setEnabled(true); Project project = WorkspaceBrowser.getProject(m);
		p.setWorld(w, project.tile_size); p.worldSize(w.getWidth()*project.tile_size, w.getHeight()*project.tile_size);
		history.clearHistory(); history.world = w; setMapName(m.getName());
		tileset_viewer.setProject(project); w.refresh(project);
	}
	public void updateMap(Map m){
		World w = m.getWorld();
		WorldPanel p = world_overlay.getPanel();
		if(p.getWorld() != w) return;
		p.worldSize(w.getWidth()*p.tile_size, w.getHeight()*p.tile_size);
	}
	public boolean removeMap(Map m){
		World w = m.getWorld();
		WorldPanel p = world_overlay.getPanel();
		if(p.getWorld() != w) return false; current_map = null; lastSave = (m.isModified())?-1:0;
		prop.setEnabled(false); p.setWorld(null, 0); p.worldSize(p.tile_size, p.tile_size);
		history.clearHistory(); history.world = null; map_label.setText("");
		tileset_viewer.setProject(null); return true;
	}
	public void saveMap(Map m){if(m == current_map){lastSave = history.redoPos();}}
	
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
			prev_layer.setEnabled(layer != 0);
			next_layer.setEnabled(layer != MAX_LAYERS);
			world.setEditLevel(layer);
			if(!showAll.isSelected()) world.showLevel(world.getEditLevel());
			if(world_overlay.getTool() == SELECT_TOOL) ((SelectTool)SELECT_TOOL.getTool()).deselectAll();
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
	public void dispose(){
		if(media_player != null) media_player.dispose(); 
		ScriptEditor.destroy();
		super.dispose();
	}
	public void windowClosing(WindowEvent e) {
		if(saveall.isEnabled()){
			int i = JOptionPane.showConfirmDialog(this, "Save all resources before closing?");
			if(i == JOptionPane.NO_OPTION){writeWorkspace(); dispose(); instance = null;}
			else if(i == JOptionPane.YES_OPTION){
				browser.saveAll(); writeWorkspace(); dispose(); instance = null;
			}
		} else{writeWorkspace(); dispose(); instance = null;}
	}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	public static MapEditor instance;
	
	public static final int DEF_MEDIA=0, DEF_PROJECT=1, DEF_TILEMAP=2, DEF_MAP=3, N_DEF=4;
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
	
	private static final DocumentBuilderFactory document_factory = DocumentBuilderFactory.newInstance();
	private static final TransformerFactory transformer_factory = TransformerFactory.newInstance();
	public static int getSelectedIndex(ButtonGroup g) throws Exception {
		Enumeration<AbstractButton> e = g.getElements(); int i = 0;
		while(e.hasMoreElements()){if(g.isSelected(e.nextElement().getModel())) return i; i++;} throw new Exception();
	}
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
			if(showGrid.isSelected()) root.appendChild(doc.createElement("showGrid"));
			if(showAll.isSelected()) root.appendChild(doc.createElement("showAllLayers"));
			element = doc.createElement("layer");
			element.setTextContent(layer_spinner.getValue().toString()); root.appendChild(element);
			element = doc.createElement("zoom");
			element.setTextContent(zoom_spinner.getValue().toString()); root.appendChild(element);
			try{element = doc.createElement("tool");
			element.setTextContent(Integer.toString(getSelectedIndex(tools))); root.appendChild(element);}catch(Exception ex){}
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
	public static <T> T getByIndex(Enumeration<T> e, int i) throws Exception {
		while(i > 0){e.nextElement(); i--;} return e.nextElement();
	}
	public void run(){
		File f = new File(".workspace");
		if(f.exists()) try{
			Document doc = document_factory.newDocumentBuilder().parse(f);
			Element root = doc.getDocumentElement();
			try{Project.folderChooser.setCurrentDirectory(new File(root.getElementsByTagName("projectDirectory").item(0).getTextContent()));}catch(Exception ex){}
			try{WorkspaceBrowser.imgChooser.setCurrentDirectory(new File(root.getElementsByTagName("imageDirectory").item(0).getTextContent()));}catch(Exception ex){}
			try{WorkspaceBrowser.sndChooser.setCurrentDirectory(new File(root.getElementsByTagName("mediaDirectory").item(0).getTextContent()));}catch(Exception ex){}
			try{Resource.resourceChooser.setCurrentDirectory(new File(root.getElementsByTagName("resourceDirectory").item(0).getTextContent()));}catch(Exception ex){}
			if(root.getElementsByTagName("showGrid").getLength() > 0) showGrid.getMenuItem(true).doClick();
			if(root.getElementsByTagName("showAllLayers").getLength() > 0) showAll.getMenuItem(true).doClick();
			try{layer_spinner.setValue(Integer.parseInt(root.getElementsByTagName("layer").item(0).getTextContent()));}catch(Exception ex){}
			try{zoom_spinner.setValue(Double.parseDouble(root.getElementsByTagName("zoom").item(0).getTextContent()));}catch(Exception ex){}
			try{getByIndex(tools.getElements(), Integer.parseInt(root.getElementsByTagName("tool").item(0).getTextContent())).doClick();}catch(Exception ex){}
			NodeList list = root.getElementsByTagName("project");
			for(int i=0; i<list.getLength(); i++){
				try{
					Element element = (Element)list.item(i);
					Project p = browser.addProject(new File(element.getAttribute("directory")));
					try{p.getMapById(Long.parseLong(element.getElementsByTagName("map").item(0).getTextContent())).edit();}catch(Exception exx){}
					try{p.getTilemapById(Long.parseLong(element.getElementsByTagName("tileset").item(0).getTextContent())).edit();}catch(Exception exx){}
					NodeList n = element.getElementsByTagName("autotile");
					for(int j=0; j<n.getLength(); j++)
						try{p.getTilemapById(Long.parseLong(n.item(j).getTextContent())).edit();}catch(Exception exx){}
				}catch(Exception ex){}
			}			
		} catch(Exception e){e.printStackTrace();}
	}
	private static void setupMenuBar(){
		Menu menu = menu_bar.addMenu("File", null);
		menu.addItem("Create New Project", Project.PROJECT, KeyEvent.VK_N, ActionEvent.CTRL_MASK, null);
		menu.addItem("Open Existing Project", OPEN, KeyEvent.VK_O, ActionEvent.CTRL_MASK, null);
		menu.addItem(Folder.new_options); menu.addSeparator();
		save_map = new ToggleableMenuItem("Save Current Map", SAVE, KeyEvent.VK_S, ActionEvent.CTRL_MASK, actionListener, false);
		save = new ToggleableMenuItem("Save Selected", SAVE, null, false);
		saveall = new ToggleableMenuItem("Save All", SAVE_ALL, KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, null, false);
		revert = new ToggleableMenuItem("Revert", REVERT, null, false);
		menu.addItem(save_map); menu.addItem(save); menu.addItem(saveall); menu.addItem(revert); menu.addSeparator();
		menu.addItem(Folder.import_options); menu.addSeparator();
		refresh = new ToggleableMenuItem("Refresh", REFRESH, KeyEvent.VK_F5, 0, null, false); menu.addItem(refresh);
		ToggleableMenuItem item = new ToggleableMenuItem("Rename", RENAME, null);
		buttons.add(item); menu.addItem(item);
		item = new ToggleableMenuItem("Properties", PROPERTIES, KeyEvent.VK_P, ActionEvent.CTRL_MASK, null);
		buttons.add(item); menu.addItem(item);
		menu = menu_bar.addMenu("Edit", null);
		undo = new ToggleableMenuItem("Undo", UNDO, KeyEvent.VK_Z, ActionEvent.CTRL_MASK, actionListener);
		redo = new ToggleableMenuItem("Redo", REDO, KeyEvent.VK_Y, ActionEvent.CTRL_MASK, actionListener);
		menu.addItem(undo); menu.addItem(redo); menu.addSeparator();
		cut = new ToggleableMenuItem("Cut", CUT, KeyEvent.VK_X, ActionEvent.CTRL_MASK, actionListener);
		copy = new ToggleableMenuItem("Copy", COPY, KeyEvent.VK_C, ActionEvent.CTRL_MASK, actionListener);
		paste = new ToggleableMenuItem("Paste", PASTE, KeyEvent.VK_V, ActionEvent.CTRL_MASK, actionListener);
		delete = new ToggleableMenuItem("Delete", DELETE, KeyEvent.VK_DELETE, 0, actionListener);
		menu.addItem(cut); menu.addItem(copy); menu.addItem(paste); menu.addItem(delete); menu.addSeparator();
		dsel = new ToggleableMenuItem("Deselect All", DSEL_ALL, KeyEvent.VK_D, ActionEvent.CTRL_MASK, actionListener);
		menu.addItem("Select All", SEL_ALL, KeyEvent.VK_A, ActionEvent.CTRL_MASK, actionListener); menu.addItem(dsel);
		menu = menu_bar.addMenu("View", null);
		showGrid = new CheckboxMenuItem("Show Grid", SHOW_GRID, KeyEvent.VK_G, ActionEvent.CTRL_MASK, actionListener);
		showAll = new CheckboxMenuItem("Show All Layers", SHOW_ALL, KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, actionListener);
		menu.addSeparator();
		prev_layer = new ToggleableMenuItem("Previous Layer", PREV_LAYER, KeyEvent.VK_L, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, actionListener, false);
		next_layer = new ToggleableMenuItem("Next Layer", NEXT_LAYER, KeyEvent.VK_L, ActionEvent.CTRL_MASK, actionListener);
		menu.addItem(prev_layer); menu.addItem(next_layer);
		menu = menu_bar.addMenu("Tools", null);
		item = new ToggleableMenuItem("Test Game", TEST, KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK, null);
		buttons.add(item); menu.addItem(item);
		item = new ToggleableMenuItem("Build Game", BUILD, KeyEvent.VK_ENTER, ActionEvent.ALT_MASK, null);
		buttons.add(item); menu.addItem(item); menu.addSeparator();
		menu.addItem("Media Player", M_PLAYER, KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, actionListener);
		menu.addItem("Search", SEARCH, KeyEvent.VK_F, ActionEvent.CTRL_MASK, actionListener);
		menu = menu_bar.addMenu("Help", null);
		menu.addItem("Help Contents", HELP, KeyEvent.VK_F1, 0, actionListener);
		menu.addItem("About", ABOUT, actionListener);
	}
	public static void setupToolbar(){
		Menu file = (Menu)menu_bar.children.get(0);
		toolbar.addItem(file.children.get(0)); //Create New Project
		toolbar.addItem(file.children.get(1)); //Open Existing Project
		toolbar.addSeparator();
		toolbar.addItem(save_map);
		toolbar.addItem(buttons.get(0)); //Rename
		toolbar.addSeparator();
		toolbar.addItem(prev_layer);
		toolbar.addItem(next_layer);
		toolbar.addSeparator();
		toolbar.addItem(buttons.get(2)); //Test Game
		toolbar.addItem(buttons.get(3)); //Build Game
		toolbar.addSeparator();
		Menu tools = (Menu)menu_bar.children.get(3);
		toolbar.addItem(tools.children.get(3)); //Search
		toolbar.addSeparator();
		Menu help = (Menu)menu_bar.children.get(4);
		toolbar.addItem(help.children.get(0)); //Help Contents
	}
	public static void setupTileToolbar(){
		tile_toolbar.addItem(undo);
		tile_toolbar.addItem(redo);
		tile_toolbar.addSeparator();
		SELECT_TOOL = new ToolItem(SelectTool.class); tile_toolbar.addItem(SELECT_TOOL);
		ZOOM_TOOL = new ToolItem(ZoomTool.class); tile_toolbar.addItem(ZOOM_TOOL);
		tile_toolbar_right.addSeparator();
		prop = new ToggleableMenuItem("Map properties", PROPERTIES, actionListener, false);
		tile_toolbar_right.addItem(prop);
	}
	public static void main(String[] args){
		ScriptEditor.init();
		setupMenuBar(); setupToolbar(); setupTileToolbar();
		Resource.register(); Folder.register(); Image.register(); Media.register();
		Map.register(); Tileset.register(); AutoTile.register(); Script.register();
		tile_toolbar.addItem(new ToolItem(PencilTool.class));
		tile_toolbar.addItem(new ToolItem(EraserTool.class));
		tile_toolbar.addItem(new ToolItem(LineTool.class));
		tile_toolbar.addItem(new ToolItem(RectTool.class));
		tile_toolbar.addItem(new ToolItem(FillTool.class));
		tile_toolbar.children.addAll(tile_toolbar_right.children);
		//TODO: player twitch when hitting two arrow keys rapidly?
		//TODO: drag and select region for tilemaps and autotiles - autotiles may be a subset of the tilemap.
		//TODO: If map is missing tileset, load all Tile.empty for that tileset, don't throw exception!
		//TODO: GUI UI Form Builder
		//TODO: fix up autotile management to work with the new system. Allow an autotile to be a subset in a tilemap.
		//TODO: when target changes, script editor has to refresh, as monsters might reference DIFFERENT database entries. Register script quick commands too!
		//TODO: read plugin directory, and install plugins.
		//TODO: Project "get by id" has to allow new types via plugins (hash map from long to table?)
		//TODO: enable/disable resources in a project based on target.
		//TODO: built in error checking and exception handling in the client - so we know what went wrong when something goes wrong.
		//TODO: Image Editing based on the map editor?
		instance = new MapEditor(); instance.init();
		SwingUtilities.invokeLater(instance);
	}
	
	private static class Listener implements ActionListener {
		public void actionPerformed(ActionEvent e){MapEditor.instance.actionPerformed(e);}
	} public static final Listener actionListener = new Listener();
}
