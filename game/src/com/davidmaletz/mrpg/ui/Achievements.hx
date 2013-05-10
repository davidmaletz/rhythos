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
package com.davidmaletz.mrpg.ui;
import com.davidmaletz.mrpg.Character;
import com.davidmaletz.mrpg.Dequeue;
import com.davidmaletz.mrpg.equipment.Equipment;
import nme.display.Sprite;
import nme.events.Event;

class Achievements extends Sprite {
	public static inline var NEVER_GIVE_UP:Int=0; public static inline var SPEEDY_GONZALES:Int=1;
	public static inline var PENNY_PINCHER:Int=2; public static inline var CLEAN_SWEEP:Int=3;
	private static var MAX_BLINK:Int=8; private static var SCROLL_H:Int=236; public static inline var ITEM_HT:Int=40;
	public static inline var SCROLL_Y:Int=24+16+10; private var sel:Int; private var arrow:Text; private var up_arrow:Text;
	private var down_arrow:Text; private var pause:Int; private var scroll:Sprite; private var blink_ct:Int;
	public function new() {
		super(); var w:Int=400,h:Int=300; Frame.drawFrame(graphics,w,h); pause = Main.pause();
		CharSheet._addTitle(this, "ACHIEVEMENTS ("+getCount()+"/23)", 16, w);
		Main.safeEnterFrame(this, handleKey); sel = 0; arrow = new Text(Status.YELLOW, 12, 12, 0, "\202"); arrow.x = 10; addChild(arrow);
		up_arrow = new Text(Status.YELLOW, 16, w, 1, "\200"); up_arrow.y = SCROLL_Y-16; addChild(up_arrow);
		down_arrow = new Text(Status.YELLOW, 16, w, 1, "\177"); down_arrow.y = SCROLL_Y+SCROLL_H; addChild(down_arrow);
		var end:Int = achievements.length; scroll = new Sprite(); for(i in 0...end){
			var t:Achievement = new Achievement(i); t.y = ITEM_HT*i; scroll.addChild(t);
		} var m:Sprite = new Sprite(); m.graphics.beginFill(0); m.graphics.drawRect(24,SCROLL_Y,w,SCROLL_H); m.graphics.endFill();
		addChild(m); scroll.mask = m; scroll.x = 24; scroll.y = SCROLL_Y; addChild(scroll); updateSel(); y = 300; blink_ct = MAX_BLINK;
	}
	public inline function getSel():Achievement {return cast(scroll.getChildAt(sel), Achievement);}
	private inline function updateSel():Void {
		var t:Achievement = getSel(); t.select(); if(scroll.y+t.y < SCROLL_Y) scroll.y = SCROLL_Y-t.y;
		else if(scroll.y+t.y > SCROLL_H+SCROLL_Y-(ITEM_HT-4)) scroll.y = SCROLL_H+SCROLL_Y-(ITEM_HT-4)-t.y; arrow.y = scroll.y+t.y+(ITEM_HT-4-12)*0.5;
		up_arrow.alpha = (scroll.y < SCROLL_Y)?1:0; down_arrow.alpha = (scroll.y > SCROLL_H+SCROLL_Y+4-achievements.length*ITEM_HT)?1:0;
	}
	public function cycleSel(d:Int):Void {
		getSel().deselect(); var l:Int = achievements.length;
		sel += d; if(sel >= l) sel = 0; else if(sel < 0) sel = l-1; updateSel(); Main.playClick();
	}
	private function exit():Void {if(pause > 0){Main.unpause(); pause = 0;} parent.removeChild(this);}
	public function handleKey(e:Event):Void {
		var dh:Int = 75; if(sel < 0){y += dh; if(y >= 300) exit(); return;} if(y > 0){y -= dh;
			if(y <= 0 && Main.score == 0 && Main.gold == 200 && Shop.firstShop){
				parent.addChild(new DialogBox("Hit '\004ESC\000' to exit.",null,true,0,24,1,1));
			} if(y <= 0) Shop.firstShop = false; return;
		} blink_ct--; if(blink_ct == 0){blink_ct = MAX_BLINK; up_arrow.visible = !up_arrow.visible; down_arrow.visible = !down_arrow.visible;}
		if(Main.isPressed(Main.ESCAPE, pause)){Main.resetPressed(pause); Main.playCancel(); sel = -2; return;}
		var u:Bool = Main.isPressed(Main.UP, pause), d:Bool = Main.isPressed(Main.DOWN, pause);
		if(u && !d) cycleSel(-1); if(d && !u) cycleSel(1);
		Main.resetPressed(pause);
	}
	
