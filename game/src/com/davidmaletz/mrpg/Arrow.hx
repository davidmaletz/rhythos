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
package com.davidmaletz.mrpg;
import nme.display.Sprite;
import nme.events.Event;

class Arrow extends Sprite {
	public var target:Character; private var ct:Int; private var dy:Float;
	public function new(){super(); scaleX = scaleY = 2; addEventListener(Event.ENTER_FRAME, enter_frame); ct = 0; dy = Math.random()*4-2;}
	private inline function destroy():Void {removeEventListener(Event.ENTER_FRAME, enter_frame); if(parent != null) parent.removeChild(this);}
	private function enter_frame(e:Event):Void {
		if(Main.isPaused()) return; if(ct > 0){
			alpha = 1-ct/16; ct++; if(ct > 16) destroy(); return;
		} var d:Float = -32*scaleX, x1:Float=target.x+((target.scaleX < 0)?-96:32), x2:Float = x1+64, x3:Float=x+((scaleX < 0)?-92:36), x4:Float=x3+64;
		if(d > 0) x4 += d; else x3 += d; x += d; y += dy; if(ct == 0 && (x4 >= x1) && (x3 <= x2)){
			if(target.phys_def == 0) ct = -1; else {x = target.x-36*scaleX; ct = 1;} if(Main.battle != null) Main.battle.weaponHit(target);
		} else if(x < -128 || x > 528) destroy();
	}
}