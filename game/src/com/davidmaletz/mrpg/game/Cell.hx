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