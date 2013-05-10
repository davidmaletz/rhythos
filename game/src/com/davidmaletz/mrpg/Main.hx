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

import com.davidmaletz.mrpg.equipment.Equipment;
import com.davidmaletz.mrpg.game.Game;
import com.davidmaletz.mrpg.transition.Mosaic;
import com.davidmaletz.mrpg.ui.Achievements;
import com.davidmaletz.mrpg.ui.DialogBox;
import com.davidmaletz.mrpg.ui.GameMenu;
import com.davidmaletz.mrpg.ui.GameSaves;
import com.davidmaletz.mrpg.ui.Instructions;
import com.davidmaletz.mrpg.ui.LearnSpell;
import com.davidmaletz.mrpg.ui.MainMenu;
import com.davidmaletz.mrpg.ui.SavedCharacter;
import haxe.Timer;
import nme.display.Bitmap;
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
import com.davidmaletz.mrpg.ui.CharSheet;
import com.davidmaletz.mrpg.ui.CreateChar;
import com.davidmaletz.mrpg.ui.Frame;
import nme.utils.ByteArray;

/**
 * TODO:
 * SFX and BGM vol should be customizable, not local variable = 1
 * Walking around/out of battle stuff?
 * Load Project data for: System (frame, font, bg), Skin, Hair, Eye colors, Weapons, Equipment, Spells, Enemies
 * Extra battle engines?
 * Java version?
 */

