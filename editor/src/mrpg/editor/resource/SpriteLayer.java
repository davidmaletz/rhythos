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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.TreePath;

import com.jhlabs.image.ColorMatrix;
import com.jhlabs.image.ColorMatrixFilter;

import layout.SpringUtilities;
import mrpg.editor.DragList;
import mrpg.editor.ImageChooser;
import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;

public class SpriteLayer extends Resource {
	private static final long serialVersionUID = -5394199071824545816L;
	public static final String EXT = "spl", TYPE = "sl";
	private static final Icon icon = MapEditor.getIcon("chr_appearance");
	private final ArrayList<Img> images = new ArrayList<Img>(); private final ArrayList<Color> colors = new ArrayList<Color>();
	private final Properties properties; private long id;
	public SpriteLayer(File f, MapEditor editor){super(f, editor); properties = new Properties(this);}
	public long getId(){return id;}
	public void contextMenu(JPopupMenu menu){
		menu.add(editor.getBrowser().properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public boolean edit(){properties(); return true;}
	public void properties(){properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public Icon getIcon(){return icon;}
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeId(TYPE, this, id); super.remove(delete);
	}
	public void save() throws Exception {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getFile())));
		try{
			out.writeLong(id); out.writeShort(images.size()); out.writeShort(colors.size());
			for(Img i : images){out.writeUTF(i.name); out.writeLong((i.image==null)?0:i.image.getId());}
			for(Color c : colors){
				out.writeUTF(c.name); for(int i=0; i<20; i++) out.writeDouble(c.color.matrix[i]);
			} out.flush(); out.close();
		}catch(Exception e){out.close(); throw e;}
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_PROJECT);}
	public void deferredRead(File f) throws Exception{
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{
			Project p = WorkspaceBrowser.getProject(this);
			id = in.readLong(); short nImg = in.readShort(), nCol = in.readShort();
			images.clear(); colors.clear(); for(int i=0; i<nImg; i++){
				String n = in.readUTF(); long id = in.readLong(); Image img = null;
				if(id != 0) try{img = p.getImageById(id);}catch(Exception ex){}
				images.add(new Img(n, img));
			} for(int c=0; c<nCol; c++){
				String n = in.readUTF(); double m[] = new double[20];
				for(int i=0; i<20; i++) m[i] = in.readDouble();
				colors.add(new Color(n, new ColorMatrix(m)));
			} long i = p.setId(TYPE, this, id); if(i != id){id = i; save();}
		}catch(Exception e){in.close(); throw e;}
	}
	public static SpriteLayer create(Resource parent, MapEditor e, Project p) throws Exception {
		String dir = parent.getFile().toString();
		File f = new File(dir,"New Layer"+"."+EXT);
		SpriteLayer ret = new SpriteLayer(f,e); ret._setName(null); ret.id = p.newId(TYPE); ret.properties();
		if(!ret.properties.updated) throw new Exception();
		p.setId(TYPE, ret, ret.id); return ret;
	}
	
	private static class Properties extends JDialog implements ActionListener, MouseListener, ListSelectionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String OK = "ok", CANCEL = "cancel", ADDI = "addi", REMI = "remi", ADDC = "addc", REMC = "remc";
		public boolean updated;
		private final SpriteLayer chara; private final JTextField name, id;
		private final JLabel image_thumb; private final JList images, colors;
		public Properties(SpriteLayer chr){
			super(JOptionPane.getFrameForComponent(chr.editor), "Sprite Layer Properties", true); chara = chr;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			name = new JTextField(chr.getName(), 20); name.setActionCommand(OK); name.addActionListener(this);
			inner.add(name); JPanel p = new JPanel(); p.add(new JLabel("Id: "));
			id = new JTextField("", 15); id.setOpaque(false); id.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			id.setEditable(false); p.add(id); inner.add(p);
			settings.add(inner); inner = new JPanel(new GridLayout(1,2)); p = new JPanel(new BorderLayout());
			p.setBorder(BorderFactory.createTitledBorder("Images"));
			images = new JList(); images.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			images.addMouseListener(this); images.addListSelectionListener(this); new DragList(images, Img.class);
			JScrollPane pane = new JScrollPane(images); pane.setPreferredSize(new Dimension(100,120));
			p.add(pane, BorderLayout.CENTER); JButton addi = new JButton("+"); addi.setActionCommand(ADDI);
			addi.addActionListener(this); JButton remi = new JButton("-"); remi.setActionCommand(REMI);
			remi.addActionListener(this); JPanel inner2 = new JPanel(new GridLayout(1,2)); inner2.add(addi);
			inner2.add(remi); p.add(inner2, BorderLayout.SOUTH); inner.add(p); p = new JPanel(new BorderLayout());
			p.setBorder(BorderFactory.createTitledBorder("Colors"));
			colors = new JList(); colors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			colors.addMouseListener(this); colors.addListSelectionListener(this); new DragList(colors, Color.class);
			pane = new JScrollPane(colors); pane.setPreferredSize(new Dimension(100,120));
			p.add(pane, BorderLayout.CENTER); JButton addc = new JButton("+"); addc.setActionCommand(ADDC);
			addc.addActionListener(this); JButton remc = new JButton("-"); remc.setActionCommand(REMC);
			remc.addActionListener(this); inner2 = new JPanel(new GridLayout(1,2)); inner2.add(addc); inner2.add(remc);
			p.add(inner2, BorderLayout.SOUTH); inner.add(p); settings.add(inner);
			image_thumb = new JLabel(new ImageIcon());
			pane = new JScrollPane(image_thumb); pane.setPreferredSize(Image.THUMB_SIZE);
			pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Preview"),
					BorderFactory.createLoweredBevelBorder())); settings.add(pane);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean b){
			if(b == true){
				updated = false; name.setText(chara.getName()); name.requestFocus(); name.selectAll();
				DefaultListModel m = new DefaultListModel();
				for(Img i : chara.images) m.addElement(i); images.setModel(m);
				m = new DefaultListModel(); for(Color c : chara.colors) m.addElement(c); colors.setModel(m);
				if(chara.images.size() > 0) images.setSelectedIndex(0);
				if(chara.colors.size() > 0) colors.setSelectedIndex(0);
				id.setText(Long.toHexString(chara.id)); updatePreview();
			}
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == OK){
				chara.images.clear(); chara.colors.clear(); ListModel m = images.getModel();
				for(int i=0; i<m.getSize(); i++) chara.images.add((Img)m.getElementAt(i)); m = colors.getModel();
				for(int i=0; i<m.getSize(); i++) chara.colors.add((Color)m.getElementAt(i));
				try{
					chara.setName(name.getText());
				} catch(Exception ex){name.setText(chara.getName()); return;}
				try{chara.save(); chara.editor.updateSaveButtons(); updated = true;}catch(Exception ex){}
				setVisible(false);
			} else if(command == REMI){
				int i = images.getSelectedIndex(); if(i != -1) ((DefaultListModel)images.getModel()).removeElementAt(i);
			} else if(command == REMC){
				int i = colors.getSelectedIndex(); if(i != -1) ((DefaultListModel)colors.getModel()).removeElementAt(i);
			} else if(command == ADDI){
				Img img = editImage(null); if(img != null){
					DefaultListModel m = (DefaultListModel)images.getModel(); int sz = m.getSize();
					m.addElement(img); images.setSelectedIndex(sz);
				}
			} else if(command == ADDC){
				Color col = editColor(null); if(col != null){
					DefaultListModel m = (DefaultListModel)colors.getModel(); int sz = m.getSize();
					m.addElement(col); colors.setSelectedIndex(sz);
				} else updatePreview();
			} else setVisible(false);
		}
		public void mouseClicked(MouseEvent e){
			if(e.getClickCount() == 2){
				int idx = ((JList)e.getSource()).getSelectedIndex(); if(idx == -1) return;
				Object o = ((JList)e.getSource()).getSelectedValue();
				if(e.getSource() == images){
					Img img = editImage((Img)o); if(img != null){
						DefaultListModel m = (DefaultListModel)images.getModel();
						m.setElementAt(img, idx); updatePreview();
					}
				} else {
					Color col = editColor((Color)o); if(col != null){
						DefaultListModel m = (DefaultListModel)colors.getModel();
						m.setElementAt(col, idx); updatePreview();
					} else updatePreview();
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
		private void updatePreview(){
			Img i = (Img)images.getSelectedValue(); if(i == null || i.image == null){image_thumb.setIcon(new ImageIcon()); return;}
			Color color = (Color)colors.getSelectedValue(); ColorMatrix c = (color == null)?ColorMatrix.identity:color.color;
			updatePreview(i.image, c);
		}
		private void updatePreview(ColorMatrix c){
			Img i = (Img)images.getSelectedValue(); if(i == null || i.image == null){image_thumb.setIcon(new ImageIcon()); return;}
			updatePreview(i.image, c);
		}
		private void updatePreview(Image i, ColorMatrix c){
			BufferedImage b = new ColorMatrixFilter(c).filter(i.getImage(), null);
			image_thumb.setIcon(new ImageIcon(b));
		}
		
		private ImageEdit img_edit;
		private Img editImage(Img i){
			if(img_edit == null) img_edit = new ImageEdit(this, chara);
			img_edit.show(i); if(img_edit.updated) return img_edit.get(); else return null;
		}
		private ColorEdit color_edit;
		private Color editColor(Color c){
			if(color_edit == null) color_edit = new ColorEdit(this);
			color_edit.show(c); if(color_edit.updated) return color_edit.get(); else return null;
		}
	}
	private static class Img {
		public final String name; public final Image image;
		public Img(String n, Image i){name = n; image = i;}
		public String toString(){return name;}
	}
	private static class ImageEdit extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated; private SpriteLayer chara;
		private final JTextField name; private final JLabel image_thumb; private Image image;
		public ImageEdit(JDialog d, SpriteLayer chr){
			super(JOptionPane.getFrameForComponent(d), "Image", true); chara = chr; setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField("", 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name); settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Image"));
			image_thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(image_thumb); pane.setPreferredSize(Image.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); JPanel inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			JButton set = new JButton("Set"); set.setActionCommand(MapEditor.SET); set.addActionListener(this); inner2.add(set);
			JButton clear = new JButton("Clear"); clear.setActionCommand(MapEditor.CLEAR); clear.addActionListener(this); inner2.add(clear);
			inner.add(inner2);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void show(Img i){
			updated = false; if(i != null){name.setText(i.name); image = i.image;} else {name.setText("Image"); image = null;}
			name.requestFocus(); name.selectAll();
			if(image == null) image_thumb.setIcon(new ImageIcon()); else image_thumb.setIcon(new ImageIcon(image.getImage()));
			setVisible(true);
		}
		public Img get(){return new Img(name.getText(), image);}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				updated = true; setVisible(false);
			} else if(command == MapEditor.SET){
				Project p = WorkspaceBrowser.getProject(chara);
				if(p == null){
					WorkspaceBrowser b = chara.editor.getBrowser();
					TreePath path; if(b.isSelectionEmpty() && b.getRowCount() == 0){path = null;}
					else if(b.isSelectionEmpty()) path = b.getPathForRow(0);
					else path = b.getSelectionPath();
					if(path != null && path.getPathCount() > 1) p = (Project)path.getPathComponent(1);
				}
				if(p == null){JOptionPane.showMessageDialog(this, "Sprite Layer is not added to any project, no images to load...", "Cannot Find Images", JOptionPane.ERROR_MESSAGE); return;}
				ImageChooser c = new ImageChooser(p, image);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){image = im; image_thumb.setIcon(new ImageIcon(image.getImage()));}
			} else if(command == MapEditor.CLEAR){
				image = null; image_thumb.setIcon(new ImageIcon());
			} else setVisible(false);
		}
	}
	private static class Color {
		public final String name; public final ColorMatrix color;
		public Color(String n, ColorMatrix c){name = n; color = c;}
		public String toString(){return name;}
	}
	private static class ColorEdit extends JDialog implements ActionListener, TableModelListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated; private Properties props;
		private final JTextField name; private Matrix matrix;
		private static String HUE = "hue", ADD = "add", IDEN = "iden";
		public ColorEdit(Properties p){
			super(JOptionPane.getFrameForComponent(p), "Color", true); props = p; setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField("", 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name); settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Matrix"));
			matrix = new Matrix(4,5); matrix.getModel().addTableModelListener(this);
			matrix.setBorder(BorderFactory.createEtchedBorder()); inner.add(matrix); settings.add(inner);
			inner = new JPanel(new GridLayout(1,3)); inner.setBorder(BorderFactory.createTitledBorder("Adjust"));
			JButton b = new JButton("Hue/Saturation..."); b.setActionCommand(HUE); b.addActionListener(this); inner.add(b);
			b = new JButton("Add/Multiply..."); b.setActionCommand(ADD); b.addActionListener(this); inner.add(b);
			b = new JButton("Reset Transform"); b.setActionCommand(IDEN); b.addActionListener(this); inner.add(b);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void show(Color c){
			updated = false; if(c != null){name.setText(c.name); matrix.setMatrix(c.color.matrix);}
			else {name.setText("Color"); matrix.setMatrix(ColorMatrix.identity.matrix);}
			name.requestFocus(); name.selectAll();
			setVisible(true);
		}
		public Color get(){return new Color(name.getText(), new ColorMatrix(matrix.getMatrix()));}
		private HueEdit hue_edit; private AddMulEdit add_edit;
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				updated = true; setVisible(false);
			} else if(command == HUE){
				if(hue_edit == null) hue_edit = new HueEdit(this); hue_edit.setVisible(true);
				if(hue_edit.updated){
					if(hue_edit.matrix != null) matrix.setMatrix(hue_edit.matrix.matrix); 
				} else tableChanged(null);
			} else if(command == ADD){
				if(add_edit == null) add_edit = new AddMulEdit(this); add_edit.setVisible(true);
				if(add_edit.updated){
					if(add_edit.matrix != null) matrix.setMatrix(add_edit.matrix.matrix); 
				} else tableChanged(null);
			} else if(command == IDEN){
				matrix.setMatrix(ColorMatrix.identity.matrix);
			} else setVisible(false);
		}
		public void tableChanged(TableModelEvent e) {
			props.updatePreview(new ColorMatrix(matrix.getMatrix()));
		}
	}
	
	private static class HueEdit extends JDialog implements ActionListener, ChangeListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public boolean updated; private ColorEdit edit;
		private final JSlider hue, sat, lum, contrast; private ColorMatrix matrix;
		private static void setup(JSlider s, int t){
			s.setMinorTickSpacing(t); s.setMajorTickSpacing(t);
			s.setPaintLabels(true); s.setPaintTicks(true);
		}
		public HueEdit(ColorEdit e){
			super(JOptionPane.getFrameForComponent(e), "Adjust Color", true); edit = e; setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(new SpringLayout()); inner.setBorder(BorderFactory.createTitledBorder("Adjust"));
			inner.add(new JLabel("Hue: "));
			hue = new JSlider(-180, 180, 0); hue.addChangeListener(this); setup(hue, 180); inner.add(hue);
			inner.add(new JLabel("Saturation: "));
			sat = new JSlider(-100, 100, 0); sat.addChangeListener(this); setup(sat, 100); inner.add(sat);
			inner.add(new JLabel("Brightness: "));
			lum = new JSlider(-100, 100, 0); lum.addChangeListener(this); setup(lum, 100); inner.add(lum);
			inner.add(new JLabel("Contrast: "));
			contrast = new JSlider(-100, 100, 0); contrast.addChangeListener(this); setup(contrast, 100); inner.add(contrast);
			SpringUtilities.makeCompactGrid(inner, 4, 2, 3, 3, 3, 3); settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean v){
			if(v){updated = false; hue.setValue(0); sat.setValue(0); lum.setValue(0); contrast.setValue(0);}
			super.setVisible(v);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				updated = true; setVisible(false);
			} else setVisible(false);
		}
		public void stateChanged(ChangeEvent e) {
			matrix = new ColorMatrix(edit.matrix.getMatrix());
			matrix.adjustColor(lum.getValue(), contrast.getValue(), sat.getValue(), hue.getValue());
			edit.props.updatePreview(matrix);
		}
	}
	
	private static class ColorIcon implements Icon {
		private java.awt.Color color;
		public ColorIcon(int r, int g, int b){color = new java.awt.Color(r,g,b);}
		public ColorIcon(java.awt.Color c){color = c;}
		public int getIconHeight(){return 32;}
		public int getIconWidth() {return 32;}
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color); g.fillRect(x+1, y+1, 30, 30);
			g.setColor(java.awt.Color.black); g.drawRect(x, y, 32, 32);
		}
		
	}
	private static class AddMulEdit extends JDialog implements ActionListener, ChangeListener {
		private static final long serialVersionUID = -4987880557990107307L;
		public static final String MUL = "mul";
		public boolean updated; private ColorEdit edit; private final JButton add, mul;
		private final JSlider add_power, mul_power; private ColorMatrix matrix;
		public AddMulEdit(ColorEdit e){
			super(JOptionPane.getFrameForComponent(e), "Adjust Color", true); edit = e; setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(new SpringLayout()); inner.setBorder(BorderFactory.createTitledBorder("Adjust"));
			inner.add(new JLabel("Add Color: ")); add = new JButton(new ColorIcon(0,0,0));
			add.setActionCommand(ColorEdit.ADD); add.addActionListener(this); inner.add(add);
			inner.add(new JLabel("Add Power: "));
			add_power = new JSlider(-100, 100, 0); add_power.addChangeListener(this); HueEdit.setup(add_power, 100); inner.add(add_power);
			inner.add(new JLabel("Mul Color: "));
			mul = new JButton(new ColorIcon(255,255,255));
			mul.setActionCommand(MUL); mul.addActionListener(this); inner.add(mul);
			inner.add(new JLabel("Mul Power: "));
			mul_power = new JSlider(0, 100, 100); mul_power.addChangeListener(this); HueEdit.setup(mul_power, 100); inner.add(mul_power);
			SpringUtilities.makeCompactGrid(inner, 4, 2, 3, 3, 3, 3); settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean v){
			if(v){
				updated = false; add.setIcon(new ColorIcon(0,0,0)); add_power.setValue(0);
				mul.setIcon(new ColorIcon(255,255,255)); mul_power.setValue(100);
			} super.setVisible(v);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				updated = true; setVisible(false);
			} else if(command == ColorEdit.ADD){
				java.awt.Color c = JColorChooser.showDialog(this, "Add Color", ((ColorIcon)add.getIcon()).color);
				if(c != null){add.setIcon(new ColorIcon(c)); stateChanged(null);}
			} else if(command == MUL){
				java.awt.Color c = JColorChooser.showDialog(this, "Mul Color", ((ColorIcon)mul.getIcon()).color);
				if(c != null){mul.setIcon(new ColorIcon(c)); stateChanged(null);}
			} else setVisible(false);
		}
		public void stateChanged(ChangeEvent e) {
			java.awt.Color a = ((ColorIcon)add.getIcon()).color, m = ((ColorIcon)mul.getIcon()).color;
			double ap = add_power.getValue()*0.01, mp = mul_power.getValue()*0.01/255;
			matrix = new ColorMatrix(new double[]{
					1+(m.getRed()-255)*mp,0,0,0,a.getRed()*ap,
					0,1+(m.getGreen()-255)*mp,0,0,a.getGreen()*ap,
					0,0,1+(m.getBlue()-255)*mp,0,a.getBlue()*ap,
					0,0,0,1,0});
			matrix.concat(new ColorMatrix(edit.matrix.getMatrix()).matrix);
			edit.props.updatePreview(matrix);
		}
	}
	
	public static void register(){
		Resource.register("Sprite Layer Files", SpriteLayer.EXT, SpriteLayer.class);
		Folder.new_options.addMenu("Sprite", "chr_appearance").
			addItem("Layer", "chr_appearance", new CreateCharaAction());
	}
	private static class CreateCharaAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().addResource(SpriteLayer.class);
		}
	}
}
