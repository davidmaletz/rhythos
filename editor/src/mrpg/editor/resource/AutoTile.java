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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import mrpg.editor.AutoTileEditor;
import mrpg.editor.MapEditor;
import mrpg.editor.TilesetViewer;
import mrpg.editor.WorkspaceBrowser;
import mrpg.world.AutoTilemap;
import mrpg.world.Tilemap;
import mrpg.world.WallTilemap;


public class AutoTile extends TileResource implements ActionListener {
	private static final long serialVersionUID = 3981925226292874481L;
	public static final String EXT = "atm", TYPE = "at"; private static final short VERSION=1;
	public static final String ADD_AUTOTILE="set-tileset";
	private Tilemap autotile; private Properties properties; private ImageResource image; private long id;
	private AnimationSet animation; private int aid;
	private JMenuItem add_autotile = MapEditor.createMenuItemIcon("Toggle Autotile", ADD_AUTOTILE, this);
	public AutoTile(File f, MapEditor e){super(f, e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(add_autotile); menu.add(browser.properties); menu.addSeparator();
	}
	public long getId(){return id;}
	public String getType(){return TYPE;}
	public boolean edit(){editor.getTilesetViewer().toggleAutoTile(autotile, WorkspaceBrowser.getProject(this)); return true;}
	public void properties(){if(properties == null) properties = new Properties(this); properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public ImageResource getImage(){return image;}
	public Tilemap getTilemap(){return autotile;}
	public void actionPerformed(ActionEvent e) {edit();}
	
	private boolean active = false;
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeId(TYPE, this, id); super.remove(delete);
		active = editor.getTilesetViewer().removeAutoTile(autotile); MapEditor.instance.refreshTilesets();
	}
	public void addToProject(Project p) throws Exception {
		long i = p.setId(TYPE, this, id); if(i != id){id = i; save();}
		if(WorkspaceBrowser.getProject(image) != p){
			image = p.getImageById(image.getId());
			if(animation != null) try{animation = (AnimationSet)p.getById(AnimationSet.TYPE, animation.getId());}catch(Exception ex){animation = null;}
		}
	}
	public void save() throws Exception {
		File f = getFile(); DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		try{
			out.writeShort(VERSION); out.writeLong(id); writeTileSize(out); ImageResource.write(out, image); autotile.write(out);
			out.writeLong((animation==null)?0:animation.getId()); out.writeShort(aid); out.flush(); out.close();
		}catch(Exception e){out.close(); throw e;}
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public boolean isCompatible(Project p){
		try{p.getImageById(image.getId()); return super.isCompatible(p);}catch(Exception e){return false;}
	}
	public void copyAssets(Project p) throws Exception {
		if(!image.isCompatible(p)) image.copyAssets(p);
		p.editor.getBrowser().addResource(Resource.readFile(image.copy(p.getFile(), p, false), p.editor), p);
	}
	public void deferredRead(File f) throws Exception {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{if(in.readShort() != VERSION) throw new Exception();
			id = in.readLong(); checkTileSize(in); Project p = WorkspaceBrowser.getProject(this); image = ImageResource.read(in, p);
			if(image.getImage().getHeight() == p.tile_size*2)
				autotile = new WallTilemap(in, image.getImage(), this, p.tile_size);
			else autotile = new AutoTilemap(in, image.getImage(), this, p.tile_size);
			long asid = in.readLong(); AnimationSet ani = null;
			if(asid != 0) try{ani = (AnimationSet)p.getById(AnimationSet.TYPE, asid);}catch(Exception ex){}
			animation = ani; aid = in.readShort(); in.close(); addToProject(p);
			if(active){active = false; editor.getTilesetViewer().addAutoTile(autotile, p);}
		}catch(Exception e){in.close(); throw e;}
	}
	public static AutoTile createAutoTile(Resource parent, MapEditor e, Project p) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,"New AutoTile"+"."+EXT);
		AutoTile ret = new AutoTile(f, e); ret._setName(null); ret.id = p.newId(TYPE);
		ret.properties(); if(!ret.properties.updated) throw new Exception();
		p.setId(TYPE, ret, ret.id); return ret;
	}
	public Animation getAnimation(){
		if(aid < 0 || animation == null || aid >= animation.numAnimations()) return null;
		return animation.getAnimation(aid);
	}
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated; private static final String SET_A = "set_a", CLEAR_A = "clear_a";
		private final AutoTile autotile; private final JTextField name, id; private AnimationSet animation;
		private final JTextField ani_label;
		private final AutoTileEditor editor; private ImageResource image; private Tilemap tilemap; private final JComboBox preview_ani;
		public Properties(AutoTile t){
			super(JOptionPane.getFrameForComponent(t.editor), "AutoTile Properties", true); autotile = t;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			name = new JTextField(t.getName(), 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name); JPanel p = new JPanel(); p.add(new JLabel("Id: "));
			id = new JTextField("", 15); id.setOpaque(false); id.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			id.setEditable(false); p.add(id); inner.add(p);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Auto Tile"));
			editor = new AutoTileEditor(getTileSize());
			p = new JPanel(); p.add(editor); p.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(p);
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner.add(set);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Animation")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			p = new JPanel(); ani_label = new JTextField("", 15); ani_label.setEditable(false); p.add(ani_label);
			set = new JButton("Set"); set.setActionCommand(SET_A); set.addActionListener(this); p.add(set);
			JButton clear = new JButton("Clear"); clear.setActionCommand(CLEAR_A); clear.addActionListener(this); p.add(clear);
			inner.add(p); p = new JPanel(); p.add(new JLabel("Use: ")); preview_ani = new JComboBox(); preview_ani.setEnabled(false);
			preview_ani.addActionListener(this); preview_ani.setPreferredSize(ani_label.getPreferredSize()); p.add(preview_ani); inner.add(p);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			setTilemap(null);
			pack();
		}
		private int getTileSize(){Project p = autotile.getProject(); if(p == null) return TilesetViewer.TILE_SIZE; else return p.tile_size;}
		private void setTilemap(Tilemap t){
			tilemap = t;
		}
		public void setVisible(boolean b){
			if(b == true){
				updated = false; id.setText(Long.toHexString(autotile.id));
				name.setText(autotile.getName()); name.requestFocus(); name.selectAll();
				image = autotile.image; setTilemap(autotile.autotile);
				editor.setTilemap(tilemap); setAnimation(autotile.animation, autotile.aid);
			} super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == preview_ani){
				if(preview_ani.isEnabled()) updateAnimation(); return;
			}
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				autotile.image = image; autotile.autotile = tilemap; autotile.animation = animation; autotile.aid = preview_ani.getSelectedIndex();
				try{
					autotile.setName(name.getText());
				} catch(Exception ex){
					name.setText(autotile.getName()); return;
				}
				if(image != null && tilemap != null) try{autotile.save(); updated = true;}catch(Exception ex){}
				setVisible(false); MapEditor.instance.refreshTilesets();
			} else if(command == MapEditor.SET){
				Project p = autotile.getProject();
				if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageResource im = ImageResource.choose(p, image);
				if(im != null){
					try{
						if(im.getImage().getHeight() == p.tile_size*2)
							tilemap = new WallTilemap(im.getImage(), autotile, p.tile_size);
						else tilemap = new AutoTilemap(im.getImage(), autotile, p.tile_size);
						setTilemap(tilemap);
						image = im; editor.setTilemap(tilemap);
					}catch(Exception ex){JOptionPane.showMessageDialog(autotile.editor, "Unable to create AutoTile: "+ex.getMessage(), "AutoTile Creation Error", JOptionPane.ERROR_MESSAGE); updated = false; autotile.autotile = null;}
				}
			} else if(command == SET_A){
				AnimationSet a = AnimationSet.choose(getProject(), animation); if(a != null) setAnimation(a,0);
			} else if(command == CLEAR_A){
				setAnimation(null,0);
			} else setVisible(false);
		}
		private Project getProject(){
			Project p = autotile.getProject();
			if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return null;}
			return p;
		}
		private void setAnimation(AnimationSet a, int aid){
			animation = a; if(animation != null && animation.numAnimations() > 0){
				ani_label.setText(animation.getName()); preview_ani.setEnabled(false); DefaultComboBoxModel model = (DefaultComboBoxModel)preview_ani.getModel();
				model.removeAllElements(); for(Animation ani : animation) model.addElement(ani); preview_ani.setSelectedIndex(aid); preview_ani.setEnabled(true);
			} else {ani_label.setText(""); preview_ani.setEnabled(false); ((DefaultComboBoxModel)preview_ani.getModel()).removeAllElements();}
			updateAnimation();
		}
		private void updateAnimation(){
			editor.setAnimation(animation, preview_ani.getSelectedIndex());
		}
	}
	public String getExt(){return EXT;}
	public static void register(){
		Resource.register("Auto Tile Files", AutoTile.EXT, AutoTile.class);
		Folder.new_options.addMenu("Map", Map.MAP).
			addItem("Auto Tile", "database", KeyEvent.VK_T, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, new CreateAutoTileAction());
	}
	private static class CreateAutoTileAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addAutoTile();
		}
	}
}
