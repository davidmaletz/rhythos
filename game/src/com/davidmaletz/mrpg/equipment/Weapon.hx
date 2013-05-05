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
package com.davidmaletz.mrpg.equipment;

import com.davidmaletz.mrpg.Arrow;
import com.davidmaletz.mrpg.Character;
import nme.display.BitmapData;
import nme.display.Graphics;
import nme.geom.Matrix;

class Weapon {
	public static inline var SLASH:Int=0; public static inline var THRUST:Int=1; public static inline var BOW:Int=2;
	public static inline var CRITICAL_HIT:Int=0; public static inline var ARMOR_PIERCE:Int=1; public static inline var MANA_GEN:Int=2; public static inline var NONE:Int=3;
	private static var FRAME_ST:Array<Int> = [Character.SLASH_ST, Character.THRUST_ST, Character.BOW_ST, Character.CAST_ST];
	private static var dx_f:Array<Int> = [1,2,1,0,3,1]; private static var dy_f:Array<Int> = [0,0,3,1,0,0]; //TODO: should this be customizable? How do we know what type is female? Terrible hack!
	private static var _arrow:BitmapData;
	
	private var bitmap:BitmapData; private var type:Int; private var off_x:Int; private var off_y:Int; private var dist:Float;
	public var lowDmg:Int; public var highDmg:Int; public var cooldown:Int; public var combo:Int;
	public function new(id:Int, t:Int, offx:Int, offy:Int, d:Float, l:Int, h:Int, c:Int, c2:Int){
		bitmap = (id < 0)?null:Main.getBitmap("weapon", id); type = t; off_x = offx; off_y = offy; dist = d; lowDmg = l; highDmg = h; cooldown = c; combo = c2;
	}
	public function playSFX(hasArrows:Bool=true, isRepeat:Bool=false):Void {
		var t:Int = (!hasArrows && type == BOW)?SLASH:type; if(t == SLASH && (combo == MANA_GEN || bitmap == null || type == BOW)) t = 3;
		if(t == BOW && isRepeat) t = 4;
		switch(t){//TODO: custom weapon sfx?
			case SLASH: Main.playSFX("slash");
			case THRUST: Main.playSFX("thrust");
			case BOW: Main.playSFX("bow");
			case 3: Main.playSFX("wand");
			case 4: Main.playSFX("bow_repeat");
		}
	}
	public function drawFrame(ctype:Int, frame:Int, g:Graphics):Void {
		if(bitmap != null && frame >= FRAME_ST[type] && frame < FRAME_ST[type+1]){
			var f:Int = frame-FRAME_ST[type], sz:Int = bitmap.height, dx:Int=off_x, dy:Int=off_y;
			if(type == SLASH && ctype == 1){dx += dx_f[f]; dy += dy_f[f];}
			g.beginBitmapFill(bitmap, new Matrix(1,0,0,1,-sz*f+dx,dy), false, false); g.drawRect(dx,dy,sz,sz); g.endFill();
			if(type == BOW){ var _arrow = Main.getBitmap("arrow"); sz = _arrow.height; //TODO: different projectiles?
				g.beginBitmapFill(_arrow, new Matrix(1,0,0,1,-sz*f,0), false, false); g.drawRect(0,0,sz,sz); g.endFill();
			}
		}
	}
	public inline function getType():Int {return type;}
	public inline function distance():Float {return dist;}
	public inline function startFrame():Int {return FRAME_ST[type];}
	public inline function endFrame():Int {return FRAME_ST[type+1];}
	public inline function nFrames():Int {return FRAME_ST[type+1]-FRAME_ST[type];}
	public inline function getArrow():Arrow {//TODO: different projectiles?
		var a:Arrow = new Arrow(); var _arrow = Main.getBitmap("arrow"); var g:Graphics = a.graphics, sz:Int = _arrow.height;
		g.beginBitmapFill(_arrow, new Matrix(1,0,0,1,-sz*8,0), false, false); g.drawRect(0,0,sz,sz); g.endFill(); return a;
	}
}
