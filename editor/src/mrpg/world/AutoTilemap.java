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
import java.util.Hashtable;

import mrpg.editor.resource.TileResource;
import mrpg.world.Direction;
import mrpg.world.SplitTile;
import mrpg.world.Tile;
import mrpg.world.Tilemap;

public class AutoTilemap implements Tilemap {
	private final Tile tiles[]; private byte walkable = (byte)Direction.LINEAR;
	private final TileResource resource; public final AutoTileFormat format; public final int tile_size;
	public AutoTilemap(AutoTileFormat fmt, BufferedImage image, TileResource r, int ts) throws Exception {
		resource = r; tile_size = ts; format = fmt;
		if(image.getWidth()%tile_size != 0 || image.getHeight()%tile_size != 0)
			throw new Exception("Tilemap dimensions must be divisible by the tile size ("+ts+" px).");
		int height = image.getHeight()/tile_size;
		if(height%fmt.tilesY != 0) throw new Exception("This auto tilemap must be a multiple of "+fmt.tilesX+" tiles height.");
		int width = image.getWidth()/tile_size;
		if(width%fmt.tilesX != 0) throw new Exception("This auto tilemap must be a multiple of "+fmt.tilesY+" tiles width.");
		if(width == 0 || height == 0) throw new Exception("Auto tilemap images must not be empty (0x0).");
		tiles = new Tile[256]; int half_tile = tile_size>>1;
		Hashtable<AutoTileFormat.Coords, Tile> tilemap = new Hashtable<AutoTileFormat.Coords, Tile>();
		for(int i=0; i<256; i++){
			AutoTileFormat.Coords c = fmt.getCoords(i);
			if(tilemap.containsKey(c)) tiles[i] = tilemap.get(c);
			else {
				tiles[i] = SplitTile.Quad.createTile(image, c.x1*half_tile, c.y1*half_tile, c.x2*half_tile, c.y2*half_tile,
						c.x3*half_tile, c.y3*half_tile, c.x4*half_tile, c.y4*half_tile, new Tile.Info(this, i), tile_size);
				tilemap.put(c, tiles[i]);
			}
		}
	}
	public AutoTilemap(DataInputStream in, AutoTileFormat fmt, BufferedImage image, TileResource r, int tile_size) throws Exception {
		this(fmt, image, r, tile_size); walkable = in.readByte();
	}
	public void write(DataOutputStream out) throws Exception {out.writeByte(walkable);}
	public int getTileSize(){return tile_size;}
	public int getTilesX(){return format.tilesX;}
	public int getTilesY(){return format.tilesY;}
	public TileResource getResource(){return resource;}
	public Tile getTile(int neighbors){return tiles[neighbors&255];}
	public boolean indexNeighbors(){return true;}
	public byte getWalkable(int index) {return walkable;}
	public void setWalkable(int index, byte w){walkable = w;}
}
