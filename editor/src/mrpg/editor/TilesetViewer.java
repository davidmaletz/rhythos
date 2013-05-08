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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import mrpg.editor.resource.Project;
import mrpg.world.BasicTilemap;
import mrpg.world.Direction;
import mrpg.world.Tile;
import mrpg.world.Tilemap;


public class TilesetViewer extends JPanel implements MouseListener, MouseMotionListener, Scrollable, Iterable<Tile> {
	private static final long serialVersionUID = -782835855502107652L;
	private final Rectangle rect = new Rectangle();
	private BasicTilemap mainMap = null; private ArrayList<Tilemap> extraTiles = null;
	private int selX1 = 0, selY1 = 0, selX2 = 0, selY2 = 0; private boolean show_grid = false;
	public static final Color selectColor1 = new Color(250,248,160), selectColor2 = new Color(192,237,254);
	public WorldOverlay overlay = null;
	public TilesetViewer(){addMouseListener(this); addMouseMotionListener(this);}
	private void computeSize(){
		Dimension d = new Dimension(Tile.tile_size*8, 0);
		if(mainMap != null){
			d.width = mainMap.getTilesX()*Tile.tile_size;
			d.height += mainMap.getTilesY()*Tile.tile_size;
		}
		if(extraTiles != null){
			d.height += (int)Math.ceil(extraTiles.size()*1.0/(d.width/Tile.tile_size))*Tile.tile_size;
		}
		if(show_grid){d.width += 1; d.height += 1;}
		setMinimumSize(d); setPreferredSize(d);
		revalidate(); repaint();
	}
	public void setShowGrid(boolean b){show_grid = b; computeSize();}
	private Project project = null;
	public void setProject(Project p){
		if(project != p){
			if(project != null && p != null){selX1 = 0; selY1 = 0; selX2 = 0; selY2 = 0; mainMap = null; extraTiles = null; computeSize();}
			project = p;
		}
	}
	public void refresh(){
		if(project == null) return; try{
			if(mainMap != null) mainMap = (BasicTilemap)project.getTilemapById(mainMap.getId()).getTilemap();
			if(extraTiles != null) for(int i=0; i<extraTiles.size(); i++) extraTiles.set(i, project.getTilemapById(extraTiles.get(i).getId()).getTilemap());
			computeSize();
		} catch(Exception e){}
	}
	public void setTilemap(BasicTilemap map, Project p){
		if(mainMap == map || (project != null && project != p)) return; selX1 = 0; selY1 = 0; selX2 = 0; selY2 = 0; mainMap = map; computeSize();
	}
	public boolean removeTilemap(BasicTilemap map){
		if(mainMap == map){selX1 = 0; selY1 = 0; selX2 = 0; selY2 = 0; mainMap = null; computeSize(); return true;} else return false;
	}
	public BasicTilemap getTilemap(){return mainMap;}
	public void toggleAutoTile(Tilemap m, Project p){
		if(!m.indexNeighbors() || (project != null && project != p)) return;
		if(extraTiles == null) extraTiles = new ArrayList<Tilemap>();
		int idx = extraTiles.indexOf(m); if(idx != -1) extraTiles.remove(idx); else extraTiles.add(m); computeSize();
	}
	public void addAutoTile(Tilemap m, Project p){
		if(!m.indexNeighbors() || (project != null && project != p)) return;
		if(extraTiles == null) extraTiles = new ArrayList<Tilemap>();
		extraTiles.add(m); computeSize();
	}
	public boolean removeAutoTile(Tilemap m){
		if(extraTiles == null) return false; boolean ret = extraTiles.remove(m); computeSize(); return ret;
	}
	public void paint(Graphics g){
		g.getClipBounds(rect);
		g.clearRect(rect.x, rect.y, rect.width, rect.height);
		int ht = 0;
		if(mainMap != null){
			ht = mainMap.getTilesY()*Tile.tile_size;
			int w = Math.min(rect.x+rect.width, mainMap.getTilesX()*Tile.tile_size)-rect.x;
			int h = Math.min(rect.y+rect.height, ht)-rect.y;
			if(w > 0 && h > 0)
				g.drawImage(mainMap.getTile(0,0).image, rect.x, rect.y, rect.x+w, rect.y+h, rect.x, rect.y, rect.x+w, rect.y+h, this);
		}
		if(extraTiles != null){
			int width = getPreferredSize().width/Tile.tile_size;
			int stx = Math.max(0, (int)Math.floor(((double)(rect.x))/Tile.tile_size)),
			sty = Math.max(0, (int)Math.floor(((double)(rect.y-ht))/Tile.tile_size)),
			endx = Math.min(width-1, (int)Math.floor(((double)(rect.x+rect.width))/Tile.tile_size)),
			endy = Math.min((int)Math.ceil(extraTiles.size()*1.0/width)-1, (int)Math.floor(((double)(rect.y+rect.height-ht))/Tile.tile_size));
			for(int y=sty; y<=endy; y++)
				for(int x=stx; x<=endx; x++){
					int i = y*width+x; if(i >= extraTiles.size()) continue;
					int _x = x*Tile.tile_size, _y = ht+y*Tile.tile_size, _w = Tile.tile_size, _h = Tile.tile_size;
					int sx = 0, sy = 0, rw = rect.x+rect.width, rh = rect.y+rect.height;
					if(_x < rect.x){sx += rect.x-_x; _w -= sx; _x = rect.x;}
					if(_x+_w > rw) _w -= _x+_w-rw;
					if(_y < rect.y){sy += rect.y-_y; _h -= sy; _y = rect.y;}
					if(_y+_h > rh) _h -= _y+_h-rh;
					extraTiles.get(i).getTile(Direction.NONE).paint(g, 0, _x, _y, sx, sy, _w, _h, this);
				}
		}
		if(mainMap != null || extraTiles != null){
			g.setColor(Color.black);
			if(show_grid){
				Dimension d = getPreferredSize();
				int rw = Math.min(d.width, rect.x+rect.width), rh = Math.min(d.height, rect.y+rect.height);
				for(int y=(int)Math.ceil(rect.y*1.0/Tile.tile_size)*Tile.tile_size; y<rh; y+=Tile.tile_size)
					g.drawLine(rect.x, y, rw-1, y);
				for(int x=(int)Math.ceil(rect.x*1.0/Tile.tile_size)*Tile.tile_size; x<rw; x+=Tile.tile_size)
					g.drawLine(x, rect.y, x, rh-1);
			}
			
			int x = Math.min(selX1, selX2), y = Math.min(selY1, selY2), w = Math.abs(selX1-selX2)+1, h = Math.abs(selY1-selY2)+1;
			g.drawRect(x*Tile.tile_size+3, y*Tile.tile_size+3, w*Tile.tile_size-6, h*Tile.tile_size-6);
			g.setColor(selectColor2);
			g.drawRect(x*Tile.tile_size+1, y*Tile.tile_size+1, w*Tile.tile_size-2, h*Tile.tile_size-2);
			g.setColor(selectColor1);
			g.drawRect(x*Tile.tile_size+2, y*Tile.tile_size+2, w*Tile.tile_size-4, h*Tile.tile_size-4);
		}
	}
	private Tile getTile(int x, int y){
		int w = 8, h = 0;
		if(mainMap != null){
			w = mainMap.getTilesX();
			if(x >= w) return Tile.empty;
			h = mainMap.getTilesY();
			if(y < h) return mainMap.getTile(x, y);
		} else if(x >= w) return Tile.empty;
		if(extraTiles != null){
			int i = (y-h)*w+x;
			if(i < extraTiles.size()) return extraTiles.get(i).getTile(Direction.NONE);
		}
		return Tile.empty;
	}
	public Tile getSelectedTile(){return getTile(selX1, selY1);}
	public Tile getSelectedTile(int dx, int dy){return getTile(selX1+dx, selY1+dy);}
	public SelectionIterator getSelectedTiles(){return new SelectionIterator(selX1, selY1, selX2, selY2);}
	public SelectionIterator iterator(){return new SelectionIterator(selX1, selY1, selX2, selY2);}
	public class SelectionIterator implements Iterator<Tile>{
		private int stSelX, curSelX, endSelX, stSelY, curSelY, endSelY;
		public SelectionIterator(int selX1, int selY1, int selX2, int selY2){
			curSelX = Math.min(selX1, selX2); stSelX = curSelX; endSelX = Math.max(selX1, selX2);
			curSelY = Math.min(selY1, selY2); stSelY = curSelY; endSelY = Math.max(selY1, selY2);
		}
		public boolean hasNext() {return curSelX <= endSelX && curSelY <= endSelY;}
		public Tile next() {
			Tile ret = getTile(curSelX, curSelY);
			curSelX++; if(curSelX > endSelX){curSelX = stSelX; curSelY++;}
			return ret;
		}
		public int deltaX(){return curSelX-stSelX;}
		public int deltaY(){return curSelY-stSelY;}
		public void remove() {}
	}
	public int getSelectionWidth(){return Math.abs(selX1-selX2)+1;}
	public int getSelectionHeight(){return Math.abs(selY1-selY2)+1;}
	public void mouseDragged(MouseEvent e) {
		Dimension size = getPreferredSize();
		int x = Math.min(size.width-Tile.tile_size, Math.max(0, e.getX()))/Tile.tile_size,
			y = Math.min(size.height-Tile.tile_size, Math.max(0, e.getY()))/Tile.tile_size;
		if(selX2 != x || selY2 != y){
			int ox = selX2, oy = selY2;
			selX2 = x; selY2 = y;
			if(overlay != null) overlay.updateSelection(Math.abs(selX1-ox)+1, Math.abs(selX2-oy)+1);
			repaint();
		}
	}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		overlay.getEditor().removeFocus();
		int osw = getSelectionWidth(), osh = getSelectionHeight();
		Dimension size = getPreferredSize();
		int x = Math.min(size.width-Tile.tile_size, Math.max(0, e.getX()))/Tile.tile_size,
			y = Math.min(size.height-Tile.tile_size, Math.max(0, e.getY()))/Tile.tile_size;
		selX1 = x; selY1 = y; selX2 = x; selY2 = y;
		if(overlay != null) overlay.updateSelection(osw, osh);
		repaint();
	}
	public void mouseReleased(MouseEvent e) {}
	
	public int getScrollableUnitIncrement(Rectangle r, int o, int d){return Tile.tile_size;}
	public int getScrollableBlockIncrement(Rectangle r, int o, int d){return Tile.tile_size;}
	public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
	public boolean getScrollableTracksViewportWidth(){return false;}
	public boolean getScrollableTracksViewportHeight(){return false;}
}
