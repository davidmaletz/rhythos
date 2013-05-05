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
import nme.events.Event;

class Options extends ChoiceDialog {
	public static var BGM_VOL:String = "BGM Vol:     "; public static var SFX_VOL:String = "SFX Vol:     ";
	private var bgm_vol:Volume; private var sfx_vol:Volume; private var bgm:Int; private var sfx:Int;
	public function new(choices:Array<String>, selectable:Array<Bool>=null, func:Int->Bool=null, _align:Int=1, modal:Bool=true, ew:Int=16){
		super(choices, selectable, func, _align, modal, ew); bgm = -2; sfx = -2;
		for(i in 0...choices.length){if(choices[i] == BGM_VOL) bgm = i; else if(choices[i] == SFX_VOL) sfx = i;}
		if(bgm >= 0){bgm_vol = new Volume(Main.getBGMVol()); bgm_vol.x = 180; bgm_vol.y = 8+20*bgm; addChild(bgm_vol);}
		if(sfx >= 0){sfx_vol = new Volume(Main.getSFXVol()); sfx_vol.x = 180; sfx_vol.y = 8+20*sfx; addChild(sfx_vol);}
	}
	private override function select(i:Int):Void {if(i != bgm && i != sfx) super.select(i);}
	public function cycleVol(d:Int):Void {
		var v:Int; switch(ids[sel]){
			case bgm: v = Main.getBGMVol()+d; if(v <= 5 && v >= 0){Main.setBGMVol(v); bgm_vol.set(v); Main.playChange();}
			case sfx: v = Main.getSFXVol()+d; if(v <= 5 && v >= 0){Main.setSFXVol(v); sfx_vol.set(v); Main.playChange();}
		}
	}
	public override function handleKey(e:Event):Void {
		var r:Bool = Main.isPressed(Main.RIGHT, pause), l:Bool = Main.isPressed(Main.LEFT, pause); super.handleKey(e);
		if(r && !l) cycleVol(1); if(l && !r) cycleVol(-1);
	}
}