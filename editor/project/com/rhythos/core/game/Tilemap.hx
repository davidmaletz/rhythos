/*******************************************************************************
 * Rhythos Game is the base game client which runs games exported from the editor.
 * 
 * Copyright (C) 2013  David Maletz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.rhythos.core.game;
import nme.display.BitmapData;
import nme.display.Graphics;
import nme.display.Tilesheet;
import nme.geom.Rectangle;
import nme.utils.ByteArray;

/**
 * ...
 * @author David Maletz
 */

class Tilemap {
	private var tilesheet:Tilesheet; private var walkable:Array<Int>; private var tiles:Array<Tile>;
	public function new(d:ByteArray){
		trace(d.readUTF()); //TODO: load tileset from the classname - don't ignore!
		var b:BitmapData = Main.getImageResource(d);
		var w:Int=Math.floor(b.width/Tile.tile_size), h:Int = Math.floor(b.height/Tile.tile_size);
		tilesheet = new Tilesheet(b); walkable = new Array<Int>(); tiles = new Array<Tile>();
		for(y in 0...h) for(x in 0...w){
			tilesheet.addTileRect(new Rectangle(x*Tile.tile_size, y*Tile.tile_size, Tile.tile_size, Tile.tile_size)); walkable.push(d.readUnsignedByte());
			var i:Int = tiles.length; tiles.push(new Tile(this, i));
		}
	}
	public inline function getWalkable(i:Int):Int {return walkable[i];}
	public inline function getTile(i:Int):Tile {return tiles[i];}
	public inline function drawTiles(g:Graphics, tiles:Array<Float>):Void {tilesheet.drawTiles(g, tiles);}
	
	private static var cache:Hash<Tilemap> = new Hash<Tilemap>();
	public static function get(id:String):Tilemap {
		if(cache.exists(id)) return cache.get(id);
		var t = new Tilemap(Main.getData("t", id)); cache.set(id, t); return t;
	}
}