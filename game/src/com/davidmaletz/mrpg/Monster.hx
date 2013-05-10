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
import nme.display.BitmapData;
import nme.events.Event;
import nme.geom.Matrix;

class Monster extends Character {
	private var cur:Int;
	public function new(n:String, hp:Int, mp:Int, l:Int, b:BitmapData){
		super(n, -1, hp, mp, l, b); cur = 0;
	}
	public override function reset():Void {super.reset(); cur = 0; alpha = 1;}
	private static function playDeath(c:Character):Void {Main.playSFX("death");}
	public override function queueDeath(f:Character->Void=null):Int {var f:Int = super.queueDeath(f); setFunction(f, playDeath); return f;}
	private override function renderFrame():Void {
		cur++; if(cur == 4) cur = 0; var _x:Int = (cur == 3)?1:cur, _y = getDir(), w:Int=Std.int(bitmap.width/3), h:Int=bitmap.height>>2; graphics.clear(); if(_y > 3) _y = 1;
		var x2:Int = (64-w)>>1, y2:Int = (h < 62)?62-h:64-h; graphics.beginBitmapFill(bitmap, new Matrix(1,0,0,1,-w*_x+x2,-h*_y+y2), false, false);
		graphics.drawRect(x2,y2,w,h); graphics.endFill();
		if(frame >= Character.DEATH_ST){alpha = 1-(frame-Character.DEATH_ST)/(Character.N_FRAMES-1-Character.DEATH_ST);}
	}
	public override function enter_frame(e:Event):Void {
		if(!Main.isPaused() && frame_ct == 1) updateFrame(); super.enter_frame(e);
	}	
}