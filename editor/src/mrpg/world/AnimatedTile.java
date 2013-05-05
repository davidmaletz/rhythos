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
package mrpg.world;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

public class AnimatedTile extends Tile {
	public AnimatedTile(Image _image, int _x, int _y, Tile.Info info){
		super(_image,_x,_y, info);
	}
	public void paint(Graphics g, int frame, int dx, int dy, int sx, int sy, int w, int h, ImageObserver observer){
		if(image != null){
			frame /= info.getSpeed();
			int[] frames = info.getFrames();
			frame %= (frames.length); int _x = x+frames[frame];
			g.drawImage(image, dx, dy, dx+w, dy+h, _x+sx, y+sy, _x+sx+w, y+sy+h, observer);
		}
	}
	public static class Horiz extends AnimatedTile {
		public final int x2, y2;
		public Horiz(Image _image, int _x1, int _y1, int _x2, int _y2, Tile.Info info){
			super(_image, _x1, _y1, info); x2 = _x2; y2 = _y2;
		}
		
		public void paint(Graphics g, int frame, int dx, int dy, int sx, int sy, int w, int h, ImageObserver observer){
			if(image != null){
				frame /= info.getSpeed();
				int[] frames = info.getFrames();
				frame %= (frames.length); int _x = frames[frame];
				int half_tile = Tile.tile_size>>1;
				int _h = Math.min(sy+h, half_tile)-sy;
				if(_h > 0){
					g.drawImage(image, dx, dy, dx+w, dy+_h, x+sx+_x, y+sy, x+sx+_x+w, y+sy+_h, observer);
					dy += _h; sy = 0; h -= _h;
				} else sy -= half_tile;
				_h = Math.min(sy+h, half_tile)-sy;
				if(_h > 0) g.drawImage(image, dx, dy, dx+w, dy+_h, x2+sx+_x, y2+sy, x2+sx+_x+w, y2+sy+_h, observer);
			}
		}
	}
	public static class Vert extends AnimatedTile {
		public final int x2, y2;
		public Vert(Image _image, int _x1, int _y1, int _x2, int _y2, Tile.Info info){
			super(_image, _x1, _y1, info); x2 = _x2; y2 = _y2;
		}
		
		public void paint(Graphics g, int frame, int dx, int dy, int sx, int sy, int w, int h, ImageObserver observer){
			if(image != null){
				frame /= info.getSpeed();
				int[] frames = info.getFrames();
				frame %= (frames.length); int _x = frames[frame];
				int half_tile = Tile.tile_size>>1;
				int _w = Math.min(sx+w, half_tile)-sx;
				if(_w > 0){
					g.drawImage(image, dx, dy, dx+_w, dy+h, x+sx+_x, y+sy, x+sx+_x+_w, y+sy+h, observer);
					dx += _w; sx = 0; w -= _w;
				} else sx -= half_tile;
				_w = Math.min(sx+w, half_tile)-sx;
				if(_w > 0) g.drawImage(image, dx, dy, dx+_w, dy+h, x2+sx+_x, y2+sy, x2+sx+_x+_w, y2+sy+h, observer);
			}
		}
	}
	public static class Quad extends AnimatedTile {
		public final int x2, y2, x3, y3, x4, y4;
		public Quad(Image _image, int _x1, int _y1, int _x2, int _y2, int _x3, int _y3, int _x4, int _y4, Tile.Info info){
			super(_image, _x1, _y1, info); x2 = _x2; y2 = _y2; x3 = _x3; y3 = _y3; x4 = _x4; y4 = _y4;
		}
		
		public void paint(Graphics g, int frame, int dx, int dy, int sx, int sy, int w, int h, ImageObserver observer){
			if(image != null){
				frame /= info.getSpeed();
				int[] frames = info.getFrames();
				frame %= (frames.length); int _x = frames[frame];
				int half_tile = Tile.tile_size>>1;
				int _w = Math.min(sx+w, half_tile)-sx, _h = Math.min(sy+h, half_tile)-sy;
				int odx = dx, osx = sx, ow = w, _ow = _w;
				if(_w > 0 && _h > 0) g.drawImage(image, dx, dy, dx+_w, dy+_h, x+sx+_x, y+sy, x+sx+_x+_w, y+sy+_h, observer);
				if(_w > 0){dx += _w; sx = 0; w -= _w;} else sx -= half_tile;
				_w = Math.min(sx+w, half_tile)-sx;
				if(_w > 0 && _h > 0) g.drawImage(image, dx, dy, dx+_w, dy+_h, x2+sx+_x, y2+sy, x2+sx+_x+_w, y2+sy+_h, observer);
				if(_h > 0){dy += _h; sy = 0; h -= _h;} else sy -= half_tile;
				dx = odx; sx = osx; w = ow; int _ow2 = _w; _w = _ow;
				_h = Math.min(sy+h, half_tile)-sy;
				if(_w > 0 && _h > 0) g.drawImage(image, dx, dy, dx+_w, dy+_h, x3+sx+_x, y3+sy, x3+sx+_x+_w, y3+sy+_h, observer);
				if(_w > 0){dx += _w; sx = 0;} else sx -= half_tile;
				_w = _ow2;
				if(_w > 0 && _h > 0) g.drawImage(image, dx, dy, dx+_w, dy+_h, x4+sx+_x, y4+sy, x4+sx+_x+_w, y4+sy+_h, observer);
			}
		}
		
		public static AnimatedTile createTile(Image image, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, Tile.Info info){
			int half_tile = Tile.tile_size>>1;
			boolean x = x1+half_tile == x2 && x3+half_tile == x4 && y1 == y2 && y3 == y4;
			boolean y = y1+half_tile == y3 && y2+half_tile == y4 && x1 == x3 && x2 == x4;
			if(x && y) return new AnimatedTile(image, x1, y1, info);
			if(x) return new AnimatedTile.Horiz(image, x1, y1, x3, y3, info);
			if(y) return new AnimatedTile.Vert(image, x1, y1, x2, y2, info);
			return new AnimatedTile.Quad(image, x1, y1, x2, y2, x3, y3, x4, y4, info);
		}
	}
}
