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
import nme.display.Sprite;
import nme.events.Event;
import nme.net.SharedObject;
import nme.utils.ByteArray;

class GameSaves extends Sprite {
	private static inline var MAGIC:Int=82; private static inline var VERSION:Int=5;
	private static inline var N_SLOTS:Int = 3; private static var SAVES:Array<ByteArray>;
	private var sel:Int; private var pause:Int; private var onSelect:GameSaves->Int->SavedCharacter->Bool;
	public function new(func:GameSaves->Int->SavedCharacter->Bool=null){
		super(); Frame.drawFrame(graphics, 328, 296); x = 36; var _y:Int = 12; pause = Main.pause();
		for(i in 0...N_SLOTS){var s=new SaveSlot(i,loadSlot(i)); s.x = 12; s.y = _y; addChild(s); _y += 92;}
		y = 302; onSelect = func; sel = 0; updateSel(); Main.safeEnterFrame(this, handleKey);
	}
	private static function loadSlot(i:Int):SavedCharacter {
		initSaves(); if(SAVES[i] == null) return null; SAVES[i].position = 0; return loadCharacter(SAVES[i]);
	}
	public inline function getSelSlot():SaveSlot {return cast(getChildAt(sel), SaveSlot);}
	private inline function updateSel():Void {getSelSlot().setSelected(true);}
	public function changeSel(d:Int):Void {
		getSelSlot().setSelected(false); var len:Int = N_SLOTS; sel += d;
		if(sel >= len) sel = 0; else if(sel < 0) sel = len-1; updateSel(); Main.playClick();
	}
	public inline function beginClose(){if(pause > 0){Main.unpause(); pause = 0;}}
	private function select(i:Int, sc:SavedCharacter):Void {if(onSelect == null || onSelect(this,i,sc)) beginClose();}
	public function handleKey(e:Event):Void {
		var dh:Int = 75; if(pause == 0){y += dh; if(y >= 302) parent.removeChild(this); return;} if(y > 2){y -= dh; return;}
		if(Main.isPressed(Main.ESCAPE, pause)){Main.resetPressed(pause); select(-1,null); Main.playCancel(); return;}
		var u:Bool = Main.isPressed(Main.UP, pause), d:Bool = Main.isPressed(Main.DOWN, pause);
		if(u && !d) changeSel(-1); if(d && !u) changeSel(1); var ent:Bool = Main.isPressed(Main.ENTER, pause);
		Main.resetPressed(pause); if(ent){select(sel,getSelSlot().char); Main.playSelect();}
	}
	public static function saveCharacter(sc:SavedCharacter, ar:ByteArray):Void {
		ar.writeByte(MAGIC); ar.writeByte(VERSION); var c:Character = sc.getChar(); ar.writeUTF(c.cname); ar.writeByte(c.getType());
		ar.writeInt(sc.experience); ar.writeInt(sc.gold); ar.writeInt(sc.score); ar.writeByte(sc.max_type);
		ar.writeByte(c.getSkinColor()); ar.writeByte(c.getHairStyle()); ar.writeByte(c.getHairColor()); ar.writeByte(c.getEyeColor());
		ar.writeByte(CharSheet.indexOf(Equipment.WEAPONS, c.weapon)); ar.writeByte(CharSheet.indexOf(Equipment.HELMS, c.equip[Character.HEAD]));
		ar.writeByte(CharSheet.indexOf(Equipment.SHIRTS, c.equip[Character.TORSO]));
		ar.writeByte(CharSheet.indexOf(Equipment.PANTS, c.equip[Character.LEGS])); ar.writeByte(CharSheet.indexOf(Spell.SPELLS, c.spell));
		ar.writeInt(sc.weapons); ar.writeInt(sc.spells); ar.writeInt(sc.helms); ar.writeInt(sc.shirts); ar.writeInt(sc.pants);
	}
	public static function loadCharacter(ar:ByteArray):SavedCharacter {
		if(ar.readByte() != MAGIC) return null; var ver:Int = ar.readByte(); if(ver < 2 || ver > VERSION) return null;
		var n:String = ar.readUTF(); var ret:SavedCharacter = new SavedCharacter(new Character(n, ar.readByte(), Main.BASE_HP, Main.BASE_MP));
		var c:Character = ret.getChar(); ret.experience = ar.readInt(); ret.gold = ar.readInt(); ret.score = ar.readInt(); ret.max_type = ar.readByte();
		c.setSkinColor(ar.readByte()); c.setHairStyle(ar.readByte()); c.setHairColor(ar.readByte()); c.setEyeColor(ar.readByte());
		c.weapon = Equipment.WEAPONS[ar.readByte()]; c.equip[Character.HEAD] = Equipment.HELMS[ar.readByte()];
		c.equip[Character.TORSO] = Equipment.SHIRTS[ar.readByte()]; c.equip[Character.LEGS] = Equipment.PANTS[ar.readByte()];
		c.spell = Spell.SPELLS[ar.readByte()]; c.setLevel(Main.levelFromExperience(ret.experience)); c.updateBitmap(); c.setFrame(c.getIdle());
		ret.weapons = ar.readInt(); ret.spells = ar.readInt(); ret.helms = ar.readInt(); ret.shirts = ar.readInt(); ret.pants = ar.readInt();
		return ret;
	}
	private static var _saves:SharedObject;
	private static function initSaves():Void {
		if(_saves == null){
			_saves = SharedObject.getLocal("_saves"); SAVES = new Array<ByteArray>();
			for(i in 0...N_SLOTS){var s=Reflect.field(_saves.data, "slot"+i); if(s != null) SAVES.push(cast(s,ByteArray)); else SAVES.push(null);}
		}
	}
	public static function writeSlot(i:Int, sc:SavedCharacter):Void {
		initSaves(); SAVES[i] = new ByteArray(); saveCharacter(sc, SAVES[i]); Reflect.setField(_saves.data, "slot"+i, SAVES[i]); _saves.flush();
	}
}