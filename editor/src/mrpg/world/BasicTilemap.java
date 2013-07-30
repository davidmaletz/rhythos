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
import java.io.DataInputStream;
import java.io.DataOutputStream;

import mrpg.editor.resource.TileResource;

public class BasicTilemap implements Tilemap {
	private final int width; private final Tile tiles[]; private byte walkable[]; private final TileResource resource; public final int tile_size;
	public BasicTilemap(BufferedImage image, TileResource r, int ts) throws Exception {
		resource = r; tile_size = ts;
		if(image.getWidth()%tile_size != 0 || image.getHeight()%tile_size != 0)
			throw new Exception("Tilemap dimensions must be divisible by the tile size ("+tile_size+" px).");
		width = image.getWidth()/tile_size;
		int height = image.getHeight()/tile_size;
		tiles = new Tile[width*height];
		walkable = new byte[width*height];
		int i=0;
		for(int y=0; y<height*tile_size; y+=tile_size)
			for(int x=0; x<width*tile_size; x+=tile_size){
				tiles[i] = new Tile(image, x, y, new Tile.Info(this, i));
				walkable[i] = (byte)Direction.LINEAR;
				i++;
			}
	}
	public BasicTilemap(DataInputStream in, BufferedImage image, TileResource r, int tile_size) throws Exception {
		this(image, r, tile_size); for(int i=0; i<walkable.length; i++) walkable[i] = in.readByte();
	}
	public void write(DataOutputStream out) throws Exception {
		for(int i=0; i<walkable.length; i++) out.writeByte(walkable[i]);
	}
	public TileResource getResource(){return resource;}
	public int getTileSize(){return tile_size;}
	public int getTilesX(){return width;}
	public int getTilesY(){return tiles.length/width;}
	public Tile getTile(int index){return (index >= tiles.length)?Tile.empty:tiles[index];}
	public Tile getTile(int x, int y){return getTile(y*width+x);}
	public boolean indexNeighbors(){return false;}
	
	public byte getWalkable(int index) {return walkable[index];}
	public void setWalkable(int index, byte w){walkable[index] = w;}
}
