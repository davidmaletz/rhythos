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
import com.davidmaletz.mrpg.equipment.Weapon;
import com.davidmaletz.mrpg.ui.CharSheet;
import com.davidmaletz.mrpg.ui.Options;
import com.davidmaletz.mrpg.ui.DialogBox;
import com.davidmaletz.mrpg.ui.Frame;
import com.davidmaletz.mrpg.ui.Preloader;
import com.davidmaletz.mrpg.ui.Status;
import com.davidmaletz.mrpg.ui.Bar;
import com.davidmaletz.mrpg.ui.BounceWriter;
import haxe.Timer;
import nme.display.Bitmap;
import nme.display.DisplayObject;
import nme.display.Sprite;
import nme.events.Event;
import nme.media.Sound;
import nme.media.SoundChannel;

class Battle extends Sprite {
	public static inline var PLAYER_X:Float = 272-20; public static inline var ENEMY_X:Float = 128+20; public static inline var CHAR_Y:Float = 120;
	private var etype:EnemyType; private var channel:SoundChannel; public var player:Character; private var enemy:Character;
	private var status:Status; private var lastTime:Int; private var length:Float; private var spells:Sprite;
	private var cur_score:Int; public var state:Int; private var arrows:Int; private var combo:Int;
	public function new(p:Character, e:EnemyType){
		super(); etype = e; var snd:Sound=e.getSound(); length = snd.length; channel_pos = Math.NaN;
		addBG(); player = p; p.reset(); p.setFrame(p.getIdle()); p.x = PLAYER_X; p.y = CHAR_Y;
		enemy = e.getChar(); enemy.x = ENEMY_X; enemy.y = CHAR_Y; enemy.scaleX = -enemy.scaleY; addChild(enemy); addChild(p);
		addChild(player.getTop()); addChild(enemy.getTop()); cur_score = Main.score; state = -1; combo = 0;
		arrows = Math.floor(Math.min(999,enemy.max_health/(((CharSheet.indexOf(EnemyType.ENEMIES,e) == 6)?1:enemy.phys_def)*0.5*(p.weapon.lowDmg+p.weapon.highDmg+2*p.level*20))+4));
		status = new Status(); addChild(status); status.enemy.setText(enemy.cname); status.player.setText(player.cname);
		lastTime = Std.int(length/1000.0-60.0/e.getBPM()*e.getLead()); status.time.setText(Std.string(lastTime)); status.score.setText(getScore());
		if(player.weapon.getType() == Weapon.BOW) status.setArrows(arrows); else status.combo.setPercent(0);
		Main.safeEnterFrame(this, enter_frame); enemyPattern = e.getPattern(); enemy.setFrame(enemy.getIdle()); spells = new Sprite(); addChild(spells);
	}
	private function addBG():Void {
		var bg:DisplayObject = null; if(etype.bg==-2) bg = new StarBG();
		else{if(etype.bg==-1) bg = new Bitmap(Frame.background); else bg = new Bitmap(Main.getBitmap("bg",etype.bg));}
		if(bg != null){bg.scaleX = bg.scaleY = 2; addChild(bg);}
	}
	private function updateBar(bar:Bar, p:Float, d:Float):Void {
		var f:Float = bar.getPercent(); if(f > p){f -= d; if(p > f) f = p; bar.setPercent(f);}
		else if(f < p){f += d; if(p < f) f = p; bar.setPercent(f);}
	}
	private function snd_complete(e:Event):Void {if(state == 0) state = 1;}
	private function handleFrame():Void {
		var t:Int = Std.int((length-channel.position)/1000.0); if(lastTime != t){lastTime = t; status.time.setText(Std.string(lastTime));}
		var f:Int = 0; if(player.queueLength() <= 1 && Main.isHeld(Main.LEFT)){
			var type:Int = player.weapon.getType(); if(arrows == 0) type = Weapon.SLASH; switch(type){
				case Weapon.SLASH: f = player.queueSlash(attackPos); player.setFunction(f, weaponSt); player.setFunction(f+4, weaponHit2);
				case Weapon.THRUST: f = player.queueThrust(attackPos); player.setFunction(f, weaponSt); player.setFunction(f+5, weaponHit2);
				case Weapon.BOW: f = player.queueBow(); player.setFunction(f+((player.getQueuedFrame(f)==Character.FIRE_ST)?5:1), weaponSt);
			} if(type != Weapon.BOW){
				player.setFunction(player.queueLength()-1, attackEnd); player.queueIdle(player.weapon.cooldown);
			} else{player.setFunction(player.queueLength()-1, fireBow); arrows--; status.setArrows(arrows);}
		}
		if(player.queueLength() <= 1 && Main.isHeld(Main.RIGHT)){
			castSpell(player);
		} if(player.queueLength() <= 1 && Main.isHeld(Main.UP)){
			f = player.queueWalk(Character.LEFT); player.setFunction(f, playEvade); player.setFunction(f+2, evadeSt); player.setFunction(f+5, evadeEnd);
		} if(player.queueLength() <= 1 && Main.isHeld(Main.DOWN)){
			var c:Int = Spell.DEFEND.getCost(); if(c <= player.mana){
				f = player.queueCast(); player.setFunction(f, playDefendSFX);
				player.setFunction(f+2, defendStart); player.queueIdle(9);
				player.setFunction(player.queueLength()-2, defendEnd); player.mana -= c;
			}
		} Main.resetPressed();
	}
	private function playDefendSFX(c:Character):Void {Spell.DEFEND.playSFX();}
	public function enemyAttack():Void {
		var f:Int = 0, type:Int = enemy.weapon.getType(); switch(type){
			case Weapon.SLASH: f = enemy.queueSlash(attackPos); enemy.setFunction(f, weaponSt); enemy.setFunction(f+4, weaponHit2);
			case Weapon.THRUST: f = enemy.queueThrust(attackPos); enemy.setFunction(f, weaponSt); enemy.setFunction(f+5, weaponHit2);
			case Weapon.BOW: f = enemy.queueBow(); enemy.setFunction(f+((enemy.getQueuedFrame(f)==Character.FIRE_ST)?5:1), weaponSt);
		} if(type != Weapon.BOW){
			enemy.setFunction(enemy.queueLength()-1, attackEnd); enemy.queueIdle(enemy.weapon.cooldown);
		} else{enemy.setFunction(enemy.queueLength()-1, fireBow); enemy.queueIdle(enemy.weapon.cooldown);}
	}
	public function enemyDodge():Void {
		var f:Int = enemy.queueWalk(Character.LEFT); enemy.setFunction(f, playEvade); enemy.setFunction(f+2, evadeSt);
		enemy.setFunction(f+5, evadeEnd);
	}
	public function enemyDefend():Bool {
		var f:Int = 0, c:Int = Spell.DEFEND.getCost(); if(c <= enemy.mana){
			f = enemy.queueCast(); enemy.setFunction(f, playDefendSFX); enemy.setFunction(f+2, defendStart); enemy.queueIdle(9);
			enemy.setFunction(enemy.queueLength()-2, defendEnd); enemy.mana -= c; return true;
		} else return false;
	}
	public inline function getOpponent(c:Character):Character {return (c==player)?enemy:player;}
	private function weaponSt(c:Character):Void {if(c.weapon.getType() != Weapon.BOW) attackPos(c); c.weapon.playSFX(arrows>0, c.getFrame()>Character.BOW_ST);}
	public function weaponHit(target:Character):Void {var c:Character = getOpponent(target); hit(target, c.weapon.lowDmg+c.level*20, c.weapon.highDmg+c.level*20, false);}
	private function weaponHit2(c:Character):Void {
		var target:Character = getOpponent(c); attackPos(c); var w:Weapon = (c == player && arrows == 0)?Equipment.WEAPONS[0]:c.weapon;
		hit(target, w.lowDmg+c.level*20, w.highDmg+c.level*20, false);
	}
	private function _playSFX(c:Character):Void {c.spell.playSFX();}
	public function castSpell(char:Character):Bool {
		if(char.spell == null) return false; var c:Int = char.spell.getCost(); if(c <= char.mana){
			var f:Int = char.queueCast(); char.setFunction(f, _playSFX); f += 3; char.setFunction(f, spellStart);
			char.queueIdle(12); char.spell.use(char,f); char.mana -= c; return true;
		} else return false;
	}
	private static function playEvade(c:Character):Void {Main.playSFX("evade");}
	private function evadeSt(c:Character):Void {c.startEvade(); c.y = CHAR_Y-20;}
	private function evadeEnd(c:Character):Void {c.resetDefense(); c.y = CHAR_Y;}
	private function defendStart(c:Character):Void {
		var s:SpellSprite = Spell.DEFEND.createSprite(); if(c == player) s.x += player.x; else {s.scaleX = -s.scaleX; s.x = enemy.x-s.x;} s.y += CHAR_Y;  c.startEvade(); spells.addChild(s);
	}
	private function defendEnd(c:Character):Void {c.resetDefense();}
	private function attackPos(c:Character):Void {
		var dist:Float = c.weapon.distance(); if(c == player) player.x = Math.min(PLAYER_X, enemy.x+dist); else enemy.x = Math.max(ENEMY_X, player.x-dist); c.top.x = c.x;
	}
	private function spellStart(c:Character):Void {
		var s:SpellSprite = c.spell.createSprite(); if(c == player) s.x += ENEMY_X; else {s.scaleX = -s.scaleX; s.x = PLAYER_X-s.x;} s.y += CHAR_Y; spells.addChild(s);
	}
	private function getPos(c:Character):Float {return (c == player)?PLAYER_X:ENEMY_X;}
	public function hit(c:Character, low:Int, high:Int, mag:Bool, blockable:Bool=true, mana:Float=0):Int {
		if(state != 0) return 0; var def:Float = (blockable)?((mag)?c.mag_def:c.phys_def):1, t:String = "MISS";
		if(c == enemy && !mag && combo == 4){
			combo = 0; switch(player.weapon.combo){
				case Weapon.CRITICAL_HIT: low *= 2; if(low < high) low = high; high *= 2; if(def != 0) addChild(new BounceWriter(Status.YELLOW,32,"CRITICAL HIT",8,108,24));
				case Weapon.ARMOR_PIERCE: def = 1; addChild(new BounceWriter(Status.YELLOW,32,"ARMOR PIERCE",8,108,24));
				case Weapon.MANA_GEN: player.mana += 500; if(player.mana > player.max_mana) player.mana = player.max_mana; addChild(new BounceWriter(Status.YELLOW,32,"MANA GEN",72,108,24));
			}
		} if(def == 0){
			addChild(new BounceWriter(Frame.TEXT,16,t,getPos(c)+32*c.scaleX-t.length*16*0.5,c.y+16)); if(c == player) Main.score += Std.int(high/10); else if(!mag) combo = 0; return 0;
		} var dmg:Int = Std.int((Math.random()*(high-low)+low)*0.1*def)*10; t = format(Std.string(dmg));
		addChild(new BounceWriter(0xe43c3c,16,t,getPos(c)+32*c.scaleX-t.length*16*0.5,c.y+16)); c.health -= dmg; c.flashRed();
		if(mana != 0){c.mana -= Std.int(dmg*mana); if(c.mana < 0) c.mana = 0;}
		if(!mag) Main.playSFX("hit");
		if(c == enemy){Main.score += Std.int(high/10); if(!mag) combo++;} return dmg;
	}
	public function heal(c:Character, low:Int, high:Int):Void {
		if(state != 0) return; var h:Int = Std.int((Math.random()*(high-low)+low)*0.1)*10, t:String = format(Std.string(h));
		addChild(new BounceWriter(0x4ad000,16,t,getPos(c)+32*c.scaleX-t.length*16*0.5,c.y+16));
		c.health += h; if(c.health > c.max_health) c.health = c.max_health; c.flashGreen();
	}
	public function hitDOT(c:Character, low:Int, high:Int, mag:Bool, blockable:Bool=true, mana:Float=0):Int {
		if(state != 0) return 0; var def:Float = (blockable)?((mag)?c.mag_def:c.phys_def):1; if(def == 0){if(c == player) Main.score += Std.int(high/10); return 0;}
		var dmg:Int = Std.int((Math.random()*(high-low)+low)*0.1*def)*10;
		if(dmg > 0){
			c.health -= dmg; c.flashRed(); if(c == enemy) Main.score += Std.int(high/10); if(mana != 0){c.mana -= Std.int(dmg*mana); if(c.mana < 0) c.mana = 0;}
			return dmg;
		} else return 0;
	}
	public function displayDmg(c:Character, dmg:Int):Void {
		var t:String = format(Std.string(dmg)); addChild(new BounceWriter(0xe43c3c,16,t,getPos(c)+32*c.scaleX-t.length*16*0.5,c.y+16));
	}
	private function attackEnd(c:Character):Void {c.x = (c==player)?PLAYER_X:ENEMY_X; c.y = CHAR_Y; c.top.x = c.x; c.top.y = CHAR_Y;}
	public function resetPlayer():Void {
		Main.removeAllChildren(spells); attackEnd(player); enemy.clearColor(); player.clearColor(); player.clearQueue(); player.setFrame(player.getIdle());
	}
	private function fireBow(c:Character):Void {
		var a:Arrow = c.weapon.getArrow(); if(c == player) a.x = player.x-30; else {a.scaleX = -a.scaleX; a.x = enemy.x+30;} a.y = CHAR_Y; a.target = getOpponent(c); spells.addChild(a);
	}
	private function endBattle():Void {
		if(state == 2) Main.instance.battleLost(); else Main.instance.battleComplete();
	}
	private function endDelay(d:DialogBox):Bool {Timer.delay(endBattle, 500); return true;}
	private function winBattle(c:Character):Void {
		var lvlup:DialogBox = null, l:Int = player.level, exp:Int = Main.experience, gold:Int = Main.gold, ds:Int = Main.score-Main.old_score;
		Main.finishBattle(etype.experience); Main.gold += etype.gold; var de:Int = Main.experience-exp, dg:Int = Main.gold-gold;
		if(l < player.level) lvlup = new DialogBox("\300Level Up!\300\377", null, true, 0, 24, 1, 1);
		function showLevelUp(d:DialogBox):Bool {if(lvlup != null){addChild(lvlup); Main.learnSpells(l);} d.onClose = endDelay; return false;}
		addChild(new DialogBox("\002\300"+player.cname+" is Victorious!\300\310\n\000\300EXP:\005\300\310 "+format(Std.string(de))+"!\376\n\310\004\300GOLD:\005\300\310 "+format(Std.string(dg))+"!\n\310\003\300SCORE:\005\300\310 "+format(Std.string(ds))+"!", showLevelUp, true, 1, 24, 4));
	}
	private function loseBattle(c:Character):Void {
		var lvlup:DialogBox = null, l:Int = player.level, exp:Int = Main.experience, ds:Int = Main.score-Main.old_score;
		Main.finishBattle(0); var de:Int = Main.experience-exp;
		if(l < player.level) lvlup = new DialogBox("\300Level Up!\300\377", null, true, 0, 24, 1, 1);
		function showLevelUp(d:DialogBox):Bool {if(lvlup != null){addChild(lvlup); Main.learnSpells(l);} d.onClose = endDelay; return false;}
		addChild(new DialogBox("\001\300"+player.cname+" was Defeated!\300\310\n\000\300EXP:\005\300\310 "+format(Std.string(de))+"!\376\n\310\003\300SCORE:\005\300\310 "+format(Std.string(ds))+"!\n\310Returning to town...", showLevelUp, true, 1, 24, 4));
	}
	private static var channel2:SoundChannel;
	private function playWeapSnd(c:Character):Void {c.weapon.playSFX();}
	private function victoryDance(c:Character):Void {
		Main.playBGM(((c==player)?Main.getBGM("victory"):Main.getBGM("defeat")), 0);
		var e:Character->Void = (c==player)?winBattle:loseBattle; var f:Int=0, type:Int = c.weapon.getType(); switch(type){
			case Weapon.SLASH: c.queueIdle(20); f=c.queueSlash(); c.queueIdle(12); c.setFunction(c.queueLength()-1, e);
			case Weapon.THRUST: c.queueIdle(20); f=c.queueThrust(); c.queueIdle(12); c.setFunction(c.queueLength()-1, e);
			case Weapon.BOW: c.queueIdle(20); f=c.queueBow(); c.queueIdle(12); c.setFunction(c.queueLength()-1, e);
		} c.setFunction(f, playWeapSnd);
	}
	public static inline var VICTORY_COL:Int = 0xebb453; public static inline var DEFEAT_COL:Int = 0x777777;
	private function victory():Void {
		addChild(new BounceWriter(VICTORY_COL,40,"VICTORY!!",(400-9*40)*0.5,(300-40)*0.5+30,48)); state = 3; enemy.clearQueue();
		var f:Int = enemy.queueDeath(); if(etype.onDefeat != null) enemy.setFunction(f,etype.onDefeat); player.queueIdle(1);
		channel2 = Main.loopSFX("point"); Main.battleTime(lastTime);
	}
	private function defeat():Void {
		addChild(new BounceWriter(DEFEAT_COL,40,"DEFEAT",(400-6*40)*0.5,(300-40)*0.5+30,48)); state = 2; player.clearQueue();
		player.queueDeath(); victoryDance(enemy);
	}
	public function spellWarning():Void {
		addChild(new BounceWriter(Status.RED,24,"SPELL INCOMING!",(400-15*24)*0.5,(300-24)*0.5+30,24));
	}
	private function getScore():String {return format(Std.string(cur_score));}
	public static function format(s:String):String {
		var ret:String="", l:Int=0, len:Int=s.length;
		while(l < len-3){
			l += 3; ret = ","+s.substr(len-l,3)+ret;
		} ret = s.substr(0,len-l)+ret; return ret;
	}
	private var channel_pos:Float; private var enemyPattern:EnemyPattern;
	private function enemyAction(c:Character=null):Void {
		if(state != 0) return; enemyPattern.next(this); var i:Int = enemy.queueLength()-1; var f:Character->Void = enemy.getQueuedFunc(i);
		if(f == null) enemy.setFunction(i, enemyAction); else enemy.setFunction(i, function(c:Character):Void {f(c); enemyAction(c);});
	}
	private function begin():Void {state = 0; enemyAction();}
	private function showFight():Void {addChild(new BounceWriter(Status.GREEN,40,"FIGHT!!",(400-7*40)*0.5,(300-40)*0.5+30,48));}
	private function showSt():Void {
		var t:Float = 60.0/etype.getBPM()*etype.getLead(), t2:Float = t/4;
		addChild(new BounceWriter(Status.RED,40,"Ready?",(400-6*40)*0.5,(300-40)*0.5+30,Std.int(t2*24)));
		function getSet():Void {addChild(new BounceWriter(Status.YELLOW,40,"Get Set!",(400-8*40)*0.5,(300-40)*0.5+30,Std.int(t2*24)));}
		Timer.delay(getSet,Std.int(t2*2000)-292); Timer.delay(showFight,Std.int(t*1000)-292); Timer.delay(begin,Std.int(t*1000));
	}
	public function pause():Void {
		if(channel == null || state >= 2) return;
		channel_pos = channel.position; channel.removeEventListener(Event.SOUND_COMPLETE, snd_complete); Main.stopBGM(); channel = null;
	}
	public function unpause():Void {
		if(channel != null || state >= 2) return; if(Math.isNaN(channel_pos)){showSt(); channel_pos = 0;}
		channel = Main.playBGM(etype.getSound(),0,channel_pos); channel.addEventListener(Event.SOUND_COMPLETE, snd_complete);
	}
	private function pauseMenu(i:Int):Bool {if(i == 4){state = 2; Main.instance.returnToTown();} return true;}
	private function enter_frame(e:Event):Void {
		if(state == 3){
			if(lastTime <= 0 && lastTime != -10000){if(channel2 != null){channel2.stop(); channel2 = null;} if(!Main.isPaused()){victoryDance(player); lastTime=-10000;}}
			else {if(lastTime > 0){lastTime--; status.time.setText(Std.string(lastTime)); Main.score += 5;}}
		} if(state == 0 && Main.isPressed(Main.ESCAPE)){
			Main.resetPressed(); Main.playCancel();
			addChild(new Options(["Game Paused.", "Return to Game", Options.BGM_VOL, Options.SFX_VOL, "Return to Town"], [false,true,true,true,true], pauseMenu));
			return;
		} if(state != 3){if(channel == null){Main.resetPressed(); return;}
		if(state == 0){if(player.health <= 0) defeat(); else if(enemy.health <= 0) victory();}
		else if(state == 1){
			if(player.health/player.max_health >= enemy.health/enemy.max_health) victory(); else defeat();
		} if(state == 0) handleFrame();} updateBar(status.enemy_hp, Math.max(0,enemy.health/enemy.max_health), 2/150);
		updateBar(status.player_hp, Math.max(0,player.health/player.max_health), 2/150);
		if(enemy.mana < enemy.max_mana) enemy.mana+=10; updateBar(status.enemy_mp, Math.max(0,enemy.mana/enemy.max_mana), 2/110);
		if(player.mana < player.max_mana) player.mana+=10; updateBar(status.player_mp, Math.max(0,player.mana/player.max_mana), 2/110);
		if(player.weapon.getType() != Weapon.BOW) updateBar(status.combo, combo*0.25, 1/40);
		if(Main.score > cur_score){cur_score += 25; if(cur_score > Main.score) cur_score = Main.score; status.score.setText(getScore());}
	}
}
