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
import nme.display.Sprite;
import nme.events.Event;

class LearnSpell extends Sprite {
	private var spell:SpellDisplay; private var sel:Int; private var pause:Int; private var func:Bool->Int->Void;
	public function new(f:Bool->Int->Void=null) {
		super(); var w:Int=248, h:Int=100; Frame.drawFrame(graphics, w, h); var _y:Int=16; x = (400-w)>>1; y = 300;
		func = f; Main.safeEnterFrame(this, handleKey); pause = Main.pause();
		CharSheet._addTitle(this, "LEARN SPELL", _y, w+2); CharSheet._addSlot(this, Spell.SPELL_NAMES[0], _y, w); _y += CharSheet.SPACE;
		spell = new SpellDisplay(0); spell.x = 16; spell.y = _y-4; addChild(spell); var s:Text = getTitle(); s.setColor(Status.YELLOW);
		var t:Text = new Text(Status.YELLOW, 12, 12, 0, "\201"); t.x = 32; t.y = s.y; addChild(t);
		t = new Text(Status.YELLOW, 12, 12, 0, "\202"); t.x = w-12-16; t.y = s.y; addChild(t); sel = 0; cycleSel(1, false);
	}
	private inline function callFunc(closed:Bool):Void {if(func != null) func(closed, sel);}
	public inline function getTitle():Text {return cast(getChildAt(1), Text);}
	private function exit():Void {if(pause > 0){Main.unpause(); pause = 0;} parent.removeChild(this); callFunc(true);}
	public function cycleSel(d:Int, sfx:Bool=true):Void {
		var l:Int = Spell.SPELLS.length; do{sel += d; if(sel >= l) sel = 0; else if(sel < 0) sel = l-1;}while(Main.spells.get(sel));
		getTitle().setText(Spell.SPELL_NAMES[sel]); spell.setSpell(sel); if(sfx && Spell.SPELLS[sel] != null) Spell.SPELLS[sel].playSFX();
	}
	public function handleKey(e:Event):Void {
		var dh:Int = 50; if(spell == null){y += dh; if(y >= 300) exit(); return;} if(y > 100){y -= dh; return;}
		if(Main.isPressed(Main.ENTER, pause)){Main.resetPressed(pause); Main.playSelect(); spell = null; callFunc(false); return;}
		var r:Bool = Main.isPressed(Main.RIGHT, pause), l:Bool = Main.isPressed(Main.LEFT, pause);
		if(r && !l) cycleSel(1); if(l && !r) cycleSel(-1);
		Main.resetPressed(pause);
	}
}