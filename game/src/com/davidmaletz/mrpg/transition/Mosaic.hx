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
package com.davidmaletz.mrpg.transition;
import nme.display.BitmapData;
import nme.display.Sprite;
import nme.events.Event;
import nme.geom.Matrix;
import nme.Lib;

class Mosaic extends Sprite {
	private var orig:BitmapData; private var cur:BitmapData; private var w:Int; private var delta:Int;
	public function new() {
		super(); w = Main.width; orig = new BitmapData(Main.width,Main.height,false); orig.draw(Main.instance); cur = new BitmapData(Main.width,Main.height,false);
		Main.safeEnterFrame(this, enter_frame); Main.instance.parent.removeChild(Main.instance); enter_frame(null); delta = -Math.floor(Main.width/20);
		Main.pause(); Lib.current.addChild(this); scaleX = scaleY = Main.instance.scaleX;
	}
	private function enter_frame(e:Event):Void {
		var d:Int = Math.floor(Main.width/20); if(w <= d){delta = d; orig.draw(Main.instance);} else if(delta > 0 && w >= Main.width){
			parent.removeChild(this); Lib.current.addChild(Main.instance); Main.unpause(); return;
		} w += delta; var s:Float = w/Main.width; cur.draw(orig, new Matrix(s,0,0,s,0,0)); s = Main.width/w; graphics.clear();
		graphics.beginBitmapFill(cur, new Matrix(s,0,0,s,0,0)); graphics.drawRect(0,0,Main.width,Main.height); graphics.endFill();
	}
}