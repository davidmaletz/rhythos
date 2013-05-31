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
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class WallTilemap implements Tilemap {
	private final Tile tiles[]; private byte walkable = (byte)Direction.LINEAR, speed=2; private int frames[] = null; private final long id;
	public WallTilemap(BufferedImage image, long _id, int tile_size) throws Exception {
		id = _id;
		if(image.getWidth()%tile_size != 0 || image.getHeight()%tile_size != 0)
			throw new Exception("Tilemap dimensions must be divisible by the tile size ("+tile_size+" px).");
		int height = image.getHeight()/tile_size;
		if(height != 2) throw new Exception("Wall tilemap images must have a height of 2 tiles.");
		int width = image.getWidth()/tile_size;
		if(width%2 != 0) throw new Exception("Wall tilemap images must have multiple of 2 tiles width.");
		int frames = width/2;
		if(frames == 0) throw new Exception("Wall tilemap images must have at least one frame.");
		tiles = new Tile[16];
		BufferedImage i2 = new BufferedImage(tile_size*16*frames, tile_size, image.getType());
		Graphics g = i2.getGraphics();
		int half_tile = tile_size>>1;
		int dx = 0;
		for(int i=0; i<tile_size*2*frames; i+=tile_size*2){
			copyTile(g, image, dx, 0, i, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+tile_size+half_tile, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size+half_tile, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i+tile_size, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+tile_size+half_tile, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size+half_tile, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+half_tile, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+half_tile, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i+tile_size, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+half_tile, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+half_tile, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i, tile_size, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+tile_size+half_tile, tile_size, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size+half_tile, tile_size+half_tile, half_tile, half_tile);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i+tile_size, tile_size, tile_size, tile_size);
			dx += tile_size;
			
			copyTile(g, image, dx, 0, i, tile_size, tile_size, tile_size);
			dx += tile_size;
			
			copyTile(g, image, dx, 0, i+tile_size, tile_size, half_tile, tile_size);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+half_tile, tile_size, half_tile, tile_size);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i, half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+tile_size+half_tile, 0, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size+half_tile, half_tile, half_tile, half_tile);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i+tile_size, 0, tile_size, tile_size);
			dx += tile_size;
			
			copyTile(g, image, dx, 0, i, 0, tile_size, tile_size);
			dx += tile_size;
			
			copyTile(g, image, dx, 0, i+tile_size, 0, half_tile, tile_size);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+half_tile, 0, half_tile, tile_size);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i, tile_size, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i, half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+tile_size+half_tile, tile_size, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size+half_tile, half_tile, half_tile, half_tile);
			dx += half_tile;
			
			copyTile(g, image, dx, 0, i+tile_size, tile_size, tile_size, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size, half_tile, tile_size, half_tile);
			dx += tile_size;

			copyTile(g, image, dx, 0, i, tile_size, tile_size, half_tile);
			copyTile(g, image, dx, half_tile, i, half_tile, tile_size, half_tile);
			dx += tile_size;
			
			copyTile(g, image, dx, 0, i+tile_size, tile_size, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+tile_size, half_tile, half_tile, half_tile);
			dx += half_tile;
			copyTile(g, image, dx, 0, i+half_tile, tile_size, half_tile, half_tile);
			copyTile(g, image, dx, half_tile, i+half_tile, half_tile, half_tile, half_tile);
			dx += half_tile;
		}
		if(frames > 1){
			this.frames = new int[frames*2-2]; int f = 0;
			for(int i=0; i<frames; i++) this.frames[f++] = tile_size*2*i;
			for(int i=frames-2; i>0; i--) this.frames[f++] = tile_size*2*i;
			for(int i=0; i<16; i++) tiles[i] = new AnimatedTile(i2, i*tile_size, 0 , new Tile.Info(this, i));
		} else for(int i=0; i<16; i++) tiles[i] = new Tile(i2, i*tile_size, 0, new Tile.Info(this, i));
	}
	public WallTilemap(DataInputStream in, BufferedImage image, long _id, int tile_size) throws Exception {
		this(image, _id, tile_size); walkable = in.readByte(); speed = in.readByte(); int len = in.readShort();
		if(len == 0) frames = null; else {frames = new int[len]; for(int i=0; i<len; i++) frames[i] = in.readInt();}
	}
	public void write(DataOutputStream out) throws Exception {
		out.writeByte(walkable); out.writeByte(speed); if(frames == null) out.writeShort(0);
		else{out.writeShort(frames.length); for(int i=0; i<frames.length; i++) out.writeInt(frames[i]);}
	}
	public long getId(){return id;}
	private static final void copyTile(Graphics g, BufferedImage image, int dx, int dy, int sx, int sy, int w, int h){
		g.drawImage(image, dx, dy, dx+w, dy+h, sx, sy, sx+w, sy+h, null);
	}
	public Tile getTile(int neighbors){return tiles[neighbors&15];}
	public boolean indexNeighbors(){return true;}
	
	public byte getWalkable(int index) {return walkable;}
	public void setWalkable(int index, byte w){walkable = w;}
	public int[] getFrames(int index){return frames;}
	public void setFrames(int index, int[] f){frames = f;}
	public int getSpeed(int index){return (int)speed;}
	public void setSpeed(int index, int s){speed = (byte)s;}
}
