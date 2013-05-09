package com.davidmaletz.mrpg.game;
import nme.display.Graphics;
import nme.display.Sprite;
import nme.utils.ByteArray;

/**
 * ...
 * @author David Maletz
 */

class Map extends Sprite {
	private var w:Int; private var h:Int; private var max_level:Int; private var cells:Array<Cell>; private var wrapX:Bool; private var wrapY:Bool;
	public function new(d:ByteArray){
		super(); w = d.readShort(); h = d.readShort(); var wrap:Int = d.readByte(); wrapX = (wrap & 1) != 0; wrapY = (wrap & 2) != 0;
		cells = new Array<Cell>(); max_level = 0; for(y in 0...h) for(x in 0...w){
			var tiles:Int = d.readByte(); if(tiles == 0) cells.push(null);
			else {
				var c:Cell = new Cell(this, x, y); cells.push(c);
				for(t in 0...tiles){
					var level:Int = d.readByte(); c.setTile(Tile.read(d), level); if(level > max_level) max_level = level;
				}
			}
		} max_level++; for(l in 0...max_level){var s:Sprite = new Sprite(); renderLevel(s.graphics, l); addChild(s);}
	}
	public inline function updateLevel(l:Int):Void {renderLevel(cast(getChildAt(l),Sprite).graphics, l);}
	private inline function draw(g:Graphics, t:Tilemap, data:Array<Float>):Void {if(t != null) t.drawTiles(g, data);}
	public function renderLevel(g:Graphics, l:Int):Void {
		g.clear(); var last:Tilemap = null, data:Array<Float> = null;
		var end:Int = cells.length; for(i in 0...end){
			var c:Cell = cells[i]; if(c == null) continue;
			var t:Tile = c.getTile(l); if(t == Tile.empty) continue;
			var tm:Tilemap = t.getTilemap(); if(tm != last){draw(g, last, data); last = tm; data = new Array<Float>();}
			data.push(c.getX()*Tile.tile_size); data.push(c.getY()*Tile.tile_size); data.push(t.getIndex());
		} draw(g, last, data);
	}
	
	private static var cache:Array<Map> = new Array<Map>();
	public static function get(id:Int):Map {
		var st:Int=cache.length, end:Int=id+1; for(i in st...end) cache.push(null);
		var t:Map = cache[id]; if(t == null){t = new Map(Main.getData("map", id)); cache[id] = t;}
		return t;
	}
}