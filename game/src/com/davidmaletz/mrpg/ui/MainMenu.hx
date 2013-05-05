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
import com.davidmaletz.mrpg.equipment.Equipment;
import nme.display.Bitmap;
import nme.display.Sprite;
import nme.events.KeyboardEvent;
import nme.Lib;
import nme.net.URLRequest;
import nme.ui.Keyboard;

class MainMenu extends Sprite {
	public function new() {
		super(); var bg:Bitmap = new Bitmap(Frame.background); bg.scaleX = bg.scaleY = 2; addChild(bg);
		Main.playBGM(Main.getBGM("menu")); var t:Text = new Text(Status.BLUE, 40, 400, 1, "Rhythos!"); t.y = 50; addChild(t);
		t = new Text(Status.YELLOW, 24, 400, 1, "ARCADE"); t.x = 0; t.y = 100; addChild(t);
		t = new Text(Status.RED, 16, 400, 1, "BETA"); t.x = 110; t.y = 100; addChild(t);
		addChild(new ChoiceDialog(["New Character", "Load Character", "Options", "Battle Controls", "Credits", "Manual", "Fancy Fish Games"], null, handleChoice, 2, false));
	}
	private function saveSlot(saves:GameSaves, i:Int, c:SavedCharacter):Bool {
		if(i == -1) return true; function createChar(c:Int):Bool {
			var char:Character = new Character("Hero", 0, Main.BASE_HP, Main.BASE_MP);
			char.equip[Character.LEGS] = Equipment.PANTS[0]; char.equip[Character.TORSO] = Equipment.SHIRTS[0];
			char.setHairColor(0); char.setEyeColor(0); char.setSkinColor(0);
			function doSave(closed:Bool, esc:Bool):Void {
				if(closed && !esc){saves.beginClose(); var sc = new SavedCharacter(char); GameSaves.writeSlot(i,sc); startGame(sc,i);}
			} Main.instance.addChild(new CreateChar(char, doSave));
			return true;
		} if(c != null){
			var cd = new ChoiceDialog(["Overwrite?","Yes","No"],[false,true,true]);
			cd.onSelect = function(c:Int):Bool {if(c == 1) cd.onSelect = createChar; return true;} Main.instance.addChild(cd);
		} else createChar(0);
		return false;
	}
	private function loadSlot(saves:GameSaves, i:Int, c:SavedCharacter):Bool {
		if(c != null){startGame(c,i); return true;} return i < 0;
	}
	private function startGame(c:SavedCharacter, i:Int):Void {Main.loadCharacter(c, i);}
	private static function loadURL(u:String):Void {
		function _loadURL(e:KeyboardEvent):Void {
			if(e.keyCode == Keyboard.ENTER){
				Lib.current.stage.removeEventListener(KeyboardEvent.KEY_UP, _loadURL); Lib.getURL(new URLRequest(u), "_blank");
			}
		} Lib.current.stage.addEventListener(KeyboardEvent.KEY_UP, _loadURL);
	}
	private function handleChoice(i:Int):Bool {
		switch(i){
			case 0: Main.instance.addChild(new GameSaves(saveSlot));
			case 1: Main.instance.addChild(new GameSaves(loadSlot));
			case 2: Main.instance.addChild(new Options(["Game Options","Import Character",Options.BGM_VOL,Options.SFX_VOL,"Close Options"],[false,true,true,true,true], GameMenu.importChar,0));
			case 3: Main.instance.addChild(new Instructions());
			case 4: Main.instance.addChild(new DialogBox("Made by \003David Maletz\000.\nMusic by \002Robot Horse\000.\nArt from \004LPC\000.\nBetter credits later!"));
			case 5: loadURL("http://fancyfishgames.com/rhythos");
			case 6: loadURL("http://fancyfishgames.com");
		} return false;
	}
}