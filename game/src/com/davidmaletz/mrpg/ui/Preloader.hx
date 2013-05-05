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
import nme.display.Bitmap;
import nme.display.BitmapData;
import nme.display.Sprite;
import nme.events.Event;

class Preloader extends NMEPreloader {
	private var bar:Bar; public var text:Text; public var text2:Text;
	public static inline var MSG_MAX:Int=120; private var msg_ct:Int; private var cur_msg:Int;
	private var messages:Array<String>;
	public function new() {
		super(); while(numChildren > 0) removeChildAt(0); 
		Frame.init(); var bg = new Bitmap(Frame.background); bg.scaleX = bg.scaleY = 2; addChild(bg); msg_ct = MSG_MAX;
		var t:Text = new Text(Status.BLUE, 40, 400, 1, "Rhythos!"); t.y = 50; addChild(t);
		t = new Text(Status.YELLOW, 24, 400, 1, "ARCADE"); t.x = 0; t.y = 100; addChild(t);
		t = new Text(Status.RED, 16, 400, 1, "BETA"); t.x = 110; t.y = 100; addChild(t);
		bar = new Bar(Bar.HP, false); bar.setPercent(0); bar.scaleX = bar.scaleY = 2; bar.x = 50; bar.y = 138; addChild(bar);
		var s:Sprite = new Sprite(); Frame.drawFrame(s.graphics, 400, 52, true); s.y = 197; addChild(s);
		text = new Text(Status.GRAY, 16, 400, 1); text.y = 205; addChild(text);
		text2 = new Text(Status.GRAY, 16, 400, 1); text2.y = text.y+20; addChild(text2);
		initMessages(); setMsg(messages[cur_msg]); addEventListener(Event.ADDED_TO_STAGE, init); addEventListener(Event.REMOVED_FROM_STAGE, destroy);
	}
	private function init(e:Event):Void {addEventListener(Event.ENTER_FRAME, enter_frame);}
	private function destroy(e:Event):Void {removeEventListener(Event.ENTER_FRAME, enter_frame);}
	public function setMsg(s:String):Void {
		var a:Array<String> = s.split("\n"); text.setText(a[0]); if(a.length > 1) text2.setText(a[1]); else text2.setText("");
	}
	public function initMessages():Void {
		if(messages == null){
		messages = ["Hold down LEFT to\nattack rapidly","Enemies follow simple\npatterns of action","Some enemies change\npatterns at half health",
		"Enemy spells revert to\nattacks when low on MP","Spells can be deadly,\nmake sure to block them","Having trouble? Try a\ndifferent spell/weapon",
		"The manual contains all\nthese tips, and more","Buying an item in the\nshop does not equip it","Attacks will always hit\nunless the enemy blocks",
		"Timing is important!\nFollow the rhythm!","Different weapon types\nhave unique properties","Score gives you bonus\nexp, even if you lose",
		"Having trouble? Level\nUP beating a weaker foe","If the timer runs out,\nhighest HP left wins","Enemy patterns are in\nsync with the music",
		"Hit LEFT to attack and\nRIGHT to cast a spell", "Hit UP to dodge and\nDOWN to block (750 MP)","The combo meter gives\nyou special abilities",
		"Game will autosave each\ntime you win a battle."];
		}
		var i:Int = messages.length; while(i>1){
			var t:String = messages[i-1], j:Int = Std.int(Math.random()*i); messages[i-1] = messages[j]; messages[j] = t; i--;
		} cur_msg = 0;
	}
	private function enter_frame(e:Event):Void {
		msg_ct--; if(msg_ct == 0){cur_msg++; if(cur_msg >= messages.length) initMessages(); msg_ct = MSG_MAX; setMsg(messages[cur_msg]);}
	}
	public override function onUpdate(bytesLoaded:Int, bytesTotal:Int){
		bar.setPercent(bytesLoaded/bytesTotal);
	}
}