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
import com.davidmaletz.mrpg.Spell;
import nme.display.Sprite;
import nme.events.Event;

class CharSheet extends Sprite {
	private static inline var WEAPON:Int=0; private static inline var HEAD:Int=1; private static inline var TORSO:Int=2;
	private static inline var LEGS:Int=3; private static inline var SPELL:Int=4; private static inline var EXIT:Int=5;
	private static inline var N_SEL:Int=6; public static inline var SPACE:Int=42;
	private var char:Character; private var card:Sprite; private var sel:Int; private var arrows:Sprite; private var pause:Int;
	private var wId:Int; private var sId:Int; private var lId:Int; private var tId:Int; private var hId:Int; public var func:CharSheet->Bool->Bool->Void;
	public function new(c:Character, f:CharSheet->Bool->Bool->Void=null) {
		super(); var w:Int=Main.width,h:Int=Main.height; func = f; c.reset(); char = c; refreshChar();
		Frame.drawFrame(graphics,w,h); pause = Main.pause();
		card = createCharCard(c); card.x = w-128-16; addChild(card); w -= 128+32; Frame.drawDividerV(graphics,w,6,h-12); w += 8;
		wId = indexOf(Equipment.WEAPONS, c.weapon); sId = indexOf(Spell.SPELLS, c.spell);
		lId = indexOf(Equipment.PANTS, c.equip[Character.LEGS]); tId = indexOf(Equipment.SHIRTS, c.equip[Character.TORSO]);
		hId = indexOf(Equipment.HELMS, c.equip[Character.HEAD]); Main.safeEnterFrame(this, handleKey); sel = WEAPON;
		var _y:Int = 16; addTitle("WEAPON", _y, w); addSlot(Equipment.WEAPON_NAMES[wId], _y, w); _y += SPACE;
		addTitle("HEAD", _y, w); addSlot(Equipment.HELM_NAMES[hId], _y, w); _y += SPACE;
		addTitle("TORSO", _y, w); addSlot(Equipment.SHIRT_NAMES[tId], _y, w); _y += SPACE;
		addTitle("LEGS", _y, w); addSlot(Equipment.PANT_NAMES[lId], _y, w); _y += SPACE;
		addTitle("SPELL", _y, w); addSlot(Spell.SPELL_NAMES[sId], _y, w); _y += SPACE;
		var s:SpellDisplay = new SpellDisplay(sId); s.x = 16; s.y = _y-4; addChild(s); _y += SPACE;
		Frame.drawDivider(graphics, 6, _y-2, w-14); addSlot("DONE", _y-12, w);
		arrows = new Sprite(); var t:Text = new Text(Status.YELLOW, 12, 12, 0, "\201"); t.x = 32; arrows.addChild(t);
		t = new Text(Status.YELLOW, 12, 12, 0, "\202"); t.x = w-12-16; arrows.addChild(t); addChild(arrows); updateSel(); y = Main.height; c.enter_frame(null);
	}
	public static function _addTitle(s:Sprite, title:String, _y:Int, w:Int):Void {
		Frame.drawDivider(s.graphics, 6, _y+4, 18,false,true); var _x:Int = 40+title.length*16; Frame.drawDivider(s.graphics, _x, _y+4, w-_x-8,true);
		var t:Text = new Text(Status.GRAY, 16, w, 0, title); t.y = _y; t.x = 32; s.addChild(t);
	}
	public static function _addSlot(s:Sprite, name:String, _y:Int, w:Int):Void {
		var t:Text = new Text(Frame.TEXT, 12, w, 0, name); t.y = _y+16+5; t.x = 48; s.addChild(t);
	}
	private inline function addTitle(title:String, _y:Int, w:Int):Void {_addTitle(this, title, _y, w);}
	private inline function addSlot(name:String, _y:Int, w:Int):Void {_addSlot(this, name, _y, w);}
	public static function indexOf<T>(ar:Array<T>, e:T):Int {
		var end:Int = ar.length; for(i in 0...end) if(ar[i] == e) return i; return -1;
	}
	public inline function getChar():Character {return char;}
	public function cycleSel(d:Int):Void {
		var l:Int; switch(sel){
			case WEAPON: l = Equipment.WEAPONS.length; do{wId += d; if(wId >= l) wId = 0; else if(wId < 0) wId = l-1;}while(!Main.weapons.get(wId));
				char.weapon = Equipment.WEAPONS[wId]; updateWeapon();
			case SPELL: l = Spell.SPELLS.length; do{sId += d; if(sId >= l) sId = 0; else if(sId < 0) sId = l-1;}while(!Main.spells.get(sId));
				char.spell = Spell.SPELLS[sId]; updateSpell();
			case LEGS: l = Equipment.PANTS.length; do{lId += d; if(lId >= l) lId = 0; else if(lId < 0) lId = l-1;}while(!Main.pants.get(lId));
				char.equip[Character.LEGS] = Equipment.PANTS[lId]; updateEquipment();
			case TORSO: l = Equipment.SHIRTS.length; do{tId += d; if(tId >= l) tId = 0; else if(tId < 0) tId = l-1;}while(!Main.shirts.get(tId));
				char.equip[Character.TORSO] = Equipment.SHIRTS[tId]; updateEquipment();
			case HEAD: l = Equipment.HELMS.length; do{hId += d; if(hId >= l) hId = 0; else if(hId < 0) hId = l-1;}while(!Main.helms.get(hId));
				char.equip[Character.HEAD] = Equipment.HELMS[hId]; updateEquipment();
		}
	}
	public inline function getSelText():Text {return cast(getChildAt((sel+1)*2), Text);}
	private inline function updateSel():Void {
		var t:Text = getSelText(); t.setColor(Status.YELLOW); if(sel == EXIT) arrows.x = -188; else arrows.x = 0; arrows.y = t.y;
	}
	public function cycleGroup(d:Int):Void {
		getSelText().setColor(Frame.TEXT); sel += d; if(sel >= N_SEL) sel = 0; else if(sel < 0) sel = N_SEL-1; updateSel(); Main.playClick();
	}
	private inline function callFunc(closed:Bool):Void {if(func != null) func(this, closed, sel == -2);}
	private function exit():Void {if(pause > 0){Main.unpause(); pause = 0;} parent.removeChild(this); callFunc(true);}
	public function handleKey(e:Event):Void {
		var dh:Int = Main.height>>2; if(sel < 0){y += dh; if(y >= Main.height) exit(); return;} if(y > 0){y -= dh; return;}
		if(Main.isPressed(Main.ESCAPE, pause)){Main.resetPressed(pause); Main.playCancel(); sel = -2; callFunc(false); return;}
		if(sel == EXIT && Main.isPressed(Main.ENTER, pause)){Main.resetPressed(pause); Main.playSelect(); sel = -1; callFunc(false); return;}
		var r:Bool = Main.isPressed(Main.RIGHT, pause), l:Bool = Main.isPressed(Main.LEFT, pause), u:Bool = Main.isPressed(Main.UP, pause), d:Bool = Main.isPressed(Main.DOWN, pause);
		if(r && !l) cycleSel(1); if(l && !r) cycleSel(-1); if(u && !d) cycleGroup(-1); if(d && !u) cycleGroup(1);
		Main.resetPressed(pause);
	}
	public static function createCharCard(c:Character):Sprite {
		var s:Sprite = new Sprite(); Frame.drawFrame(s.graphics, 128, 128, true); s.addChild(c); s.addChild(c.getTop()); s.y = (Main.height-128-16)*0.5;
		var t:Text = new Text(Frame.TEXT, 16, 128, 1, c.cname), _y:Int = -64; t.y = _y; _y += 16+4; Frame.drawDivider(s.graphics, 0, _y-2, 128,true,true); _y += 8; s.addChild(t);
		t = new Text(Status.RED, 8, 128, 1, "HP: "+Battle.format(Std.string(c.max_health))); t.y = _y; _y += 8+4; s.addChild(t);
		t = new Text(Status.BLUE, 8, 128, 1, "MP: "+Battle.format(Std.string(c.max_mana))); t.y = _y; _y += 8+4; s.addChild(t);
		t = new Text(Status.GRAY, 8, 128, 1, "LEVEL: "+(c.level+1)); t.y = _y; _y = 128+4; s.addChild(t);
		t = new Text(Frame.TEXT, 8, 128, 1, "DMG: "+Battle.format(Std.string(c.weapon.lowDmg+c.level*20))+"-"+Battle.format(Std.string(c.weapon.highDmg+c.level*20))); t.y = _y; _y += 8+4; s.addChild(t);
		t = new Text(Frame.TEXT, 8, 128, 1, "PHYS DEF: "+Std.int(100.5-c.phys_def*100)+"%"); t.y = _y; _y += 8+4; s.addChild(t);
		t = new Text(Frame.TEXT, 8, 128, 1, "MAG DEF: "+Std.int(100.5-c.mag_def*100)+"%"); t.y = _y; _y += 8+6; s.addChild(t);
		Frame.drawDivider(s.graphics, 0, _y-2, 128,true,true); _y += 8; t = new Text(Status.GRAY, 8, 128, 1, "EXP: "+Battle.format(Std.string(Main.experience))); t.y = _y; _y += 8+4; s.addChild(t);
		t = new Text(Status.YELLOW, 8, 128, 1, "GOLD: "+Battle.format(Std.string(Main.gold))); t.y = _y; _y += 8+4; s.addChild(t);
		t = new Text(Status.GREEN, 8, 128, 1, "SCORE: "+Battle.format(Std.string(Main.score))); t.y = _y; s.addChild(t); return s;
	}
	public static inline var CARD_NAME:Int=2; public static inline var CARD_HP:Int=3; public static inline var CARD_MP:Int=4;
	public static inline var CARD_LVL:Int=5; public static inline var CARD_DMG:Int=6; public static inline var CARD_PDEF:Int=7;
	public static inline var CARD_MDEF:Int=8; public static inline var CARD_EXP:Int=9; public static inline var CARD_GOLD:Int=10;
	public static inline var CARD_SCORE:Int=11;
	public static inline function setText(c:Sprite, i:Int, s:String):Void {cast(c.getChildAt(i), Text).setText(s);}
	public function updateWeapon():Void {
		setText(card, CARD_DMG, "DMG: "+Battle.format(Std.string(char.weapon.lowDmg+char.level*20))+"-"+Battle.format(Std.string(char.weapon.highDmg+char.level*20))); refreshChar();
		getSelText().setText(Equipment.WEAPON_NAMES[wId]); char.weapon.playSFX();
	}
	public function updateSpell():Void {
		getSelText().setText(Spell.SPELL_NAMES[sId]); cast(getChildAt((sel+1)*2+1), SpellDisplay).setSpell(sId); if(char.spell != null) char.spell.playSFX();
	}
	public function updateEquipment():Void {
		char.resetDefense(); setText(card, CARD_PDEF, "PHYS DEF: "+Std.int(100.5-char.phys_def*100)+"%");
		setText(card, CARD_MDEF, "MAG DEF: "+Std.int(100.5-char.mag_def*100)+"%"); char.updateBitmap(); refreshChar(); var t:Text = getSelText();
		switch(sel){
			case LEGS: t.setText(Equipment.PANT_NAMES[lId]);
			case TORSO: t.setText(Equipment.SHIRT_NAMES[tId]);
			case HEAD: t.setText(Equipment.HELM_NAMES[hId]);
		} Main.playChange();
	}
	private inline function refreshChar():Void {char.setFrame(char.getIdle());}
}