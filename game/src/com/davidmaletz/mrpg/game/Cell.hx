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

/**
 * ...
 * @author David Maletz
 */

class Cell {
	private var map:Map; private var x:Int; private var y:Int; private var tiles:Array<Tile>;
	public function new(m:Map, _x:Int, _y:Int) {map = m; x = _x; y = _y; tiles = null;}
	public inline function getTile(level:Int):Tile {return (tiles == null || level >= tiles.length)?Tile.empty:tiles[level];}
	public function setTile(tile:Tile, level:Int){
		if(tiles == null){tiles = new Array<Tile>(); for(l in 0...level) tiles.push(Tile.empty); tiles.push(tile);}
		else if(level >= tiles.length){
			var st:Int = tiles.length; for(l in st...level) tiles.push(Tile.empty); tiles.push(tile);
		} else tiles[level] = tile;
	}
	public inline function getMap():Map {return map;}
	public inline function getX():Int {return x;}
	public inline function getY():Int {return y;}
	public inline function hasTiles():Bool {return tiles != null;}
}