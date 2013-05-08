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
package mrpg.display;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.Timer;

import mrpg.editor.MapEditor;
import mrpg.world.Cell;
import mrpg.world.Tile;
import mrpg.world.World;



public class WorldPanel extends JPanel implements ActionListener, Scrollable {
	private static final long serialVersionUID = 6141752899325918664L;
	private final Rectangle rect = new Rectangle(); public Overlay overlay = null;
	private World world = null; public int left = 0, top = 0, bgleft = 0, bgtop = 0; private int width, height;
	private int frame_num = 0; private Timer timer; private boolean show_grid = false; private double scale = 1;
	private int edit_level = 0, show_level = -1; private static final Color transparent = new Color(0, 0, 0, 80);
	public WorldPanel(int _width, int _height){worldSize(_width, _height); timer = new Timer(100, this);}
	public void startAnim(){timer.start();}
	public void worldSize(int _width, int _height){
		width = _width; height = _height;
		Dimension size = new Dimension(width,height);
		if(show_grid){size.width += 1; size.height += 1;}
		size.width *= scale; size.height *= scale;
		setPreferredSize(size); setMinimumSize(size);
		revalidate(); repaint();
	}
	public void setScale(double s){if(scale != s){scale = s; worldSize(width, height);}}
	public double getScale(){return scale;}
	public int getEditLevel(){return edit_level;}
	public void setEditLevel(int level){edit_level = level;}
	public void showLevel(int level){if(level != show_level){show_level = level; repaint();}}
	public void setWorld(World w){world = w; repaint();}
	public World getWorld(){return world;}
	public void setShowGrid(boolean b){show_grid = b; worldSize(width, height);}
	public void paint(Graphics g){
		if(scale != 1){
			g = g.create();
			((Graphics2D)g).scale(scale, scale);
		}
		g.getClipBounds(rect);
		if(rect.x+rect.width > width) rect.width = width-rect.x; if(rect.y+rect.height > height) rect.height = height-rect.y;
		g.clearRect(rect.x, rect.y, rect.width, rect.height);
		if(world == null) return;
		if(!world.wrapX){left = Math.min(world.getWidth()*Tile.tile_size-width, Math.max(0, left));}
		if(!world.wrapY){top = Math.min(world.getHeight()*Tile.tile_size-height, Math.max(0, top));}
		if(world.background != null) renderBG(g);
		renderCells(g);
		if(show_grid){
			g.setColor(Color.black);
			int rw = rect.x+rect.width, rh = rect.y+rect.height;
			for(int y=(int)Math.ceil(rect.y*1.0/Tile.tile_size)*Tile.tile_size; y<=rh; y+=Tile.tile_size)
				g.drawLine(rect.x, y, rw-1, y);
			for(int x=(int)Math.ceil(rect.x*1.0/Tile.tile_size)*Tile.tile_size; x<=rw; x+=Tile.tile_size)
				g.drawLine(x, rect.y, x, rh);
		}
		if(overlay != null) overlay.paintOverlay(g, rect);
	}
	private void lowerLevel(Graphics g, int dx, int dy, int w, int h){
		Color old = g.getColor(); g.setColor(transparent);
		g.fillRect(dx, dy, w, h);
		g.setColor(old);
	}
	private final void renderCell(Graphics g, int x, int y, int dx, int dy, int sx, int sy, int w, int h){
		Cell c = world.getCell(x,y);
		if(c == null || !c.hasTiles()){
			if(show_level >= 0) lowerLevel(g, dx, dy, w, h);
			return;
		}
		Composite com = null;
		if(show_level >= 0) com = ((Graphics2D)g).getComposite();
		int level = 0;
		for(Tile tile : c){
			if(level == show_level) lowerLevel(g, dx, dy, w, h);
			tile.paint(g, frame_num, dx, dy, sx, sy, w, h, this);
			if(level == show_level) ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			level++;
		}
		if(com != null) ((Graphics2D)g).setComposite(com);
		if(level <= show_level) lowerLevel(g, dx, dy, w, h);
	}
	private final void renderCellClamped(Graphics g, int x, int y, int dx, int dy, int sx, int sy, int w, int h){
		w = Math.min(rect.x+rect.width-dx, w); if(w <= 0) return;
		h = Math.min(rect.y+rect.height-dy, h); if(h <= 0) return;
		renderCell(g, x, y, dx, dy, sx, sy, w, h);
	}
	private final void renderCells(Graphics g){
		int stx = (int)Math.floor(((double)(rect.x+left))/Tile.tile_size),
			sty = (int)Math.floor(((double)(rect.y+top))/Tile.tile_size),
			endx = (int)Math.floor(((double)(rect.x+rect.width+left))/Tile.tile_size),
			endy = (int)Math.floor(((double)(rect.y+rect.height+top))/Tile.tile_size);
		int dl = rect.x-(stx*Tile.tile_size-left);
		int dr = rect.x+rect.width-(endx*Tile.tile_size-left);
		int dt = rect.y-(sty*Tile.tile_size-top);
		int db = rect.y+rect.height-(endy*Tile.tile_size-top);
		renderCellClamped(g, stx, sty, rect.x, rect.y, dl, dt, Tile.tile_size-dl, Tile.tile_size-dt);
		for(int x=stx+1; x<endx; x++)
			renderCellClamped(g, x, sty, x*Tile.tile_size-left, rect.y, 0, dt, Tile.tile_size, Tile.tile_size-dt);
		if(stx != endx) renderCellClamped(g, endx, sty, rect.x+rect.width-dr, rect.y, 0, dt, dr, Tile.tile_size-dt);
		for(int y=sty+1; y<endy; y++){
			renderCellClamped(g, stx, y, rect.x, y*Tile.tile_size-top, dl, 0, Tile.tile_size-dl, Tile.tile_size);
			for(int x=stx+1; x<endx; x++)
				renderCell(g, x, y, x*Tile.tile_size-left, y*Tile.tile_size-top, 0, 0, Tile.tile_size, Tile.tile_size);
			if(stx != endx) renderCellClamped(g, endx, y, rect.x+rect.width-dr, y*Tile.tile_size-top, 0, 0, dr, Tile.tile_size);
		}
		if(sty != endy){
			renderCellClamped(g, stx, endy, rect.x, rect.y+rect.height-db, dl, 0, Tile.tile_size-dl, db);
			for(int x=stx+1; x<endx; x++)
				renderCellClamped(g, x, endy, x*Tile.tile_size-left, rect.y+rect.height-db, 0, 0, Tile.tile_size, db);
			if(stx != endx) renderCellClamped(g, endx, endy, rect.x+rect.width-dr, rect.y+rect.height-db, 0, 0, dr, db);
		}
	}
	private final void renderBGClamped(Graphics g, int x, int y, int dx, int dy, int sx, int sy, int w, int h){
		w = Math.min(rect.x+rect.width-dx, w); if(w <= 0) return;
		h = Math.min(rect.y+rect.height-dy, h); if(h <= 0) return;
		g.drawImage(world.background, dx, dy, dx+w, dy+h, sx, sy, sx+w, sy+h, this);
	}
	private final void renderBG(Graphics g, int x, int y, int dx, int dy, int sx, int sy, int w, int h){
		g.drawImage(world.background, dx, dy, dx+w, dy+h, sx, sy, sx+w, sy+h, this);
	}
	private final void renderBG(Graphics g){
		int ww = world.background.getWidth(this), wh = world.background.getHeight(this);
		int stx = (int)Math.floor(((double)(rect.x+bgleft))/ww),
			sty = (int)Math.floor(((double)(rect.y+bgtop))/wh),
			endx = (int)Math.floor(((double)(rect.x+rect.width+bgleft))/ww),
			endy = (int)Math.floor(((double)(rect.y+rect.height+bgtop))/wh);
		int dl = rect.x-(stx*ww-bgleft);
		int dr = rect.x+rect.width-(endx*ww-bgleft);
		int dt = rect.y-(sty*wh-bgtop);
		int db = rect.y+rect.height-(endy*wh-bgtop);
		renderBGClamped(g, stx, sty, rect.x, rect.y, dl, dt, ww-dl, wh-dt);
		for(int x=stx+1; x<endx; x++)
			renderBGClamped(g, x, sty, x*ww-bgleft, rect.y, 0, dt, ww, wh-dt);
		if(stx != endx) renderBGClamped(g, endx, sty, rect.x+rect.width-dr, rect.y, 0, dt, dr, wh-dt);
		for(int y=sty+1; y<endy; y++){
			renderBGClamped(g, stx, y, rect.x, y*wh-bgtop, dl, 0, ww-dl, wh);
			for(int x=stx+1; x<endx; x++)
				renderBG(g, x, y, x*ww-bgleft, y*wh-bgtop, 0, 0, ww, wh);
			if(stx != endx) renderBGClamped(g, endx, y, rect.x+rect.width-dr, y*wh-bgtop, 0, 0, dr, wh);
		}
		if(sty != endy){
			renderBGClamped(g, stx, endy, rect.x, rect.y+rect.height-db, dl, 0, ww-dl, db);
			for(int x=stx+1; x<endx; x++)
				renderBGClamped(g, x, endy, x*ww-bgleft, rect.y+rect.height-db, 0, 0, ww, db);
			if(stx != endx) renderBGClamped(g, endx, endy, rect.x+rect.width-dr, rect.y+rect.height-db, 0, 0, dr, db);
		}
	}
	public void actionPerformed(ActionEvent e) {
		if(isDisplayable()){
			frame_num++;
			if(world != null) repaint();
		} if(!MapEditor.instance.isDisplayable()) timer.stop();
	}
	
	public int getFrame(){return frame_num;}
	
	public int getScrollableUnitIncrement(Rectangle r, int o, int d){return (int)Math.floor(Tile.tile_size*scale);}
	public int getScrollableBlockIncrement(Rectangle r, int o, int d){return (int)Math.floor(Tile.tile_size*scale);}
	public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
	public boolean getScrollableTracksViewportWidth(){return false;}
	public boolean getScrollableTracksViewportHeight(){return false;}
}
