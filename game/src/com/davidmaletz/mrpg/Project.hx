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
import com.davidmaletz.mrpg.equipment.Equipment;
import com.davidmaletz.mrpg.equipment.Weapon;
import nme.geom.ColorTransform;

class Project {
	//TODO: remove below
	public static inline var IDLE:Int=EnemyPattern.IDLE; public static inline var ATTACK:Int=EnemyPattern.ATTACK;
	public static inline var DEFEND:Int=EnemyPattern.DEFEND; public static inline var CAST:Int=EnemyPattern.CAST;
	public static inline var DODGE:Int=EnemyPattern.DODGE;
	
	public static inline function createWraith():EnemyType {
		var ret:EnemyType = new EnemyType(/*bg*/-1,/*name*/"Wraith",/*type*/-1,/*hp*/16000,/*mp*/12000,/*exp*/3890,/*gold*/800,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/0,/*song*/2,/*weapon*/0,/*spell*/3,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/[CAST,DEFEND,ATTACK,DEFEND],/*skin*/Main.getBitmap("monster",4),400,1000);
		ret.setDefense(0.3,1); return ret;
	}
	public static inline function createFlower():EnemyType {
		var ret:EnemyType = new EnemyType(/*bg*/-2,/*name*/"Flower",/*type*/-1,/*hp*/25000,/*mp*/15000,/*exp*/5010,/*gold*/1200,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/0,/*song*/3,/*weapon*/0,/*spell*/3,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/[CAST,DODGE,ATTACK,ATTACK],/*skin*/Main.getBitmap("monster",5),600,1000);
		ret.setDefense(0.9,0.95); return ret;
	}
	public static inline function createOrcWarrior():EnemyType {
		var ret:EnemyType = new EnemyType(/*bg*/1,/*name*/"Goblin",/*type*/3,/*hp*/28000,/*mp*/15000,/*exp*/5430,/*gold*/1250,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/30,/*song*/1,/*weapon*/2,/*spell*/2,/*head*/0,
			/*torso*/2,/*legs*/-1,/*pattern low*/[CAST,IDLE,ATTACK,DODGE]);
		ret.setDefense(1.25,1.25); return ret;
	}
	public static inline function createBeholder():EnemyType {
		var ret:EnemyType = new EnemyType(/*bg*/-1,/*name*/"Beholder",/*type*/-1,/*hp*/40000,/*mp*/20000,/*exp*/7020,/*gold*/1700,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/0,/*song*/2,/*weapon*/0,/*spell*/5,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/[CAST,DODGE,ATTACK,DODGE],/*skin*/Main.getBitmap("monster",6),700,1500);
		ret.setDefense(0.85,0.5); return ret;
	}
	public static inline function createSandworm():EnemyType {
		var ret:EnemyType = new EnemyType(/*bg*/1,/*name*/"Sandworm",/*type*/-1,/*hp*/50000,/*mp*/15000,/*exp*/8690,/*gold*/2200,
			/*pattern*/[ATTACK,DEFEND,DEFEND,DEFEND],/*lvl*/0,/*song*/2,/*weapon*/0,/*spell*/1,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/[CAST,DEFEND,DEFEND,DEFEND],/*skin*/Main.getBitmap("monster",7));
		ret.setWeapon(new Weapon(-1, Weapon.SLASH, 0, 0, -76, 2000, 2800, 11, Weapon.CRITICAL_HIT)); return ret;
	}
	public static function load():Void {//TODO: load from database.
		Character.GENDERS = ["Male", "Female"];
		Character.SKIN_COLORS = ["Light", "Tanned", "Dark", "Darker", "Darkest"];
		Character.HAIR_STYLES =  [["Normal", "Short", "Long", "Messy 1", "Messy 2", "Mohawk", "Page", "Parted", "Bald"],
								  ["Normal", "Short", "Long", "Pixie", "Ponytail", "Princess", "Swoop", "Curly", "Bald"]];
		Character.HAIR_COLORS = ["Brown", "Blond", "Red", "Dark Brown", "Black", "White", "Pink", "Blue", "Green"];
		Character.EYE_COLORS = ["Brown", "Blue", "Green", "Gray", "Red"];
		Character.SKIN_MAT = [new ColorTransform(1,1,1,1,0,0,0,0), new ColorTransform(0.9,0.9,0.85,1,-10,-10,-10,0),
			new ColorTransform(0.9,0.9,0.9,1,-35,-35,-35,0), new ColorTransform(0.85,0.85,0.85,1,-52,-52,-52,0), new ColorTransform(0.8,0.8,0.8,1,-80,-70,-70,0)];
		Character.HAIR_MAT = [new ColorTransform(1,0.8,0.7,1,-5,-10,-50,0), new ColorTransform(1,1,0.7,1,0,0,-50,0),
			new ColorTransform(1,0.7,0.6,1,15,-20,-50,0), new ColorTransform(1,0.8,0.7,1,-60,-50,-70,0), new ColorTransform(0.7,0.7,0.7,1,-60,-60,-60,0),
			new ColorTransform(1,1,1,1,20,60,20,0), new ColorTransform(1,0.9,0.8,1,30,10,10,0), new ColorTransform(0.8,0.8,1,1,-40,0,20,0),
			new ColorTransform(0.7,0.95,0.5,1,-40,-20,-60,0)];
		Character.EYES_MAT = [new ColorTransform(0.9,0.6,0.5,1,15,-30,-60,0), new ColorTransform(1,1,1,1,0,0,0,0),
			new ColorTransform(1,0.9,0.6,1,15,15,-50,0), new ColorTransform(1,0.7,0.5,1,50,30,50,0), new ColorTransform(1,0,0,1,128,0,0,0)];
		Character.EYE_WHITE = 0xfff2f8f9;
		
		Equipment.TYPES = [0,1,0,0];
		
		Equipment.WEAPONS = [new Weapon(-1, Weapon.SLASH, 0, 0, -76, 50, 100, 2, Weapon.CRITICAL_HIT), new Weapon(0, Weapon.SLASH, 0, 0, -64, 100, 250, 2, Weapon.CRITICAL_HIT),
			new Weapon(1, Weapon.SLASH, -32, -32, -54, 250, 500, 4, Weapon.CRITICAL_HIT), new Weapon(2, Weapon.SLASH, -64, -64, -10, 500, 1000, 6, Weapon.CRITICAL_HIT), new Weapon(3, Weapon.THRUST, 0, 0, -54, 300, 400, 9, Weapon.ARMOR_PIERCE),
			new Weapon(4, Weapon.THRUST, -64, -64, 20, 600, 800, 10, Weapon.ARMOR_PIERCE), new Weapon(5, Weapon.THRUST, -64, -64, 32, 1000, 1200, 11, Weapon.ARMOR_PIERCE), new Weapon(6, Weapon.SLASH, 0, 0, -64, 50, 300, 2, Weapon.MANA_GEN),
			new Weapon(7, Weapon.THRUST, 0, 0, -56, 100, 500, 2, Weapon.MANA_GEN), new Weapon(8, Weapon.SLASH, -32, -32, -54, 200, 800, 2, Weapon.MANA_GEN), new Weapon(9, Weapon.BOW, 0, 0, -76, 200, 350, 2, Weapon.NONE),
			new Weapon(10, Weapon.BOW, -32, -32, -76, 350, 600, 2, Weapon.NONE), new Weapon(11, Weapon.BOW, -32, -32, -76, 600, 1000, 2, Weapon.NONE)];
		Equipment.PANTS = [new Equipment(0, 1, 1), new Equipment(1, 0.95, 1), new Equipment(2, 0.9, 1), new Equipment(3, 0.85, 1), new Equipment(4, 0.85, 0.9)];
		Equipment.SHIRTS = [new Equipment(5, 1, 1), new Equipment(6, 0.95, 1), new Equipment(7, 0.9, 1), new Equipment(8, 0.85, 1), new Equipment(9, 0.85, 0.9)];
		Equipment.HELMS = [null, new Equipment(10, 1, 0.95), new Equipment(11, 1, 0.85, false), new Equipment(12, 0.95, 1), new Equipment(13, 0.9, 1, false), new Equipment(14, 0.85, 1, false), new Equipment(15, 0.85, 0.9, false)];
	
		Equipment.WEAPON_NAMES = ["Unarmed", "Dagger", "Saber", "Longsword", "Spear", "Long Spear", "Dragonslayer",
								  "Wand", "Staff", "Mace", "Short Bow", "Long Bow", "Recurve Bow"];
		Equipment.PANT_NAMES   = ["Pants", "Boots", "Heavy Boots", "Greaves", "Arcane Greaves"];
		Equipment.SHIRT_NAMES  = ["Shirt", "Leather Armor", "Chain Mail", "Plate Armor", "Arcane Plate"];
		Equipment.HELM_NAMES   = ["None", "Cap", "Cloth Hood", "Skull Cap", "Chain Hood", "Helm", "Arcane Helm"];
	
		Equipment.WEAPON_COSTS = [0,100,600,4000,100,600,4000,100,600,4000,100,600,4000];
		Equipment.PANT_COSTS   = [0,100,500,2000,3500];
		Equipment.SHIRT_COSTS  = [0,100,500,2000,3500];
		Equipment.HELM_COSTS   = [0,100,1500,100,500,2000,3500];
		
		Spell.DEFEND = new Spell(0, 750, 0, 0, 32, 0, null);
		Spell.SPELLS = [new Spell(1, 3000, 500, 1500, -128, 12, Spell.DirectDamage(3), 7, 1), new Spell(2, 3000, 3000, 5000, -64, 0, Spell.DirectDamage(12)),
			new Spell(3, 3000, 300, 500, -96, -16, Spell.DamageOverTime(6,14)), new Spell(4, 3000, 1000, 2000, -112, 0, Spell.DirectDamage(11,true,0,true)), new Spell(5, 3000, 250, 350, -112, -32, Spell.DamageOverTime(4,12,false)),
			new Spell(6, 3000, -200, 600, -112, 0, Spell.DamageOverTime(2,15,true,1)), new Spell(7, 3000, 1500, 3000, 112, 0, Spell.Heal(2,14,8))];
		Spell.SPELL_NAMES = ["Spike", "Snake", "Lion", "Squid", "Tornado", "Lightning", "Heal"];
		Spell.SPELL_DESC1 = ["Basic earth damage.", "Basic earth damage.", "Burns the enemy.", "Drains enemy health.", "Unblockable damage.", "Damages HP & MP.", "Heals & Blocks."];
		Spell.SPELL_DESC2 = [" DMG", " DMG", " DMG/FRAME", " DMG+HEAL", " DMG/FRAME", " DMG/FRAME", " HEAL"];
		Spell.SPELL_DESC3 = [" MP", " MP", " MP, 9 FRAMES", " MP", " MP, 9 FRAMES", " MP, 14 FRAMES", " MP"];
		for(i in 0...Spell.SPELL_NAMES.length){
			Spell.SPELL_DESC2[i] = Battle.format(Std.string(Math.max(0,Spell.SPELLS[i].lowDmg)))+"-"+Battle.format(Std.string(Spell.SPELLS[i].highDmg))+Spell.SPELL_DESC2[i];
			Spell.SPELL_DESC3[i] = Battle.format(Std.string(Spell.SPELLS[i].getCost()))+Spell.SPELL_DESC3[i];
		}
		
		EnemyType.ENEMIES = [new EnemyType(/*bg*/0,/*name*/"Slime",/*type*/-1,/*hp*/6000,/*mp*/3000,/*exp*/400,/*gold*/50,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/0,/*song*/1,/*weapon*/0,/*spell*/0,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/null,/*skin*/Main.getBitmap("monster",0),100,200),
			new EnemyType(/*bg*/0,/*name*/"Giant Bee",/*type*/-1,/*hp*/8000,/*mp*/4000,/*exp*/820,/*gold*/100,
			/*pattern*/[ATTACK,ATTACK,ATTACK,DEFEND],/*lvl*/0,/*song*/1,/*weapon*/0,/*spell*/0,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/[ATTACK,ATTACK,CAST,IDLE],/*skin*/Main.getBitmap("monster",1),200,400),
			new EnemyType(/*bg*/0,/*name*/"Snake",/*type*/-1,/*hp*/10000,/*mp*/4000,/*exp*/1240,/*gold*/150,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/0,/*song*/1,/*weapon*/0,/*spell*/1,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/[CAST,IDLE,ATTACK,DODGE],/*skin*/Main.getBitmap("monster",2),300,600),
			new EnemyType(/*bg*/0,/*name*/"Vampire",/*type*/-1,/*hp*/12000,/*mp*/10000,/*exp*/2100,/*gold*/400,
			/*pattern*/[CAST,ATTACK,DEFEND,ATTACK],/*lvl*/0,/*song*/2,/*weapon*/0,/*spell*/3,/*head*/-1,
			/*torso*/-1,/*legs*/-1,/*pattern low*/null,/*skin*/Main.getBitmap("monster",3),400,700),
			new EnemyType(/*bg*/-1,/*name*/"Skeleton",/*type*/2,/*hp*/15000,/*mp*/12000,/*exp*/2520,/*gold*/450,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/0,/*song*/1,/*weapon*/5,/*spell*/3,/*head*/3,
			/*torso*/2,/*legs*/-1,/*pattern low*/[CAST,DEFEND,ATTACK,DEFEND]),
			new EnemyType(/*bg*/-1,/*name*/"Skeleton",/*type*/2,/*hp*/18000,/*mp*/12000,/*exp*/2940,/*gold*/500,
			/*pattern*/[ATTACK,ATTACK,ATTACK,IDLE],/*lvl*/10,/*song*/1,/*weapon*/11,/*spell*/3,/*head*/1,
			/*torso*/1,/*legs*/-1,/*pattern low*/[CAST,ATTACK,ATTACK,IDLE]),
			createWraith(),
			createFlower(),
			createOrcWarrior(),
			new EnemyType(/*bg*/1,/*name*/"Goblin",/*type*/3,/*hp*/30000,/*mp*/15000,/*exp*/5980,/*gold*/1350,
			/*pattern*/[ATTACK,ATTACK,ATTACK,IDLE],/*lvl*/5,/*song*/1,/*weapon*/12,/*spell*/2,/*head*/0,
			/*torso*/1,/*legs*/-1,/*pattern low*/[CAST,ATTACK,ATTACK,IDLE]),
			createBeholder(),
			new EnemyType(/*bg*/1,/*name*/"Shaman",/*type*/3,/*hp*/35000,/*mp*/40000,/*exp*/7570,/*gold*/1800,
			/*pattern*/[CAST,IDLE,CAST,ATTACK],/*lvl*/5,/*song*/1,/*weapon*/9,/*spell*/2,/*head*/0,
			/*torso*/1,/*legs*/1,/*pattern low*/null),
			createSandworm(),
			new EnemyType(/*bg*/-2,/*name*/"Lich Lord",/*type*/2,/*hp*/40000,/*mp*/50000,/*exp*/10420,/*gold*/3000,
			/*pattern*/[CAST,IDLE,CAST,ATTACK],/*lvl*/15,/*song*/3,/*weapon*/8,/*spell*/5,/*head*/2,
			/*torso*/1,/*legs*/1,/*pattern low*/null),
			new EnemyType(/*bg*/-2,/*name*/"Evil Hero",/*type*/0,/*hp*/40000,/*mp*/40000,/*exp*/13180,/*gold*/5000,
			/*pattern*/[ATTACK,DEFEND,ATTACK,DEFEND],/*lvl*/15,/*song*/3,/*weapon*/6,/*spell*/5,/*head*/6,
			/*torso*/4,/*legs*/4,/*pattern low*/[ATTACK,DEFEND,CAST,DODGE])];
			EnemyType.DESC = ["The stereotypical foe!", "Ain't that a nasty stinger?",
			"A legless, carnivorous reptile.", "Not another health-stealing bat!", "Once warrior, now pile of bones!",
			"I wonder if it uses bone arrows?", "I bet weapons are ineffective.", "This is not your average flower.",
			"Cruel, wicked and bad-hearted.", "Now comes with a bow.", "I see you.", "An intelligent goblin?!?!",
			"He who controls the spice...", "Mastermind behind the undead.", "A hero turned to the dark side."];
	}
}