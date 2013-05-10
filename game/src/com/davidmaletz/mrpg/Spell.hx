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
package com.davidmaletz.mrpg;
import nme.display.BitmapData;
import nme.display.Graphics;

class Spell {
	private var id:Int; private var cost:Int; public var lowDmg:Int; public var highDmg:Int;
	private var x:Int; private var y:Int; private var w:Int; private var h:Int; private var action:Spell->Character->Int->Void;
	public function new(i:Int, c:Int, ld:Int, hd:Int, _x:Int, _y:Int, a:Spell->Character->Int->Void, _w:Int=4, _h:Int=4){
		id = i; cost = c; lowDmg = ld; highDmg = hd; x = _x; y = _y; w = _w; h = _h; action = a;
	}
	public inline function getCost():Int {return cost;}
	public inline function use(c:Character, f:Int):Void {action(this,c,f);}
	public inline function createSprite():SpellSprite {var ret:SpellSprite = new SpellSprite(id,w,h); ret.x = x; ret.y = y; return ret;}
	public inline function playSFX():Void {playSpellSFX(id);}
	
	public static function playSpellSFX(spell:Int):Void {Main.playSFX("spell_sfx", spell);}
	
	public static function DirectDamage(frame:Int, blockable:Bool=true, mana:Float=0, drain:Bool=false):Spell->Character->Int->Void {
		function Action(s:Spell, c:Character, f:Int):Void {
			function hit(x:Character):Void {
				var dmg:Int = Main.battle.hit(Main.battle.getOpponent(c), s.lowDmg, s.highDmg, true, blockable, mana);
				if(drain && dmg != 0) Main.battle.heal(c, dmg, dmg);
			} c.setFunction(f+frame, hit);
		} return Action;
	}
	
	public static function DamageOverTime(st:Int, end:Int, blockable:Bool=true, mana:Float=0, drain:Bool=false):Spell->Character->Int->Void {
		function Action(s:Spell, c:Character, f:Int):Void {
			var dmg:Int = 0;
			function hit(x:Character):Void {dmg += Main.battle.hitDOT(Main.battle.getOpponent(c), s.lowDmg, s.highDmg, true, blockable, mana);}
			function finalHit(x:Character):Void {
				var ch:Character = Main.battle.getOpponent(c); dmg += Main.battle.hitDOT(ch, s.lowDmg, s.highDmg, true, blockable, mana);
				Main.battle.displayDmg(ch, dmg); if(drain && dmg != 0) Main.battle.heal(c, dmg, dmg);
			} for(i in st...end) c.setFunction(f+i, hit); c.setFunction(f+end, finalHit);
		} return Action;
	}
	
	public static function Heal(_st:Int, _end:Int, _heal:Int):Spell->Character->Int->Void {
		function Action(s:Spell, c:Character, f:Int):Void {
			if(_st != _end){function st(x:Character):Void {c.startEvade();} c.setFunction(f+_st, st);}
			function heal(x:Character):Void {Main.battle.heal(c, s.lowDmg, s.highDmg);} c.setFunction(f+_heal, heal);
			if(_st != _end){function end(x:Character):Void {c.resetDefense();} c.setFunction(f+_end, end);}
		} return Action;
	}
	
	public static var DEFEND:Spell; public static var SPELLS:Array<Spell>;
	
	public static var SPELL_NAMES:Array<String>;
	public static var SPELL_DESC1:Array<String>;
	public static var SPELL_DESC2:Array<String>;
	public static var SPELL_DESC3:Array<String>;
	
	private static var SPELL_ICON:Array<Int> = [3,11,5,8,9,8,9];
	private static var SPELL_X:Array<Int> = [32,-8,0,0,2,-4,2]; private static var SPELL_Y:Array<Int> = [32,10,0,6,-24,12,2];
	public static function drawIcon(g:Graphics, id:Int):Void {
		//TODO: spell icons: g.clear(); var b=Main.getBitmap("spell_icon", id); if(b!=null){g.beginBitmapFill(b); g.drawRect(0,0,48,48); g.endFill();}
		g.clear(); var s:Spell = SPELLS[id]; if(s == null) return; var w:Int=s.w, h:Int=s.h;
		var frame:Int=SPELL_ICON[id], bitmap:BitmapData = Main.getBitmap("spell", s.id), ty:Int = Math.floor(frame/w), tx:Int = frame-ty*w,
		fw:Int = Std.int(bitmap.width/w), fh:Int = Std.int(bitmap.height/h), ox:Float = SPELL_X[id], oy:Float = SPELL_Y[id];
		g.beginBitmapFill(bitmap, new nme.geom.Matrix(0.375,0,0,0.375,ox-fw*tx,oy-fh*ty), false, false);
		g.drawRect(ox,oy,fw,fh); g.endFill();
	}
}