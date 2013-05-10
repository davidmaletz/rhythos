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
package com.davidmaletz.mrpg;

import nme.display.BitmapData;
import nme.display.Sprite;
import nme.events.Event;
import nme.geom.Matrix;

class SpellSprite extends Sprite {
	private static inline var FRAME_LENGTH:Int=2;
	private var bitmap:BitmapData; private var frame:Int; private var frame_ct:Int; private var w:Int; private var h:Int;
	public function new(spell:Int, _w:Int, _h:Int) {
		super(); bitmap = Main.getBitmap("spell", spell); frame = 0; frame_ct = FRAME_LENGTH; w = _w; h = _h; updateFrame();
		scaleX = scaleY = 2; addEventListener(Event.ENTER_FRAME, enter_frame);
	}
	private function updateFrame():Void {
		graphics.clear(); var ty:Int = Math.floor(frame/w), tx:Int = frame-ty*w, fw:Int = Std.int(bitmap.width/w),
		fh:Int = Std.int(bitmap.height/h), ox:Float = (64-fw)*0.5, oy:Float = (64-fh)*0.5;
		graphics.beginBitmapFill(bitmap, new Matrix(1,0,0,1,ox-fw*tx,oy-fh*ty), false, false);
		graphics.drawRect(ox,oy,fw,fh); graphics.endFill();
	}
	private function enter_frame(e:Event):Void {
		if(Main.isPaused()) return; frame_ct--; if(frame_ct==0){
			frame++; if(frame == w*h){
				removeEventListener(Event.ENTER_FRAME, enter_frame);
				if(parent != null) parent.removeChild(this); return;
			} frame_ct = FRAME_LENGTH; updateFrame();
		}
	}
}
