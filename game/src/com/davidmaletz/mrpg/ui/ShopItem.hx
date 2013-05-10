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
import com.davidmaletz.mrpg.equipment.Equipment;

class ShopItem {
	public static inline var WEAPON:Int=0; public static inline var HEAD:Int=1; public static inline var TORSO:Int=2;
	public static inline var LEGS:Int=3; public var type:Int; public var id:Int; public var cost:Int; public var available:Bool;
	public function new(t:Int, i:Int){type = t; id = i; cost = getCost(); available = getAvailable();}
	public function getName():String {
		switch(type){
			case WEAPON: return Equipment.WEAPON_NAMES[id];
			case HEAD: return Equipment.HELM_NAMES[id];
			case TORSO: return Equipment.SHIRT_NAMES[id];
			case LEGS: return Equipment.PANT_NAMES[id];
		} return "Unknown";
	}
	public function getCost():Int {
		switch(type){
			case WEAPON: return Equipment.WEAPON_COSTS[id];
			case HEAD: return Equipment.HELM_COSTS[id];
			case TORSO: return Equipment.SHIRT_COSTS[id];
			case LEGS: return Equipment.PANT_COSTS[id];
		} return 0;
	}
	public function getAvailable():Bool {
		switch(type){
			case WEAPON: return Main.gold>=cost&&!Main.weapons.get(id);
			case HEAD: return Main.gold>=cost&&!Main.helms.get(id);
			case TORSO: return Main.gold>=cost&&!Main.shirts.get(id);
			case LEGS: return Main.gold>=cost&&!Main.pants.get(id);
		} return false;
	}
	public function purchase():Void {
		Main.gold -= cost; switch(type){
			case WEAPON: Main.weapons.set(id,true);
			case HEAD: Main.helms.set(id,true);
			case TORSO: Main.shirts.set(id,true);
			case LEGS: Main.pants.set(id,true);
		}
	}
	public function equip(c:Character, s:Shop):Void {
		switch(type){
			case WEAPON: c.weapon = Equipment.WEAPONS[id]; s.updateWeapon();
			case HEAD: c.equip[Character.HEAD] = Equipment.HELMS[id]; s.updateEquipment();
			case TORSO: c.equip[Character.TORSO] = Equipment.SHIRTS[id]; s.updateEquipment();
			case LEGS: c.equip[Character.LEGS] = Equipment.PANTS[id]; s.updateEquipment();
		}
	}
	public function unequip(c:Character, s:Shop):Void {
		switch(type){
			case WEAPON: c.weapon = Main.getPlayer().weapon; s.updateWeapon();
			case HEAD: c.equip[Character.HEAD] = Main.getPlayer().equip[Character.HEAD]; s.updateEquipment();
			case TORSO: c.equip[Character.TORSO] = Main.getPlayer().equip[Character.TORSO]; s.updateEquipment();
			case LEGS: c.equip[Character.LEGS] = Main.getPlayer().equip[Character.LEGS]; s.updateEquipment();
		}
	}
}