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
import com.davidmaletz.mrpg.EnemyType;
import flash.net.FileFilter;
import flash.net.FileReference;
import nme.display.Bitmap;
import nme.display.Sprite;
import nme.events.Event;
import nme.events.KeyboardEvent;
import nme.Lib;
import nme.ui.Keyboard;
import nme.utils.ByteArray;

class GameMenu extends Sprite {
	public function new() {
		super(); var bg:Bitmap = new Bitmap(Frame.background); bg.scaleX = bg.scaleY = 2; addChild(bg);
		Main.playBGM(Main.getBGM("menu")); var t:Text = new Text(Status.BLUE, 32, 400, 1, "Ye Olde"); t.y = 50; addChild(t);
		t = new Text(Status.BLUE, 32, 400, 1, "Towne Menu"); t.y = 90; addChild(t);
		addChild(new ChoiceDialog(["Equipment", "Shop", "Save", "Next Battle", "Achievements", "Options", "Main Menu"], handleChoice, 2, false));
	}
	private function quit(i:Int):Bool {
		if(i == 1) Main.instance.mainMenu(); return true;
	}
	private function selectEnemy(closed:Bool, enemy:Int):Void {
		if(closed && enemy != -1 && enemy <= Main.max_type) Main.instance.startBattle(enemy);
	}
	private static var ref:FileReference;
	public static function exportChar(i:Int):Bool {
		if(i == 1) Lib.current.stage.addEventListener(KeyboardEvent.KEY_UP, doExportChar); return true;
	}
	public static function importChar(i:Int):Bool {
		if(i == 1) Lib.current.stage.addEventListener(KeyboardEvent.KEY_UP, doImportChar); return true;
	}
	public static function doExportChar(e:KeyboardEvent):Void {
		if(e.keyCode != Keyboard.ENTER) return; Lib.current.stage.removeEventListener(KeyboardEvent.KEY_UP, doExportChar);
		if(ref == null) ref = new FileReference(); function selectFile(e:Event):Void {
			ref.removeEventListener(Event.SELECT, selectFile);
		} ref.addEventListener(Event.SELECT, selectFile); var b:ByteArray = new ByteArray();
		GameSaves.saveCharacter(SavedCharacter.getCurrent(), b); ref.save(b, Main.getPlayer().cname+".rch");
	}
	public static function doImport(b:ByteArray):Void {
		function saveSlot(saves:GameSaves, i:Int, c:SavedCharacter):Bool {
			if(i == -1) return true; function createChar(c:Int):Bool {
				b.position = 0; saves.beginClose(); var sc = GameSaves.loadCharacter(b); if(sc != null){GameSaves.writeSlot(i,sc); Main.loadCharacter(sc, i);}
				return true;
			} if(c != null){
				var cd = new ChoiceDialog(["Overwrite?","Yes","No"],[false,true,true]);
				cd.onSelect = function(c:Int):Bool {if(c == 1) cd.onSelect = createChar; return true;} Main.instance.addChild(cd);
			} else createChar(0);
			return false;
		}
		Main.instance.addChild(new GameSaves(saveSlot));
	}
	public static function doImportChar(e:KeyboardEvent):Void {
		if(e.keyCode != Keyboard.ENTER) return; Lib.current.stage.removeEventListener(KeyboardEvent.KEY_UP, doImportChar);
		if(ref == null) ref = new FileReference();
		function loadFile(e:Event):Void {
			ref.removeEventListener(Event.COMPLETE, loadFile); doImport(ref.data);
		}
		var selectFile:Event->Void = null;
		function cancelFile(e:Event):Void {
			ref.removeEventListener(Event.SELECT, selectFile); ref.removeEventListener(Event.CANCEL, cancelFile);
		}
		selectFile = function(e:Event):Void {
			ref.removeEventListener(Event.SELECT, selectFile); ref.removeEventListener(Event.CANCEL, cancelFile); ref.addEventListener(Event.COMPLETE, loadFile); ref.load();
		} ref.addEventListener(Event.SELECT, selectFile); ref.addEventListener(Event.CANCEL, cancelFile);
		ref.browse([new FileFilter("Rhythos Character Files (*.rch)", "*.rch")]);
	}
	private function handleChoice(i:Int):Bool {
		switch(i){
			case 0: Main.instance.addChild(new CharSheet(Main.getPlayer()));
			case 1: Main.instance.addChild(new Shop());
			case 2: Main.saveGame(); Main.instance.addChild(new DialogBox("\300Game Saved!\300",null,true,0,24,1,1));
			case 3: Main.instance.addChild(new SelectEnemy(selectEnemy));
			case 4: Main.instance.addChild(new Achievements());
			case 5: Main.instance.addChild(new Options(["Game Options","Export Character",Options.BGM_VOL,Options.SFX_VOL,"Close Options"],[false,true,true,true,true], exportChar,0));
			case 6: addChild(new ChoiceDialog(["Really Quit?","Yes","No"],[false,true,true],quit,0));
		} return false;
	}
}