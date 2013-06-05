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
package com.rhythos.core;

import com.rhythos.core.game.Tile;
import nme.display.Bitmap;
import nme.display.BitmapData;
import nme.display.Sprite;
import nme.events.Event;
import nme.geom.ColorTransform;
import nme.geom.Matrix;
import nme.geom.Rectangle;
import com.rhythos.core.equipment.Equipment;

class Character extends Sprite {
	private static inline var FRAME_LENGTH:Int=2;
	public static inline var WALK_ST:Int=0; public static inline var WALK_LEN:Int=9; public static inline var UP:Int=0;
	public static inline var LEFT:Int=1; public static inline var DOWN:Int=2; public static inline var RIGHT:Int=3;
	public static inline var N_DIR:Int=4; public static inline var SLASH_ST:Int=WALK_ST+WALK_LEN*N_DIR;
	public static inline var THRUST_ST:Int=SLASH_ST+6; public static inline var BOW_ST:Int=THRUST_ST+8;
	public static inline var FIRE_ST:Int=BOW_ST+9; public static inline var CAST_ST:Int = FIRE_ST+4;
	public static inline var DEATH_ST:Int = CAST_ST+7; public static inline var N_FRAMES:Int = DEATH_ST+6;
	public static inline var LEGS:Int = 0; public static inline var TORSO:Int = 1; public static inline var HEAD:Int = 2;
	private var type:Int; private var frame:Int; private var frame_ct:Int; private var skin:BitmapData; public var top:Sprite; private var eyewhite:BitmapData;
	private var eyes:BitmapData; private var hair:BitmapData; private var bitmap:BitmapData; private var anim:Dequeue<Int>; private var func:Dequeue<Character->Void>;
	public var skin_mat:ColorTransform; public var eyes_mat:ColorTransform; public var hair_mat:ColorTransform;
	public var equip:Array<Equipment>; public var level(default,null):Int;
	public var phys_def(default,null):Float; public var mag_def(default,null):Float; private var dirty:Int;
	public var health:Int; public var max_health:Int; public var mana:Int; public var max_mana:Int; public var cname:String; private var hairId:Int;
	public function new(n:String, t:Int, hp:Int, mp:Int, l:Int=0, b:BitmapData=null){
		super(); cname = n; type = t; frame = 0; frame_ct = FRAME_LENGTH; anim = new Dequeue<Int>(); func = new Dequeue<Character->Void>();
		level = l; max_health = hp+l*400; health = max_health; max_mana = mp+l*400; mana = max_mana;
		skin_mat = _clear; eyes_mat = _clear; hair_mat = _clear;
		equip = [null,null,null]; top = new Sprite(); hairId = 0;
		if(type >= 0){
			skin = Main.getBitmap("skin", type); if(type < 2){eyes = Main.getBitmap("eyes", type); eyewhite = getEyeWhite(skin, type);}
			if(type != 2 && hairId >= 0) hair = Main.getBitmap("hair", type, hairId); else hair = null;
		} if(b != null) skin = b; scaleX = scaleY = 2; bitmap = new BitmapData(skin.width, skin.height, true, 0); resetDefense();
		updateBitmap(); updateFrame(); Main.safeEnterFrame(this, enter_frame, true);
	}
	public function clone():Character {
		var c:Character = new Character(cname, type, max_health-level*400, max_mana -level*400, level, skin); c.frame = frame;
		c.skin_mat = skin_mat; c.setHairStyle(getHairStyle()); c.hair_mat = hair_mat; c.eyes_mat = eyes_mat;
		for(i in 0...equip.length) c.equip[i] = equip[i]; c.resetDefense(); return c;
	}
	public static var GENDERS:Array<String>;
	public static var SKIN_COLORS:Array<String>;
	public static var HAIR_STYLES:Array<Array<String> >;
	public static var HAIR_COLORS:Array<String>;
	public static var EYE_COLORS:Array<String>;
	public inline function getType():Int {return type;}
	public function setType(t:Int, b:BitmapData=null):Void {
		type = t; if(type >= 0){
			skin = Main.getBitmap("skin", type); eyes = Main.getBitmap("eyes", type); eyewhite = getEyeWhite(skin, type);
			if(hairId >= 0) hair = Main.getBitmap("hair", type, hairId); else hair = null;
		} if(b != null) skin = b; updateBitmap(); updateFrame();
	}
	public inline function getHairStyle():Int {return hairId;}
	public function setHairStyle(h:Int):Void {
		hairId = h; if(hairId >= 0) hair = Main.getBitmap("hair", type, hairId); else hair = null;
		updateBitmap(); updateFrame();
	}
	public static var SKIN_MAT:Array<ColorTransform>;
	public inline function getSkinColor():Int {return Main.indexOf(SKIN_MAT, skin_mat);}
	public function setSkinColor(id:Int):Void {
		skin_mat = SKIN_MAT[id]; updateBitmap(); updateFrame();
	}
	public static var HAIR_MAT:Array<ColorTransform>;
	public inline function getHairColor():Int {return Main.indexOf(HAIR_MAT, hair_mat);}
	public function setHairColor(id:Int):Void {
		hair_mat = HAIR_MAT[id]; updateBitmap(); updateFrame();
	}
	public static var EYES_MAT:Array<ColorTransform>;
	public inline function getEyeColor():Int {return Main.indexOf(EYES_MAT, eyes_mat);}
	public function setEyeColor(id:Int):Void {
		eyes_mat = EYES_MAT[id]; updateBitmap(); updateFrame();
	}
	public inline function getFrame():Int {return frame;}
	public inline function getTop():Sprite {top.x = x; top.y = y; top.scaleX = scaleX; top.scaleY = scaleY; return top;}
	public inline function queueLength():Int {return anim.size();}
	public inline function clearQueue():Void {anim.clear(); func.clear();}
	public inline function getIdle():Int {return 0;}
	public inline function restore():Void {health = max_health; mana = max_mana;}
	public inline function queueFrame(f:Int, _func:Character->Void=null):Void {anim.addLast(f); func.addLast(_func);}
	public function queueIdle(n:Int,f:Character->Void=null):Int {var ret:Int = queueLength(); for(i in 0...n){anim.addLast(getIdle()); func.addLast(f);} return ret;}
	public function queueWalk(dir:Int, f:Character->Void=null):Int {
		var ret:Int = queueLength(), st:Int = WALK_ST+WALK_LEN*dir, end:Int = st+WALK_LEN; for(i in st...end){anim.addLast(i); func.addLast(f);} return ret;
	}
	public function queueSlash(f:Character->Void=null):Int {var ret:Int = queueLength(); for(i in SLASH_ST...THRUST_ST){anim.addLast(i); func.addLast(f);} return ret;}
	public function queueThrust(f:Character->Void=null):Int {var ret:Int = queueLength(); for(i in THRUST_ST...BOW_ST){anim.addLast(i); func.addLast(f);} return ret;}
	public function queueBow(f:Character->Void=null):Int {
		var ret:Int = queueLength(); if(anim.getLast() == FIRE_ST-1){
			for(i in FIRE_ST...CAST_ST){anim.addLast(i); func.addLast(f);}
			for(i in (BOW_ST+6)...FIRE_ST){anim.addLast(i); func.addLast(f);}
		} else for(i in BOW_ST...FIRE_ST){anim.addLast(i); func.addLast(f);} return ret;
	}
	public inline function getQueuedFrame(f:Int):Int {return anim.get(f);}
	public inline function getQueuedFunc(f:Int):Character->Void {return func.get(f);}
	public function queueCast(f:Character->Void=null):Int {var ret:Int = queueLength(); for(i in CAST_ST...DEATH_ST){anim.addLast(i); func.addLast(f);} return ret;}
	public function queueDeath(f:Character->Void=null):Int {var ret:Int = queueLength(); for(i in DEATH_ST...N_FRAMES){anim.addLast(i); func.addLast(f);} return ret;}
	public inline function setFunction(i:Int, f:Character->Void):Void {func.set(i, f);}
	public inline function startEvade():Void {phys_def = 0; mag_def = 0;}
	public function resetDefense():Void {
		phys_def = Math.pow(0.99,level); mag_def = phys_def; for(e in equip) if(e != null){phys_def *= e.phys_def; mag_def *= e.mag_def;}
	}
	public function setLevel(l:Int):Void {
		if(l != level){max_health += (l-level)*400; max_mana += (l-level)*400; level = l; resetDefense(); restore();}
	}
	
