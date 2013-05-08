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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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
import javax.swing.tree.TreePath;

import mrpg.editor.ImageChooser;
import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.export.WorldIO;
import mrpg.world.World;


public class Map extends Resource {
	private static final long serialVersionUID = 3067630717384565840L;
	public static final String EXT = "map";
	private static final Icon icon = MapEditor.getIcon(MapEditor.MAP);
	private World world; private Properties properties; private Image background; private long id; private boolean modified = false;
	public Map(File f, MapEditor e){super(f,e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(browser.edit); menu.add(browser.properties); menu.addSeparator();
		browser.save.setEnabled(isModified()); menu.add(browser.save);
		browser.revert.setEnabled(isModified()); menu.add(browser.revert); menu.addSeparator();
	}
	public long getId(){return id;}
	public void setName(String n) throws Exception {
		super.setName(n); if(editor.getWorld() == world) editor.setMapName(getName());
	}
	public World getWorld(){return world;}
	public boolean edit(){editor.setMap(this); return true;}
	public void properties(){if(properties == null) properties = new Properties(this); properties.setVisible(true);}
	private boolean active = false;
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeMapId(this, id); super.remove(delete); active = editor.removeMap(this);
	}
	public boolean hasProperties(){return true;}
	public boolean isModified(){return modified;}
	public void setModified(boolean m){if(m != modified){modified = m; updateName();}}
	public String toString(){if(modified) return "*"+super.toString(); else return super.toString();}
	public Icon getIcon(){return icon;}
	public void refresh() throws Exception {if(!modified) super.refresh();}
	public void revert() throws  Exception {super.refresh();}
	public void save() throws Exception {
		File f = getFile(); DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		out.writeLong(id); if(background == null) out.writeByte(0); else {out.writeByte(1); out.writeLong(background.getId());}
		WorldIO w = new WorldIO(); world.write(w); w.write(out); out.flush(); out.close(); setModified(false); editor.saveMap(this);
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_MAP);}
	public void deferredRead(File f) throws Exception{
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f))); Project p = WorkspaceBrowser.getProject(this);
		id = in.readLong(); if(in.readByte() == 1) try{background = p.getImageById(in.readLong());}catch(Exception e){background = null;} else background = null;
		world = World.read(new WorldIO(p, in)); in.close(); if(background != null) world.background = background.getImage();
		long i = p.setMapId(this, id); if(i != id){id = i; save();}
		if(active){active = false; edit();} setModified(false);
	}
	
	public static Map createMap(File f, MapEditor e, Project p) throws Exception{
		Map ret = new Map(f,e); ret.world = new World(20,15); ret.id = p.newMapId(); ret.properties();
		if(!ret.properties.updated) throw new Exception();
		p.setMapId(ret, ret.id); return ret;
	}
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final int MIN_SIZE = 10, MAX_SIZE = 500;
		public boolean updated;
		private final Map map; private final JTextField name; private final JSpinner width, height;
		private final JCheckBox x_wrap, y_wrap; private final JLabel image_thumb; private Image background;
		public Properties(Map m){
			super(JOptionPane.getFrameForComponent(m.editor), "Map Properties", true); map = m;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField(map.getName(), 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Size"));
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
			JScrollPane pane = new JScrollPane(image_thumb); pane.setPreferredSize(Image.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); JPanel inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner2.add(set);
			JButton clear = new JButton("Clear"); clear.setActionCommand(MapEditor.CLEAR); clear.addActionListener(this); inner2.add(clear);
			inner.add(inner2);
			//inner.add(new JCheckBox("Horizontal Scroll"));
			//inner.add(new JCheckBox("Vertical Scroll"));
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean b){
			if(b == true){
				updated = false;
				name.setText(map.getName()); name.requestFocus(); name.selectAll();
				width.setValue(map.world.getWidth()); height.setValue(map.world.getHeight());
				x_wrap.setSelected(map.world.wrapX); y_wrap.setSelected(map.world.wrapY);
				background = map.background;
				if(background == null) image_thumb.setIcon(new ImageIcon()); else image_thumb.setIcon(new ImageIcon(background.getImage()));
			}
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				int w = (Integer)width.getValue(), h = (Integer)height.getValue(); boolean u = false;
				if(w != map.world.getWidth() || h != map.world.getHeight()){map.world.resize(w, h); u = true;}
				u |= (map.world.wrapX != x_wrap.isSelected()) || (map.world.wrapY != y_wrap.isSelected());
				map.world.wrapX = x_wrap.isSelected(); map.world.wrapY = y_wrap.isSelected();
				if(u) map.world.updateNeighbors();
				map.background = background; if(background != null) map.world.background = background.getImage(); else map.world.background = null;
				map.editor.updateMap(map);
				try{
					map.setName(name.getText());
				}catch(Exception ex){}
				try{map.save(); updated = true;}catch(Exception ex){}
				setVisible(false);
			} else if(command == MapEditor.SET){
				Project p = WorkspaceBrowser.getProject(map);
				if(p == null){
					WorkspaceBrowser b = map.editor.getBrowser();
					TreePath path; if(b.isSelectionEmpty() && b.getRowCount() == 0){path = null;}
					else if(b.isSelectionEmpty()) path = b.getPathForRow(0);
					else path = b.getSelectionPath();
					if(path != null && path.getPathCount() > 1) p = (Project)path.getPathComponent(1);
				}
				if(p == null){JOptionPane.showMessageDialog(this, "Map is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageChooser c = new ImageChooser(p, background);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){background = im; image_thumb.setIcon(new ImageIcon(background.getImage()));}
			} else if(command == MapEditor.CLEAR){
				background = null; image_thumb.setIcon(new ImageIcon());
			}
			else setVisible(false);
		}
	}
}
