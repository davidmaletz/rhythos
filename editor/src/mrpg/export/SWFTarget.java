/*******************************************************************************
 * Rhythos Editor is a game editor and project management tool for making RPGs on top of the Rhythos Game system.
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
package mrpg.export;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mrpg.editor.resource.Project;
import mrpg.editor.resource.TileResource;
import mrpg.world.BasicTilemap;

import com.flagstone.transform.DefineData;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieObject;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.SymbolClass;

public class SWFTarget implements Target {
	private Movie movie; private int id; private SymbolClass bitmaps, sounds, bytearrays;
	public SWFTarget(){}
	private void init(Graphic frame, Graphic font, Graphic bg) throws Exception{
		movie = new Movie(); movie.decodeFromStream(SWFTarget.class.getResourceAsStream("/base.swf"));
		List<MovieTag> tags = movie.getObjects(); bitmaps = new SymbolClass(); sounds = new SymbolClass();
		bytearrays = new SymbolClass(); tags.set(3, font.defineImage(4));
		tags.set(4, bg.defineImage(3)); tags.set(5, frame.defineImage(2)); id = 5;
	}
	public void build(Project p){
		/*
		 * TODO: load assets from project, call init, addImage, addSound and addData to add them to the
		 * swf, finish() to finish adding data, and finally writeToFile(f)
		 */
		try {
			File f = new File("img"); if(!f.exists()) f.mkdir();
			f = new File("snd"); if(!f.exists()) f.mkdir();
			String FOLDER = "../assets/";
			init(p.getFrame(), p.getFont(), p.getBG());
			addImage(loadImage(new File(FOLDER+"body_m.png")), "skin", 0);
			addImage(loadImage(new File(FOLDER+"body_f.png")), "skin", 1);
			addImage(loadImage(new File(FOLDER+"body_s.png")), "skin", 2);
			addImage(loadImage(new File(FOLDER+"body_o.png")), "skin", 3);
			addImage(loadImage(new File(FOLDER+"eyes_m.png")), "eyes", 0);
			addImage(loadImage(new File(FOLDER+"eyes_f.png")), "eyes", 1);
			addImage(loadImage(new File(FOLDER+"hair0_m.png")), "hair", 0, 0);
			addImage(loadImage(new File(FOLDER+"hair0_f.png")), "hair", 1, 0);
			addImage(loadImage(new File(FOLDER+"hair1_m.png")), "hair", 0, 1);
			addImage(loadImage(new File(FOLDER+"hair1_f.png")), "hair", 1, 1);
			addImage(loadImage(new File(FOLDER+"hair2_m.png")), "hair", 0, 2);
			addImage(loadImage(new File(FOLDER+"hair2_f.png")), "hair", 1, 2);
			addImage(loadImage(new File(FOLDER+"hair3_m.png")), "hair", 0, 3);
			addImage(loadImage(new File(FOLDER+"hair3_f.png")), "hair", 1, 3);
			addImage(loadImage(new File(FOLDER+"hair4_m.png")), "hair", 0, 4);
			addImage(loadImage(new File(FOLDER+"hair4_f.png")), "hair", 1, 4);
			addImage(loadImage(new File(FOLDER+"hair5_m.png")), "hair", 0, 5);
			addImage(loadImage(new File(FOLDER+"hair5_f.png")), "hair", 1, 5);
			addImage(loadImage(new File(FOLDER+"hair6_m.png")), "hair", 0, 6);
			addImage(loadImage(new File(FOLDER+"hair6_f.png")), "hair", 1, 6);
			addImage(loadImage(new File(FOLDER+"hair7_m.png")), "hair", 0, 7);
			addImage(loadImage(new File(FOLDER+"hair7_f.png")), "hair", 1, 7);
			addImage(loadImage(new File(FOLDER+"head_o.png")), "hair", 3, 0);
			
			addImage(loadImage(new File(FOLDER+"field.png")), "bg", 0);
			addImage(loadImage(new File(FOLDER+"desert.png")), "bg", 1);
			
			addImage(loadImage(new File(FOLDER+"weapons/arrow.png")), "arrow");
			addImage(loadImage(new File(FOLDER+"weapons/dagger.png")), "weapon", 0);
			addImage(loadImage(new File(FOLDER+"weapons/saber.png")), "weapon", 1);
			addImage(loadImage(new File(FOLDER+"weapons/longsword.png")), "weapon", 2);
			addImage(loadImage(new File(FOLDER+"weapons/spear.png")), "weapon", 3);
			addImage(loadImage(new File(FOLDER+"weapons/longspear.png")), "weapon", 4);
			addImage(loadImage(new File(FOLDER+"weapons/dragonspear.png")), "weapon", 5);
			addImage(loadImage(new File(FOLDER+"weapons/wand.png")), "weapon", 6);
			addImage(loadImage(new File(FOLDER+"weapons/staff.png")), "weapon", 7);
			addImage(loadImage(new File(FOLDER+"weapons/mace.png")), "weapon", 8);
			addImage(loadImage(new File(FOLDER+"weapons/bow.png")), "weapon", 9);
			addImage(loadImage(new File(FOLDER+"weapons/greatbow.png")), "weapon", 10);
			addImage(loadImage(new File(FOLDER+"weapons/recurvebow.png")), "weapon", 11);
			
			addImage(loadImage(new File(FOLDER+"equipment/pants_m.png")), "equip", 0, 0);
			addImage(loadImage(new File(FOLDER+"equipment/pants_f.png")), "equip", 0, 1);
			addImage(loadImage(new File(FOLDER+"equipment/boots_m.png")), "equip", 1, 0);
			addImage(loadImage(new File(FOLDER+"equipment/boots_f.png")), "equip", 1, 1);
			addImage(loadImage(new File(FOLDER+"equipment/metalboots_m.png")), "equip", 2, 0);
			addImage(loadImage(new File(FOLDER+"equipment/metalboots_f.png")), "equip", 2, 1);
			addImage(loadImage(new File(FOLDER+"equipment/greaves_m.png")), "equip", 3, 0);
			addImage(loadImage(new File(FOLDER+"equipment/greaves_f.png")), "equip", 3, 1);
			addImage(loadImage(new File(FOLDER+"equipment/goldgreaves_m.png")), "equip", 4, 0);
			addImage(loadImage(new File(FOLDER+"equipment/goldgreaves_f.png")), "equip", 4, 1);
			addImage(loadImage(new File(FOLDER+"equipment/shirt_m.png")), "equip", 5, 0);
			addImage(loadImage(new File(FOLDER+"equipment/shirt_f.png")), "equip", 5, 1);
			addImage(loadImage(new File(FOLDER+"equipment/leather_m.png")), "equip", 6, 0);
			addImage(loadImage(new File(FOLDER+"equipment/leather_f.png")), "equip", 6, 1);
			addImage(loadImage(new File(FOLDER+"equipment/chain_m.png")), "equip", 7, 0);
			addImage(loadImage(new File(FOLDER+"equipment/chain_f.png")), "equip", 7, 1);
			addImage(loadImage(new File(FOLDER+"equipment/plate_m.png")), "equip", 8, 0);
			addImage(loadImage(new File(FOLDER+"equipment/plate_f.png")), "equip", 8, 1);
			addImage(loadImage(new File(FOLDER+"equipment/gold_m.png")), "equip", 9, 0);
			addImage(loadImage(new File(FOLDER+"equipment/gold_f.png")), "equip", 9, 1);
			addImage(loadImage(new File(FOLDER+"equipment/cap_m.png")), "equip", 10, 0);
			addImage(loadImage(new File(FOLDER+"equipment/cap_f.png")), "equip", 10, 1);
			addImage(loadImage(new File(FOLDER+"equipment/clothhood_m.png")), "equip", 11, 0);
			addImage(loadImage(new File(FOLDER+"equipment/clothhood_f.png")), "equip", 11, 1);
			addImage(loadImage(new File(FOLDER+"equipment/skullcap_m.png")), "equip", 12, 0);
			addImage(loadImage(new File(FOLDER+"equipment/skullcap_f.png")), "equip", 12, 1);
			addImage(loadImage(new File(FOLDER+"equipment/chainhood_m.png")), "equip", 13, 0);
			addImage(loadImage(new File(FOLDER+"equipment/chainhood_f.png")), "equip", 13, 1);
			addImage(loadImage(new File(FOLDER+"equipment/helm_m.png")), "equip", 14, 0);
			addImage(loadImage(new File(FOLDER+"equipment/helm_f.png")), "equip", 14, 1);
			addImage(loadImage(new File(FOLDER+"equipment/goldhelm_m.png")), "equip", 15, 0);
			addImage(loadImage(new File(FOLDER+"equipment/goldhelm_f.png")), "equip", 15, 1);
			
			addImage(loadImage(new File(FOLDER+"spells/defend.png")), "spell", 0);
			addImage(loadImage(new File(FOLDER+"spells/spike.png")), "spell", 1);
			addImage(loadImage(new File(FOLDER+"spells/earth.png")), "spell", 2);
			addImage(loadImage(new File(FOLDER+"spells/fire.png")), "spell", 3);
			addImage(loadImage(new File(FOLDER+"spells/water.png")), "spell", 4);
			addImage(loadImage(new File(FOLDER+"spells/wind.png")), "spell", 5);
			addImage(loadImage(new File(FOLDER+"spells/lightning.png")), "spell", 6);
			addImage(loadImage(new File(FOLDER+"spells/heal.png")), "spell", 7);
			
			addImage(loadImage(new File(FOLDER+"monsters/slime.png")), "monster", 0);
			addImage(loadImage(new File(FOLDER+"monsters/bee.png")), "monster", 1);
			addImage(loadImage(new File(FOLDER+"monsters/snake.png")), "monster", 2);
			addImage(loadImage(new File(FOLDER+"monsters/bat.png")), "monster", 3);
			addImage(loadImage(new File(FOLDER+"monsters/ghost.png")), "monster", 4);
			addImage(loadImage(new File(FOLDER+"monsters/man_eater_flower.png")), "monster", 5);
			addImage(loadImage(new File(FOLDER+"monsters/eyeball.png")), "monster", 6);
			addImage(loadImage(new File(FOLDER+"monsters/big_worm.png")), "monster", 7);
			
			addImage(loadImage(new File(FOLDER+"achievements.png")), "achievements");

			addSound(loadSound(new File(FOLDER+"bgm/menu.mp3")), "menu");
			addSound(loadSound(new File(FOLDER+"bgm/battle1.mp3")), "battle", 1);
			addSound(loadSound(new File(FOLDER+"bgm/battle2.mp3")), "battle", 2);
			addSound(loadSound(new File(FOLDER+"bgm/battle3.mp3")), "battle", 3);
			
			addSound(loadSound(new File(FOLDER+"sfx/sfx_achievement.mp3")), "achievement");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_change.mp3")), "change");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_click.mp3")), "click");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_select.mp3")), "select");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_close.mp3")), "close");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_hit.mp3")), "hit");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_evade.mp3")), "evade");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_death.mp3")), "death");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_Victory.mp3")), "victory");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_Defeat.mp3")), "defeat");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_point.mp3")), "point");
			
			addSound(loadSound(new File(FOLDER+"sfx/sfx_attack_sword.mp3")), "slash");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_attack_pole.mp3")), "thrust");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_attack_ranged.mp3")), "bow");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_attack_wand.mp3")), "wand");
			addSound(loadSound(new File(FOLDER+"sfx/sfx_attack_ranged-shot-only.mp3")), "bow_repeat");
			
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_defend.mp3")), "spell_sfx", 0);
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_spike.mp3")), "spell_sfx", 1);
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_earth.mp3")), "spell_sfx", 2);
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_fire.mp3")), "spell_sfx", 3);
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_water.mp3")), "spell_sfx", 4);
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_wind.mp3")), "spell_sfx", 5);
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_lightning.mp3")), "spell_sfx", 6);
			addSound(loadSound(new File(FOLDER+"sfx/sfx_spell_heal.mp3")), "spell_sfx", 7);
			
			WorldIO io = new WorldIO();
			p.getFirstMap().getWorld().write(io);
			ByteArrayOutputStream buf = io.getBuffer(); addData(buf, "map", 0);
			ArrayList<Long> images = new ArrayList<Long>(); DataOutputStream obuf = new DataOutputStream(buf); int j=0;
			for(Long l : io.getTilemaps()){
				io.resetBuffer(); TileResource tileset = (TileResource)p.getTilemapById(l);
				BasicTilemap t = (BasicTilemap)tileset.getTilemap(); long img = tileset.getImage().getId();
				int i = images.indexOf(img); if(i == -1){
					i = images.size(); images.add(img);
				} obuf.writeShort(i); t.write(obuf); addData(buf, "tileset", j++);
			}
			j = 0; for(Long l : images){
				addImage(p.getImageById(l).getGraphic(), "img", j++);
			}
			finish(); writeToFile(new File("out.swf"));
		} catch(Exception e){e.printStackTrace();}
	}
	public void run(Project p){
		try{
			Desktop.getDesktop().open(new File("out.swf"));
		}catch(Exception e){e.printStackTrace();}
	}
	public void addImage(Graphic b, String type) throws Exception {addImage(b, type, -1, -1);}
	public void addImage(Graphic b, String type, int t1) throws Exception {addImage(b, type, t1, -1);}
	public void addImage(Graphic b, String type, int t1, int t2) throws Exception {
		if(t1 >= 0) type += t1; if(t2 >= 0) type += "_"+t2; type = "assets."+type;
		movie.add(b.defineImage(id)); bitmaps.add(id, type); id++;
	}
	public void addSound(Sound s, String type) throws Exception {addSound(s, type, -1, -1);}
	public void addSound(Sound s, String type, int t1) throws Exception {addSound(s, type, t1, -1);}
	public void addSound(Sound s, String type, int t1, int t2) throws Exception {
		if(t1 >= 0) type += t1; if(t2 >= 0) type += "_"+t2; type = "assets."+type;
		movie.add(s.getSound(id)); sounds.add(id, type); id++;
	}
	public void addData(ByteArrayOutputStream ar, String type) throws Exception {addData(ar, type, -1, -1);}
	public void addData(ByteArrayOutputStream ar, String type, int t1) throws Exception {addData(ar, type, t1, -1);}
	public void addData(ByteArrayOutputStream ar, String type, int t1, int t2) throws Exception {
		if(t1 >= 0) type += t1; if(t2 >= 0) type += "_"+t2; type = "assets."+type;
		movie.add(new DefineData(id, ar.toByteArray())); bytearrays.add(id, type); id++;
	}
	private static int[] _bitmap = {16,0,46,0,0,0,0,12,1,122,6,97,115,115,101,116,115,6,66,105,116,109,97,112,13,102,108,97,115,104,46,100,105,115,112,108,97,121,24,97,115,115,101,116,115,58,122,58,58,97,115,115,101,116,115,58,122,36,99,105,110,105,116,10,97,115,115,101,116,115,46,122,47,122,0,6,79,98,106,101,99,116,15,69,118,101,110,116,68,105,115,112,97,116,99,104,101,114,12,102,108,97,115,104,46,101,118,101,110,116,115,13,68,105,115,112,108,97,121,79,98,106,101,99,116,5,22,2,22,4,22,7,22,10,1,6,7,1,1,7,2,3,7,3,8,7,4,9,7,2,11,3,0,0,5,0,0,0,6,0,0,0,7,0,0,1,1,2,1,0,1,0,0,0,1,2,1,1,4,0,0,3,0,1,1,6,7,3,-48,48,71,0,0,1,2,1,7,8,6,-48,48,-48,73,0,71,0,0,2,2,1,1,6,37,-48,48,101,0,93,3,102,3,48,93,4,102,4,48,93,5,102,5,48,93,2,102,2,48,93,2,102,2,88,0,29,29,29,29,104,1,71,0,0};
	private static int[] _sound = {16,0,46,0,0,0,0,11,1,122,6,97,115,115,101,116,115,5,83,111,117,110,100,11,102,108,97,115,104,46,109,101,100,105,97,24,97,115,115,101,116,115,58,122,58,58,97,115,115,101,116,115,58,122,36,99,105,110,105,116,10,97,115,115,101,116,115,46,122,47,122,0,6,79,98,106,101,99,116,15,69,118,101,110,116,68,105,115,112,97,116,99,104,101,114,12,102,108,97,115,104,46,101,118,101,110,116,115,5,22,2,22,4,22,7,22,10,1,5,7,1,1,7,2,3,7,3,8,7,4,9,3,0,0,5,0,0,0,6,0,0,0,7,0,0,1,1,2,1,0,1,0,0,0,1,2,1,1,4,0,0,3,0,1,1,5,6,3,-48,48,71,0,0,1,2,1,6,7,6,-48,48,-48,73,0,71,0,0,2,2,1,1,5,31,-48,48,101,0,93,3,102,3,48,93,4,102,4,48,93,2,102,2,48,93,2,102,2,88,0,29,29,29,104,1,71,0,0};
	private static int[] _data = {16,0,46,0,0,0,0,9,1,122,6,97,115,115,101,116,115,9,66,121,116,101,65,114,114,97,121,11,102,108,97,115,104,46,117,116,105,108,115,24,97,115,115,101,116,115,58,122,58,58,97,115,115,101,116,115,58,122,36,99,105,110,105,116,10,97,115,115,101,116,115,46,122,47,122,0,6,79,98,106,101,99,116,4,22,2,22,4,22,7,1,4,7,1,1,7,2,3,7,3,8,3,0,0,5,0,0,0,6,0,0,0,7,0,0,1,1,2,1,0,1,0,0,0,1,2,1,1,4,0,0,3,0,1,1,4,5,3,-48,48,71,0,0,1,2,1,5,6,6,-48,48,-48,73,0,71,0,0,2,2,1,1,4,25,-48,48,101,0,93,3,102,3,48,93,2,102,2,48,93,2,102,2,88,0,29,29,104,1,71,0,0};
	public void finish() throws Exception {
		for(String asset : bitmaps.getObjects().values()){
			asset = asset.substring(7); int l = asset.length(), j=0;
			byte[] data = new byte[_bitmap.length+(l-1)*5]; for(int i=0; i<_bitmap.length; i++){
				if(_bitmap[i] == 122){
					if(data[j-1] == 1) data[j-1] = (byte)l; for(int k=0; k<l; k++) data[j++] = (byte)asset.charAt(k);
				} else data[j++] = (byte)_bitmap[i];
			} data[38+(l-1)] += (l-1)*2; data[63+(l-1)*3] += (l-1)*2;
			movie.add(new MovieObject(72,data));
		} for(String asset : sounds.getObjects().values()){
			asset = asset.substring(7); int l = asset.length(), j=0;
			byte[] data = new byte[_sound.length+(l-1)*5]; for(int i=0; i<_sound.length; i++){
				if(_sound[i] == 122){
					if(data[j-1] == 1) data[j-1] = (byte)l; for(int k=0; k<l; k++) data[j++] = (byte)asset.charAt(k);
				} else data[j++] = (byte)_sound[i];
			} data[35+(l-1)] += (l-1)*2; data[60+(l-1)*3] += (l-1)*2;
			movie.add(new MovieObject(72,data));
		} for(String asset : bytearrays.getObjects().values()){
			asset = asset.substring(7); int l = asset.length(), j=0;
			byte[] data = new byte[_data.length+(l-1)*5]; for(int i=0; i<_data.length; i++){
				if(_data[i] == 122){
					if(data[j-1] == 1) data[j-1] = (byte)l; for(int k=0; k<l; k++) data[j++] = (byte)asset.charAt(k);
				} else data[j++] = (byte)_data[i];
			} data[39+(l-1)] += (l-1)*2; data[64+(l-1)*3] += (l-1)*2;
			movie.add(new MovieObject(72,data));
		} movie.add(bitmaps); movie.add(sounds); movie.add(bytearrays); movie.add(ShowFrame.getInstance());
	}
	public void writeToFile(File f) throws Exception {movie.encodeToFile(f);}
	public void printTags(){
		for(MovieTag tag : movie.getObjects()) System.out.println(tag);
	}
	public static Graphic loadImage(File file) throws Exception {
		String n = file.getName(); File f = new File("img/"+n.substring(0, n.length()-4)+".mimg");
		Graphic i; if(f.exists()) i = new Graphic(f); else{i = Graphic.decode(file); i.write(f);} return i;
	}
	public static Sound loadSound(File file) throws Exception {
		String n = file.getName(); File f = new File("snd/"+n.substring(0, n.length()-4)+".msnd");
		Sound s; if(f.exists()) s = new Sound(f); else{s = Sound.decode(file); s.write(f);} return s;
	}
}
