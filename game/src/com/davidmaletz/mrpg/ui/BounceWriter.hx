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
import nme.display.Sprite;
import nme.display.Tilesheet;
import nme.events.Event;

class BounceWriter extends Sprite {
	private var ct:Int; private var _end:Int;
	public function new(col:Int, sz:Int, text:String, _x:Float, _y:Float, e:Int=0) {
		super(); Text._setColor(this,col); Text.initTilesheet(); scaleX = scaleY = sz*0.125; x = _x; y = _y;
		var len:Int = text.length; var x:Int = 0; for(i in 0...len){addChild(new BounceChar(text.charCodeAt(i),x)); x += 8;}
		addEventListener(Event.ENTER_FRAME, enter_frame); ct = 0; _end = text.length+6+e;
	}
	private function enter_frame(e:Event):Void {
		if(Main.isPaused()) return; ct++; var end:Int = numChildren; for(i in 0...end){
			var b:BounceChar = cast(getChildAt(i)); b.y = Math.max(-6, Math.min(0, (i-ct)*2)); b.alpha = -b.y/6;
		} if(ct > _end){
			alpha = 1-(ct-_end)/6; if(ct >= _end+6){removeEventListener(Event.ENTER_FRAME, enter_frame); parent.removeChild(this);}
		}
	}
}

class BounceChar extends Sprite {
	public function new(c:Int,_x:Float){super(); Text.draw(graphics, [0.0,0.0,c-32]); x = _x; alpha = 0;}
}