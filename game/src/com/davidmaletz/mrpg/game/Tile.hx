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
package com.davidmaletz.mrpg.game;
import nme.utils.ByteArray;

/**
 * ...
 * @author David Maletz
 */

class Tile {
	private var tilemap:Tilemap; private var index:Int;
	public function new(t:Tilemap, i:Int) {tilemap = t; index = i;}
	public inline function getTilemap():Tilemap {return tilemap;}
	public inline function getIndex():Int {return index;}
	public inline function getWalkable():Int {return tilemap.getWalkable(index);}
	public static var empty:Tile; public static var tile_size:Int;
	public static function read(d:ByteArray):Tile {
		var map:Int = d.readByte(); if(map == 255) return empty; return Tilemap.get(map).getTile(d.readShort());
	}
}