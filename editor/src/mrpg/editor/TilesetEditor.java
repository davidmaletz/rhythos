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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import mrpg.world.BasicTilemap;
import mrpg.world.Direction;
import mrpg.world.Tile;


public class TilesetEditor extends JPanel implements MouseListener, MouseMotionListener, Scrollable {
	private static final long serialVersionUID = -2758279297055018804L;
	public static Color dirColor = new Color(255,255,255,200), dirSelColor = new Color(250,248,160,200), dirColorOutline = new Color(0,0,0,200);
	public static final Polygon arrow = new Polygon(new int[]{0,0,-3,2,7,4,4}, new int[]{6,2,2,-4,2,2,6}, 7);
	private BasicTilemap tilemap; private final Rectangle rect = new Rectangle(); private int selX, selY, edge=0, tile_size;
	public TilesetEditor(int ts){tile_size = ts; setPreferredSize(new Dimension(8*TilesetViewer.TILE_SIZE+1, 8*TilesetViewer.TILE_SIZE+1)); addMouseListener(this); addMouseMotionListener(this);}
	public void setTilemap(BasicTilemap t){
		tilemap = null; Dimension d;
		if(t == null) d = new Dimension(8*TilesetViewer.TILE_SIZE+1, 8*TilesetViewer.TILE_SIZE+1);
		else {tilemap = t; d = new Dimension(t.getTilesX()*TilesetViewer.TILE_SIZE+1, t.getTilesY()*TilesetViewer.TILE_SIZE+1);}
		setMinimumSize(d); setPreferredSize(d);
		revalidate(); repaint();
	}
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g; Graphics2D g2 = (Graphics2D)g.create(); double s = ((double)TilesetViewer.TILE_SIZE)/tile_size;
		g2.scale(s,s); g2.getClipBounds(rect); g2.clearRect(rect.x, rect.y, rect.width, rect.height);
		if(tilemap != null){
			BufferedImage image = (BufferedImage)tilemap.getTile(0).image;
			int w = Math.min(rect.x+rect.width, image.getWidth())-rect.x;
			int h = Math.min(rect.y+rect.height, image.getHeight())-rect.y;
			if(w > 0 && h > 0){
				g2.drawImage(image, rect.x, rect.y, rect.x+w, rect.y+h, rect.x, rect.y, rect.x+w, rect.y+h, this);
				g.getClipBounds(rect);
				int stx = (int)Math.floor(((double)(rect.x))/TilesetViewer.TILE_SIZE),
				sty = (int)Math.floor(((double)(rect.y))/TilesetViewer.TILE_SIZE),
				endx = Math.min(tilemap.getTilesX()-1, (int)Math.floor(((double)(rect.x+w*s))/TilesetViewer.TILE_SIZE)),
				endy = Math.min(tilemap.getTilesY()-1, (int)Math.floor(((double)(rect.y+h*s))/TilesetViewer.TILE_SIZE));
				int d = 4, first = (TilesetViewer.TILE_SIZE-(5*d))/2+1;
				for(int y=sty; y<=endy; y++)
					for(int x=stx; x<=endx; x++){
						int e = 0;
						if(selX == x && selY == y) e = edge;
						byte walkable = tilemap.getTile(x, y).info.getWalkable();
						AffineTransform t = g2d.getTransform();
						g2d.translate(x*TilesetViewer.TILE_SIZE+first+d*2-1, y*TilesetViewer.TILE_SIZE+first-1);
						g.setColor((Direction.up(e))?dirSelColor:dirColor);
						if(Direction.up(walkable)) g2d.fillPolygon(arrow); else g2d.fillRect(0, 0, d, d);
						g.setColor(dirColorOutline);
						if(Direction.up(walkable)) g2d.drawPolygon(arrow); else g2d.drawRect(0, 0, d, d);
						g2d.translate(d*2, d*2); g2d.rotate(Math.PI/2, 2, 2);
						g.setColor((Direction.right(e))?dirSelColor:dirColor);
						if(Direction.right(walkable)) g2d.fillPolygon(arrow); else g2d.fillRect(0, 0, d, d);
						g.setColor(dirColorOutline);
						if(Direction.right(walkable)) g2d.drawPolygon(arrow); else g2d.drawRect(0, 0, d, d);
						g2d.translate(d*2, d*2); g2d.rotate(Math.PI/2, 2, 2);
						g.setColor((Direction.down(e))?dirSelColor:dirColor);
						if(Direction.down(walkable)) g2d.fillPolygon(arrow); else g2d.fillRect(0, 0, d, d);
						g.setColor(dirColorOutline);
						if(Direction.down(walkable)) g2d.drawPolygon(arrow); else g2d.drawRect(0, 0, d, d);
						g2d.translate(d*2, d*2); g2d.rotate(Math.PI/2, 2, 2);
						g.setColor((Direction.left(e))?dirSelColor:dirColor);
						if(Direction.left(walkable)) g2d.fillPolygon(arrow); else g2d.fillRect(0, 0, d, d);
						g.setColor(dirColorOutline);
						if(Direction.left(walkable)) g2d.drawPolygon(arrow); else g2d.drawRect(0, 0, d, d);
						g2d.setTransform(t);
					}
			} else g.getClipBounds(rect);
		} else g.getClipBounds(rect);
		g.setColor(Color.black);
		Dimension dim = getPreferredSize();
		int rw = Math.min(dim.width, rect.x+rect.width), rh = Math.min(dim.height, rect.y+rect.height);
		for(int y=(int)Math.ceil(rect.y*1.0/TilesetViewer.TILE_SIZE)*TilesetViewer.TILE_SIZE; y<rh; y+=TilesetViewer.TILE_SIZE)
			g.drawLine(rect.x, y, rw-1, y);
		for(int x=(int)Math.ceil(rect.x*1.0/TilesetViewer.TILE_SIZE)*TilesetViewer.TILE_SIZE; x<rw; x+=TilesetViewer.TILE_SIZE)
			g.drawLine(x, rect.y, x, rh-1);
	}
	
	public int getScrollableUnitIncrement(Rectangle r, int o, int d){return TilesetViewer.TILE_SIZE;}
	public int getScrollableBlockIncrement(Rectangle r, int o, int d){return TilesetViewer.TILE_SIZE;}
	public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
	public boolean getScrollableTracksViewportWidth(){return false;}
	public boolean getScrollableTracksViewportHeight(){return false;}
	
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {
		int x = e.getX()/TilesetViewer.TILE_SIZE, y = e.getY()/TilesetViewer.TILE_SIZE, dx = e.getX()-x*TilesetViewer.TILE_SIZE, dy = e.getY()-y*TilesetViewer.TILE_SIZE;
		int d = 4, first = (TilesetViewer.TILE_SIZE-(5*d))/2+1;
		boolean inxc = dx >= first+d && dx <= first+d*3, inyc = dy >= first+d && dy <= first+d*3;
		int ed = 0;
		if(inxc && dy < first+d) ed = Direction.UP;
		if(inxc && dy > first+3*d) ed = Direction.DOWN;
		if(inyc && dx < first+d) ed = Direction.LEFT;
		if(inyc && dx > first+3*d) ed = Direction.RIGHT;
		if(inxc && inyc) ed = Direction.LINEAR;
		if(x != selX || y != selY || ed != edge){repaint(selX*TilesetViewer.TILE_SIZE,selY*TilesetViewer.TILE_SIZE,TilesetViewer.TILE_SIZE,TilesetViewer.TILE_SIZE); selX = x; selY = y; edge = ed; repaint(selX*TilesetViewer.TILE_SIZE,selY*TilesetViewer.TILE_SIZE,TilesetViewer.TILE_SIZE,TilesetViewer.TILE_SIZE);}
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if(tilemap == null) return;
		if(selX >= 0 && selX < tilemap.getTilesX() && selY >= 0 && selY < tilemap.getTilesY() && edge != 0){
			Tile.Info info = tilemap.getTile(selX, selY).info;
			info.setWalkable((byte)(info.getWalkable() ^ edge));
			repaint(selX*TilesetViewer.TILE_SIZE,selY*TilesetViewer.TILE_SIZE,TilesetViewer.TILE_SIZE,TilesetViewer.TILE_SIZE);
		}
	}
	public void mouseReleased(MouseEvent e) {}
}
