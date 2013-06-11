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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import mrpg.editor.ImageChooser;
import mrpg.editor.MapEditor;
import mrpg.editor.TilesetEditor;
import mrpg.editor.TilesetViewer;
import mrpg.editor.WorkspaceBrowser;
import mrpg.world.BasicTilemap;

public class Tileset extends TileResource implements ActionListener {
	private static final long serialVersionUID = 3981925226292874481L;
	private static final Icon icon = MapEditor.getIcon("database");
	public static final String EXT = "btm"; private static final short VERSION=1;
	public static final String SET_TILESET="set-tileset";
	private BasicTilemap tilemap; private Properties properties; private Image image; private long id;
	private JMenuItem set_tileset = MapEditor.createMenuItemIcon("Set Tileset", SET_TILESET, this);
	public Tileset(File f, MapEditor e){super(f,e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(set_tileset); menu.add(browser.properties); menu.addSeparator();
	}
	public long getId(){return id;}
	public boolean edit(){editor.getTilesetViewer().setTilemap(tilemap, WorkspaceBrowser.getProject(this)); return true;}
	public void properties(){if(properties == null) properties = new Properties(this); properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public Icon getIcon(){return icon;}
	public Image getImage(){return image;}
	public BasicTilemap getTilemap(){return tilemap;}
	public void actionPerformed(ActionEvent e) {edit();}
	private boolean active = false;
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeTilemapId(this, id); super.remove(delete);
		active = editor.getTilesetViewer().removeTilemap(tilemap);
	}
	public void save() throws Exception {
		File f = getFile(); DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		try{out.writeShort(VERSION);
			out.writeLong(id); out.writeLong(image.getId()); tilemap.write(out); out.flush(); out.close();
		}catch(Exception e){out.close(); throw e;}
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public void deferredRead(File f) throws Exception {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{if(in.readShort() != VERSION) throw new Exception();
			id = in.readLong(); Project p = WorkspaceBrowser.getProject(this); image = p.getImageById(in.readLong());
			tilemap = new BasicTilemap(in, image.getImage(), getId(), p.tile_size);
			in.close(); long i = p.setTilemapId(this, id); if(i != id){id = i; save();}
			if(active){active = false; editor.getTilesetViewer().setTilemap(tilemap, p);}
		}catch(Exception e){in.close(); throw e;}
	}
	
	public static Tileset createTileset(Resource parent, MapEditor e, Project p) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,"New Tileset"+"."+EXT);
		Tileset ret = new Tileset(f, e); ret._setName(null); ret.id = p.newTilemapId();
		ret.properties(); if(!ret.properties.updated) throw new Exception();
		p.setTilemapId(ret, ret.id); return ret;
	}
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated;
		private final Tileset tileset; private final JTextField name, id;
		private final TilesetEditor editor; private BasicTilemap tilemap; private Image image;
		public Properties(Tileset t){
			super(JOptionPane.getFrameForComponent(t.editor), "Tileset Properties", true); tileset = t;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			name = new JTextField(t.getName(), 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name); JPanel p = new JPanel(); p.add(new JLabel("Id: "));
			id = new JTextField("", 15); id.setOpaque(false); id.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			id.setEditable(false); p.add(id); inner.add(p);
			settings.add(inner);
			inner = new JPanel(new BorderLayout()); inner.setBorder(BorderFactory.createTitledBorder("Tileset"));
			editor = new TilesetEditor(getTileSize()); JScrollPane sp = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setPreferredSize(new Dimension(TilesetViewer.TILE_SIZE*8+sp.getVerticalScrollBar().getPreferredSize().width+4,TilesetViewer.TILE_SIZE*8+4)); inner.add(sp, BorderLayout.CENTER);
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner.add(set, BorderLayout.SOUTH);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		private Project getProject(){
			Project p = WorkspaceBrowser.getProject(tileset);
			if(p == null){
				WorkspaceBrowser b = tileset.editor.getBrowser();
				TreePath path; if(b.isSelectionEmpty() && b.getRowCount() == 0){path = null;}
				else if(b.isSelectionEmpty()) path = b.getPathForRow(0);
				else path = b.getSelectionPath();
				if(path != null && path.getPathCount() > 1) p = (Project)path.getPathComponent(1);
			} return p;
		}
		private int getTileSize(){Project p = getProject(); if(p == null) return TilesetViewer.TILE_SIZE; else return p.tile_size;}
		public void setVisible(boolean b){
			if(b == true){
				updated = false; id.setText(Long.toHexString(tileset.id));
				name.setText(tileset.getName()); name.requestFocus(); name.selectAll();
				image = tileset.image; tilemap = tileset.tilemap;
				editor.setTilemap(tilemap);
			}
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				tileset.image = image; tileset.tilemap = tilemap;
				try{
					tileset.setName(name.getText());
				}catch(Exception ex){
					name.setText(tileset.getName()); return;
				}
				if(image != null && tilemap != null) try{tileset.save(); updated = true;}catch(Exception ex){}
				setVisible(false); MapEditor.instance.refreshTilesets();
			} else if(command == MapEditor.SET){
				Project p = getProject();
				if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageChooser c = new ImageChooser(p, image);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){
					try{tilemap = new BasicTilemap(im.getImage(), tileset.getId(), p.tile_size); editor.setTilemap(tilemap); image = im;}
					catch(Exception ex){JOptionPane.showMessageDialog(tileset.editor, "Unable to create Tileset: "+ex.getMessage(), "Tileset Creation Error", JOptionPane.ERROR_MESSAGE); updated = false; tileset.tilemap = null;}
				}
			}
			else setVisible(false);
		}
	}
	public static void register(){
		Resource.register("Tileset Files", Tileset.EXT, Tileset.class);
		Folder.new_options.addMenu("Map", Map.MAP).
			addItem("Tileset", "database", KeyEvent.VK_T, ActionEvent.CTRL_MASK, new CreateTilesetAction());
	}
	private static class CreateTilesetAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addTileset();
		}
	}
}
