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

public class AutoTile extends TileResource implements ActionListener {
	private static final long serialVersionUID = 3981925226292874481L;
	public static final String EXT = "atm", TYPE = "at"; private static final short VERSION=1;
	public static final String ADD_AUTOTILE="set-tileset";
	private Tilemap autotile; private ImageResource image;
	private AnimationSet animation; private int aid; private AutotileFormat format;
	private JMenuItem add_autotile = MapEditor.createMenuItemIcon("Toggle Autotile", ADD_AUTOTILE, this);
	public AutoTile(File f, MapEditor e){super(f, e);}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(add_autotile); menu.add(browser.properties); menu.addSeparator();
	}
	public String getType(){return TYPE;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public boolean edit(){editor.getTilesetViewer().toggleAutoTile(autotile, WorkspaceBrowser.getProject(this)); return true;}
	public ImageResource getImage(){return image;}
	public Tilemap getTilemap(){return autotile;}
	public void actionPerformed(ActionEvent e) {edit();}
	
	private boolean active = false;
	public void remove(boolean delete) throws Exception {
		super.remove(delete);
		active = editor.getTilesetViewer().removeAutoTile(autotile); MapEditor.instance.refreshTilesets();
	}
	public void addToProject(Project p, boolean changeProject) throws Exception {
		super.addToProject(p, changeProject);
		if(changeProject){
			image = (ImageResource)p.getById(image.getType(), image.getId());
			if(animation != null) try{animation = (AnimationSet)p.getById(AnimationSet.TYPE, animation.getId());}catch(Exception ex){animation = null;}
		}
	}
	public void writeInner(DataOutputStream out) throws Exception {
		super.writeInner(out); ImageResource.write(out, image); out.writeLong(format.getId()); autotile.write(out);
		out.writeLong((animation==null)?0:animation.getId()); out.writeShort(aid);
	}
	public void readInner(DataInputStream in) throws Exception {
		super.readInner(in); Project p = WorkspaceBrowser.getProject(this); image = ImageResource.read(in, p);
		format = (AutotileFormat)p.getById(AutotileFormat.TYPE, in.readLong());
		autotile = new AutoTilemap(in, format.getFormat(), image.getImage(), this, p.tile_size);
		long asid = in.readLong(); AnimationSet ani = null;
		if(asid != 0) try{ani = (AnimationSet)p.getById(AnimationSet.TYPE, asid);}catch(Exception ex){}
		animation = ani; aid = in.readShort();
	}
	public void deferredRead(File f) throws Exception {
		super.deferredRead(f);
		if(active){active = false; editor.getTilesetViewer().addAutoTile(autotile, WorkspaceBrowser.getProject(this));}
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public boolean isCompatible(Project p){
		try{p.getById(image.getType(), image.getId()); return super.isCompatible(p);}catch(Exception e){return false;}
	}
	public void copyAssets(Project p) throws Exception {
		if(!image.isCompatible(p)) image.copyAssets(p);
		p.editor.getBrowser().addResource(Resource.readFile(image.copy(p.getFile(), p, false), p.editor), p);
	}
	public static AutoTile createAutoTile(Resource parent, MapEditor e, Project p) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,"New AutoTile"+"."+EXT);
		AutoTile ret = new AutoTile(f, e); ret.newId(p); ret._setName(null);
		ret.properties(); if(!((Properties)ret.properties).updated) throw new Exception();
		ret.addToProject(p,false); return ret;
	}
	public static AutoTile createAutoTile(Resource parent, MapEditor e, Project p, AutotileFormat fmt, String name, ImageResource im) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,name+"."+EXT);
		AutoTile ret = new AutoTile(f, e); ret.newId(p); ret.format = fmt;
		ret.image = im; ret.autotile = new AutoTilemap(ret.format.getFormat(), im.getImage(), ret, p.tile_size);
		parent.add(ret); ret.save(); ret.addToProject(p,false); return ret;
	}
	public Animation getAnimation(){
		if(aid < 0 || animation == null || aid >= animation.numAnimations()) return null;
		return animation.getAnimation(aid);
	}
	private static class Properties extends TypedResource.Properties {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String SET_F = "set_f", SET_A = "set_a", CLEAR_A = "clear_a";
		private AnimationSet animation; private JTextField ani_label, fmt_label; private AutotileFormat format;
		private AutoTileEditor editor; private ImageResource image; private Tilemap tilemap; private JComboBox preview_ani;
		public Properties(AutoTile t){super(t, "AutoTile Properties");}
		public void addControls(JPanel settings){
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Format"));
			JPanel p = new JPanel(); fmt_label = new JTextField("", 15); fmt_label.setEditable(false); p.add(fmt_label);
			JButton set = new JButton("Set"); set.setActionCommand(SET_F); set.addActionListener(this); p.add(set);
			inner.add(p); settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Auto Tile"));
			editor = new AutoTileEditor(getTileSize());
			p = new JPanel(); p.add(editor); p.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(p);
			set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner.add(set);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Animation")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			p = new JPanel(); ani_label = new JTextField("", 15); ani_label.setEditable(false); p.add(ani_label);
			set = new JButton("Set"); set.setActionCommand(SET_A); set.addActionListener(this); p.add(set);
			JButton clear = new JButton("Clear"); clear.setActionCommand(CLEAR_A); clear.addActionListener(this); p.add(clear);
			inner.add(p); p = new JPanel(); p.add(new JLabel("Use: ")); preview_ani = new JComboBox(); preview_ani.setEnabled(false);
			preview_ani.addActionListener(this); preview_ani.setPreferredSize(ani_label.getPreferredSize()); p.add(preview_ani); inner.add(p);
			settings.add(inner);
		}
		private int getTileSize(){Project p = resource.getProject(); if(p == null) return TilesetViewer.TILE_SIZE; else return p.tile_size;}
		public void updateControls(){
			AutoTile autotile = (AutoTile)resource; image = autotile.image; format = autotile.format;
			if(format == null) fmt_label.setText(""); else fmt_label.setText(format.getName());
			tilemap = autotile.autotile; editor.setTilemap(tilemap); setAnimation(autotile.animation, autotile.aid);
		}
		public void acceptControls() throws Exception {
			if(format == null){
				JOptionPane.showMessageDialog(resource.editor, "You must specify an Auto Tile Format for the auto tile.", "Auto Tile Creation Error", JOptionPane.ERROR_MESSAGE);
				throw new Exception();
			} else if(image == null || tilemap == null){
				JOptionPane.showMessageDialog(resource.editor, "You must specify an image for the auto tile.", "Auto Tile Creation Error", JOptionPane.ERROR_MESSAGE);
				throw new Exception();
			} AutoTile autotile = (AutoTile)resource; autotile.image = image; autotile.format = format;
			autotile.autotile = tilemap; autotile.animation = animation; autotile.aid = preview_ani.getSelectedIndex();
			MapEditor.instance.refreshTilesets();
		}
		public boolean saveOnEdit(){return true;}
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == preview_ani){
				if(preview_ani.isEnabled()) updateAnimation(); return;
			}
			String command = e.getActionCommand();
			if(command == MapEditor.SET){
				if(format == null){
					JOptionPane.showMessageDialog(this, "You must choose an Auto Tile Format before setting an image.", "Cannot Set Image", JOptionPane.ERROR_MESSAGE); return;
				} AutoTile autotile = (AutoTile)resource; Project p = autotile.getProject();
				if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageResource im = ImageResource.choose(p, image);
				if(im != null){
					try{
						tilemap = new AutoTilemap(format.getFormat(), im.getImage(), autotile, p.tile_size);
						image = im; editor.setTilemap(tilemap);
					}catch(Exception ex){JOptionPane.showMessageDialog(autotile.editor, "Unable to create AutoTile: "+ex.getMessage(), "AutoTile Creation Error", JOptionPane.ERROR_MESSAGE);}
				}
			} else if(command == SET_F){
				Project p = resource.getProject(); AutotileFormat f = AutotileFormat.choose(p, format);
				if(f != null){
					try{
						if(image != null){
							tilemap = new AutoTilemap(f.getFormat(), image.getImage(), (AutoTile)resource, p.tile_size);
							editor.setTilemap(tilemap);
						} format = f; fmt_label.setText(format.getName());
					} catch(Exception ex){JOptionPane.showMessageDialog(resource.editor, "The selected format can not render the selected image: "+ex.getMessage(), "Set Format Error", JOptionPane.ERROR_MESSAGE);}
				}
			} else if(command == SET_A){
				AnimationSet a = AnimationSet.choose(getProject(), animation); if(a != null) setAnimation(a,0);
			} else if(command == CLEAR_A){
				setAnimation(null,0);
			} else super.actionPerformed(e);
		}
		private Project getProject(){
			Project p = resource.getProject();
			if(p == null){JOptionPane.showMessageDialog(this, "Tileset is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return null;}
			return p;
		}
		private void setAnimation(AnimationSet a, int aid){
			animation = a; if(animation != null && animation.numAnimations() > 0){
				ani_label.setText(animation.getName()); preview_ani.setEnabled(false);
				DefaultComboBoxModel model = (DefaultComboBoxModel)preview_ani.getModel();
				model.removeAllElements(); for(Animation ani : animation) model.addElement(ani);
				preview_ani.setSelectedIndex(aid); preview_ani.setEnabled(true);
			} else {ani_label.setText(""); preview_ani.setEnabled(false); ((DefaultComboBoxModel)preview_ani.getModel()).removeAllElements();}
			updateAnimation();
		}
		private void updateAnimation(){
			editor.setAnimation(animation, preview_ani.getSelectedIndex());
		}
	}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Auto Tile Files", AutoTile.EXT, AutoTile.TYPE, AutoTile.class);
		Resource.register("Auto Tile Format Files", AutotileFormat.EXT, AutotileFormat.TYPE, AutotileFormat.class);
		Folder.new_options.addMenu("Map", Map.MAP).
			addItem("Auto Tile", "database", KeyEvent.VK_T, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, new CreateAutoTileAction());
	}
	private static class CreateAutoTileAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addAutoTile();
		}
	}
}
