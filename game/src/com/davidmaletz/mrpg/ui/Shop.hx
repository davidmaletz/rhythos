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
import com.davidmaletz.mrpg.equipment.Equipment;
import nme.display.Sprite;
import nme.events.Event;

class Shop extends Sprite {
	private static var items:Array<ShopItem>; private static var MAX_BLINK:Int=8; private static var SCROLL_H:Int=252;
	private var char:Character; private var card:Sprite; private var sel:Int; private var arrow:Text; private var up_arrow:Text;
	private var down_arrow:Text; private var pause:Int; private var scroll:Sprite; private var blink_ct:Int;
	public function new() {
		super(); var w:Int=400,h:Int=300; char = Main.getPlayer().clone(); refreshChar(); Frame.drawFrame(graphics,w,h); pause = Main.pause();
		card = CharSheet.createCharCard(char); card.x = w-128-16; addChild(card); w -= 128+32; Frame.drawDividerV(graphics,w,6,h-12); w += 8;
		Main.safeEnterFrame(this, handleKey); sel = 0; arrow = new Text(Status.YELLOW, 12, 12, 0, "\202"); arrow.x = 10; addChild(arrow);
		up_arrow = new Text(Status.YELLOW, 16, w, 1, "\200"); up_arrow.y = 8; addChild(up_arrow);
		down_arrow = new Text(Status.YELLOW, 16, w, 1, "\177"); down_arrow.y = h-24; addChild(down_arrow);
		initItems(); var end:Int=items.length; for(i in 0...end) items[i].available = items[i].getAvailable();
		scroll = new Sprite(); items.sort(compareItem); for(i in 0...end){
			var t:Text = new Text((items[i].available)?Status.GRAY:Frame.TEXT, 12, w, 0, items[i].getName()); t.y = 16*i; scroll.addChild(t);
			t = new Text(Status.YELLOW, 8, w-36, 2, Battle.format(Std.string(items[i].cost))); t.y = 16*i+4; scroll.addChild(t);
		} var m:Sprite = new Sprite(); m.graphics.beginFill(0); m.graphics.drawRect(24,24,w,SCROLL_H); m.graphics.endFill();
		addChild(m); scroll.mask = m; scroll.x = 24; scroll.y = 24; addChild(scroll); updateSel(); y = 300; blink_ct = MAX_BLINK;
	}
	private function compareItem(a:ShopItem, b:ShopItem):Int {
		if(a.available == b.available){
			if(a.cost == b.cost){if(a.type == b.type) return a.id-b.id; else return a.type-b.type;} else return a.cost-b.cost;
		} else return (a.available)?-1:1;
	}
	public inline function getText(i:Int):Text {return cast(scroll.getChildAt(2*i), Text);}
	public inline function getSelText():Text {return getText(sel);}
	private inline function updateSel():Void {
		var t:Text = getSelText(); if(items[sel].available) t.setColor(Status.YELLOW); if(scroll.y+t.y < 24) scroll.y = 24-t.y;
		else if(scroll.y+t.y > SCROLL_H+12) scroll.y = SCROLL_H+12-t.y; arrow.y = scroll.y+t.y;
		up_arrow.alpha = (scroll.y < 24)?1:0; down_arrow.alpha = (scroll.y > SCROLL_H+28-items.length*16)?1:0;
		items[sel].equip(char, this); 
	}
	public function cycleSel(d:Int):Void {
		if(items[sel].available) getSelText().setColor(Status.GRAY); items[sel].unequip(char, this); var l:Int = items.length;
		sel += d; if(sel >= l) sel = 0; else if(sel < 0) sel = l-1; updateSel(); Main.playClick();
	}
	private function purchase():Void {
		var i:ShopItem = items[sel]; if(!i.available) return; i.purchase(); CharSheet.setText(card, CharSheet.CARD_GOLD, "GOLD: "+Battle.format(Std.string(Main.gold)));
		var end:Int=items.length; for(c in 0...end){
			i = items[c]; i.available = i.getAvailable(); if(!i.available) getText(c).setColor(Frame.TEXT);
		} Achievements.updateItems();
	}
	private function exit():Void {if(pause > 0){Main.unpause(); pause = 0;} parent.removeChild(this);}
	public static var firstShop:Bool = true;
	public function handleKey(e:Event):Void {
		var dh:Int = 75; if(sel < 0){y += dh; if(y >= 300) exit(); return;} if(y > 0){y -= dh;
			if(y <= 0 && Main.score == 0 && Main.gold == 200 && firstShop){
				parent.addChild(new DialogBox("Hit '\004ESC\000' to exit.",null,true,0,24,1,1));
			} if(y <= 0) firstShop = false; return;
		} blink_ct--; if(blink_ct == 0){blink_ct = MAX_BLINK; up_arrow.visible = !up_arrow.visible; down_arrow.visible = !down_arrow.visible;}
		if(Main.isPressed(Main.ESCAPE, pause)){Main.resetPressed(pause); Main.playCancel(); sel = -2; return;}
		if(Main.isPressed(Main.ENTER, pause)){Main.resetPressed(pause); Main.playSelect(); purchase(); return;}
		var u:Bool = Main.isPressed(Main.UP, pause), d:Bool = Main.isPressed(Main.DOWN, pause);
		if(u && !d) cycleSel(-1); if(d && !u) cycleSel(1);
		Main.resetPressed(pause);
	}
	public function updateWeapon():Void {
		CharSheet.setText(card, CharSheet.CARD_DMG, "DMG: "+Battle.format(Std.string(char.weapon.lowDmg+char.level*20))+"-"+Battle.format(Std.string(char.weapon.highDmg+char.level*20)));
		refreshChar();
	}
	public function updateEquipment():Void {
		char.resetDefense(); CharSheet.setText(card, CharSheet.CARD_PDEF, "PHYS DEF: "+Std.int(100.5-char.phys_def*100)+"%");
		CharSheet.setText(card, CharSheet.CARD_MDEF, "MAG DEF: "+Std.int(100.5-char.mag_def*100)+"%"); char.updateBitmap();
		refreshChar();
	}
	private inline function refreshChar():Void {char.setFrame(char.getIdle());}
	
	private static function initItems():Void {
		if(items == null){
			items = new Array<ShopItem>(); var end:Int;
			end = Equipment.WEAPONS.length; for(i in 1...end) items.push(new ShopItem(ShopItem.WEAPON, i));
			end = Equipment.HELMS.length; for(i in 1...end) items.push(new ShopItem(ShopItem.HEAD, i));
			end = Equipment.SHIRTS.length; for(i in 1...end) items.push(new ShopItem(ShopItem.TORSO, i));
			end = Equipment.PANTS.length; for(i in 1...end) items.push(new ShopItem(ShopItem.LEGS, i));
		}
	}
}