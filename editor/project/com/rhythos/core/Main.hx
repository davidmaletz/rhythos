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

import com.rhythos.core.game.Game;
import haxe.Timer;
import nme.display.BitmapData;
import nme.display.Sprite;
import nme.errors.Error;
import nme.events.Event;
import nme.events.EventDispatcher;
import nme.events.KeyboardEvent;
import nme.Lib;
import nme.media.Sound;
import nme.media.SoundChannel;
import nme.media.SoundTransform;
import nme.net.SharedObject;
import nme.ui.Keyboard;
import nme.utils.ByteArray;
#if neko
import sys.io.FileSeek;
#end

/**
 * TODO:
 * SFX and BGM vol should be customizable, not local variable = 1
 * Walking around/out of battle stuff?
 * Load Project data for: System (frame, font, bg), Skin, Hair, Eye colors, Weapons, Equipment, Spells, Enemies
 * Extra battle engines?
 * Java version?
 */

class Main extends Sprite {
	private static var pressed:Array<Int> = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]; public static var instance:Main; private var player:Character;
	public static inline var UP:Int = 0; public static inline var LEFT:Int = 1; public static inline var RIGHT:Int = 2;
	public static inline var DOWN:Int = 3; public static inline var ENTER:Int = 4; public static inline var ESCAPE:Int = 5;
	public static var width:Int; public static var height:Int;
	private static var bgmc:SoundChannel; private static var cur_bgm:Sound;
	private static inline var SPACE:Int = 10; public static inline var MENU:Int = 0;
	public static inline var W:Int = 6; public static inline var A:Int = 7; public static inline var D:Int = 8; public static inline var S:Int = 9;
	public function new() {
		super();
		#if iphone
		Lib.current.stage.addEventListener(Event.RESIZE, init);
		#else
		addEventListener(Event.ADDED_TO_STAGE, init);
		#end
	}
	public static function indexOf<T>(ar:Array<T>, e:T):Int {
		var end:Int = ar.length; for(i in 0...end) if(ar[i] == e) return i; return -1;
	}
	public static function safeEnterFrame(e:EventDispatcher, f:Event->Void, first:Bool=false):Void {
		function _init(ev:Event):Void {e.removeEventListener(Event.ENTER_FRAME, f); e.addEventListener(Event.ENTER_FRAME, f); if(first) f(ev);}
		function _destroy(ev:Event):Void {e.removeEventListener(Event.ENTER_FRAME, f);}
		e.addEventListener(Event.ADDED_TO_STAGE, _init); e.addEventListener(Event.REMOVED_FROM_STAGE, _destroy);
	}
	
	public static function removeAllChildren(s:Sprite):Void {while(s.numChildren > 0) s.removeChildAt(0);}

	private static var settings:SharedObject;
	private function init(e) {
		instance = this; removeEventListener(Event.ADDED_TO_STAGE, init); Project.load(); Main.width = Std.int(stage.stageWidth/scaleX);
		Main.height = Std.int(stage.stageHeight/scaleY); Lib.current.stage.addEventListener(KeyboardEvent.KEY_DOWN, key_down);
		Lib.current.stage.addEventListener(KeyboardEvent.KEY_UP, key_up);
		settings = SharedObject.getLocal("settings"); var d:Dynamic = Reflect.field(settings.data, "bgm_vol");
		if(d == null) BGM_VOL = 5; else BGM_VOL = d; d = Reflect.field(settings.data, "sfx_vol");
		if(d == null) SFX_VOL = 5; else SFX_VOL = d;
		player = new Character("Test", 0, 8000, 8000);
		player.equip[0] = new com.rhythos.core.equipment.Equipment("325a90048ef03ffb", 1,1);
		player.equip[1] = new com.rhythos.core.equipment.Equipment("add6fde6a0f6ac93", 1,1);
		addChild(new Game());
	}
	public static inline function getPlayer():Character {return instance.player;}
	private static inline var KEY_W:Int = 87; private static inline var KEY_A:Int = 65;
	private static inline var KEY_S:Int = 83; private static inline var KEY_D:Int = 68;
	private function key_down(e:KeyboardEvent):Void {
		switch(e.keyCode){
			case Keyboard.UP: if(pressed[UP]%3 == 0) pressed[UP] = 2;
			case Keyboard.LEFT: if(pressed[LEFT]%3 == 0) pressed[LEFT] = 2;
			case Keyboard.RIGHT: if(pressed[RIGHT]%3 == 0) pressed[RIGHT] = 2;
			case Keyboard.DOWN: if(pressed[DOWN]%3 == 0) pressed[DOWN] = 2;
			case KEY_W: if(pressed[W]%3 == 0) pressed[W] = 2;
			case KEY_A: if(pressed[A]%3 == 0) pressed[A] = 2;
			case KEY_D: if(pressed[D]%3 == 0) pressed[D] = 2;
			case KEY_S: if(pressed[S]%3 == 0) pressed[S] = 2;
			case Keyboard.ENTER: if(pressed[ENTER]%3 == 0) pressed[ENTER] = 2;
			case Keyboard.SPACE: if(pressed[SPACE]%3 == 0) pressed[SPACE] = 2;
			case Keyboard.ESCAPE: if(pressed[ESCAPE]%3 == 0) pressed[ESCAPE] = 2;
		}
	}
	private function key_up(e:KeyboardEvent):Void {
		switch(e.keyCode){
			case Keyboard.UP: if(pressed[UP] <= 1) pressed[UP] = 0; else pressed[UP] = 3;
			case Keyboard.LEFT: if(pressed[LEFT] <= 1) pressed[LEFT] = 0; else pressed[LEFT] = 3;
			case Keyboard.RIGHT: if(pressed[RIGHT] <= 1) pressed[RIGHT] = 0; else pressed[RIGHT] = 3;
			case Keyboard.DOWN: if(pressed[DOWN] <= 1) pressed[DOWN] = 0; else pressed[DOWN] = 3;
			case KEY_W: if(pressed[W] <= 1) pressed[W] = 0; else pressed[W] = 3;
			case KEY_A: if(pressed[A] <= 1) pressed[A] = 0; else pressed[A] = 3;
			case KEY_D: if(pressed[D] <= 1) pressed[D] = 0; else pressed[D] = 3;
			case KEY_S: if(pressed[S] <= 1) pressed[S] = 0; else pressed[S] = 3;
			case Keyboard.ENTER: if(pressed[ENTER] <= 1) pressed[ENTER] = 0; else pressed[ENTER] = 3;
			case Keyboard.SPACE: if(pressed[SPACE] <= 1) pressed[SPACE] = 0; else pressed[SPACE] = 3;
			case Keyboard.ESCAPE: if(pressed[ESCAPE] <= 1) pressed[ESCAPE] = 0; else pressed[ESCAPE] = 3;
		}
	}
	private static var SFX_VOL:Int; private static var BGM_VOL:Int; private static var bgm_vol:Float;
	private static var asset_cache:Hash<Dynamic> = new Hash<Dynamic>();
	private static function getAsset(id:String):Dynamic {
		if(asset_cache.exists(id)) return asset_cache.get(id); var a:Dynamic = null;
		#if neko
		if(id.charCodeAt(0) == 105){
			var f = sys.io.File.read("assets/A"+id, true); f.bigEndian = true;
			var w = f.readInt31(), h = f.readInt31(), len = f.readInt31();
			var b = new BitmapData(w, h, true);
			var ar = ByteArray.fromBytes(f.read(len)); f.close();
			ar.inflate(); b.setPixels(new nme.geom.Rectangle(0,0,w,h), ar); a = b;
		} else if(id.charCodeAt(1) == 115){
			//TODO
		} else {
			a = ByteArray.fromBytes(sys.io.File.getBytes("assets/A"+id));
		}
		#else
		var c = Type.resolveClass("A"+id); if(c != null) a = Type.createInstance(c,[]);
		#end
		asset_cache.set(id, a); return a;
	}
	public static function readID(d:ByteArray):String {
		var ret:String = null;
		for(i in 0...8){
			var ch = d.readUnsignedByte();
			if(ret == null && ch == 0) continue;
			if(ret == null) ret = StringTools.hex(ch);
			else ret += StringTools.hex(ch,2);
		} return ret.toLowerCase();
	}
	public static function playSFX(id:String):SoundChannel {
		var c = getAsset("s"+id); if(c != null){
			var s:Sound = cast(c, Sound); var vol:Float = 1; return s.play(0,0,new SoundTransform(SFX_VOL*0.2*vol));
		} ResourceError("sound (sfx)",id); return null;
	}
	public static function loopSFX(id:String):SoundChannel {
		var c = getAsset("s"+id); if(c != null){
			var s:Sound = cast(c, Sound); var vol:Float = 1; return s.play(0,0x3FFFFFFF,new SoundTransform(SFX_VOL*0.2*vol));
		} ResourceError("sound (sfx)",id); return null;
	}
	public static function getBitmap(id:String):BitmapData {
		var c = getAsset("i"+id); if(c == null){ResourceError("image",id); return null;}
		return cast(c, BitmapData);
	}
	public static function getData(type:String, id:String):ByteArray {
		var c = getAsset(type+id); if(c == null){ResourceError("data",type+id); return null;}
		return cast(c, ByteArray);
	}
	public static function getBGM(id:String):Sound {
		var c = getAsset("s"+id); if(c == null){ResourceError("sound (bgm)",id); return null;}
		return cast(c, Sound);
	}
	public static function playBGM(s:Sound, loops:Int=0x3FFFFFFF, st:Float=0):SoundChannel { var vol:Float = 1;
		if(s == null || cur_bgm == s) return bgmc; cur_bgm = s; if(bgmc != null) bgmc.stop(); bgm_vol = vol; bgmc = s.play(st, loops, new SoundTransform(BGM_VOL*0.2*bgm_vol)); return bgmc;
	}
	public static inline function getSFXVol():Int {return SFX_VOL;}
	public static inline function setSFXVol(v:Int):Void {
		SFX_VOL=v; Reflect.setField(settings.data, "sfx_vol", SFX_VOL); settings.flush();
	}
	public static inline function getBGMVol():Int {return BGM_VOL;}
	public static inline function setBGMVol(v:Int):Void {
		BGM_VOL=v; if(bgmc != null) bgmc.soundTransform = new SoundTransform(BGM_VOL*0.2*bgm_vol);
		Reflect.setField(settings.data, "bgm_vol", BGM_VOL); settings.flush();
	}
	public static function stopBGM():Void {if(bgmc != null){bgmc.stop(); cur_bgm = null; bgmc = null;}}
	private static var pauseCt:Int = 0;
	public static inline function pause():Int {pauseCt++; return pauseCt;}
	public static inline function unpause():Void {pauseCt--;}
	public static inline function pauseLevel():Int {return pauseCt;}
	public static inline function isPaused():Bool {return pauseCt > 0;}
	public static inline function isPressed(dir:Int, allowPaused:Int=0):Bool {return (pressed[dir] >= 2 || pressed[dir+6] >= 2) && (pauseCt == allowPaused);}
	public static inline function isHeld(dir:Int, allowPaused:Int=0):Bool {
		return (pressed[dir] >= 1 || pressed[dir+6] >= 1) && (pauseCt == allowPaused);}
	public static function resetPressed(allowPaused:Int=0):Void {
		if(pauseCt == allowPaused){
			if(pressed[UP] == 2) pressed[UP] = 1;
			if(pressed[LEFT] == 2) pressed[LEFT] = 1;
			if(pressed[RIGHT] == 2) pressed[RIGHT] = 1;
			if(pressed[DOWN] == 2) pressed[DOWN] = 1;
			if(pressed[W] == 2) pressed[W] = 1;
			if(pressed[A] == 2) pressed[A] = 1;
			if(pressed[D] == 2) pressed[D] = 1;
			if(pressed[S] == 2) pressed[S] = 1;
			if(pressed[ENTER] == 2) pressed[ENTER] = 1;
			if(pressed[SPACE] == 2) pressed[SPACE] = 1;
			if(pressed[ESCAPE] == 2) pressed[ESCAPE] = 1;
			if(pressed[UP] == 3) pressed[UP] = 0;
			if(pressed[LEFT] == 3) pressed[LEFT] = 0;
			if(pressed[RIGHT] == 3) pressed[RIGHT] = 0;
			if(pressed[DOWN] == 3) pressed[DOWN] = 0;
			if(pressed[W] == 3) pressed[W] = 0;
			if(pressed[A] == 3) pressed[A] = 0;
			if(pressed[D] == 3) pressed[D] = 0;
			if(pressed[S] == 3) pressed[S] = 0;
			if(pressed[ENTER] == 3) pressed[ENTER] = 0;
			if(pressed[SPACE] == 3) pressed[SPACE] = 0;
			if(pressed[ESCAPE] == 3) pressed[ESCAPE] = 0;
		}
	}
	static public function main() {
		var stage = Lib.current.stage;
		stage.scaleMode = nme.display.StageScaleMode.SHOW_ALL;
		stage.align = nme.display.StageAlign.TOP;
		Lib.current.addChild(new Main());
	}
	
	private static function ResourceError(type:String, id:String):Void {
		trace("Unable to locate "+type+" with id:\n'"+id+"'");
	}
}
