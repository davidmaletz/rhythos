/*******************************************************************************
 * Rhythos Game is the base game client which runs games exported from the editor.
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
package com.davidmaletz.mrpg.ui;
import nme.display.BitmapData;
import nme.display.Graphics;
import nme.display.Sprite;
import nme.display.Tilesheet;
import nme.geom.ColorTransform;
import nme.geom.Rectangle;

 @:bitmap("assets/font.png") class FONT extends BitmapData {}
 
class Text extends Sprite {
	private static var tilesheet:Tilesheet; private var w:Int; private var align:Int; private var len:Int; private var data:Array<Float>;
	public function new(col:Int, sz:Int, _w:Int, _align:Int=1, text:String=null) {
		super(); setColor(col); initTilesheet(); scaleX = scaleY = sz*0.125; w = Std.int(_w/scaleX); align = _align; if(text != null) setText(text);
	}
	public static function initTilesheet():Void {
		if(tilesheet == null){
			var f:BitmapData = new FONT(0,0,true,0);
			tilesheet = new Tilesheet(f); var n:Int = f.width>>3;
			for(i in 0...n) tilesheet.addTileRect(new Rectangle(i*8,0,8,8));
		}
	}
	public function setText(text:String):Void {
		len = text.length; var x:Int = ((align == 1)?((w-len*8)>>1):((align==0)?0:(w-len*8))); data = new Array<Float>();
		for(i in 0...len){var c:Int = text.charCodeAt(i); if(c != 32){data.push(x); data.push(0); data.push(c-32);} x += 8;}
		draw(graphics, data, 0);
	}
	public inline function length():Int {return len;}
	public function append(t:String):Void {
		if(data == null) setText(t); else {
			var l:Int = t.length, x:Int = len*8;
			for(i in 0...l){var c:Int = t.charCodeAt(i); if(c != 32){data.push(x); data.push(0); data.push(c-32);} x += 8;}
			len += l; draw(graphics, data, 0); 
		}
	}
	public function appendChar(c:Int):Void {
		if(data == null) setText(String.fromCharCode(c)); else {
			if(c != 32){data.push(len*8); data.push(0); data.push(c-32);}
			len++; draw(graphics, data, 0); 
		}
	}
	public inline function setColor(color:Int):Void {_setColor(this, color);}
	public static function _setColor(s:Sprite, color:Int):Void {
		s.transform.colorTransform = new ColorTransform(((color>>16)&0xFF)/147.0, ((color>>8)&0xFF)/147.0, (color&0xFF)/147.0);
	}
	public inline static function draw(g:Graphics, data:Array<Float>, flags:Int=0, smooth:Bool=false):Void {g.clear(); tilesheet.drawTiles(g, data, smooth, flags);}
}