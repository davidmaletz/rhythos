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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;

public class CroppedImage extends ImageResource {
	private static final long serialVersionUID = -5394199071824545816L;
	public static final String EXT = "cimg", TYPE = "ci"; private static final short VERSION=1;
	private ImageResource image; private int x = 0, y = 0, w, h; private BufferedImage cache;
	public CroppedImage(File f, MapEditor editor){super(f, editor);}
	public String getType(){return TYPE;}
	public short getVersion(){return VERSION;}
	public JDialog getProperties(){return new Properties(this);}
	public BufferedImage getImage(){
		if(cache == null){
			cache = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			cache.getGraphics().drawImage(image.getImage(), -x, -y, null);
		} return cache;
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
		ImageResource.write(out, image); out.writeShort(x); out.writeShort(y); out.writeShort(w); out.writeShort(h);
	}
	public void readInner(DataInputStream in) throws Exception {
		Project p = WorkspaceBrowser.getProject(this); image = ImageResource.read(in, p);
		if(image == null) throw new Exception();
		x = in.readShort(); y = in.readShort(); w = in.readShort(); h = in.readShort();
	}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_IMG_RESOURCE);}
	
	public static CroppedImage create(ImageResource im) {
		try{
			Resource parent = im.getParent(); String dir = parent.getFile().toString();
			File f = new File(dir,"Cropped "+im.getName()+"."+EXT);
			CroppedImage ret = new CroppedImage(f,im.editor); ret._setName(null); ret.image = im;
			ret.x = 0; ret.y = 0; BufferedImage img = ret.image.getImage(); ret.w = img.getWidth(); ret.h = img.getHeight();
			Project p = WorkspaceBrowser.getProject(im); ret.properties();
			if(!((Properties)ret.properties).updated) throw new Exception();
			ret.addToProject(p,false); im.editor.getBrowser().addResource(ret, parent); return ret;
		}catch(Exception e){return null;}
	}
	public static CroppedImage create(Project p, ImageResource im, String name, int x, int y, int w, int h){return create(p, im, im.getParent(), name, x, y, w, h);}
	public static CroppedImage create(Project p, ImageResource im, Resource parent, String name, int x, int y, int w, int h){
		try{
			String dir = parent.getFile().toString(); File f = new File(dir,name+"."+EXT); if(f.exists()) throw new Exception();
			CroppedImage ret = new CroppedImage(f,im.editor); ret.newId(p);
			ret.image = im; ret.x = x; ret.y = y; ret.w = w; ret.h = h;
			ret.addToProject(p, false); im.editor.getBrowser().addResource(ret, parent); ret.save(); return ret;
		}catch(Exception e){return null;}
	}

	private static class Properties extends TypedResource.Properties {
		private static final long serialVersionUID = -4987880557990107307L;
		private ImageCropper cropper; private ImageResource img; private JComboBox lock;
		public Properties(CroppedImage i){super(i, "Cropped Image Properties", true);}
		public void addControls(JPanel settings){
			JPanel inner = new JPanel(new BorderLayout()); inner.setBorder(BorderFactory.createTitledBorder("Image"));
			cropper = new ImageCropper();
			JScrollPane pane = new JScrollPane(cropper); pane.setPreferredSize(new Dimension(400, 350));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane, BorderLayout.CENTER);
			JPanel p2 = new JPanel(); p2.add(new JLabel("Lock to Grid: "));
			lock = new JComboBox(new String[]{"1","2","4","8","16","32","64"}); lock.addActionListener(this);
			lock.setEditable(true); p2.add(lock); p2.add(new JLabel("  ")); JButton b = new JButton("Set");
			b.setActionCommand(MapEditor.SET); b.addActionListener(this); p2.add(b); inner.add(p2, BorderLayout.SOUTH);
			settings.add(inner);
		}
		public void updateControls(){
			CroppedImage image = (CroppedImage)resource;
			img = image.image; cropper.setImage(img.getImage(), image.x, image.y, image.w, image.h);
		}
		public void acceptControls(){
			CroppedImage image = (CroppedImage)resource; image.image = img; image.x = cropper.x1; image.y = cropper.y1;
			image.w = cropper.x2-cropper.x1; image.h = cropper.y2-cropper.y1; image.cache = null;
		}
		public boolean saveOnEdit(){return true;}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(e.getSource() == lock){
				try{cropper.lock = Integer.parseInt(lock.getSelectedItem().toString());}catch(Exception ex){lock.setSelectedItem(Integer.toString(cropper.lock));}
			} if(command == MapEditor.SET){
				ImageResource im = ImageResource.choose(WorkspaceBrowser.getProject(img), img);
				if(im != null && im != resource){img = im; cropper.setImage(img.getImage());}
			} else super.actionPerformed(e);
		}
	}
	public String getExt(){return EXT;}
	
	private static final Color blue = new Color(0xb8cfe5), blue_trans = new Color(0x33b8cfe5, true);
	private static final BasicStroke stroke = new BasicStroke(2);
	private static final int NONE=0, INSIDE=1, LEFT=2, RIGHT=3, UP=4, BOTTOM=5, UP_LEFT=6, BOTTOM_LEFT=7, UP_RIGHT=8, BOTTOM_RIGHT=9, SZ=8, SZ2 = SZ*2;
	private static class ImageCropper extends JPanel implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = -7395640251248933309L;
		private int x1, y1, x2, y2; private BufferedImage image; private int mX, mY, pX1, pY1, pX2, pY2, type, lock = 1;
		public ImageCropper(){setOpaque(false); addMouseListener(this); addMouseMotionListener(this);}
		public void setImage(BufferedImage i){
			image = i; x1 = Math.min(image.getWidth()-1, x1); y1 = Math.min(image.getHeight()-1, y1);
			x2 = Math.min(image.getWidth(), Math.max(x2, x1+SZ2)); y2 = Math.min(image.getHeight(), Math.max(y2, y1+SZ2));
			setPreferredSize(new Dimension(i.getWidth(), i.getHeight())); revalidate(); repaint();
		}
		public void setImage(BufferedImage i, int _x, int _y, int _w, int _h){
			image = i; x1 = _x; y1 = _y; x2 = x1+_w; y2 = y1+_h; setPreferredSize(new Dimension(i.getWidth(), i.getHeight()));
		}
		public void mouseDragged(MouseEvent e){
			int dX = e.getX()-mX, dY = e.getY()-mY;
			switch(type){
			case INSIDE:
				x1 = Math.max(Math.min(image.getWidth()-(pX2-pX1), pX1+dX), 0); y1 = Math.max(Math.min(image.getHeight()-(pY2-pY1), pY1+dY), 0);
				x2 = x1+(pX2-pX1); y2 = y1+(pY2-pY1); repaint(); break;
			case LEFT: x1 = Math.min(x2-SZ2, Math.max(pX1+dX, 0)); repaint(); break;
			case RIGHT: x2 = Math.max(Math.min(image.getWidth(), pX2+dX), x1+SZ2); repaint(); break;
			case UP: y1 = Math.min(y2-SZ2, Math.max(pY1+dY, 0)); repaint(); break;
			case BOTTOM: y2 = Math.max(y1+SZ2, Math.min(image.getHeight(), pY2+dY)); repaint(); break;
			case UP_LEFT: x1 = Math.min(x2-SZ2, Math.max(pX1+dX, 0)); y1 = Math.min(y2-SZ2, Math.max(pY1+dY, 0)); repaint(); break;
			case BOTTOM_LEFT: x1 = Math.min(x2-SZ2, Math.max(pX1+dX, 0)); y2 = Math.max(y1+SZ2, Math.min(image.getHeight(), pY2+dY)); repaint(); break;
			case UP_RIGHT: x2 = Math.max(Math.min(image.getWidth(), pX2+dX), x1+SZ2); y1 = Math.min(y2-SZ2, Math.max(pY1+dY, 0)); repaint(); break;
			case BOTTOM_RIGHT: x2 = Math.max(Math.min(image.getWidth(), pX2+dX), x1+SZ2); y2 = Math.max(y1+SZ2, Math.min(image.getHeight(), pY2+dY)); repaint(); break;
			} if(lock != 1){
				x1 = (x1/lock)*lock; x2 = Math.max(x1+lock, (x2/lock)*lock);
				y1 = (y1/lock)*lock; y2 = Math.max(y1+lock, (y2/lock)*lock);
			}
		}
		public void mouseMoved(MouseEvent e){
			mX = e.getX(); mY = e.getY(); int l = x1+SZ, u = y1+SZ, r = x2-SZ-1, b = y2-SZ-1;
			if(mX < x1-SZ || mY < y1-SZ || mX > x2+SZ-1 || mY > y2+SZ-1) type = NONE;
			else if(mX < l && mY < u) type = UP_LEFT; else if(mX < l) type = (mY > b)?BOTTOM_LEFT:LEFT;
			else if(mY < u) type = (mX > r)?UP_RIGHT:UP; else if(mX > r && mY > b) type = BOTTOM_RIGHT;
			else if(mX > r) type = RIGHT; else if(mY > b) type = BOTTOM; else type = INSIDE;
			switch(type){
			case NONE: setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); break;
			case INSIDE: setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); break;
			case LEFT: setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)); break;
			case RIGHT: setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)); break;
			case UP: setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)); break;
			case BOTTOM: setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)); break;
			case UP_LEFT: setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)); break;
			case BOTTOM_LEFT: setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR)); break;
			case UP_RIGHT: setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)); break;
			case BOTTOM_RIGHT: setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)); break;
			}
		}
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){
			if(lock != 1){
				x1 = (x1/lock)*lock; x2 = Math.max(x1+lock, (x2/lock)*lock);
				y1 = (y1/lock)*lock; y2 = Math.max(y1+lock, (y2/lock)*lock); repaint();
			} mX = e.getX(); mY = e.getY(); pX1 = x1; pY1 = y1; pX2 = x2; pY2 = y2; mouseMoved(e);
		}
		public void mouseReleased(MouseEvent e){}
		
		public void paint(Graphics g){
			g.drawImage(image, 0, 0, this); ((Graphics2D)g).setStroke(stroke);
			g.setColor(blue_trans); g.fillRect(x1+1, y1+1, x2-x1-2, y2-y1-2); g.setColor(blue); g.drawRect(x1+1,y1+1,x2-x1-2,y2-y1-2);
		}
	}
}
