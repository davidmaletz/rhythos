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
package com.davidmaletz.mrpg.transition;
import nme.display.BitmapData;
import nme.display.Sprite;
import nme.events.Event;
import nme.geom.Matrix;
import nme.Lib;

class Mosaic extends Sprite {
	private var orig:BitmapData; private var cur:BitmapData; private var w:Int; private var delta:Int;
	public function new() {
		super(); w = 400; orig = new BitmapData(400,300,false); orig.draw(Main.instance); cur = new BitmapData(400,300,false);
		Main.safeEnterFrame(this, enter_frame); Main.instance.parent.removeChild(Main.instance); enter_frame(null); delta = -20;
		Main.pause(); Lib.current.addChild(this);
	}
	private function enter_frame(e:Event):Void {
		if(w <= 20){delta = 20; orig.draw(Main.instance);} else if(delta > 0 && w >= 400){
			parent.removeChild(this); Lib.current.addChild(Main.instance); Main.unpause(); return;
		} w += delta; var s:Float = w/400; cur.draw(orig, new Matrix(s,0,0,s,0,0)); s = 400/w; graphics.clear();
		graphics.beginBitmapFill(cur, new Matrix(s,0,0,s,0,0)); graphics.drawRect(0,0,400,300); graphics.endFill();
	}
}