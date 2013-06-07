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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.export.Graphic;


public class Image extends Resource {
	private static final long serialVersionUID = -5394199071824545816L;
	public static final String EXT = "mimg";
	private static final Icon icon = MapEditor.getIcon(WorkspaceBrowser.IMAGE_ICON);
	private final Properties properties; private Graphic graphic; private long id;
	public Image(File f, MapEditor editor){super(f, editor); properties = new Properties(this);}
	public long getId(){return id;}
	public BufferedImage getImage(){try{return graphic.getBufferedImage();}catch(Exception e){} return null;}
	public void contextMenu(JPopupMenu menu){
		menu.add(editor.getBrowser().properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public boolean edit(){properties(); return true;}
	public void properties(){properties.setVisible(true);}
	public boolean hasProperties(){return true;}
	public Icon getIcon(){return icon;}
	public void remove(boolean delete) throws Exception {
		WorkspaceBrowser.getProject(this).removeImageId(this, id); super.remove(delete);
	}
	public Graphic getGraphic(){return graphic;}
	public void save() throws Exception {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getFile())));
		try{
			out.writeLong(id); graphic.write(out);
		}catch(Exception e){out.close(); throw e;}
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_MEDIA);}
	public void deferredRead(File f) throws Exception{
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{
			id = in.readLong(); graphic = new Graphic(in); long i = WorkspaceBrowser.getProject(this).setImageId(this, id);
			if(i != id){id = i; save();}
		}catch(Exception e){in.close(); throw e;}
	}
	
	public static Image importImage(File img, File f, MapEditor e, Project p) throws Exception {
		Graphic g = Graphic.decode(img); Image ret = new Image(f, e); ret.id = p.newImageId();
		ret.graphic = g; ret.save(); p.setImageId(ret, ret.id); return ret;
	}
	public static Dimension THUMB_SIZE = new Dimension(150,150);
	
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private static final String OK = "ok", CANCEL = "cancel";
		private final Image image; private final JTextField name, id; private final JLabel dim, thumb;
		public Properties(Image i){
			super(JOptionPane.getFrameForComponent(i.editor), "Image Properties", true); image = i;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name")); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
			name = new JTextField(image.getName(), 20); name.setActionCommand(OK); name.addActionListener(this);
			inner.add(name); JPanel p = new JPanel(); p.add(new JLabel("Id: "));
			id = new JTextField("", 15); id.setOpaque(false); id.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			id.setEditable(false); p.add(id); inner.add(p);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Dimensions"));
			dim = new JLabel("0 x 0"); inner.add(dim);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Thumbnail"));
			thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(thumb); pane.setPreferredSize(Image.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane);
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean b){
			if(b == true){
				name.setText(image.getName()); name.requestFocus(); name.selectAll();
				id.setText(Long.toHexString(image.id)); BufferedImage im = image.getImage();
				dim.setText(im.getWidth()+" x "+im.getHeight());
				thumb.setIcon(new ImageIcon(image.getImage()));
			}
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == OK){
				try{
					image.setName(name.getText());
				} catch(Exception ex){}
			}
			setVisible(false);
		}
	}
}
