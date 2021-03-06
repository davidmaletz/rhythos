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

import java.io.DataOutputStream;

import mrpg.editor.resource.TileResource;

public interface Tilemap {
	public void write(DataOutputStream out) throws Exception;
	public Tile getTile(int idx);
	public boolean indexNeighbors();
	public TileResource getResource();
	
	public byte getWalkable(int index);
	public void setWalkable(int index, byte w);
	
	public int getTileSize();
	public int getTilesX();
	public int getTilesY();
}
