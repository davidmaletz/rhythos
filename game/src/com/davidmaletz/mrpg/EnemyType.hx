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
import com.davidmaletz.mrpg.equipment.Equipment;
import com.davidmaletz.mrpg.equipment.Weapon;
import com.davidmaletz.mrpg.ui.DialogBox;
import nme.display.BitmapData;
import nme.media.Sound;

class EnemyType {
	public static inline var IDLE:Int=EnemyPattern.IDLE; public static inline var ATTACK:Int=EnemyPattern.ATTACK;
	public static inline var DEFEND:Int=EnemyPattern.DEFEND; public static inline var CAST:Int=EnemyPattern.CAST;
	public static inline var DODGE:Int=EnemyPattern.DODGE;
	private var song:Int; private var char:Character; private var pattern:EnemyPattern; public var onDefeat:Character->Void;
	public var bg:Int; public var gold:Int; public var experience:Int;
	public function new(_bg:Int, name:String, type:Int, hp:Int, mp:Int, e:Int, g:Int, p:Array<Int>, l:Int=0, s:Int=1, weapon:Int=0, spell:Int=0, head:Int=0, torso:Int=0, legs:Int=0, pLow:Array<Int>=null, b:BitmapData=null, low:Int=0, high:Int=0){
		bg = _bg; song = s; char = (type==-1)?new Monster(name, hp-400*l, mp-400*l, l, b):new Character(name, type, hp-400*l, mp-400*l, l, b);
		char.weapon = Equipment.WEAPONS[weapon]; char.spell = Spell.SPELLS[spell]; gold = g; experience = e;
		if(low > 0) char.weapon = new Weapon(-1, Weapon.SLASH, 0, 0, -76, low, high, 5, Weapon.CRITICAL_HIT);
		char.equip[Character.HEAD] = Equipment.HELMS[head]; if(torso >= 0) char.equip[Character.TORSO] = Equipment.SHIRTS[torso];
		if(legs >= 0) char.equip[Character.LEGS] = Equipment.PANTS[legs]; pattern = new EnemyPattern(getBPM(), char, p);
		if(pLow != null) pattern.lowHP = new EnemyPattern(getBPM(), char, pLow); char.updateBitmap(); char.setFrame(char.getIdle());
	}
	public inline function getChar():Character {char.reset(); char.restore(); return char;}
	public inline function getSound():Sound {return Main.getBGM("battle", song);}
	public inline function getBPM():Int {return Main.getBPM(song);}
	public inline function getLead():Int {return Main.getLead(song);}
	public inline function getPattern():EnemyPattern {pattern.reset(); return pattern;}
	
	public inline function setDefense(pdef:Float, mdef:Float):Void {char.equip[Character.HEAD] = new Equipment(-1, pdef, mdef);}
	public inline function setWeapon(w:Weapon):Void {char.weapon = w;}
	
	public inline function getSong():Int {return song;}
	
	public static var ENEMIES:Array<EnemyType>;
	public static var DESC:Array<String>;
}