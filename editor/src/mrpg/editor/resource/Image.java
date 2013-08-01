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
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import mrpg.editor.MapEditor;
import mrpg.export.Export;
import mrpg.export.Graphic;


public class Image extends ImageResource implements ActionListener {
	private static final long serialVersionUID = -5394199071824545816L;
	public static final String EXT = "mimg"; private static final short VERSION=1;
	private Graphic graphic;
	public Image(File f, MapEditor editor){super(f, editor);}
	public String getType(){return Export.IMAGE;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public BufferedImage getImage(){try{return graphic.getBufferedImage();}catch(Exception e){} return null;}
	private JMenuItem crop_image = MapEditor.createMenuItemIcon("Crop Image", ImageResource.ICON, this);
	public void contextMenu(JPopupMenu menu){
		menu.add(editor.getBrowser().properties); menu.add(crop_image); menu.addSeparator();
	}
	public void actionPerformed(ActionEvent e){CroppedImage.create(this);}
	public Graphic getGraphic(){return graphic;}
	public void writeInner(DataOutputStream out) throws Exception {graphic.write(out);}
	public void readInner(DataInputStream in) throws Exception {graphic = new Graphic(in);}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_MEDIA);}

	public static Image createImage(Graphic g, File f, MapEditor e, Project p) throws Exception {
		Image ret = new Image(f, e); ret.newId(p);
		ret.graphic = g; ret.save(); ret.addToProject(p, false); return ret;
	}
	public static Image importImage(File img, File f, MapEditor e, Project p) throws Exception {
		return createImage(Graphic.decode(img), f, e, p);
	}
	private static class Properties extends TypedResource.Properties {
		private static final long serialVersionUID = -4987880557990107307L;
		private JLabel dim, thumb;
		public Properties(Image i){super(i, "Image Properties");}
		public void addControls(JPanel settings){
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Dimensions"));
			dim = new JLabel("0 x 0"); inner.add(dim);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Thumbnail"));
			thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(thumb); pane.setPreferredSize(ImageResource.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane);
			settings.add(inner);
		}
		public void updateControls(){
			Image image = (Image)resource; BufferedImage im = image.getImage();
			dim.setText(im.getWidth()+" x "+im.getHeight());
			thumb.setIcon(new ImageIcon(image.getImage()));
		}
	}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Image Files", Image.EXT, Export.IMAGE, Image.class);
		Folder.import_options.addItem("Image File", "image", KeyEvent.VK_I, ActionEvent.CTRL_MASK, new ImportImageAction());
		Resource.register("Cropped Image Files", CroppedImage.EXT, CroppedImage.TYPE, CroppedImage.class);
	}
	private static class ImportImageAction implements ActionListener {
		public void actionPerformed(ActionEvent e){
			MapEditor.instance.getBrowser().importImages();
		}
	}
}
