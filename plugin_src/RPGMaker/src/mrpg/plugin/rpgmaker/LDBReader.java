/*******************************************************************************
 * RPGMaker is a plugin which imports RPGMaker 2000 or 2003 maps into Rhythos.
 * Use at your own risk, this plugin is not feature-complete, and requires some
 * fixing up of maps and project structure after import.
 * 
 * The RPGMaker import code was inspired by EasyRPG
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
package mrpg.plugin.rpgmaker;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import mrpg.editor.MapEditor;
import mrpg.editor.resource.Image;
import mrpg.editor.resource.TileResource;


public class LDBReader implements LCFInputStream.Handler {
	//These are found in include/ldb_chunks.h
	private static final int NAME=0x01, CHIPSET_NAME=0x02, PASS_LOW=0x04, PASS_HIGH=0x05, CHIPSETS=0x14/*,
		TODO: read chipset DOWN = 0x01, LEFT = 0x02, RIGHT = 0x04, UP = 0x08, ABOVE = 0x10, WALL = 0x020, COUNTER = 0x40*/;
	private boolean readChipsets; public final ArrayList<Chipset> chipsets = new ArrayList<Chipset>(); private Chipset cur;
	private File file;
	public LDBReader(File f) throws Exception {
		file = f.getParentFile(); LCFInputStream in = new LCFInputStream(new BufferedInputStream(new FileInputStream(f)));
		if(!in.readString(in.readInt()).equals("LcfDataBase")) throw new Exception();
		try{readChipsets = false; in.readChunks(this);}catch(Exception e){} in.close();
	}
	public void readChipsets(LCFInputStream in) throws Exception {
		readChipsets = true; int count = in.readInt(); for(int i=0; i<count; i++){
			int id = in.readInt(); cur = new Chipset(); in.readChunks(this);
			if(cur.validate()){for(int j=chipsets.size(); j<=id; j++) chipsets.add(null); chipsets.set(id, cur);}
		} readChipsets = false;
	}
	public boolean handle(LCFInputStream in, int id, int len) throws Exception {
		if(readChipsets){
			switch(id){
			case NAME: cur.name = in.readString(len); return true;
			case CHIPSET_NAME:
				String c = in.readString(len); File f = new File(file, "ChipSet");
				File file = new File(f, c+".png"); if(file.exists()) cur.image = file;
				file = new File(f, c+".bmp"); if(file.exists()) cur.image = file;
			return true;
			case PASS_LOW: return false;
			case PASS_HIGH: return false;
			default: return false;
			}
		} else {if(id == CHIPSETS){readChipsets(in); return true;} else return false;}
	}
	public static class Chipset {
		public String name;
		private File image;
		public BufferedImage chipset;
		public Image _image;
		public final ArrayList<TileResource> autotiles = new ArrayList<TileResource>();
		public TileResource tileset;
		private boolean validate(){
			if(image == null){return false;}
			if(name == null){name = image.getName(); int i = name.lastIndexOf('.'); if(i != -1) name = name.substring(0, i);}
			name = MapEditor.safeName(name);
			if(chipset == null) try{
				chipset = ImageIO.read(image); chipset = transparentColor(chipset, chipset.getRGB(16*18+1, 16*8+1));
			}catch(Exception e){return false;}
			return true;
		}
	}
	private static BufferedImage transparentColor(BufferedImage i, int c){
		int w = i.getWidth(), h = i.getHeight(); BufferedImage ret = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		for(int y=0; y<h; y++)
			for(int x=0; x<w; x++){
				int p = i.getRGB(x, y);
				if(p == c) ret.setRGB(x, y, 0); else ret.setRGB(x, y, p);
			}
		return ret;
	}
}