	public inline function updateBitmap():Void {dirty = 2;}
	private function renderBitmap():Void {
		bitmap.fillRect(new Rectangle(0,0,bitmap.width, bitmap.height), 0);
		bitmap.draw(skin, null, skin_mat); if(eyes != null){if(eyewhite != null) bitmap.draw(eyewhite); bitmap.draw(eyes, null, eyes_mat);}
		if(equip[LEGS] != null) equip[LEGS].draw(type, bitmap);
		if(equip[TORSO] != null) equip[TORSO].draw(type, bitmap);
		if(hair != null && (equip[HEAD] == null || equip[HEAD].allowHair)) bitmap.draw(hair, null, hair_mat);
		if(equip[HEAD] != null) equip[HEAD].draw(type, bitmap);
	}
	private var flashCt:Int; private static var _red:ColorTransform = new ColorTransform(0,0,0,1,255,0,0,0);
	public inline function flashRed():Void {transform.colorTransform = _red; flashCt = 3;}
	private static var _green:ColorTransform = new ColorTransform(0,0,0,1,29,208,0,0);
	public inline function flashGreen():Void {transform.colorTransform = _green; flashCt = 3;}
	private static var _gray:ColorTransform = new ColorTransform(0,0,0,1,51,51,51,0);
	public inline function makeGray():Void {transform.colorTransform = _gray; top.transform.colorTransform = _gray;}
	private static var _clear:ColorTransform = new ColorTransform(1,1,1,1,0,0,0,0);
	public inline function clearColor():Void {transform.colorTransform = _clear; top.transform.colorTransform = _clear; flashCt = 0;}
	public function reset():Void {
		x = 0; y = 0; top.x = 0; top.y = 0; scaleX = scaleY = 2; clearColor(); clearQueue(); resetDefense();
	}
	public function setFrame(f:Int):Void {frame = f; updateFrame();}
	private inline function updateFrame():Void {if(dirty == 0) dirty = 1;}
	private function renderFrame():Void {
		var _x:Int = frame%19, _y:Int = Std.int(frame/19); graphics.clear(); graphics.beginBitmapFill(bitmap, new Matrix(1,0,0,1,-64*_x,-64*_y), false, false);
		graphics.drawRect(0,0,64,64); graphics.endFill(); top.graphics.clear(); //TODO: handle top layer? Rename it to "oversize" layer?
	}
	public function enter_frame(e:Event):Void {
		if(!Main.isPaused()){frame_ct--; if(frame_ct==0){
				frame_ct = FRAME_LENGTH; var f:Int; if(anim.isEmpty()) f = (Main.battle == null || Main.battle.state != 0)?frame:getIdle(); else {
					f = anim.removeFirst(); var _f:Character->Void = func.removeFirst(); if(_f != null) _f(this);
				} if(f != frame){frame = f; updateFrame();}
			} if(flashCt > 0){flashCt--; if(flashCt == 0) clearColor();}
		} if(dirty == 2) renderBitmap(); if(dirty > 0){renderFrame(); dirty = 0;}
	}
	
