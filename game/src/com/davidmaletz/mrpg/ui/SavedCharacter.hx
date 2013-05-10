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

class SavedCharacter {
	private var char:Character; public var experience:Int; public var gold:Int; public var score:Int; public var max_type:Int;
	public var weapons:Int; public var spells:Int; public var helms:Int; public var shirts:Int; public var pants:Int;
	public var achievements:Int; public var wins:Array<Int>; public var losses:Array<Int>;
	public function new(c:Character=null,e:Int=0,g:Int=200,s:Int=0,m:Int=0,w:Int=1,sp:Int=1,h:Int=1,sh:Int=1,p:Int=1){
		char = c; experience = e; gold = g; score = s; max_type = m; weapons = w; spells = sp; helms = h; shirts = sh; pants = p; achievements = 0;
		wins = new Array<Int>(); losses = new Array<Int>(); var len:Int = Main.wins.length; for(i in 0...len){wins.push(0); losses.push(0);}
	}
	public static function getCurrent():SavedCharacter {
		var ret:SavedCharacter = new SavedCharacter(Main.getPlayer(), Main.experience, Main.gold, Main.score, Main.max_type,
			Main.weapons.getAr(),Main.spells.getAr(),Main.helms.getAr(),Main.shirts.getAr(),Main.pants.getAr());
		ret.achievements = Main.achievements.getAr();
		var len:Int = Main.wins.length; for(i in 0...len){ret.wins[i] = Main.wins[i]; ret.losses[i] = Main.losses[i];} return ret;
	}
	public inline function getChar(icon:Bool=false):Character {
		if(icon){char.scaleX = -1; char.scaleY = 1;} else {char.scaleX = char.scaleY = 2;} return char;
	}
}