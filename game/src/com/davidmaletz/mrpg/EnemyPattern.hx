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
import com.davidmaletz.mrpg.equipment.Weapon;

class EnemyPattern {
	private static inline var DEFEND_LEN:Int=16; private static inline var CAST_LEN:Int=DEFEND_LEN+3; private static inline var WARNING_LEN:Int=10;
	private static inline var DODGE_LEN:Int=Character.WALK_LEN; private static inline var BOW_LEN:Int=7;
	public static inline var IDLE:Int=0; public static inline var ATTACK:Int=1; public static inline var DEFEND:Int=2;
	public static inline var CAST:Int=3; public static inline var DODGE:Int=4; private static var lengths:Array<Int> = [0,0,DEFEND_LEN,CAST_LEN,DODGE_LEN];
	private var char:Character; private var cur:Int; private var actions:Array<Int>; public var lowHP:EnemyPattern;
	public function new(bpm:Int, c:Character, pattern:Array<Int>){
		char = c; var w:Weapon = c.weapon; cur = 0; var attack:Int = w.nFrames()+w.cooldown; actions = new Array<Int>();
		var len:Int = pattern.length, frame:Int=0; for(i in 0...len){
			var t:Int = pattern[i]; if(t == IDLE){
				var f:Int = Math.floor(Math.ceil((frame+CAST_LEN)*bpm/1440)*1440/bpm);
				if(f > frame){actions.push(IDLE); actions.push(f-frame); frame = f;}
			} else {
				var l:Int = (t == ATTACK)?attack:lengths[t]; actions.push(t); frame += l;
				var f:Int = Math.floor(Math.ceil((frame+CAST_LEN-l)*bpm/1440)*1440/bpm); actions.push(f-frame); frame = f;
			}
		}
	}
	public function reset():Void {cur = 0; if(lowHP != null) lowHP.reset();}
	private inline function pad(st:Int, l:Int):Void {var d:Int = l-(char.queueLength()-st); if(d > 0) char.queueIdle(d);}
	private function showWarning(b:Battle):Void {
		if(cur == -1) lowHP.showWarning(b);
		else if(char.mana >= 3000-(DEFEND_LEN-WARNING_LEN)*2*10 && actions[cur] == CAST) b.spellWarning();
	}
	public function next(b:Battle):Void {
		if(cur < 0){lowHP.next(b); return;} var a:Int = actions[cur++]; var i:Int = actions[cur++]; var f:Int=char.queueLength();
		switch(a){
			case ATTACK: b.enemyAttack();
			case DEFEND: if(!b.enemyDefend()){b.enemyDodge(); pad(f,DEFEND_LEN);}
			case CAST: if(!b.castSpell(char)){b.enemyAttack(); pad(f,CAST_LEN);}
			case DODGE: b.enemyDodge();
		} char.queueIdle(i); if(cur >= actions.length){if(lowHP != null && char.health/char.max_health < 0.5) cur = -1; else cur = 0;}
		if(a != CAST){
			f += WARNING_LEN; var func:Character->Void = char.getQueuedFunc(f); char.setFunction(f, function(c:Character):Void {
				if(func != null) func(c); showWarning(b);
			});
		}
	}
}