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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mrpg.editor.DragList;
import mrpg.editor.Filter;
import mrpg.editor.MapEditor;
import mrpg.editor.ResourceChooser;
import mrpg.editor.WorkspaceBrowser;

public class Sprite extends TypedResource {
	private static final long serialVersionUID = -5394199071824545816L;
	public static final String EXT = "spr", TYPE = "sp"; private static final short VERSION=1;
	private static final Icon icon = MapEditor.getIcon("chr_appearance");
	private final ArrayList<Layer> layers = new ArrayList<Layer>(); private AnimationSet animation;
	public Sprite(File f, MapEditor editor){super(f, editor);}
	public String getType(){return TYPE;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public Icon getIcon(){return icon;}
	public void addToProject(Project p, boolean changeProject) throws Exception {
		super.addToProject(p, changeProject);
		if(changeProject){
			if(animation != null)
				try{animation = (AnimationSet)p.getById(AnimationSet.TYPE, animation.getId());}catch(Exception ex){animation = null;}
			for(Layer l : layers){
				if(l != null && l.image != null){
					try{l.image = (ImageResource)p.getById(l.image.getType(), l.image.getId());}catch(Exception ex){l.image = null;}
				} if(l != null && l.layer != null){
					try{l.layer = (SpriteLayer)p.getById(SpriteLayer.TYPE, l.layer.getId());}catch(Exception ex){l.layer = null;}
				}
			}
		}
	}
	public void writeInner(DataOutputStream out) throws Exception {
		out.writeShort(layers.size()); for(Layer l : layers) l.write(out);
		out.writeLong((animation==null)?0:animation.getId());
	}
	public void readInner(DataInputStream in) throws Exception {
		Project p = WorkspaceBrowser.getProject(this);
		short nLayers = in.readShort();
		layers.clear(); for(int i=0; i<nLayers; i++) layers.add(Layer.read(p, in));
		long aid = in.readLong(); AnimationSet ani = null;
		if(aid != 0) try{ani = (AnimationSet)p.getById(AnimationSet.TYPE, aid);}catch(Exception ex){}
		animation = ani;
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public static Sprite create(Resource parent, MapEditor e, Project p) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,"New Sprite"+"."+EXT);
		Sprite ret = new Sprite(f,e); ret._setName(null); ret.newId(p); ret.properties();
		if(!((Properties)ret.properties).updated) throw new Exception();
		ret.addToProject(p, false); return ret;
	}
	
