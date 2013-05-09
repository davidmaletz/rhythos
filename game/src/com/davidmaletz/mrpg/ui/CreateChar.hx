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
package com.davidmaletz.mrpg.ui;
import com.davidmaletz.mrpg.Character;
import com.davidmaletz.mrpg.equipment.Equipment;
import com.davidmaletz.mrpg.Spell;
import nme.display.Sprite;
import nme.events.Event;

class CreateChar extends Sprite {
	private static inline var NAME:Int=0; private static inline var GENDER:Int=1; private static inline var SKIN_COLOR:Int=2;
	private static inline var HAIR_STYLE:Int=3; private static inline var HAIR_COLOR:Int=4; private static inline var EYE_COLOR:Int=5; private static inline var EXIT:Int=6;
	private static inline var N_SEL:Int=7; private static inline var SPACE:Int=42;
	private var char:Character; private var card:Sprite; private var sel:Int; private var arrows:Sprite; private var pause:Int;
	private var gId:Int; private var sId:Int; private var hsId:Int; private var hcId:Int; private var eId:Int; public var func:Bool->Bool->Void;
	public function new(c:Character, f:Bool->Bool->Void=null) {
		super(); var w:Int=400,h:Int=300; func = f; c.reset(); char = c; c.setFrame(c.getIdle());
		Frame.drawFrame(graphics,w,h); pause = Main.pause();
		card = CharSheet.createCharCard(c); card.x = w-128-16; addChild(card); w -= 128+32; Frame.drawDividerV(graphics,w,6,h-12); w += 8;
		gId = c.getType(); sId = 0; hsId = c.getHairStyle(); hcId = 0; eId = 0; Main.safeEnterFrame(this, handleKey); sel = NAME;
		var _y:Int = 16; addTitle("NAME", _y, w); addNameSlot(c.cname, _y); _y += SPACE;
		addTitle("GENDER", _y, w); addSlot(Character.GENDERS[gId], _y, w); _y += SPACE;
		addTitle("SKIN", _y, w); addSlot(Character.SKIN_COLORS[sId], _y, w); _y += SPACE;
		addTitle("HAIR STYLE", _y, w); addSlot(Character.HAIR_STYLES[gId][hsId], _y, w); _y += SPACE;
		addTitle("HAIR COLOR", _y, w); addSlot(Character.HAIR_COLORS[hcId], _y, w); _y += SPACE;
		addTitle("EYE COLOR", _y, w); addSlot(Character.EYE_COLORS[eId], _y, w); _y += SPACE;
		arrows = new Sprite(); var t:Text = new Text(Status.YELLOW, 12, 12, 0, "\201"); t.x = 32; arrows.addChild(t);
		t = new Text(Status.YELLOW, 12, 12, 0, "\202"); t.x = w-12-16; arrows.addChild(t); addChild(arrows);
		Frame.drawDivider(graphics, 6, _y-2, w-14); addSlot("DONE", _y-12, w); updateSel(); y = 300; c.enter_frame(null);
	}
	private inline function addTitle(title:String, _y:Int, w:Int):Void {CharSheet._addTitle(this, title, _y, w);}
	private inline function addSlot(name:String, _y:Int, w:Int):Void {CharSheet._addSlot(this, name, _y, w);}
	public function addNameSlot(name:String, _y:Int):Void {
		var t:TextField = new TextField(12, name, updateName); t.y = _y+16+5; t.x = 48; addChild(t);
	}
	private function updateName(tf:TextField):Void {
		var n:String = tf.getString(); if(n.length == 0) n = "Hero"; char.cname = n; CharSheet.setText(card, CharSheet.CARD_NAME, n);
	}
	public function cycleSel(d:Int):Void {
		var l:Int; switch(sel){
			case GENDER: l = Character.GENDERS.length; gId += d; if(gId >= l) gId = 0; else if(gId < 0) gId = l-1;
				char.setType(gId); getSelText().setText(Character.GENDERS[gId]); getText(HAIR_STYLE).setText(Character.HAIR_STYLES[gId][hsId]);
			case HAIR_STYLE: l = Character.HAIR_STYLES[gId].length; hsId += d; if(hsId >= l) hsId = 0; else if(hsId < 0) hsId = l-1;
				char.setHairStyle(hsId); getSelText().setText(Character.HAIR_STYLES[gId][hsId]);
			case SKIN_COLOR: l = Character.SKIN_COLORS.length; sId += d; if(sId >= l) sId = 0; else if(sId < 0) sId = l-1;
				char.setSkinColor(sId); getSelText().setText(Character.SKIN_COLORS[sId]);
			case HAIR_COLOR: l = Character.HAIR_COLORS.length; hcId += d; if(hcId >= l) hcId = 0; else if(hcId < 0) hcId = l-1;
				char.setHairColor(hcId); getSelText().setText(Character.HAIR_COLORS[hcId]);
			case EYE_COLOR: l = Character.EYE_COLORS.length; eId += d; if(eId >= l) eId = 0; else if(eId < 0) eId = l-1;
				char.setEyeColor(eId); getSelText().setText(Character.EYE_COLORS[eId]);
			default: return;
		} Main.playChange();
	}
	public inline function getText(i:Int):Text {return cast(getChildAt((i+1)*2), Text);}
	public inline function getSelText():Text {return getText(sel);}
	public inline function getNameText():TextField {return cast(getChildAt((NAME+1)*2), TextField);}
	private inline function updateSel():Void {
		if(sel == NAME){getNameText().setSelected(true); arrows.visible = false;} else {
			var t:Text = getSelText(); t.setColor(Status.YELLOW); if(sel == EXIT) arrows.x = -188; else arrows.x = 0; arrows.visible = true; arrows.y = t.y;
		}
	}
	public function cycleGroup(d:Int):Void {
		if(sel == NAME) cast(getChildAt((NAME+1)*2), TextField).setSelected(false); else getSelText().setColor(Frame.TEXT);
		sel += d; if(sel >= N_SEL) sel = 0; else if(sel < 0) sel = N_SEL-1; updateSel(); Main.playClick();
	}
	private inline function callFunc(closed:Bool):Void {if(func != null) func(closed, sel == -2);}
	private function exit():Void {if(pause > 0){Main.unpause(); pause = 0;} parent.removeChild(this); callFunc(true);}
	public function handleKey(e:Event):Void {
		var dh:Int = 75; if(sel < 0){y += dh; if(y >= 300) exit(); return;} if(y > 0){y -= dh; return;}
		if(Main.isPressed(Main.ESCAPE, pause)){Main.resetPressed(pause); Main.playCancel(); sel = -2; callFunc(false); return;}
		if(sel == EXIT && Main.isPressed(Main.ENTER, pause)){Main.resetPressed(pause); Main.playSelect(); sel = -1; callFunc(false); return;}
		var r:Bool = Main.isPressed(Main.RIGHT, pause), l:Bool = Main.isPressed(Main.LEFT, pause), u:Bool = Main.isPressed(Main.UP, pause), d:Bool = Main.isPressed(Main.DOWN, pause);
		if(r && !l) cycleSel(1); if(l && !r) cycleSel(-1); if(u && !d) cycleGroup(-1); if(d && !u) cycleGroup(1);
		Main.resetPressed(pause);
	}
}