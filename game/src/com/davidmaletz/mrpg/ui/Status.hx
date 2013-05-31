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

class Status extends Sprite {
	public static inline var BLUE:Int = 0x61a0ef; public static inline var RED:Int = 0xff5442; public static inline var GREEN:Int = 0x7ac835;
	public static inline var YELLOW:Int = 0xebb453; public static inline var GRAY:Int = 0xcccccc;
	public var player_hp:Bar; public var enemy_hp:Bar; public var player_mp:Bar; public var enemy_mp:Bar; public var combo:Bar;
	public var time:Text; public var player:Text; public var enemy:Text; public var score:Text; private var arrows:Text;
	public function new() {
		super(); player_hp = new Bar(Bar.HP,true); player_hp.x = 238; player_hp.y = 10; addChild(player_hp);
		enemy_hp = new Bar(Bar.HP,false); enemy_hp.x = 8; enemy_hp.y = 10; addChild(enemy_hp);
		player_mp = new Bar(Bar.MP,true); player_mp.x = 278; player_mp.y = 20; addChild(player_mp);
		enemy_mp = new Bar(Bar.MP,false); enemy_mp.x = 8; enemy_mp.y = 20; addChild(enemy_mp);
		var t:Text = new Text(YELLOW,16,Main.width,1,"TIME"); t.y = 8; addChild(t); time = new Text(0xebb453,24,Main.width,1); time.y = 30; addChild(time);
		player = new Text(BLUE,16,160,2); player.x = 228; player.y = 36; addChild(player);
		arrows = new Text(GRAY,16,160,2); arrows.x = 228; arrows.y = 56; arrows.visible = false; addChild(arrows);
		combo = new Bar(Bar.COMBO,true); combo.x = 268; combo.y = 58; addChild(combo);
		enemy = new Text(RED,16,160,0); enemy.x = 8; enemy.y = 36; addChild(enemy);
		t = new Text(GREEN,16,112,0,"SCORE:"); t.x = 8; t.y = 278; addChild(t);
		score = new Text(GREEN,16,208,0); score.x = 120; score.y = 278; addChild(score);
	}
	public function setArrows(a:Int):Void {
		var s:String = Std.string(a); while(s.length < 3) s = "0"+s; s = "Arrows: "+s; arrows.visible = true; combo.visible = false; arrows.setText(s); 
	}
}