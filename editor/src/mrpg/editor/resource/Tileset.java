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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import mrpg.editor.MapEditor;
import mrpg.editor.TilesetEditor;
import mrpg.editor.TilesetViewer;
import mrpg.editor.WorkspaceBrowser;
import mrpg.world.BasicTilemap;

public class Tileset extends TileResource implements ActionListener {
	private static final long serialVersionUID = 3981925226292874481L;
	public static final String EXT = "btm", TYPE = "bt"; private static final short VERSION=1;
	public static final String SET_TILESET="set-tileset";
	private BasicTilemap tilemap; private ImageResource image;
	private JMenuItem set_tileset = MapEditor.createMenuItemIcon("Set Tileset", SET_TILESET, this);
	public Tileset(File f, MapEditor e){super(f,e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(set_tileset); menu.add(browser.properties); menu.addSeparator();
	}
	public String getType(){return TYPE;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public boolean edit(){editor.getTilesetViewer().setTilemap(tilemap, WorkspaceBrowser.getProject(this)); return true;}
	public ImageResource getImage(){return image;}
	public BasicTilemap getTilemap(){return tilemap;}
	public void actionPerformed(ActionEvent e) {edit();}
	private boolean active = false;
	public void remove(boolean delete) throws Exception {
		super.remove(delete);
		active = editor.getTilesetViewer().removeTilemap(tilemap); MapEditor.instance.refreshTilesets();
	}
	public void addToProject(Project p, boolean changeProject) throws Exception {
		super.addToProject(p, changeProject);
		if(changeProject){
			image = (ImageResource)p.getById(image.getType(), image.getId());
		}
	}
	public boolean isCompatible(Project p){
		try{p.getById(image.getType(), image.getId()); return super.isCompatible(p);}catch(Exception e){return false;}
	}
	public void copyAssets(Project p) throws Exception {
		if(!image.isCompatible(p)) image.copyAssets(p);
		p.editor.getBrowser().addResource(Resource.readFile(image.copy(p.getFile(), p, false), p.editor), p);
	}
	public void writeInner(DataOutputStream out) throws Exception {
		super.writeInner(out); ImageResource.write(out, image); tilemap.write(out);
	}
	public void readInner(DataInputStream in) throws Exception {
		super.readInner(in); Project p = WorkspaceBrowser.getProject(this); image = ImageResource.read(in, p);
		tilemap = new BasicTilemap(in, image.getImage(), this, p.tile_size);
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public void deferredRead(File f) throws Exception {
		super.deferredRead(f);
		if(active){active = false; editor.getTilesetViewer().setTilemap(tilemap, WorkspaceBrowser.getProject(this));}
	}
	
	public static Tileset createTileset(Resource parent, MapEditor e, Project p) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,"New Tileset"+"."+EXT);
		Tileset ret = new Tileset(f, e); ret._setName(null); ret.newId(p);
		ret.properties(); if(!((Properties)ret.properties).updated) throw new Exception();
		ret.addToProject(p, false); return ret;
	}
	public static Tileset createTileset(Resource parent, MapEditor e, Project p, String name, ImageResource im) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,name+"."+EXT);
		Tileset ret = new Tileset(f, e); ret.newId(p);
		ret.image = im; ret.tilemap = new BasicTilemap(im.getImage(), ret, p.tile_size);
		ret.addToProject(p, false); return ret;
	}
	private static class Properties extends TypedResource.Properties {
		private static final long serialVersionUID = -4987880557990107307L;
		private TilesetEditor editor; private BasicTilemap tilemap; private ImageResource image;
		public Properties(Tileset t){super(t, "Tileset Properties");}
		public void addControls(JPanel settings){
			JPanel inner = new JPanel(new BorderLayout()); inner.setBorder(BorderFactory.createTitledBorder("Tileset"));
			editor = new TilesetEditor(getTileSize()); JScrollPane sp = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setPreferredSize(new Dimension(TilesetViewer.TILE_SIZE*8+sp.getVerticalScrollBar().getPreferredSize().width+4,TilesetViewer.TILE_SIZE*8+4)); inner.add(sp, BorderLayout.CENTER);
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner.add(set, BorderLayout.SOUTH);
			settings.add(inner);
		}
		private int getTileSize(){Project p = resource.getProject(); if(p == null) return TilesetViewer.TILE_SIZE; else return p.tile_size;}
		public void updateControls(){
			Tileset tileset = (Tileset)resource;
			image = tileset.image; tilemap = tileset.tilemap;
			editor.setTilemap(tilemap);
		}
		public void acceptControls() throws Exception {
			if(image == null || tilemap == null){
				JOptionPane.showMessageDialog(resource.editor, "You must specify an image for the tileset.", "Tileset Creation Error", JOptionPane.ERROR_MESSAGE);
				throw new Exception();
			} Tileset tileset = (Tileset)resource;
			tileset.image = image; tileset.tilemap = tilemap;
			MapEditor.instance.refreshTilesets();
		}
		public boolean saveOnEdit(){return true;}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.SET){
				Tileset tileset = (Tileset)resource; Project p = tileset.getProject();
				if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageResource im = ImageResource.choose(p, image);
				if(im != null){
					try{tilemap = new BasicTilemap(im.getImage(), tileset, p.tile_size); editor.setTilemap(tilemap); image = im;}
					catch(Exception ex){JOptionPane.showMessageDialog(tileset.editor, "Unable to create Tileset: "+ex.getMessage(), "Tileset Creation Error", JOptionPane.ERROR_MESSAGE);}
				}
			} else super.actionPerformed(e);
		}
	}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Tileset Files", Tileset.EXT, Tileset.TYPE, Tileset.class);
		Folder.new_options.addMenu("Map", Map.MAP).
			addItem("Tileset", "database", KeyEvent.VK_T, ActionEvent.CTRL_MASK, new CreateTilesetAction());
	}
	private static class CreateTilesetAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addTileset();
		}
	}
}
