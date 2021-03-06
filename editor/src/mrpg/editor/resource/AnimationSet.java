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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mrpg.editor.DragList;
import mrpg.editor.Filter;
import mrpg.editor.MapEditor;
import mrpg.editor.ResourceChooser;
import mrpg.editor.TilesetViewer;
import mrpg.editor.WorkspaceBrowser;

public class AnimationSet extends TypedResource implements Iterable<Animation> {
	private static final long serialVersionUID = -5394199071824545816L;
	public static final String EXT = "ani", TYPE = "an"; private static final short VERSION=1;
	private static final Icon icon = MapEditor.getIcon("chr_appearance");
	private ImageResource image; private BufferedImage cache; private int width, height;
	private final ArrayList<Animation> animations = new ArrayList<Animation>();
	public AnimationSet(File f, MapEditor editor){super(f, editor); width = 4; height = 4;}
	public Icon getIcon(){return icon;}
	public int numAnimations(){return animations.size();}
	public Iterator<Animation> iterator(){return animations.iterator();}
	public Animation getAnimation(int i){return animations.get(i);}
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	public Icon getAnimatedIcon(JLabel l, int i, BufferedImage img){
		return new Animation.Icon(l, animations.get(i), img, width, height);
	}
	public int getHeaderSize(){return super.getHeaderSize()+ImageResource.getSize(image);}
	public void writeInner(DataOutputStream out) throws Exception {
		ImageResource.write(out, image); out.writeShort(width);
		out.writeShort(height); out.writeShort(animations.size()); for(Animation a : animations) a.write(out);
	}
	public void readInner(DataInputStream in) throws Exception {
		Project p = WorkspaceBrowser.getProject(this); image = ImageResource.read(in, p);
		width = in.readShort(); height = in.readShort(); short nAni = in.readShort();
		animations.clear(); for(int i=0; i<nAni; i++) animations.add(Animation.read(p, in, this));
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public String getType(){return TYPE;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public static AnimationSet create(Resource parent, MapEditor e, Project p) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,"New Animation"+"."+EXT);
		AnimationSet ret = new AnimationSet(f,e); ret.newId(p); ret._setName(null); ret.properties();
		if(!((Properties)ret.properties).updated) throw new Exception();
		ret.addToProject(p,false); return ret;
	}
	
