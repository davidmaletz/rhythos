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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mrpg.editor.Filter;
import mrpg.editor.MapEditor;
import mrpg.editor.ResourceChooser;

public abstract class ImageResource extends TypedResource {
	private static final long serialVersionUID = 8050024073339824076L;
	public static Dimension THUMB_SIZE = new Dimension(150,150);
	public static final String ICON = "image";
	private static final Icon icon = MapEditor.getIcon(ICON);
	protected ImageResource(File f, MapEditor e){super(f,e);}
	public Icon getIcon(){return icon;}
	public abstract BufferedImage getImage();
	public static int getSize(ImageResource r){return (r==null)?2:10+r.getType().length();}
	public static void write(DataOutputStream out, ImageResource r) throws Exception {
		if(r == null) out.writeUTF(""); else {out.writeUTF(r.getType()); out.writeLong(r.getId());}
	}
	public static ImageResource read(DataInputStream in, Project p) throws Exception {
		String type = in.readUTF(); if(type.length() == 0) return null;
		long id = in.readLong(); try{return (ImageResource)p.getById(type, id);}catch(Exception e){return null;}
	}
	public static boolean isImage(Resource r){return r instanceof ImageResource;}
	
	public static ImageResource choose(Resource root, Resource selected){
		ResourceChooser c = new ResourceChooser(root, selected, FILTER);
		c.setVisible(true); return (ImageResource)c.getSelectedResource();
	}
	private static class IFilter extends JPanel implements Filter {
		private static final long serialVersionUID = 907354882348925575L;
		private JLabel image_thumb, size;
		public IFilter(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); image_thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(image_thumb); pane.setPreferredSize(ImageResource.THUMB_SIZE);
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); add(pane);
			size = new JLabel("Dimensions: 0 x 0"); size.setPreferredSize(new Dimension(150,20)); add(size);
		}
		public boolean filter(Resource r){return r.getExt() == null || ImageResource.isImage(r);}
		private void reset(){image_thumb.setIcon(new ImageIcon()); size.setText("Dimensions: 0 x 0");}
		public JPanel getPreview(){reset(); return this;}
		public boolean showPreview(Resource r){
			if(r.getExt() == null){reset(); return false;} 
			BufferedImage im = ((ImageResource)r).getImage(); image_thumb.setIcon(new ImageIcon(im));
			size.setText("Dimensions: "+im.getWidth()+" x "+im.getHeight());
			return true;
		}
	} public static final Filter FILTER = new IFilter();
}
