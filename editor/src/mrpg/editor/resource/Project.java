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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mrpg.editor.ImageChooser;
import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;


public class Project extends Folder {
	private static final long serialVersionUID = -8656579697414666933L;
	public static JFileChooser folderChooser = new JFileChooser();
	static {
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		folderChooser.setDialogTitle("Choose Project Directory"); 
	}
	public static final String PROJECT = "project";
	private static final Icon icon = MapEditor.getIcon(PROJECT);
	private Properties properties; private String target; private Image frame, font, bg; private ArrayList<String> options;
	public static Project createProject(MapEditor e) throws Exception {
		File f; if(folderChooser.showSaveDialog(MapEditor.instance) == JFileChooser.APPROVE_OPTION){
			f = folderChooser.getSelectedFile(); if(!f.exists() && !f.mkdirs()) throw new Exception();
		} else throw new Exception();
		if(f.listFiles().length > 0) throw new Exception();
		Project p = new Project(f, e); p.target = MapEditor.defaultTarget().getName(); p.options = new ArrayList<String>();
		p.save(); return p;
	}
	public static Project openProject(MapEditor e, Workspace w) throws Exception {
		File f; if(folderChooser.showOpenDialog(MapEditor.instance) == JFileChooser.APPROVE_OPTION){
			f = folderChooser.getSelectedFile(); if(!f.exists() && !f.mkdirs()) throw new Exception();
		} else throw new Exception(); return openProject(e,w,f);
	}
	public static Project openProject(MapEditor e, Workspace w, File f) throws Exception {
		File prop = new File(f.toString()+File.separator+".project"); if(!prop.exists()) throw new Exception();
		for(int i=0; i<w.getProjectCount(); i++) if(w.getProject(i).getFile().equals(f)){
			JOptionPane.showMessageDialog(MapEditor.instance, "The selected project is already open!", "Cannot Open Project", JOptionPane.ERROR_MESSAGE);
			throw new Exception();
		} Project p = new Project(f, e); p.read(f); return p;
	}
	public Project(File f, MapEditor e) throws Exception {super(f, e);}
	public boolean canDelete(){return false;}
	public void properties(){if(properties == null) properties = new Properties(this); properties.setVisible(true);}
	public void save() throws Exception {
		File f = new File(getFile().toString()+File.separator+".project");
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		try{
			out.writeUTF(target); if(frame == null) out.writeLong(0); else out.writeLong(frame.getId());
			if(font == null) out.writeLong(0); else out.writeLong(font.getId());
			if(bg == null) out.writeLong(0); else out.writeLong(bg.getId()); int sz = options.size();
			out.write(sz); for(int i=0; i<sz; i++) out.writeUTF(options.get(i)); out.flush(); out.close();
		}catch(Exception e){out.close(); throw e;}
	}
	protected void read(File f) throws Exception {super.read(f); MapEditor.deferRead(this, MapEditor.DEF_TILEMAP);}
	public void deferredRead(File _f) throws Exception {
		File f = new File(getFile().toString()+File.separator+".project");
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{
			target = in.readUTF(); try{frame = getImageById(in.readLong());}catch(Exception e){frame = null;}
			try{font = getImageById(in.readLong());}catch(Exception e){font = null;}
			try{bg = getImageById(in.readLong());}catch(Exception e){bg = null;} int sz = in.read();
			options = new ArrayList<String>(sz); for(int i=0; i<sz; i++) options.add(in.readUTF()); in.close();
		}catch(Exception e){in.close(); throw e;}
	}
	public boolean hasProperties(){return true;}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(browser.properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public Icon getIcon(){return icon;}
	
	private static Random random;
	private static long newId(HashMap<Long,Resource> table){
		if(random == null) random = new Random();
		long l; do{l = random.nextLong();}while(table.containsKey(l)); return l;
	}
	private static long setId(HashMap<Long,Resource> table, Resource r, long id) throws Exception {
		if(r == null) throw new Exception(); Resource old = table.get(id); if(r == old) return id;
		if(old != null) id = newId(table); table.put(id, r); return id;
	}
	private static void removeId(HashMap<Long,Resource> table, Resource r, long id) throws Exception {
		if(r == null) return; Resource old = table.get(id); if(r == old) table.remove(id);
	}
	private static Resource getById(HashMap<Long,Resource> table, long id) throws Exception {
		Resource r = table.get(id); if(r == null) throw new Exception(); else return r;
	}
	private HashMap<Long,Resource> images = new HashMap<Long,Resource>();
	public long newImageId(){return newId(images);}
	public long setImageId(Image r, long id) throws Exception {return setId(images, r, id);}
	public void removeImageId(Image r, long id) throws Exception {removeId(images, r, id);}
	public Image getImageById(long id) throws Exception {return (Image)getById(images, id);}
	private HashMap<Long,Resource> media = new HashMap<Long,Resource>();
	public long newMediaId(){return newId(media);}
	public long setMediaId(Media r, long id) throws Exception {return setId(media, r, id);}
	public void removeMediaId(Media r, long id) throws Exception {removeId(media, r, id);}
	public Media getMediaById(long id) throws Exception {return (Media)getById(media, id);}
	private HashMap<Long,Resource> tilemaps = new HashMap<Long,Resource>();
	public long newTilemapId(){return newId(tilemaps);}
	public long setTilemapId(TileResource r, long id) throws Exception {return setId(tilemaps, r, id);}
	public void removeTilemapId(TileResource r, long id) throws Exception {removeId(tilemaps, r, id);}
	public TileResource getTilemapById(long id) throws Exception {return (TileResource)getById(tilemaps, id);}
	private HashMap<Long,Resource> maps = new HashMap<Long,Resource>();
	public long newMapId(){return newId(maps);}
	public long setMapId(Map r, long id) throws Exception {return setId(maps, r, id);}
	public void removeMapId(Map r, long id) throws Exception {removeId(maps, r, id);}
	public Map getMapById(long id) throws Exception {return (Map)getById(maps, id);}
	
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private final Project project; private final JTextField name; private final JComboBox target;
		private final JLabel frame_thumb, font_thumb, bg_thumb; private Image frame, font, bg;
		private final JTextArea options;
		private static final String SET_FRAME="set_frame", SET_FONT="set_font", SET_BG="set_bg";
		public Properties(Project p){
			super(JOptionPane.getFrameForComponent(p.editor), "Project Properties", true); project = p;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField(project.getName(), 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Target"));
			target = new JComboBox(); inner.add(target);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Frame"));
			frame_thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(frame_thumb); pane.setPreferredSize(new Dimension(150,32));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); JPanel inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			JButton set = new JButton("Set"); set.setActionCommand(SET_FRAME); set.addActionListener(this); inner2.add(set);
			inner.add(inner2);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Font"));
			font_thumb = new JLabel(new ImageIcon());
			pane = new JScrollPane(font_thumb); pane.setPreferredSize(new Dimension(150,32));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			set = new JButton("Set"); set.setActionCommand(SET_FONT); set.addActionListener(this); inner2.add(set);
			inner.add(inner2);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("BG"));
			bg_thumb = new JLabel(new ImageIcon());
			pane = new JScrollPane(bg_thumb); pane.setPreferredSize(new Dimension(150,115));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			set = new JButton("Set"); set.setActionCommand(SET_BG); set.addActionListener(this); inner2.add(set);
			inner.add(inner2);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Options"));
			options = new JTextArea(5,20); inner.add(new JScrollPane(options));
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		@SuppressWarnings("unchecked")
		public void setVisible(boolean b){
			if(b == true){
				name.setText(project.getName()); name.requestFocus(); name.selectAll();
				Object[] targets = MapEditor.getTargetsArray(); String data[] = new String[targets.length];
				int selected = 0; for(int i=0; i<targets.length; i++){
					String n = ((Class)targets[i]).getName(); if(n.equals(project.target)) selected = i; data[i] = n.substring(n.lastIndexOf('.')+1);
				} target.setModel(new DefaultComboBoxModel(data)); target.setSelectedIndex(selected);
				frame = project.frame;
				if(frame == null) frame_thumb.setIcon(new ImageIcon()); else frame_thumb.setIcon(new ImageIcon(frame.getImage()));
				font = project.font;
				if(font == null) font_thumb.setIcon(new ImageIcon()); else font_thumb.setIcon(new ImageIcon(font.getImage()));
				bg = project.bg;
				if(bg == null) bg_thumb.setIcon(new ImageIcon()); else bg_thumb.setIcon(new ImageIcon(bg.getImage()));
				StringBuilder buf = new StringBuilder(); for(String o : project.options){buf.append(o); buf.append('\n');}
				options.setText(buf.toString());
			}
			super.setVisible(b);
		}
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				project.target = ((Class)MapEditor.getTargetsArray()[target.getSelectedIndex()]).getName();
				project.frame = frame; project.font = font; project.bg = bg;
				String[] o = options.getText().split("\n");
				project.options.clear(); for(int i=0; i<o.length; i++) project.options.add(o[i]);
				try{
					project.setName(name.getText());
				}catch(Exception ex){}
				try{project.save();}catch(Exception ex){}
				setVisible(false);
			} else if(command == SET_FRAME){
				ImageChooser c = new ImageChooser(project, frame);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){
					BufferedImage b = im.getImage(); if(b.getWidth() != 12 || b.getHeight() != 12)
						JOptionPane.showMessageDialog(this, "Frame images must be 12x12!", "Bad Image Dimensions", JOptionPane.ERROR_MESSAGE);
					else {frame = im; frame_thumb.setIcon(new ImageIcon(frame.getImage()));}
				}
			} else if(command == SET_FONT){
				ImageChooser c = new ImageChooser(project, font);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){
					BufferedImage b = im.getImage(); if(b.getWidth() < 792 || Math.floor(b.getWidth()*0.125) != b.getWidth()*0.125 || b.getHeight() != 8)
						JOptionPane.showMessageDialog(this, "Font images must be 8 pixels high, and >= 792 pixels wide (divisible by 8)!", "Bad Image Dimensions", JOptionPane.ERROR_MESSAGE);
					else {font = im; font_thumb.setIcon(new ImageIcon(font.getImage()));}
				}
			} else if(command == SET_BG){
				ImageChooser c = new ImageChooser(project, bg);
				c.setVisible(true);
				Image im = c.getSelectedImage();
				if(im != null){
					BufferedImage b = im.getImage(); if(b.getWidth() != 200 || b.getHeight() != 150)
						JOptionPane.showMessageDialog(this, "Background images must be 200x150!", "Bad Image Dimensions", JOptionPane.ERROR_MESSAGE);
					else {bg = im; bg_thumb.setIcon(new ImageIcon(bg.getImage()));}
				}
			} else setVisible(false);
		}
	}
}
