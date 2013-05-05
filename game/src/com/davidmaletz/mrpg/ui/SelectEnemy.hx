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
import com.davidmaletz.mrpg.EnemyType;
import nme.display.Sprite;
import nme.events.Event;

class SelectEnemy extends Sprite {
	private static var TYPE_NAME:Array<String> = ["", "", "MINIBOSS!", "BOSS!", "FINAL BOSS!"]; private static var MAX_BLINK:Int=12;
	private var char:Sprite; private var sel:Int; private var pause:Int; private var blink_ct:Int; private var func:Bool->Int->Void;
	public function new(f:Bool->Int->Void=null) {
		super(); var w:Int = 290; Frame.drawFrame(graphics, w, 256); x = (400-w)*0.5; y = 298; pause = Main.pause(); var l:Int = EnemyType.ENEMIES.length;
		sel = (Main.max_type>=l)?l-1:Main.max_type; var e:EnemyType = EnemyType.ENEMIES[sel], c:Character = e.getChar(); Main.safeEnterFrame(this, handleKey);
		func = f; char = new Sprite(); Frame.drawFrame(char.graphics, 128, 128, true); addChild(char); char.x = (w-128)>>1; char.y = 16;
		setChar(c); var t:Text = new Text(Status.YELLOW, 16, w, 1, c.cname), _y:Int = 148; t.y = _y; addChild(t);
		t = new Text(Status.YELLOW, 16, 16, 0, "\201"); t.x = 44; t.y = _y; addChild(t);
		t = new Text(Status.YELLOW, 16, 16, 0, "\202"); t.x = 230; t.y = _y; addChild(t); _y += 16+4;
		t = new Text(Status.GRAY, 8, w, 1, EnemyType.DESC[sel]); t.y = _y; addChild(t); _y += 8+4;
		var w2:Int = w>>1; t = new Text(Status.YELLOW, 8, w2, 1, "WINS: "+Main.wins[sel]); t.y = _y; addChild(t);
		t = new Text(Status.RED, 8, w2, 1, "LOSSES: "+Main.losses[sel]); t.x = w2; t.y = _y; _y += 8+4; addChild(t);
		t = new Text(Status.RED, 8, w, 1, TYPE_NAME[(sel==14)?4:e.getSong()]); t.y = _y; addChild(t); _y += 8+6; t.visible = false; blink_ct = MAX_BLINK;
		Frame.drawDivider(graphics, 6, _y-2, w-12); _y += 8;
		t = new Text(Status.RED, 8, w2, 1, "HP: "+Battle.format(Std.string(c.max_health))); t.y = _y; addChild(t);
		t = new Text(Status.BLUE, 8, w2, 1, "MP: "+Battle.format(Std.string(c.max_mana))); t.x = w2; t.y = _y; _y += 8+4; addChild(t);
		t = new Text(Frame.TEXT, 8, w2, 1, "DMG: "+Battle.format(Std.string(c.weapon.lowDmg+c.level*20))+"-"+Battle.format(Std.string(c.weapon.highDmg+c.level*20))); t.y = _y; addChild(t);
		t = new Text(Frame.TEXT, 8, w2, 1, "SPELL: "+Spell.SPELL_NAMES[CharSheet.indexOf(Spell.SPELLS, c.spell)]); t.x = w2; t.y = _y; _y += 8+4; addChild(t);
		t = new Text(Frame.TEXT, 8, w2, 1, "PHYS DEF: "+Std.int(100.5-c.phys_def*100)+"%"); t.y = _y; addChild(t);
		t = new Text(Frame.TEXT, 8, w2, 1, "MAG DEF: "+Std.int(100.5-c.mag_def*100)+"%"); t.x = w2; t.y = _y; addChild(t);
	}
	private function setChar(c:Character):Void {
		c.setFrame(c.getIdle()); Main.removeAllChildren(char); c.x = 128; char.addChild(c); char.addChild(c.getTop());
	}
	private static inline var NAME:Int=1; private static inline var DESC:Int=4; private static inline var TYPE:Int=7;
	private static inline var HP:Int=8; private static inline var MP:Int=9; private static inline var DMG:Int=10;
	private static inline var SPELL:Int=11; private static inline var PDEF:Int=12; private static inline var MDEF:Int=13;
	private static inline var WINS:Int=5; private static inline var LOSSES:Int=6;
	public inline function getText(i:Int):Text {return cast(getChildAt(i), Text);}
	private inline function callFunc(closed:Bool):Void {if(func != null) func(closed, sel);}
	private function exit():Void {if(pause > 0){Main.unpause(); pause = 0;} parent.removeChild(this); callFunc(true);}
	public function cycleSel(d:Int):Void {
		sel += d; var l:Int = EnemyType.ENEMIES.length; if(sel >= l) sel = 0; else if(sel < 0) sel = l-1;
		var e:EnemyType = EnemyType.ENEMIES[sel], c:Character = e.getChar(); if(sel > Main.max_type){
			c.makeGray(); getText(NAME).setText("????????"); getText(DESC).setText("Locked."); getText(TYPE).setText("");
			getText(HP).setText("HP: ?????"); getText(MP).setText("MP: ?????"); getText(DMG).setText("DMG: ???-???");
			getText(SPELL).setText("SPELL: ?????"); getText(PDEF).setText("PHYS DEF: ????"); getText(MDEF).setText("MAG DEF: ????");
			getText(WINS).setText("WINS: 0"); getText(LOSSES).setText("LOSSES: 0");
		} else {
			getText(NAME).setText(c.cname); getText(DESC).setText(EnemyType.DESC[sel]); getText(TYPE).setText(TYPE_NAME[(sel==14)?4:e.getSong()]);
			getText(HP).setText("HP: "+Battle.format(Std.string(c.max_health)));
			getText(MP).setText("MP: "+Battle.format(Std.string(c.max_mana)));
			getText(DMG).setText("DMG: "+Battle.format(Std.string(c.weapon.lowDmg+c.level*20))+"-"+Battle.format(Std.string(c.weapon.highDmg+c.level*20)));
			getText(SPELL).setText("SPELL: "+Spell.SPELL_NAMES[CharSheet.indexOf(Spell.SPELLS, c.spell)]);
			getText(PDEF).setText("PHYS DEF: "+Std.int(100.5-c.phys_def*100)+"%");
			getText(MDEF).setText("MAG DEF: "+Std.int(100.5-c.mag_def*100)+"%");
			getText(WINS).setText("WINS: "+Main.wins[sel]); getText(LOSSES).setText("LOSSES: "+Main.losses[sel]);
		} setChar(c); Main.playChange();
	}
	public function handleKey(e:Event):Void {
		var dh:Int = 69; if(blink_ct < 0){y += dh; if(y >= 298) exit(); return;} if(y > 22){y -= dh; return;}
		blink_ct--; if(blink_ct == 0){blink_ct = MAX_BLINK; var t:Text = getText(TYPE); t.visible = !t.visible;}
		if(Main.isPressed(Main.ESCAPE, pause)){Main.resetPressed(pause); Main.playCancel(); blink_ct = -1; sel = -1; callFunc(false); return;}
		if(Main.isPressed(Main.ENTER, pause)){Main.resetPressed(pause); Main.playSelect(); blink_ct = -1; callFunc(false); return;}
		var r:Bool = Main.isPressed(Main.RIGHT, pause), l:Bool = Main.isPressed(Main.LEFT, pause);
		if(r && !l) cycleSel(1); if(l && !r) cycleSel(-1);
		Main.resetPressed(pause);
	}
}