	private static var eyewhites:Array<BitmapData>; public static var EYE_WHITE:Int;
	private static function getEyeWhites(base:BitmapData):BitmapData {
		var b:BitmapData = base.clone(); for(y in 0...b.height) for(x in 0...b.width){
			var p:Int = b.getPixel32(x,y); if(p != EYE_WHITE) b.setPixel32(x,y,0);
		} return b;
	}
	private static function getEyeWhite(base:BitmapData, type:Int):BitmapData {
		if(EYE_WHITE == 0) return null;
		if(eyewhites == null) eyewhites = new Array<BitmapData>();
		if(eyewhites[type] == null) eyewhites[type] = getEyeWhites(base); return eyewhites[type];
	}
	
	public inline function setX(_x:Int):Void {x = _x*Tile.tile_size-(64-Tile.tile_size)*0.5;}
	public inline function setY(_y:Int):Void {y = _y*Tile.tile_size-(64-Tile.tile_size);}
	public inline function setPos(_x:Int, _y:Int):Void {setX(_x); setY(_y);}
	public inline function getX():Int {return Math.floor((x+(64-Tile.tile_size)*0.5)/Tile.tile_size+0.5);}
	public inline function getY():Int {return Math.floor((y+(64-Tile.tile_size))/Tile.tile_size+0.5);}
	public inline function getWorldX():Float {return x+(64-Tile.tile_size)*0.5;}
	public inline function getWorldY():Float {return y+(64-Tile.tile_size);}
	public inline function getDir():Int {return Math.floor((frame-Character.WALK_ST)/Character.WALK_LEN);}
}
