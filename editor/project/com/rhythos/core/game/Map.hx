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
import com.rhythos.core.Character;
import nme.display.Graphics;
import nme.display.Sprite;
import nme.display.BitmapData;
import nme.utils.ByteArray;

/**
 * ...
 * @author David Maletz
 */

class Map extends Sprite {
	private var w:Int; private var h:Int; private var max_level:Int; private var cells:Array<Cell>;
	private var wrapX:Bool; private var wrapY:Bool; private var event_layer:Int; private var background:BitmapData;
	public function new(d:ByteArray){
		super(); background = Main.getImageResource(d);
		var tilemaps = new Array<Tilemap>(); var len = d.readUnsignedByte();
		for(i in 0...len) tilemaps.push(Tilemap.get(d.readUTF(), Main.readID(d)));
		w = d.readShort(); h = d.readShort(); var wrap:Int = d.readUnsignedByte(); wrapX = (wrap & 1) != 0; wrapY = (wrap & 2) != 0;
		cells = new Array<Cell>(); event_layer = 2; max_level = event_layer; for(y in 0...h) for(x in 0...w){
			var tiles:Int = d.readUnsignedByte(); if(tiles == 0) cells.push(null);
			else {
				var c:Cell = new Cell(this, x, y); cells.push(c);
				for(t in 0...tiles){
					var level:Int = d.readUnsignedByte(); c.setTile(Tile.read(tilemaps, d), level); if(level > max_level) max_level = level;
				}
			}
		} max_level++; for(l in 0...max_level){var s:Sprite = new Sprite(); renderLevel(s.graphics, l); addChild(s);}
	}
	private function init():Void {
		var end:Int = numChildren; for(i in 0...end) Main.removeAllChildren(cast(getChildAt(i), Sprite)); //TODO: setup map events on enter.
		//TODO: also revert all changed tiles to normal (if any) - after init, the map should be in the initial state, regardless of what changes
		//events make. If events want to make permanent changes, they have to use global variables.
	}
	public inline function add(c:Character):Void {cast(getChildAt(event_layer),Sprite).addChild(c);}
	public inline function setLayer(c:Character, l:Int):Void {cast(getChildAt(l),Sprite).addChild(c);}
	public inline function getWidth():Int {return w;}
	public inline function getHeight():Int {return h;}
	public inline function centerOn(_x:Float, _y:Float):Void {
		var sw:Float = Main.width, sh:Float = Main.height, w:Float = w*Tile.tile_size, h:Float = h*Tile.tile_size;
		x = Math.max(sw-w, Math.min(0, sw*0.5-_x)); y = Math.max(sh-h, Math.min(0, sh*0.5-_y));
	}
	public inline function centerChar(c:Character):Void {centerOn(c.getWorldX(), c.getWorldY());}
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
	public function getCell(x:Int, y:Int):Cell {
		var oob:Bool = false;
		if(wrapX){x = x%w; if(x < 0) x += w;} else oob = oob || x < 0 || x >= w;
		if(wrapY){y = y%h; if(y < 0) y += h;} else oob = oob || y < 0 || y >= h;
		return (oob)?null:cells[y*w+x];
	}
	private function walkable(c:Cell, l:Int):Int {
		if(c == null) return -1; var i:Int=l; while(i >= 0){
			var t:Tile = c.getTile(i); if(t != Tile.empty) return t.getWalkable(); i--;
		} return -2;
	}
	public function canMove(c:Cell, dir:Int, l:Int):Bool {
		if(c == null) return false; var i:Int=l; while(i >= 0){
			var t:Tile = c.getTile(i); if(t != Tile.empty) return Direction.hasMask(t.getWalkable(), dir); i--;
		} return true;
	}
	public function canMoveUp(c:Character){
		var l:Int = event_layer; try{l = getChildIndex(c.parent);}catch(e:Dynamic){} var x:Int = c.getX(), y:Int = c.getY();
		return canMove(getCell(x,y), Direction.UP, l) && canMove(getCell(x,y-1), Direction.DOWN, l);
	}
	public function canMoveDown(c:Character){
		var l:Int = event_layer; try{l = getChildIndex(c.parent);}catch(e:Dynamic){} var x:Int = c.getX(), y:Int = c.getY();
		return canMove(getCell(x,y), Direction.DOWN, l) && canMove(getCell(x,y+1), Direction.UP, l);
	}
	public function canMoveLeft(c:Character){
		var l:Int = event_layer; try{l = getChildIndex(c.parent);}catch(e:Dynamic){} var x:Int = c.getX(), y:Int = c.getY();
		return canMove(getCell(x,y), Direction.LEFT, l) && canMove(getCell(x-1,y), Direction.RIGHT, l);
	}
	public function canMoveRight(c:Character){
		var l:Int = event_layer; try{l = getChildIndex(c.parent);}catch(e:Dynamic){} var x:Int = c.getX(), y:Int = c.getY();
		return canMove(getCell(x,y), Direction.RIGHT, l) && canMove(getCell(x+1,y), Direction.LEFT, l);
	}
	private static var cache:Hash<Map> = new Hash<Map>();
	public static function get(id:String):Map {
		var t:Map; if(cache.exists(id)) t = cache.get(id);
		else {t = new Map(Main.getData("m", id)); cache.set(id, t);}
		t.init(); return t;
	}
}