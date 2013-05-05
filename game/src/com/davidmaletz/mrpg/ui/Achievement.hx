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
import nme.display.BitmapData;
import nme.display.Sprite;
import nme.geom.Matrix;

class Achievement extends Sprite {
	private var id:Int; private var title:Text; private var desc:Text; private var icon:Sprite; private static var icons:BitmapData;
	public function new(i:Int) {
		super(); id = i; var b:Bool = Achievements.has(id); if(!b && Achievements.SECRET[id]) i = 23;
		Frame.drawFrame(graphics, 36, 36, true); icon = new Sprite(); var g = icon.graphics;
		g.beginBitmapFill(getIcons(), new Matrix(1,0,0,1,-32*i,0), false, false);
		g.drawRect(0,0,32,32); g.endFill(); icon.x = 2; icon.y = 2; addChild(icon); Text._setColor(icon, (b)?Status.GRAY:0x716a6b);
		title = new Text((b)?Status.GRAY:Frame.TEXT, 12, 0, 0, Achievements.ACHIEVEMENT_NAMES[i]); title.x = 42; title.y = 6; addChild(title);
		desc = new Text((b)?Status.GRAY:Frame.TEXT, 8, 0, 0, Achievements.ACHIEVEMENT_DESC[i]); desc.x = 42; desc.y = 22; addChild(desc);
	}
	public static function getIcons():BitmapData {if(icons == null) icons = Main.getBitmap("achievements"); return icons;}
	public function select():Void {if(Achievements.has(id)){title.setColor(Status.YELLOW); Text._setColor(icon, Status.YELLOW);}}
	public function deselect():Void {if(Achievements.has(id)){title.setColor(Status.GRAY); Text._setColor(icon, Status.GRAY);}}
}