	private static class Properties extends TypedResource.Properties implements MouseListener, DragList.Listener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String ADD = "add", REM = "rem";
		private AnimationSet animation; private JTextField ani_label;
		private JLabel image_thumb; private JList layers; private JComboBox preview_ani;
		public Properties(Sprite chr){super(chr, "Sprite Properties");}
		public void addControls(JPanel settings){
			JPanel inner = new JPanel(new BorderLayout()); JPanel p = new JPanel(new BorderLayout());
			p.setBorder(BorderFactory.createTitledBorder("Layers"));
			layers = new JList(); layers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			layers.addMouseListener(this); new DragList(layers, Layer.class, this);
			JScrollPane pane = new JScrollPane(layers); pane.setPreferredSize(new Dimension(200,120));
			p.add(pane, BorderLayout.CENTER); JButton add = new JButton("+"); add.setActionCommand(ADD);
			add.addActionListener(this); JButton rem = new JButton("-"); rem.setActionCommand(REM);
			rem.addActionListener(this); JPanel inner2 = new JPanel(new GridLayout(1,2)); inner2.add(add);
			inner2.add(rem); p.add(inner2, BorderLayout.SOUTH); inner.add(p, BorderLayout.WEST);
			image_thumb = new JLabel(new ImageIcon());
			pane = new JScrollPane(image_thumb); pane.setPreferredSize(ImageResource.THUMB_SIZE);
			pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Preview"),
					BorderFactory.createLoweredBevelBorder())); inner.add(pane, BorderLayout.CENTER); settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Animation")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			p = new JPanel(); ani_label = new JTextField("", 15); ani_label.setEditable(false); p.add(ani_label);
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); p.add(set);
			JButton clear = new JButton("Clear"); clear.setActionCommand(MapEditor.CLEAR); clear.addActionListener(this); p.add(clear);
			inner.add(p); p = new JPanel(); p.add(new JLabel("Preview: ")); preview_ani = new JComboBox(); preview_ani.setEnabled(false);
			preview_ani.addActionListener(this); preview_ani.setPreferredSize(ani_label.getPreferredSize()); p.add(preview_ani); inner.add(p);
			settings.add(inner);
		}
		public void updateControls(){
			Sprite chara = (Sprite)resource; setAnimation(chara.animation); DefaultListModel m = new DefaultListModel();
			for(Layer i : chara.layers) m.addElement(i); layers.setModel(m);
			if(chara.layers.size() > 0) layers.setSelectedIndex(0); updatePreview();
		}
		public void acceptControls(){
			Sprite chara = (Sprite)resource; chara.animation = animation; chara.layers.clear();
			ListModel m = layers.getModel(); for(int i=0; i<m.getSize(); i++) chara.layers.add((Layer)m.getElementAt(i));
		}
		public boolean saveOnEdit(){return true;}
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == preview_ani){
				if(preview_ani.isEnabled()) updateAnimation(); return;
			}
			String command = e.getActionCommand();
			if(command == REM){
				int i = layers.getSelectedIndex(); if(i != -1){((DefaultListModel)layers.getModel()).removeElementAt(i); updatePreview();}
			} else if(command == ADD){
				Layer layer = editLayer(null); if(layer != null){
					DefaultListModel m = (DefaultListModel)layers.getModel(); int sz = m.getSize();
					m.addElement(layer); layers.setSelectedIndex(sz); updatePreview();
				}
			} else if(command == MapEditor.SET){
				AnimationSet a = AnimationSet.choose(getProject(), animation); if(a != null) setAnimation(a);
			} else if(command == MapEditor.CLEAR){
				setAnimation(null);
			} else super.actionPerformed(e);
		}
		private void setAnimation(AnimationSet a){
			animation = a; if(animation != null && animation.numAnimations() > 0){
				ani_label.setText(animation.getName()); preview_ani.setEnabled(false); DefaultComboBoxModel model = (DefaultComboBoxModel)preview_ani.getModel();
				model.removeAllElements(); for(Animation ani : animation) model.addElement(ani); preview_ani.setSelectedIndex(0); preview_ani.setEnabled(true);
			} else {ani_label.setText(""); preview_ani.setEnabled(false); ((DefaultComboBoxModel)preview_ani.getModel()).removeAllElements();}
			updateAnimation();
		}
		public void updateDrag(){updatePreview();}
		public Object paste(Object o){return o;}
		public void mouseClicked(MouseEvent e){
			if(e.getClickCount() == 2){
				int idx = ((JList)e.getSource()).getSelectedIndex(); if(idx == -1) return;
				Object o = ((JList)e.getSource()).getSelectedValue();
				Layer layer = editLayer((Layer)o); if(layer != null){
					DefaultListModel m = (DefaultListModel)layers.getModel();
					m.setElementAt(layer, idx); updatePreview();
				}
			}
		}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
		private BufferedImage cache;
		private void updatePreview(){
			BufferedImage b = null; ListModel model = layers.getModel(); int sz = model.getSize();
			for(int i=0; i<sz; i++){
				Layer l = (Layer)model.getElementAt(i); if(l == null) continue;
				BufferedImage im = l.getImage(); if(im == null) continue;
				if(b == null) b = im; else{
					int w = im.getWidth(), h = im.getHeight(); if(b.getWidth() < w || b.getHeight() < h){
						BufferedImage tmp = new BufferedImage(Math.max(b.getWidth(),w),Math.max(b.getHeight(),h),BufferedImage.TYPE_INT_ARGB);
						tmp.setData(b.getData()); b = tmp;
					} b.getGraphics().drawImage(im, 0, 0, null);
				}
			} cache = b; updateAnimation();
		}
		private void updateAnimation(){
			if(cache != null){
				if(!preview_ani.isEnabled() || preview_ani.getSelectedIndex() == -1) image_thumb.setIcon(new ImageIcon(cache));
				else image_thumb.setIcon(animation.getAnimatedIcon(image_thumb, preview_ani.getSelectedIndex(), cache));
			} else image_thumb.setIcon(new ImageIcon());
		}
		private Project getProject(){
			Project p = resource.getProject();
			if(p == null){JOptionPane.showMessageDialog(this, "Sprite is not added to any project, no resources to load...", "Cannot Find Resources", JOptionPane.ERROR_MESSAGE); return null;}
			return p;
		}
		public Layer editLayer(Layer l){
			Project p = getProject();
			return (l==null)?chooseLayer(p, null, 0, 0):chooseLayer(p, (l.layer==null)?l.image:l.layer, l.image_id, l.color);
		}
	}
	private static class Layer {
		private SpriteLayer layer; private ImageResource image; private int image_id, color;
		public Layer(ImageResource l){image = l; layer = null;}
		public Layer(SpriteLayer l, int i, int c){layer = l; image = null; image_id = i; color = c;}
		public void write(DataOutputStream out) throws Exception {
			if(layer == null){
				out.writeShort(-1); ImageResource.write(out, image); 
			} else {
				out.writeShort(image_id); out.writeShort(color); out.writeLong(layer.getId()); 
			}
		}
		public static Layer read(Project p, DataInputStream in) throws Exception {
			short image_id = in.readShort(); if(image_id < 0){
				return new Layer(ImageResource.read(in, p));
			} else {
				short color = in.readShort(); SpriteLayer layer = null;
				try{layer = (SpriteLayer)p.getById(SpriteLayer.TYPE, in.readLong());}catch(Exception ex){}
				return new Layer(layer, image_id, color);
			}
		}
		public BufferedImage getImage(){
			if(layer == null){
				if(image == null) return null; else{
					BufferedImage im = image.getImage(); ColorModel cm = im.getColorModel();
					return new BufferedImage(cm, im.copyData(null), cm.isAlphaPremultiplied(), null);
				}
			} else return layer.get(image_id, color);
		}
		public String toString(){
			if(layer == null) return (image == null)?"Empty":image.getName();
			else return layer.getName()+" ("+layer.getColorName(color)+" "+layer.getImageName(image_id)+")";
		}
	}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Sprite Files", Sprite.EXT, Sprite.TYPE, Sprite.class);
		Folder.new_options.addMenu("Sprite", "chr_appearance").
			addItem("Sprite", "chr_appearance", new CreateCharaAction());
	}
	private static class CreateCharaAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addResource(Sprite.class);
		}
	}
	
	private static Layer chooseLayer(Resource root, Resource selected, int image, int color){
		ResourceChooser c = new ResourceChooser(root, selected, FILTER); if(selected != null) FILTER.set(image, color);
		c.setVisible(true); Resource r = c.getSelectedResource(); if(r == null) return null;
		if(ImageResource.isImage(r)) return new Layer((ImageResource)r); else return new Layer((SpriteLayer)r, FILTER.images.getSelectedIndex(), FILTER.colors.getSelectedIndex());
	}
	private static class LFilter extends JPanel implements Filter, ListSelectionListener {
		private static final long serialVersionUID = 907354882348925575L;
		private JLabel image_thumb; private JList images, colors; private SpriteLayer layer;
		private JPanel createLabeled(JScrollPane p, String label){
			JPanel ret = new JPanel(new BorderLayout()); ret.add(new JLabel(label), BorderLayout.NORTH); ret.add(p, BorderLayout.CENTER); return ret;
		}
		public LFilter(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); image_thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(image_thumb); pane.setPreferredSize(ImageResource.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); add(pane); JPanel p = new JPanel(new GridLayout(1,2));
			images = new JList(new DefaultListModel()); images.setEnabled(false); images.addListSelectionListener(this);
			pane = new JScrollPane(images); pane.setPreferredSize(new Dimension(50,100)); p.add(createLabeled(pane, "Images"));
			colors = new JList(new DefaultListModel()); colors.setEnabled(false); colors.addListSelectionListener(this);
			pane = new JScrollPane(colors); pane.setPreferredSize(new Dimension(50,100)); p.add(createLabeled(pane, "Colors")); add(p);
		}
		public boolean filter(Resource r){String ext = r.getExt(); return ext == null || ext == SpriteLayer.EXT || ImageResource.isImage(r);}
		public void set(int image, int color){
			if(images.isEnabled()) images.setSelectedIndex(image); if(colors.isEnabled()) colors.setSelectedIndex(color);
		}
		private void reset(BufferedImage im){
			image_thumb.setIcon((im==null)?new ImageIcon():new ImageIcon(im)); images.setEnabled(false); colors.setEnabled(false);
			((DefaultListModel)images.getModel()).clear(); ((DefaultListModel)colors.getModel()).clear();
		}
		public JPanel getPreview(){reset(null); return this;}
		public boolean showPreview(Resource r){
			String ext = r.getExt(); if(ext == null){reset(null); return false;} 
			else if(ImageResource.isImage(r)){reset(((ImageResource)r).getImage()); return true;}
			layer = (SpriteLayer)r; reset(layer.get(0, 0)); DefaultListModel m = (DefaultListModel)images.getModel();
			int sz = layer.getImageCount(); for(int i=0; i<sz; i++) m.addElement(layer.getImageName(i)); m = (DefaultListModel)colors.getModel();
			sz = layer.getColorCount(); for(int i=0; i<sz; i++) m.addElement(layer.getColorName(i));
			images.setSelectedIndex(0); colors.setSelectedIndex(0); images.setEnabled(true); colors.setEnabled(true);
			return true;
		}
		public void valueChanged(ListSelectionEvent e) {
			JList l = (JList)e.getSource(); if(e.getValueIsAdjusting() || !l.isEnabled()) return;
			BufferedImage im = layer.get(images.getSelectedIndex(), colors.getSelectedIndex());
			image_thumb.setIcon((im==null)?new ImageIcon():new ImageIcon(im));
		}
	} private static final LFilter FILTER = new LFilter();
}