	//TODO: load Achievements from DB.
	public static var ACHIEVEMENT_NAMES:Array<String> = ["Been There, Slimed That", "Slayer", "Flower Power", "Sightless", "Muad'Dib",
		"Doppelganger", "Apprentice", "Adventurer", "Warrior", "Hero", "Champion", "Legend", "Blademaster", "Dragonslayer", "Spellcaster",
		"Sniper", "Arcane Master", "Collector", "Hack & Grind", "Never Give Up", "Speed Demon", "Penny Pincher", "Clean Sweep", "????"];
	public static var ACHIEVEMENT_DESC:Array<String> = ["Defeat the Slime.", "Defeat the Vampire.", "Defeat the Flower.", "Defeat the Beholder.",
		"Defeat the Sandworm.","Win the Game!", "Reach level 6.", "Reach level 11.", "Reach level 16.", "Reach level 21.", "Reach level 26.",
		"Reach level 31.", "Obtain the Longsword.", "Obtain the Dragonslayer.", "Obtain the Mace.", "Obtain the Recurve Bow.",
		"Obtain all arcane armors.", "Collect all items.", "Beat the same enemy twice.", "Beat an enemy after 2 or more tries.",
		"Win with at least 120 seconds remaining.", "Hold at least 10,000 gold at one time.", "Defeat every enemy exactly once.", "????????"];
	public static var SECRET:Array<Bool> = [false,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true];
	private static var achievements:Array<Bool>;
	public static function getCount():Int {
		var ret:Int=0; for(i in 0...achievements.length) if(achievements[i]) ret++; return ret;
	}
	public static inline function has(i:Int):Bool {return achievements[i];}
	public static function loadAchievements():Void {
		var end:Int; if(achievements == null){achievements = new Array<Bool>(); end = SECRET.length; for(i in 0...end) achievements.push(false);}
		achievements[0] = Main.wins[0] > 0; //Been There, Slimed That
		achievements[1] = Main.wins[3] > 0; //Slayer
		achievements[2] = Main.wins[7] > 0; //Flower Power
		achievements[3] = Main.wins[10] > 0; //Sightless
		achievements[4] = Main.wins[12] > 0; //Muad'Dib
		achievements[5] = Main.wins[14] > 0; //Doppelganger
		var p:Character = Main.getPlayer(); achievements[6] = p.level >= 5; //Apprentice
		achievements[7] = p.level >= 10; //Adventurer
		achievements[8] = p.level >= 15; //Warrior
		achievements[9] = p.level >= 20; //Hero
		achievements[10] = p.level >= 25; //Champion
		achievements[11] = p.level >= 30; //Legend
		achievements[12] = Main.weapons.get(3); //Swordmaster
		achievements[13] = Main.weapons.get(6); //Dragonslayer
		achievements[14] = Main.weapons.get(9); //Spellcaster
		achievements[15] = Main.weapons.get(12); //Sniper
		achievements[16] = Main.helms.get(6)&&Main.shirts.get(4)&&Main.pants.get(4); //Arcane Master
		achievements[17] = Main.weapons.hasAll(Equipment.WEAPONS.length)&&Main.helms.hasAll(Equipment.HELMS.length)&&
			Main.shirts.hasAll(Equipment.SHIRTS.length)&&Main.pants.hasAll(Equipment.PANTS.length); //Collector
		end = Main.wins.length; achievements[18]=false; for(i in 0...end) if(Main.wins[i] >= 2){achievements[18] = true; break;} //Hack & Grind
		achievements[19] = Main.achievements.get(NEVER_GIVE_UP); //Never Give Up
		achievements[20] = Main.achievements.get(SPEEDY_GONZALES); //Speedy Gonzales
		achievements[21] = Main.achievements.get(PENNY_PINCHER); //Penny Pincher
		achievements[22] = Main.achievements.get(CLEAN_SWEEP); //Clean Sweep
		updateGold();
	}
	private static var queue:Dequeue<Int> = new Dequeue<Int>(); private static var wait:Bool=false;
	private static function nextAchievement(d:DialogBox=null):Bool {
		if(queue.size() == 0){wait = false; return true;} wait = true; var i:Int = queue.removeFirst(); if(i == -1){
			Main.instance.addChild(new DialogBox("You beat the game!\nYou're \004AWESOME!\000\nLook forward to more in\nin the world of \002Rhythos\000!", nextAchievement));
		} else {
			Main.instance.addChild(new MedalDialog(i, nextAchievement));
		} return true;
	}
	private static inline function getAchievement(i:Int):Void {
		if(!achievements[i]){achievements[i] = true; queue.addLast(i); if(!wait) nextAchievement();}
	}
	private static function queueWin():Void {
		queue.addLast(-1); if(!wait) nextAchievement();
	}
	public static function updateLevel():Void {
		var p:Character = Main.getPlayer();
		if(p.level >= 5) getAchievement(6); //Apprentice
		if(p.level >= 10) getAchievement(7); //Adventurer
		if(p.level >= 15) getAchievement(8); //Warrior
		if(p.level >= 20) getAchievement(9); //Hero
		if(p.level >= 25) getAchievement(10); //Champion
		if(p.level >= 30) getAchievement(11); //Legend
	}
	public static function updateItems():Void {
		if(Main.weapons.get(3)) getAchievement(12); //Swordmaster
		if(Main.weapons.get(6)) getAchievement(13); //Dragonslayer
		if(Main.weapons.get(9)) getAchievement(14); //Spellcaster
		if(Main.weapons.get(12)) getAchievement(15); //Sniper
		if(Main.helms.get(6)&&Main.shirts.get(4)&&Main.pants.get(4)) getAchievement(16); //Arcane Master
		if(Main.weapons.hasAll(Equipment.WEAPONS.length)&&Main.helms.hasAll(Equipment.HELMS.length)&&
			Main.shirts.hasAll(Equipment.SHIRTS.length)&&Main.pants.hasAll(Equipment.PANTS.length)) getAchievement(17); //Collector
		updateGold();
	}
	public static function updateGold():Void {
		if(Main.gold > 10000){Main.achievements.set(PENNY_PINCHER,true); getAchievement(21);}
	}
	public static function defeatEnemy(n:Int, firstWin:Bool):Void {
		if(n == 0) getAchievement(0); //Been There, Slimed That
		else if(n == 3) getAchievement(1); //Slayer
		else if(n == 7) getAchievement(2); //Flower Power
		else if(n == 10) getAchievement(3); //Sightless
		else if(n == 12) getAchievement(4); //Muad'Dib
		else if(n == 14){
			getAchievement(5); //Doppelganger
			var b:Bool = true; for(i in 0...14) if(Main.losses[i] != 0 || Main.wins[i] != 1){b = false; break;}
			if(b){Main.achievements.set(CLEAN_SWEEP,true); getAchievement(22);}
		}
		if(Main.wins[n] >= 2) getAchievement(18); //Hack & Grind
		if(firstWin && Main.losses[n] >= 2){Main.achievements.set(NEVER_GIVE_UP,true); getAchievement(19);} //Never Give Up
		updateGold(); updateLevel(); if(firstWin && n == 14) queueWin();
	}
	public static function loseToEnemy(n:Int):Void {updateLevel();}
	public static function battleTime(s:Int):Void {
		if(s > 120){Main.achievements.set(SPEEDY_GONZALES,true); getAchievement(20);} //Speedy Gonzales
	}
}