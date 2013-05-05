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
import nme.display.Sprite;
import nme.events.Event;
import nme.geom.Matrix;
import nme.Lib;

class MedalDialog extends DialogBox {
	private var playSFX:Bool;
	public function new(i:Int, func:DialogBox->Bool=null){
		super(Achievements.ACHIEVEMENT_NAMES[i]+"\n\305\300"+Achievements.ACHIEVEMENT_DESC[i]+"\300\370\376", func, false, 0, 24, 2); playSFX = true;
		var s:Sprite = new Sprite(); Frame.drawFrame(s.graphics, 36, 36, true); var icon:Sprite = new Sprite(); var g = icon.graphics;
		g.beginBitmapFill(Achievement.getIcons(), new Matrix(1,0,0,1,-32*i,0), false, false); g.drawRect(0,0,32,32); g.endFill();
		icon.x = 2; icon.y = 2; s.addChild(icon); Text._setColor(icon, Status.YELLOW); s.x = 8; s.y = 8; addChild(s);
		var c = getChildAt(0); c.x = 50; c.y = 14; c = getChildAt(1); c.x = 50; c.y = 30;
	}
	public override function getFontSz(l:Int):Int {return (l==0?12:8);}
	public override function enter_frame(e:Event):Void {
		if(playSFX){
			Lib.current.stage.addChild(this); Main.playSFX("achievement"); playSFX = false;
		}
		super.enter_frame(e);
	}
}