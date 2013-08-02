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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.export.WorldIO;
import mrpg.world.World;


public class Map extends TypedModifiable {
	private static final long serialVersionUID = 3067630717384565840L;
	public static final String EXT = "map", MAP = "map", TYPE = "m"; private static final short VERSION=1;
	private static final Icon icon = MapEditor.getIcon(MAP);
	private World world; private ImageResource background;
	public Map(File f, MapEditor e){super(f,e);}
	public String getType(){return TYPE;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(browser.edit); menu.add(browser.properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public void setName(String n) throws Exception {
		super.setName(n); if(editor.getWorld() == world) editor.setMapName(getName());
	}
	public boolean isCompatible(Project p){return p.tile_size == WorkspaceBrowser.getProject(this).tile_size && super.isCompatible(p);}
	public World getWorld(){return world;}
	public boolean edit(){editor.setMap(this); return true;}
	private boolean active = false;
	public void remove(boolean delete) throws Exception {
		super.remove(delete); active = editor.removeMap(this);
	}
	public void addToProject(Project p, boolean changeProject) throws Exception {
		super.addToProject(p, changeProject);
		if(changeProject){
			if(background != null){
				try{background = (ImageResource)p.getById(background.getType(), background.getId());}catch(Exception ex){background = null;}
			} world.refresh(p, true);
		}
	}
	public Icon getIcon(){return icon;}
	public void writeInner(DataOutputStream out) throws Exception {
		ImageResource.write(out, background); WorldIO w = new WorldIO(); world.write(w); w.write(out);
	}
	public void readInner(DataInputStream in) throws Exception {
		Project p = WorkspaceBrowser.getProject(this); background = ImageResource.read(in, p);
		if(background != null) world.background = background.getImage(); world = World.read(new WorldIO(p, in));
	}
	public void save() throws Exception {super.save(); editor.saveMap(this);}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_MAP);}
	public void deferredRead(File f) throws Exception{
		super.deferredRead(f); if(active){active = false; edit();}
	}
	
	public static Map createMap(Resource parent, MapEditor e, Project p) throws Exception{
		String dir = parent.getFile().toString();
		File f = new File(dir,"New Map"+"."+EXT);
		Map ret = new Map(f,e); ret.newId(p); ret._setName(null); ret.world = new World(20,15); ret.properties();
		if(!((Properties)ret.properties).updated) throw new Exception();
		ret.addToProject(p, false); return ret;
	}
	public static Map createMap(Resource parent, MapEditor e, Project p, String name, World w) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,name+"."+EXT);
		Map ret = new Map(f, e); ret.newId(p); ret.world = w;
		parent.add(ret); ret.save(); ret.addToProject(p, false); return ret;
	}
	private static class Properties extends TypedResource.Properties {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final int MIN_SIZE = 10, MAX_SIZE = 500;
		private JSpinner width, height; private JCheckBox x_wrap, y_wrap; private JLabel image_thumb; private ImageResource background;
		public Properties(Map m){super(m, "Map Properties");}
		public void addControls(JPanel settings){
			Map map = (Map)resource;
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Size"));
			width = new JSpinner(new SpinnerNumberModel(map.world.getWidth(), MIN_SIZE, MAX_SIZE, 1));
			JTextField f = ((JSpinner.DefaultEditor)width.getEditor()).getTextField();
			f.setActionCommand(MapEditor.OK); f.addActionListener(this); inner.add(width);
			inner.add(new JLabel("x"));
			height = new JSpinner(new SpinnerNumberModel(map.world.getHeight(), MIN_SIZE, MAX_SIZE, 1));
			f = ((JSpinner.DefaultEditor)height.getEditor()).getTextField();
			f.setActionCommand(MapEditor.OK); f.addActionListener(this); inner.add(height);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Wrapping"));
			x_wrap = new JCheckBox("Horizontal Wrap"); inner.add(x_wrap);
			y_wrap = new JCheckBox("Vertical Wrap"); inner.add(y_wrap);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Background"));
			image_thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(image_thumb); pane.setPreferredSize(ImageResource.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); JPanel inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner2.add(set);
			JButton clear = new JButton("Clear"); clear.setActionCommand(MapEditor.CLEAR); clear.addActionListener(this); inner2.add(clear);
			inner.add(inner2);
			//inner.add(new JCheckBox("Horizontal Scroll"));
			//inner.add(new JCheckBox("Vertical Scroll"));
			settings.add(inner);
		}
		public void updateControls(){
			Map map = (Map)resource;
			width.setValue(map.world.getWidth()); height.setValue(map.world.getHeight());
			x_wrap.setSelected(map.world.wrapX); y_wrap.setSelected(map.world.wrapY);
			background = map.background;
			if(background == null) image_thumb.setIcon(new ImageIcon()); else image_thumb.setIcon(new ImageIcon(background.getImage()));
		}
		public void acceptControls(){
			Map map = (Map)resource;
			int w = (Integer)width.getValue(), h = (Integer)height.getValue(); boolean u = false;
			if(w != map.world.getWidth() || h != map.world.getHeight()){map.world.resize(w, h); u = true;}
			u |= (map.world.wrapX != x_wrap.isSelected()) || (map.world.wrapY != y_wrap.isSelected());
			map.world.wrapX = x_wrap.isSelected(); map.world.wrapY = y_wrap.isSelected();
			if(u) map.world.updateNeighbors();
			map.background = background; if(background != null) map.world.background = background.getImage(); else map.world.background = null;
			map.editor.updateMap(map);
		}
		public boolean saveOnEdit(){return true;}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.SET){
				Project p = resource.getProject();
				if(p == null){JOptionPane.showMessageDialog(this, "Map is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageResource im = ImageResource.choose(p, background);
				if(im != null){background = im; image_thumb.setIcon(new ImageIcon(background.getImage()));}
			} else if(command == MapEditor.CLEAR){
				background = null; image_thumb.setIcon(new ImageIcon());
			} else super.actionPerformed(e);
		}
	}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Map Files", Map.EXT, Map.TYPE, Map.class);
		Folder.new_options.addMenu("Map", Map.MAP).
			addItem("Map", MAP, KeyEvent.VK_M, ActionEvent.CTRL_MASK, new CreateMapAction());
	}
	private static class CreateMapAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addMap();
		}
	}
}
