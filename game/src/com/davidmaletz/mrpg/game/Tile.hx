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
	public static var empty:Tile; public static inline var tile_size:Int = 32;
	public static function read(d:ByteArray):Tile {
		var map:Int = d.readByte(); if(map == 255) return empty; return Tilemap.get(map).getTile(d.readShort());
	}
}