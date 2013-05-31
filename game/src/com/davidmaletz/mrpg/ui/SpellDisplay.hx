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
import com.davidmaletz.mrpg.Spell;
import nme.display.Sprite;

class SpellDisplay extends Sprite{
	public function new(sId:Int) {
		super(); Frame.drawFrame(graphics, 36, 36, true);
		var i:Sprite = new Sprite(); i.x = -6; i.y = -6; Spell.drawIcon(i.graphics, sId); addChild(i);
		var t:Text = new Text(Frame.TEXT, 8, Main.width, 0, Spell.SPELL_DESC1[sId]); t.x = 40; t.y = 4; addChild(t);
		t = new Text(Frame.TEXT, 8, Main.width, 0, Spell.SPELL_DESC2[sId]); t.x = 40; t.y = 14; addChild(t);
		t = new Text(Frame.TEXT, 8, Main.width, 0, Spell.SPELL_DESC3[sId]); t.x = 40; t.y = 24; addChild(t);
	}
	public function setSpell(sId:Int):Void {
		Spell.drawIcon(cast(getChildAt(0),Sprite).graphics, sId); cast(getChildAt(1), Text).setText(Spell.SPELL_DESC1[sId]);
		cast(getChildAt(2), Text).setText(Spell.SPELL_DESC2[sId]); cast(getChildAt(3), Text).setText(Spell.SPELL_DESC3[sId]);
	}
}