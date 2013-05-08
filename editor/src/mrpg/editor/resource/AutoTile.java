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
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import mrpg.editor.AutoTileEditor;
import mrpg.editor.ImageChooser;
import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.world.AutoTilemap;
import mrpg.world.Tile;
import mrpg.world.Tilemap;
import mrpg.world.WallTilemap;


public class AutoTile extends TileResource implements ActionListener {
	private static final long serialVersionUID = 3981925226292874481L;
	private static final Icon icon = MapEditor.getIcon(WorkspaceBrowser.TILESET);
	public static final String EXT = "atm";
	public static final String ADD_AUTOTILE="set-tileset";
	private Tilemap autotile; private final Properties properties; private Image image; private long id;
	private JMenuItem add_autotile = MapEditor.createMenuItemIcon("Toggle Autotile", ADD_AUTOTILE, this);
	public AutoTile(File f, MapEditor e){super(f, e); properties = new Properties(this);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(add_autotile); menu.add(browser.properties); menu.addSeparator();
	}
	public long getId(){return id;}
	public boolean edit(){editor.getTilesetViewer().toggleAutoTile(autotile, WorkspaceBrowser.getProject(this)); return true;}
	public void properties(){properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public Icon getIcon(){return icon;}
	public Tilemap getTilemap(){return autotile;}
	public void actionPerformed(ActionEvent e) {edit();}
	
	private boolean active = false;
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeTilemapId(this, id); super.remove(delete);
		active = editor.getTilesetViewer().removeAutoTile(autotile);
	}
	public void save() throws Exception {
		File f = getFile(); DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		out.writeLong(id); out.writeLong(image.getId()); autotile.write(out); out.flush(); out.close();
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public void deferredRead(File f) throws Exception {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		id = in.readLong(); Project p = WorkspaceBrowser.getProject(this); image = p.getImageById(in.readLong());
		if(image.getImage().getHeight() == Tile.tile_size*2)
			autotile = new WallTilemap(in, image.getImage(), getId());
		else autotile = new AutoTilemap(in, image.getImage(), getId());
		in.close(); long i = p.setTilemapId(this, id); if(i != id){id = i; save();}
		if(active){active = false; editor.getTilesetViewer().addAutoTile(autotile, p);}
	}
	public static AutoTile createAutoTile(File f, MapEditor e, Project p) throws Exception {
		AutoTile ret = new AutoTile(f, e); ret.id = p.newTilemapId();
		ret.properties(); if(!ret.properties.updated) throw new Exception();
		p.setTilemapId(ret, ret.id); return ret;
	}
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated;
		private static final String FRAMES = "frames";
		private final AutoTile autotile; private final JTextField name, frames; private ButtonGroup speed = new ButtonGroup();
		private final AutoTileEditor editor; private Image image; private Tilemap tilemap;
		public Properties(AutoTile t){
			super(JOptionPane.getFrameForComponent(t.editor), "AutoTile Properties", true); autotile = t;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField(t.getName(), 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Auto Tile"));
			editor = new AutoTileEditor();
			JPanel p = new JPanel(); p.add(editor); p.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(p);
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner.add(set);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Frames"));
			frames = new JTextField(20); frames.setActionCommand(FRAMES); frames.addActionListener(this); inner.add(frames); settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Animation Speed"));
			JRadioButton r = new JRadioButton("Slow"); r.setActionCommand("3"); r.addActionListener(this); speed.add(r); inner.add(r);
			r = new JRadioButton("Normal"); r.setSelected(true); r.setActionCommand("2"); r.addActionListener(this); speed.add(r); inner.add(r);
			r = new JRadioButton("Fast"); r.setActionCommand("1"); r.addActionListener(this); speed.add(r); inner.add(r);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			setTilemap(null);
			pack();
		}
		private void setTilemap(Tilemap t){
			tilemap = t;
			boolean b = false; StringBuffer fr = new StringBuffer(); String sp = "";
			if(t != null && t.getFrames(0) != null){
				b = true;
				int ar[] = t.getFrames(0);
				for(int i=0; i<ar.length; i++){if(i != 0) fr.append(','); fr.append(ar[i]/(Tile.tile_size*2));}
				sp = Integer.toString(t.getSpeed(0));
			}
			frames.setEnabled(b); frames.setText(fr.toString());
			Enumeration<AbstractButton> e = speed.getElements();
			while(e.hasMoreElements()){AbstractButton r = e.nextElement(); if(r.getActionCommand().equals(sp)) r.setSelected(true); r.setEnabled(b);}
		}
		public void setVisible(boolean b){
			if(b == true){
				updated = false;
				name.setText(autotile.getName()); name.requestFocus(); name.selectAll();
				image = autotile.image; setTilemap(autotile.autotile);
				editor.setTilemap(tilemap); editor.start();
			} else editor.stop();
			super.setVisible(b);
		}
		private boolean updateFrames(){
			try{
				String[] params = frames.getText().split(",");
				int[] f = new int[params.length];
				for(int i=0; i<params.length; i++) f[i] = Integer.parseInt(params[i])*Tile.tile_size*2;
				tilemap.setFrames(0, f);
				return true;
			} catch(Exception ex){
				JOptionPane.showMessageDialog(this, "Could not parse frames field! Frames should be numbers separated by commas.", "Parse Error", JOptionPane.ERROR_MESSAGE);
				setTilemap(tilemap);
				return false;
			}
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				if(frames.isEnabled()){
					if(!updateFrames()) return;
					tilemap.setSpeed(0, Integer.parseInt(speed.getSelection().getActionCommand()));
				}
				autotile.image = image; autotile.autotile = tilemap;
				try{
					autotile.setName(name.getText());
				} catch(Exception ex){}
				if(image != null && tilemap != null) try{autotile.save(); updated = true;}catch(Exception ex){}
				setVisible(false); MapEditor.instance.refreshTilesets();
			} else if(command == MapEditor.SET){
				Project p = WorkspaceBrowser.getProject(autotile);
				if(p == null){
					WorkspaceBrowser b = autotile.editor.getBrowser();
					TreePath path; if(b.isSelectionEmpty() && b.getRowCount() == 0){path = null;}
					else if(b.isSelectionEmpty()) path = b.getPathForRow(0);
					else path = b.getSelectionPath();
					if(path != null && path.getPathCount() > 1) p = (Project)path.getPathComponent(1);
				}
				if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageChooser c = new ImageChooser(p, image);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){
					try{
						if(im.getImage().getHeight() == Tile.tile_size*2)
							tilemap = new WallTilemap(im.getImage(), autotile.getId());
						else tilemap = new AutoTilemap(im.getImage(), autotile.getId());
						setTilemap(tilemap);
						image = im; editor.setTilemap(tilemap);
					}catch(Exception ex){JOptionPane.showMessageDialog(autotile.editor, "Unable to create AutoTile: "+ex.getMessage(), "AutoTile Creation Error", JOptionPane.ERROR_MESSAGE); updated = false; autotile.autotile = null;}
				}
			}
			else if(command == MapEditor.CANCEL) setVisible(false);
			else if(command == FRAMES){if(frames.isEnabled()){updateFrames();}}
			else if(frames.isEnabled()) tilemap.setSpeed(0, Integer.parseInt(command));
		}
	}
}