class Main extends Sprite {
	private static var pressed:Array<Int> = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]; public static var battle:Battle; public static var instance:Main; private var player:Character;
	public static inline var UP:Int = 0; public static inline var LEFT:Int = 1; public static inline var RIGHT:Int = 2;
	public static inline var DOWN:Int = 3; public static inline var ENTER:Int = 4; public static inline var ESCAPE:Int = 5;
	public static inline var BASE_HP:Int = 8000; public static inline var BASE_MP:Int = 8000;
	private static var bgmc:SoundChannel; private static var cur_bgm:Sound; public static var experience:Int; public static var gold:Int;
	public static var old_score:Int; public static var score:Int; public static var max_type:Int;
	private static inline var SPACE:Int = 10; public static inline var MENU:Int = 0; private static var cur_type:Int;
	public static var weapons:BoolArray = new BoolArray(); public static var spells:BoolArray = new BoolArray();
	public static var helms:BoolArray = new BoolArray(); public static var shirts:BoolArray = new BoolArray(); public static var achievements:BoolArray = new BoolArray();
	public static var pants:BoolArray = new BoolArray(); public static var wins:Array<Int>; public static var losses:Array<Int>;
	public static inline var W:Int = 6; public static inline var A:Int = 7; public static inline var D:Int = 8; public static inline var S:Int = 9;
	public function new() {
		super();
		#if iphone
		Lib.current.stage.addEventListener(Event.RESIZE, init);
		#else
		addEventListener(Event.ADDED_TO_STAGE, init);
		#end
	}
	
	public static function safeEnterFrame(e:EventDispatcher, f:Event->Void, first:Bool=false):Void {
		function _init(ev:Event):Void {e.addEventListener(Event.ENTER_FRAME, f); if(first) f(ev);}
		function _destroy(ev:Event):Void {e.removeEventListener(Event.ENTER_FRAME, f);}
		e.addEventListener(Event.ADDED_TO_STAGE, _init); e.addEventListener(Event.REMOVED_FROM_STAGE, _destroy);
	}
	
	public static function removeAllChildren(s:Sprite):Void {while(s.numChildren > 0) s.removeChildAt(0);}

	private static var asset_cache:Hash<Dynamic> = new Hash<Dynamic>();
	private static function getAsset(id:String, t1:Int=-1, t2:Int=-1):Dynamic {
		if(t1 >= 0) id += Std.string(t1); if(t2 >= 0) id += "_"+Std.string(t2);
		if(asset_cache.exists(id)) return asset_cache.get(id);
		var c = Type.resolveClass("assets."+id); var a:Dynamic = null;
		if(c != null) a = Type.createInstance(c,[]); asset_cache.set(id, a); return a;
	}
	public static function playSFX(id:String, t1:Int=-1, t2:Int=-1):SoundChannel {
		var c = getAsset(id,t1,t2); if(c != null){
			var s:Sound = cast(c, Sound); var vol:Float = 1; return s.play(0,0,new SoundTransform(SFX_VOL*0.2*vol));
		} ResourceError("sfx",id,t1,t2); return null;
	}
	public static function loopSFX(id:String, t1:Int=-1, t2:Int=-1):SoundChannel {
		var c = getAsset(id,t1,t2); if(c != null){
			var s:Sound = cast(c, Sound); var vol:Float = 1; return s.play(0,0x3FFFFFFF,new SoundTransform(SFX_VOL*0.2*vol));
		} ResourceError("sfx",id,t1,t2); return null;
	}
	private static var settings:SharedObject;
	private function init(e) {
		removeEventListener(Event.ADDED_TO_STAGE, init); Project.load();
		Lib.current.stage.addEventListener(KeyboardEvent.KEY_DOWN, key_down);
		var len:Int = EnemyType.ENEMIES.length; wins = new Array<Int>(); losses = new Array<Int>(); for(i in 0...len){wins.push(0); losses.push(0);}
		Lib.current.stage.addEventListener(KeyboardEvent.KEY_UP, key_up);
		settings = SharedObject.getLocal("settings"); var d:Dynamic = Reflect.field(settings.data, "bgm_vol");
		if(d == null) BGM_VOL = 5; else BGM_VOL = d; d = Reflect.field(settings.data, "sfx_vol");
		if(d == null) SFX_VOL = 5; else SFX_VOL = d; instance = this; addChild(new MainMenu()); addEventListener(Event.ENTER_FRAME, enter_frame);
	}
	public static inline function getPlayer():Character {return instance.player;}
	private static var _game:Game;
	public static function loadCharacter(sc:SavedCharacter):Void {
		instance.transition(); experience = sc.experience; gold = sc.gold; score = old_score = sc.score; max_type = sc.max_type;
		weapons.setAr(sc.weapons); spells.setAr(sc.spells); helms.setAr(sc.helms); shirts.setAr(sc.shirts); pants.setAr(sc.pants);
		achievements.setAr(sc.achievements); var len:Int = wins.length; for(i in 0...len){wins[i] = sc.wins[i]; losses[i] = sc.losses[i];}
		instance.player = sc.getChar(); Achievements.loadAchievements(); _game = new Game(); instance.addChild(_game);
	}
	public static inline function battleTime(s:Int):Void {Achievements.battleTime(s);}
	public function battleLost():Void {
		losses[cur_type]++; returnToTown(); Achievements.loseToEnemy(cur_type);
	}
	public function battleComplete():Void {
		wins[cur_type]++; returnToTown(); var first:Bool = false; if(cur_type+1 > max_type){
			max_type = cur_type+1; first = true;
		} Achievements.defeatEnemy(cur_type, first);
	}
	private function transition():Void {battle = null; new Mosaic(); removeAllChildren(this);}
	public function mainMenu():Void {transition(); addChild(new MainMenu());}
	public function returnToTown():Void {transition(); _game.resetPlayer(); addChild(_game);}
	public static inline function experienceToNextLevel(l:Int):Int {return l*l*250+l*1250+1000;}
	private static inline var MAX_LEVEL:Int = 30;
	public static inline function levelFromExperience(e:Int):Int {
		var a:Int = 250, b:Int = 1250, c:Int = 1000-e-1; return Math.ceil(Math.min(MAX_LEVEL,(-b+Math.sqrt(b*b-4*a*c))/(2*a)));
	}
	public static function learnSpells(old:Int):Void {
		var lvl:Int = Math.floor(old/5+1)*5; var handleLearn:Bool->Int->Void=null;
		function func(closed:Bool, sel:Int):Void {
			if(closed){if(instance.player.level >= lvl){lvl += 5; instance.addChild(new LearnSpell(handleLearn));}}
			else spells.set(sel,true);
		} handleLearn = func; handleLearn(true,0);
	}
	public static function finishBattle(bonus:Int):Void {
		experience += ((score-old_score)>>3)+bonus; old_score = score; instance.player.setLevel(levelFromExperience(experience));
	}
	public static var instructions:Bool = false; //TODO: return to true to show instructions?
	public function startBattle(i:Int):Void {
		function doStartBattle(d:DialogBox=null):Bool {
			transition(); player.restore(); cur_type = i; battle = new Battle(player, EnemyType.ENEMIES[i]); addChild(battle); return true;
		} if(instructions) addChild(new Instructions(doStartBattle)); else doStartBattle();
	}
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
	public static function playClick():Void {playSFX("click");}
	public static function playSelect():Void {playSFX("select");}
	public static function playCancel():Void {playSFX("close");}
	public static function playChange():Void {playSFX("change");}
	private static var SFX_VOL:Int; private static var BGM_VOL:Int; private static var bgm_vol:Float;
	public static function getBitmap(id:String, t1:Int=-1, t2:Int=-1):BitmapData {
		var c = getAsset(id,t1,t2); if(c == null){ResourceError("image",id,t1,t2); return null;}
		return cast(c, Bitmap).bitmapData;
	}
	public static function getData(id:String, t1:Int=-1, t2:Int=-1):ByteArray {
		var c = getAsset(id,t1,t2); if(c == null){ResourceError("data",id,t1,t2); return null;}
		return cast(c, ByteArray);
	}
	public static function getBGM(id:String, t1:Int=-1, t2:Int=-1):Sound {
		var c = getAsset(id,t1,t2); if(c == null){ResourceError("bgm",id,t1,t2); return null;}
		return cast(c, Sound);
	}
	public static function getBPM(id:Int):Int {return 149;} //TODO: load song BPM
	public static function getLead(id:Int):Int {return 16;} //TODO: load song lead
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
	private static var pauseCt:Int;
	public static inline function pause():Int {if(pauseCt == 0 && battle != null) battle.pause(); pauseCt++; return pauseCt;}
	public static inline function unpause():Void {pauseCt--; if(pauseCt == 0 && battle != null) battle.unpause();}
	public static inline function pauseLevel():Int {return pauseCt;}
	public static inline function isPaused():Bool {return pauseCt > 0;}
	public static inline function isPressed(dir:Int, allowPaused:Int=0):Bool {return (pressed[dir] >= 2 || pressed[dir+6] >= 2) && (pauseCt == allowPaused);}
	public static inline function isHeld(dir:Int, allowPaused:Int=0):Bool {return (pressed[dir] >= 1 || pressed[dir+6] >= 1) && (pauseCt == allowPaused);}
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
	
	private static var queue:Dequeue<String> = new Dequeue<String>();
	private function enter_frame(e:Event):Void {
		if(pauseCt == 0 && queue.size() != 0) addChild(new DialogBox(queue.removeFirst(), null, true, 0, 24, 2));
	}
	private static function ResourceError(type:String, id:String, t1:Int, t2:Int):Void {
		if(t1 >= 0) id += Std.string(t1); if(t2 >= 0) id += "_"+Std.string(t2);
		queue.addLast("Unable to locate "+type+":\n'"+id+"'");
	}
}