	private static class Properties extends TypedResource.Properties implements MouseListener, ListSelectionListener, ChangeListener, DragList.Listener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String ADD = "add", REM = "rem";
		private JSpinner width, height; private JLabel image_thumb; private JList animations; private ImageResource image;
		public Properties(AnimationSet ani){super(ani, "Animation Set Properties");}
		public void addControls(JPanel settings){
			AnimationSet ani = (AnimationSet)resource;
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Dimensions"));
			width = new JSpinner(new SpinnerNumberModel(ani.width, 1, Short.MAX_VALUE, 1)); width.addChangeListener(this);
			inner.add(width); inner.add(new JLabel(" X ")); height = new JSpinner(new SpinnerNumberModel(ani.height, 1, Short.MAX_VALUE, 1));
			height.addChangeListener(this); inner.add(height);
			settings.add(inner); inner = new JPanel(new BorderLayout()); JPanel p = new JPanel(new BorderLayout());
			p.setBorder(BorderFactory.createTitledBorder("Animations"));
			animations = new JList(); animations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			animations.addMouseListener(this); animations.addListSelectionListener(this); new DragList(animations, Animation.class, this);
			JScrollPane pane = new JScrollPane(animations); pane.setPreferredSize(new Dimension(150,120));
			p.add(pane, BorderLayout.CENTER); JButton add = new JButton("+"); add.setActionCommand(ADD);
			add.addActionListener(this); JButton rem = new JButton("-"); rem.setActionCommand(REM);
			rem.addActionListener(this); JPanel inner2 = new JPanel(new GridLayout(1,2)); inner2.add(add);
			inner2.add(rem); p.add(inner2, BorderLayout.SOUTH); inner.add(p, BorderLayout.WEST);
			image_thumb = new JLabel(new ImageIcon());
			JPanel panel = new JPanel(new BorderLayout()); panel.setBorder(BorderFactory.createTitledBorder("Preview"));
			pane = new JScrollPane(image_thumb); pane.setPreferredSize(ImageResource.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); panel.add(pane, BorderLayout.CENTER); inner2 = new JPanel();
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner2.add(set);
			JButton clear = new JButton("Clear"); clear.setActionCommand(MapEditor.CLEAR); clear.addActionListener(this); inner2.add(clear);
			panel.add(inner2, BorderLayout.SOUTH); inner.add(panel, BorderLayout.CENTER); settings.add(inner);
		}
		public void updateControls(){
			AnimationSet ani = (AnimationSet)resource; image = ani.image;
			width.setValue(ani.width); height.setValue(ani.height); DefaultListModel m = new DefaultListModel();
			for(Animation i : ani.animations) m.addElement(i); animations.setModel(m);
			updateCache(); if(ani.animations.size() > 0) animations.setSelectedIndex(0);
		}
		public void acceptControls(){
			AnimationSet ani = (AnimationSet)resource;
			ani.image = image; ani.animations.clear(); ListModel m = animations.getModel();
			for(int i=0; i<m.getSize(); i++) ani.animations.add((Animation)m.getElementAt(i));
			ani.width = (Integer)width.getValue(); ani.height = (Integer)height.getValue();
		}
		public boolean saveOnEdit(){return true;}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == REM){
				int i = animations.getSelectedIndex(); if(i != -1) ((DefaultListModel)animations.getModel()).removeElementAt(i);
			} else if(command == ADD){
				Animation ani = editAnimation(null); if(ani != null){
					DefaultListModel m = (DefaultListModel)animations.getModel(); int sz = m.getSize();
					m.addElement(ani); animations.setSelectedIndex(sz);
				}
			} else if(command == MapEditor.SET){
				Project p = resource.getProject();
				if(p == null){JOptionPane.showMessageDialog(this, "Animation Set is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageResource im = ImageResource.choose(p, image);
				if(im != null){image = im; updateCache();}
			} else if(command == MapEditor.CLEAR){
				image = null; updateCache();
			} else super.actionPerformed(e);
		}
		public void mouseClicked(MouseEvent e){
			if(e.getClickCount() == 2){
				int idx = ((JList)e.getSource()).getSelectedIndex(); if(idx == -1) return;
				Object o = ((JList)e.getSource()).getSelectedValue();
				Animation ani = editAnimation((Animation)o); if(ani != null){
					DefaultListModel m = (DefaultListModel)animations.getModel();
					m.setElementAt(ani, idx); updatePreview();
				}
			}
		}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
		public void valueChanged(ListSelectionEvent e){
			if(!e.getValueIsAdjusting()) updatePreview();
		}
		public void stateChanged(ChangeEvent e){updateCache();}
		private static final Color trans_black = new Color(0x88000000,true), trans_white = new Color(0xAAffffff,true); 
		private void updateCache(){
			int w = (Integer)width.getValue(), h = (Integer)height.getValue(); BufferedImage b;
			if(image == null){
				b = new BufferedImage(TilesetViewer.TILE_SIZE*w, TilesetViewer.TILE_SIZE*h, BufferedImage.TYPE_INT_ARGB);
				Graphics g = b.getGraphics(); g.setColor(Color.white); g.fillRect(0, 0, b.getWidth(), b.getHeight());
			} else {
				BufferedImage im = image.getImage(); ColorModel cm = im.getColorModel();
				b = new BufferedImage(cm, im.copyData(null), cm.isAlphaPremultiplied(), null);
			} int iw = b.getWidth()/w, ih = b.getHeight()/h; Graphics g = b.getGraphics();
			g.setFont(g.getFont().deriveFont(Font.BOLD, 16)); g.setColor(trans_black);
			for(int y=0; y<h; y++)
				for(int x=0; x<w; x++){
					g.drawRect(x*iw, y*ih, iw-1, ih-1); String i = Integer.toString(y*w+x+1); FontMetrics f = g.getFontMetrics();
					int sw = f.stringWidth(i), sx = x*iw+((iw-sw)>>1), sy = y*ih+ih-20;
					g.setColor(trans_white); g.fillRect(sx-2, sy, sw+4, 18);
					g.setColor(trans_black); g.drawString(i, sx, sy+16);
				}
			((AnimationSet)resource).cache = b; updatePreview();
		}
		private void updatePreview(){
			AnimationSet ani = (AnimationSet)resource;
			Animation a = (Animation)animations.getSelectedValue();
			if(a == null || a.numFrames() == 0) image_thumb.setIcon(new ImageIcon(ani.cache));
			else image_thumb.setIcon(new Animation.Icon(image_thumb, a, ani.cache, (Integer)width.getValue(), (Integer)height.getValue()));
		}
		
		private AnimationEdit ani_edit;
		private Animation editAnimation(Animation i){
			if(ani_edit == null) ani_edit = new AnimationEdit(this, ((AnimationSet)resource));
			ani_edit.show(i, (Integer)width.getValue(), (Integer)height.getValue()); if(ani_edit.updated) return ani_edit.get(); else return null;
		}
		public void updateDrag(){}
		public Object paste(Object o){
			Animation clipboard = (Animation)o; String name = clipboard.getName(); int copy = 0; boolean changed = true;
			while(changed){
				changed = false; ListModel m = animations.getModel(); for(int i=0; i<m.getSize(); i++){
					Animation a = (Animation)m.getElementAt(i);
					if(a.getName().equals(name) && a.getDir() == clipboard.getDir()){
						copy++; name = clipboard.getName()+" copy"+((copy==1)?"":" "+copy); changed = true; break;
					}
				}
			} return new Animation(name, (byte)clipboard.getDir(), clipboard.speed, ((AnimationSet)resource), clipboard.getFramesList()); 
		}
	}
	private static class AnimationEdit extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated; private final JTextField frames; private Animation cur; private ButtonGroup speed;
		private final JComboBox name, dir; private final FrameSelector image_thumb; private AnimationSet animation;
		public AnimationEdit(JDialog d, AnimationSet ani){
			super(JOptionPane.getFrameForComponent(d), "Animation", true); animation = ani; setResizable(true);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JComboBox(Animation.default_ani); name.setEditable(true); JTextField t = (JTextField)name.getEditor().getEditorComponent();
			t.setActionCommand(MapEditor.OK); t.addActionListener(this); inner.add(name);
			dir = new JComboBox(Animation.dirs); dir.setEditable(false); inner.add(dir); settings.add(inner);
			inner = new JPanel(new BorderLayout()); inner.setBorder(BorderFactory.createTitledBorder("Select Frames"));
			image_thumb = new FrameSelector(this, new ImageIcon());
			JScrollPane pane = new JScrollPane(image_thumb); pane.setPreferredSize(new Dimension(350,350));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane, BorderLayout.CENTER);
			settings.add(inner); inner = new JPanel(new BorderLayout()); inner.setBorder(BorderFactory.createTitledBorder("Frames"));
			frames = new JTextField(); inner.add(frames, BorderLayout.CENTER); settings.add(inner);
			speed = new ButtonGroup(); inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Animation Speed"));
			JRadioButton r = new JRadioButton("Slowest"); r.setActionCommand("5"); speed.add(r); inner.add(r);
			r = new JRadioButton("Slower"); r.setActionCommand("4"); speed.add(r); inner.add(r);
			r = new JRadioButton("Slow"); r.setActionCommand("3"); speed.add(r); inner.add(r);
			r = new JRadioButton("Normal"); r.setSelected(true); r.setActionCommand("2"); speed.add(r); inner.add(r);
			r = new JRadioButton("Fast"); r.setActionCommand("1"); speed.add(r); inner.add(r); settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void show(Animation i, int w, int h){
			updated = false; cur = i; if(i != null){name.setSelectedItem(i.getName()); dir.setSelectedIndex(i.getDir()); frames.setText(i.getFrames());}
			else {name.setSelectedItem(Animation.default_ani[0]); dir.setSelectedIndex(0); frames.setText("");}
			JTextField t = (JTextField)name.getEditor().getEditorComponent(); t.requestFocus(); t.selectAll();
			if(i != null){
				Enumeration<AbstractButton> e = speed.getElements();
				String sp = Byte.toString(i.speed); while(e.hasMoreElements()){
					AbstractButton r = e.nextElement(); if(r.getActionCommand().equals(sp)) r.setSelected(true);
				}
			} image_thumb.set(new ImageIcon(animation.cache),w,h); setVisible(true);
		}
		public Animation get(){
			return new Animation(name.getSelectedItem().toString(), (byte)dir.getSelectedIndex(),
					Byte.parseByte(speed.getSelection().getActionCommand()), animation, frames.getText());
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				ListModel m = ((Properties)animation.properties).animations.getModel(); for(int i=0; i<m.getSize(); i++){
					Animation a = (Animation)m.getElementAt(i);
					if(cur != a && a.getName().equals(name.getSelectedItem().toString()) && a.getDir() == dir.getSelectedIndex()){
						JOptionPane.showMessageDialog(this, "The animation "+a+" already exists!", "Unable to Add Animation", JOptionPane.ERROR_MESSAGE); return;
					}
				} updated = true; setVisible(false);
			} else setVisible(false);
		}
	}
	private static class FrameSelector extends JLabel implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = -9201011643596250228L;
		private int lastFrame, w, h, iw, ih; private AnimationEdit edit;
		public FrameSelector(AnimationEdit e, Icon i){
			super(i); setHorizontalAlignment(JLabel.LEFT); setVerticalAlignment(JLabel.TOP);
			addMouseListener(this); addMouseMotionListener(this); edit = e;
		}
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){mouseDragged(e);}
		public void mouseReleased(MouseEvent e){lastFrame = -1;}
		public void mouseDragged(MouseEvent e){
			if(getIcon() == null) return; int x = e.getX()/iw, y = e.getY()/ih, f = y*w+x;
			if(x < 0 || x >= w || y < 0 || y >= h || f == lastFrame) return; lastFrame = f;
			String t = edit.frames.getText(); int len = t.length(); t = t.trim(); StringBuilder b = new StringBuilder();
			if(t.length() > 0 && !t.endsWith(",")) b.append(","); b.append(f+1);
			try{edit.frames.getDocument().insertString(len, b.toString(), null);}catch(Exception ex){ex.printStackTrace();}
		}
		public void mouseMoved(MouseEvent e){}
		public void set(Icon i, int _w, int _h){
			lastFrame = -1; setIcon(i); w = _w; h = _h; iw = i.getIconWidth()/w; ih = i.getIconHeight()/h;
		}
	}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Animation Files", AnimationSet.EXT, AnimationSet.TYPE, AnimationSet.class);
		Folder.new_options.addMenu("Sprite", "chr_appearance").
			addItem("Animation Set", "chr_appearance", new CreateAnimationAction());
	}
	private static class CreateAnimationAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addResource(AnimationSet.class);
		}
	}
	
	public static AnimationSet choose(Resource root, Resource selected){
		ResourceChooser c = new ResourceChooser(root, selected, FILTER);
		c.setVisible(true); return (AnimationSet)c.getSelectedResource();
	}
	private static class AFilter extends JPanel implements Filter {
		private static final long serialVersionUID = 907354882348925575L;
		private DefaultListModel model;
		public AFilter(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); model = new DefaultListModel(); JList list = new JList(model);
			list.setEnabled(false); JScrollPane pane = new JScrollPane(list); pane.setPreferredSize(new Dimension(150,120));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); add(new JLabel("Animation List:")); add(pane);
		}
		public boolean filter(Resource r){String ext = r.getExt(); return ext == null || ext == EXT;}
		private void reset(){model.clear();}
		public JPanel getPreview(){reset(); return this;}
		public boolean showPreview(Resource r){
			reset(); if(r.getExt() == null) return false;
			for(Animation a : ((AnimationSet)r).animations) model.addElement(a);
			return true;
		}
	} public static final Filter FILTER = new AFilter();
}
