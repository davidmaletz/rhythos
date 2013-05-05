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

import java.awt.image.BufferedImage;
import java.util.Hashtable;

public class AutoTilemap implements Tilemap {
	private final Tile tiles[]; private byte walkable = (byte)Direction.LINEAR, speed=2; private int frames[] = null;
	public AutoTilemap(BufferedImage image) throws Exception {
		int height = image.getHeight()/Tile.tile_size;
		if(height != 3) throw new Exception("Auto tilemap images must have a height of 3 tiles.");
		int width = image.getWidth()/Tile.tile_size;
		if(width%2 != 0) throw new Exception("Auto tilemap images must have multiple of 2 tiles width.");
		int frames = width/2;
		if(frames == 0) throw new Exception("Auto tilemap images must have at least one frame.");
		tiles = new Tile[256];
		Hashtable<Coords, Tile> tilemap = new Hashtable<Coords, Tile>();
		if(frames > 1){
			this.frames = new int[frames*2-2]; int f = 0;
			for(int i=0; i<frames; i++) this.frames[f++] = Tile.tile_size*2*i;
			for(int i=frames-2; i>0; i--) this.frames[f++] = Tile.tile_size*2*i;
			for(int i=0; i<256; i++){
				Coords c = getCoords(i);
				if(tilemap.containsKey(c)) tiles[i] = tilemap.get(c);
				else {
					tiles[i] = AnimatedTile.Quad.createTile(image, c.x1, c.y1, c.x2, c.y2, c.x3, c.y3, c.x4, c.y4, new Tile.Info(this, i));
					tilemap.put(c, tiles[i]);}
			}
		} else {
			for(int i=0; i<256; i++){
				Coords c = getCoords(i);
				if(tilemap.containsKey(c)) tiles[i] = tilemap.get(c);
				else {
					tiles[i] = SplitTile.Quad.createTile(image, c.x1, c.y1, c.x2, c.y2, c.x3, c.y3, c.x4, c.y4, new Tile.Info(this, i));
					tilemap.put(c, tiles[i]);}
			}
		}
	}
	private Coords getCoords(int i){
		int half_tile = Tile.tile_size>>1, half_tile3 = Tile.tile_size+half_tile, half_tile5 = half_tile3+Tile.tile_size;
		int x1, y1, x2, y2, x3, y3, x4, y4;
		boolean left = Direction.left(i), right = Direction.right(i), up = Direction.up(i), down = Direction.down(i),
			ul = Direction.upper_left(i), ur = Direction.upper_right(i), ll = Direction.lower_left(i), lr = Direction.lower_right(i);
		
		if(!left && !up){x1 = 0; y1 = (right || down)?Tile.tile_size:0;}
		else if(!up){x1 = Tile.tile_size; y1 = Tile.tile_size;}
		else if(!left){x1 = 0; y1 = Tile.tile_size*2;}
		else if(ul){x1 = Tile.tile_size; y1 = Tile.tile_size*2;}
		else {x1 = Tile.tile_size; y1 = 0;}
		
		if(!right && !up){x2 = (left || down)?half_tile3:half_tile; y2 = (left || down)?Tile.tile_size:0;}
		else if(!up){x2 = half_tile; y2 = Tile.tile_size;}
		else if(!right){x2 = half_tile3; y2 = Tile.tile_size*2;}
		else if(ur){x2 = half_tile; y2 = Tile.tile_size*2;}
		else {x2 = half_tile3; y2 = 0;}
		
		if(!left && !down){x3 = 0; y3 = (right || up)?half_tile5:half_tile;}
		else if(!down){x3 = Tile.tile_size; y3 = half_tile5;}
		else if(!left){x3 = 0; y3 = half_tile3;}
		else if(ll){x3 = Tile.tile_size; y3 = half_tile3;}
		else {x3 = Tile.tile_size; y3 = half_tile;}
		
		if(!right && !down){x4 = (left || up)?half_tile3:half_tile; y4 = (left || up)?half_tile5:half_tile;}
		else if(!down){x4 = half_tile; y4 = half_tile5;}
		else if(!right){x4 = half_tile3; y4 = half_tile3;}
		else if(lr){x4 = half_tile; y4 = half_tile3;}
		else {x4 = half_tile3; y4 = half_tile;}
		
		return new Coords(x1, y1, x2, y2, x3, y3, x4, y4);
	}
	public Tile getTile(int neighbors){return tiles[neighbors&255];}
	public boolean indexNeighbors(){return true;}
	private static class Coords {
		public final int x1, y1, x2, y2, x3, y3, x4, y4;
		public Coords(int _x1, int _y1, int _x2, int _y2, int _x3, int _y3, int _x4, int _y4){
			x1 = _x1; y1 = _y1; x2 = _x2; y2 = _y2; x3 = _x3; y3 = _y3; x4 = _x4; y4 = _y4;
		}
		public int hashCode(){
			return y4*Tile.tile_size*7+x4*Tile.tile_size*6+y3*Tile.tile_size*5+x3*Tile.tile_size*4+y2*Tile.tile_size*3+
				x2*Tile.tile_size*2+y1*Tile.tile_size+x1;
		}
		public boolean equals(Object o){
			Coords c = (Coords)o;
			return x1 == c.x1 && y1 == c.y1 && x2 == c.x2 && y2 == c.y2 && x3 == c.x3 && y3 == c.y3 && x4 == c.x4 && y4 == c.y4;
		}
	}
	public byte getWalkable(int index) {return walkable;}
	public void setWalkable(int index, byte w){walkable = w;}
	public int[] getFrames(int index){return frames;}
	public void setFrames(int index, int[] f){frames = f;}
	public int getSpeed(int index){return (int)speed;}
	public void setSpeed(int index, int s){speed = (byte)s;}
}
