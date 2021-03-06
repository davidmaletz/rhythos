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
package mrpg.editor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import mrpg.editor.resource.Animation;
import mrpg.editor.resource.AnimationSet;
import mrpg.world.Direction;
import mrpg.world.Tile;
import mrpg.world.Tilemap;


public class AutoTileEditor extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
	private static final long serialVersionUID = -2758279297055018804L;
	private Tilemap tilemap; private final Rectangle rect = new Rectangle(); private int edge=0, tile_size;
	private Animation ani; private int frame = 0, ct; private Timer timer;
	public AutoTileEditor(int ts){
		tile_size = ts; setPreferredSize(new Dimension(TilesetViewer.TILE_SIZE, TilesetViewer.TILE_SIZE));
		addMouseListener(this); addMouseMotionListener(this);
	}
	public void setTilemap(Tilemap t){
		tilemap = t; repaint();
	}
	public void setAnimation(AnimationSet animation, int aid){
		if(aid < 0 || animation == null || aid >= animation.numAnimations()) ani = null;
		else{ani = animation.getAnimation(aid); ct = ani.speed; if(timer == null) repaint();}
	}
	public void actionPerformed(ActionEvent e){
		if(ani == null || !isShowing()){timer.stop(); timer = null;} else{
			ct--; if(ct == 0){ct = ani.speed; frame++; if(frame >= ani.numFrames()) frame = 0; repaint();}
		}
	}
	public void paint(Graphics g){
		if(timer == null && ani != null && ani.numFrames() > 1){
			timer = new Timer(83, this); timer.start();
		} Graphics2D g2d = (Graphics2D)g;
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g.create(); double s = ((double)TilesetViewer.TILE_SIZE)/tile_size;
		g2.scale(s,s); g2.getClipBounds(rect); g2.clearRect(rect.x, rect.y, rect.width, rect.height);
		if(tilemap != null){
			BufferedImage image = (BufferedImage)tilemap.getTile(0).image;
			int w = Math.min(rect.x+rect.width, image.getWidth())-rect.x;
			int h = Math.min(rect.y+rect.height, image.getHeight())-rect.y;
			if(w > 0 && h > 0){int f = 0; if(ani != null) f = frame*ani.speed;
				tilemap.getTile(0).paint(g2, ani, f, rect.x, rect.y, rect.x, rect.y, w, h, this);
				g.getClipBounds(rect);
				int d = 4, first = (TilesetViewer.TILE_SIZE-(5*d))/2+1;
					byte walkable = tilemap.getTile(0).info.getWalkable();
					AffineTransform t = g2d.getTransform();
					g2d.translate(first+d*2-1, first-1);
					g.setColor((Direction.up(edge))?TilesetEditor.dirSelColor:TilesetEditor.dirColor);
					if(Direction.up(walkable)) g2d.fillPolygon(TilesetEditor.arrow); else g2d.fillRect(0, 0, d, d);
					g.setColor(TilesetEditor.dirColorOutline);
					if(Direction.up(walkable)) g2d.drawPolygon(TilesetEditor.arrow); else g2d.drawRect(0, 0, d, d);
					g2d.translate(d*2, d*2); g2d.rotate(Math.PI/2, 2, 2);
					g.setColor((Direction.right(edge))?TilesetEditor.dirSelColor:TilesetEditor.dirColor);
					if(Direction.right(walkable)) g2d.fillPolygon(TilesetEditor.arrow); else g2d.fillRect(0, 0, d, d);
					g.setColor(TilesetEditor.dirColorOutline);
					if(Direction.right(walkable)) g2d.drawPolygon(TilesetEditor.arrow); else g2d.drawRect(0, 0, d, d);
					g2d.translate(d*2, d*2); g2d.rotate(Math.PI/2, 2, 2);
					g.setColor((Direction.down(edge))?TilesetEditor.dirSelColor:TilesetEditor.dirColor);
					if(Direction.down(walkable)) g2d.fillPolygon(TilesetEditor.arrow); else g2d.fillRect(0, 0, d, d);
					g.setColor(TilesetEditor.dirColorOutline);
					if(Direction.down(walkable)) g2d.drawPolygon(TilesetEditor.arrow); else g2d.drawRect(0, 0, d, d);
					g2d.translate(d*2, d*2); g2d.rotate(Math.PI/2, 2, 2);
					g.setColor((Direction.left(edge))?TilesetEditor.dirSelColor:TilesetEditor.dirColor);
					if(Direction.left(walkable)) g2d.fillPolygon(TilesetEditor.arrow); else g2d.fillRect(0, 0, d, d);
					g.setColor(TilesetEditor.dirColorOutline);
					if(Direction.left(walkable)) g2d.drawPolygon(TilesetEditor.arrow); else g2d.drawRect(0, 0, d, d);
					g2d.setTransform(t);
			}
		}
	}
	
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {
		int dx = e.getX(), dy = e.getY();
		int d = 4, first = (TilesetViewer.TILE_SIZE-(5*d))/2+1;
		boolean inxc = dx >= first+d && dx <= first+d*3, inyc = dy >= first+d && dy <= first+d*3;
		int ed = 0;
		if(inxc && dy < first+d) ed = Direction.UP;
		if(inxc && dy > first+3*d) ed = Direction.DOWN;
		if(inyc && dx < first+d) ed = Direction.LEFT;
		if(inyc && dx > first+3*d) ed = Direction.RIGHT;
		if(inxc && inyc) ed = Direction.LINEAR;
		if(ed != edge){edge = ed; repaint();}
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if(tilemap == null) return;
		if(edge != 0){
			Tile.Info info = tilemap.getTile(0).info;
			info.setWalkable((byte)(info.getWalkable() ^ edge));
			repaint();
		}
	}
	public void mouseReleased(MouseEvent e) {}
}
