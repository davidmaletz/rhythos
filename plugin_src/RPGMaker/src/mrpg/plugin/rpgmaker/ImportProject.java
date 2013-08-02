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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import mrpg.editor.MapEditor;
import mrpg.editor.MenuItem;
import mrpg.editor.resource.AutoTile;
import mrpg.editor.resource.AutotileFormat;
import mrpg.editor.resource.CroppedImage;
import mrpg.editor.resource.Folder;
import mrpg.editor.resource.Image;
import mrpg.editor.resource.Map;
import mrpg.editor.resource.Project;
import mrpg.editor.resource.Resource;
import mrpg.editor.resource.TileResource;
import mrpg.editor.resource.Tileset;
import mrpg.export.Graphic;
import mrpg.plugin.Plugin;
import mrpg.world.AutoTileFormat;
import mrpg.world.BasicTilemap;
import mrpg.world.Cell;
import mrpg.world.Direction;
import mrpg.world.World;

public class ImportProject implements Plugin, ActionListener {
	private MenuItem item;
	public void install(boolean init) throws Exception {
		item = MapEditor.menu_bar.addMenu("Tools", null).addItem("Import RPGMaker Project", null, this);
		MapEditor.instance.updateMenuBar();
	}
	public void uninstall() throws Exception {
		if(item != null){
			MapEditor.menu_bar.addMenu("Tools", null).remove(item);
			MapEditor.instance.updateMenuBar();
		}
	}
	public void actionPerformed(ActionEvent e){
		Import((Component)e.getSource(), new File("C:/Games/FallingStars"), new File("C:/Games/Rhythos/FallingStars"));
	}
	public static File getAvailable(File f, String name, String ext){
		File file = new File(f, name+ext); int i=2; while(file.exists()){file = new File(f, name+ext); i++;} return file;
	}
	private static AutoTileFormat getAutoTileFormat() throws Exception {
		byte fmt[] = new byte[256*8]; for(int i=0; i<256; i++){
			int x1, y1, x2, y2, x3, y3, x4, y4;
			boolean left = Direction.left(i), right = Direction.right(i), up = Direction.up(i), down = Direction.down(i),
			ul = Direction.upper_left(i), ur = Direction.upper_right(i), ll = Direction.lower_left(i), lr = Direction.lower_right(i);
			if(!left && !up){x1 = 0; y1 = (right || down)?2:0;}
			else if(!up){x1 = (right)?2:4; y1 = 2;}
			else if(!left){x1 = 0; y1 = (down)?4:6;}
			else if(ul){x1 = (right)?2:4; y1 = (down)?4:6;}
			else {x1 = 4; y1 = 0;}
			
			if(!right && !up){x2 = (left || down)?5:1; y2 = (left || down)?2:0;}
			else if(!up){x2 = (left)?3:1; y2 = 2;}
			else if(!right){x2 = 5; y2 = (down)?4:6;}
			else if(ur){x2 = (left)?3:1; y2 = (down)?4:6;}
			else {x2 = 5; y2 = 0;}
			
			if(!left && !down){x3 = 0; y3 = (right || up)?7:1;}
			else if(!down){x3 = (right)?2:4; y3 = 7;}
			else if(!left){x3 = 0; y3 = (up)?5:3;}
			else if(ll){x3 = (right)?2:4; y3 = (up)?5:3;}
			else {x3 = 4; y3 = 1;}
			
			if(!right && !down){x4 = (left || up)?5:1; y4 = (left || up)?7:1;}
			else if(!down){x4 = (left)?3:1; y4 = 7;}
			else if(!right){x4 = 5; y4 = (up)?5:3;}
			else if(lr){x4 = (left)?3:1; y4 = (up)?5:3;}
			else {x4 = 5; y4 = 1;}
			int j = i*8; fmt[j++] = (byte)x1; fmt[j++] = (byte)y1; fmt[j++] = (byte)x2; fmt[j++] = (byte)y2;
			fmt[j++] = (byte)x3; fmt[j++] = (byte)y3; fmt[j++] = (byte)x4; fmt[j++] = (byte)y4;
		} return new AutoTileFormat(fmt,3,4);
	}
	private static AutoTileFormat getWaterFormat() throws Exception {
		byte fmt[] = new byte[256*8]; for(int i=0; i<256; i++){
			int x1, y1, x2, y2, x3, y3, x4, y4;
			boolean left = Direction.left(i), right = Direction.right(i), up = Direction.up(i), down = Direction.down(i),
			ul = Direction.upper_left(i), ur = Direction.upper_right(i), ll = Direction.lower_left(i), lr = Direction.lower_right(i);
			if(!left && !up){x1 = 0; y1 = 0;}
			else if(!up){x1 = 0; y1 = 4;}
			else if(!left){x1 = 0; y1 = 2;}
			else if(ul){x1 = 0; y1 = 8;}
			else {x1 = 0; y1 = 6;}
			
			if(!right && !up){x2 = 1; y2 = 0;}
			else if(!up){x2 = 1; y2 = 4;}
			else if(!right){x2 = 1; y2 = 2;}
			else if(ur){x2 = 1; y2 = 8;}
			else {x2 = 1; y2 = 6;}
			
			if(!left && !down){x3 = 0; y3 = 1;}
			else if(!down){x3 = 0; y3 = 5;}
			else if(!left){x3 = 0; y3 = 3;}
			else if(ll){x3 = 0; y3 = 9;}
			else {x3 = 0; y3 = 7;}
			
			if(!right && !down){x4 = 1; y4 = 1;}
			else if(!down){x4 = 1; y4 = 5;}
			else if(!right){x4 = 1; y4 = 3;}
			else if(lr){x4 = 1; y4 = 9;}
			else {x4 = 1; y4 = 7;}
			int j = i*8; fmt[j++] = (byte)x1; fmt[j++] = (byte)y1; fmt[j++] = (byte)x2; fmt[j++] = (byte)y2;
			fmt[j++] = (byte)x3; fmt[j++] = (byte)y3; fmt[j++] = (byte)x4; fmt[j++] = (byte)y4;
		} return new AutoTileFormat(fmt,3,8);
	}
	private static AutoTileFormat getDeepWaterFormat() throws Exception {
		byte fmt[] = new byte[256*8]; for(int i=0; i<256; i++){
			int x1, y1, x2, y2, x3, y3, x4, y4;
			boolean left = Direction.left(i), right = Direction.right(i), up = Direction.up(i), down = Direction.down(i);
			if(!left && !up){x1 = 0; y1 = 12;}
			else {x1 = 0; y1 = 14;}
			
			if(!right && !up){x2 = 1; y2 = 12;}
			else {x2 = 1; y2 = 14;}
			
			if(!left && !down){x3 = 0; y3 = 13;}
			else {x3 = 0; y3 = 15;}
			
			if(!right && !down){x4 = 1; y4 = 13;}
			else {x4 = 1; y4 = 15;}
			int j = i*8; fmt[j++] = (byte)x1; fmt[j++] = (byte)y1; fmt[j++] = (byte)x2; fmt[j++] = (byte)y2;
			fmt[j++] = (byte)x3; fmt[j++] = (byte)y3; fmt[j++] = (byte)x4; fmt[j++] = (byte)y4;
		} return new AutoTileFormat(fmt,3,8);
	}
	public static void Import(Component c, File from, File to){
		LDBReader db = null; LMTReader tree = null; ArrayList<LMUReader> maps = new ArrayList<LMUReader>();
		try{
			File ldb = new File(from, "RPG_RT.ldb"); if(!ldb.exists()) JOptionPane.showMessageDialog(c, "\""+from.getAbsolutePath()+"\" is not an RPGMaker project: Missing RPG_RT.ldb.", "Unable to Import!", JOptionPane.ERROR_MESSAGE);
			File lmt = new File(from, "RPG_RT.lmt"); if(!lmt.exists()) JOptionPane.showMessageDialog(c, "\""+from.getAbsolutePath()+"\" is not an RPGMaker project: Missing RPG_RT.lmt.", "Unable to Import!", JOptionPane.ERROR_MESSAGE);
			db = new LDBReader(ldb); tree = new LMTReader(lmt);
			for(int i=0; i<tree.maps.size(); i++){
				LMTReader.Map m = tree.maps.get(i); if(m == null) continue;
				try{
					LMUReader lmu = new LMUReader(new File(from, "Map"+String.format("%04d", i)+".lmu"));
					lmu.chipset = db.chipsets.get(lmu.chipset_id); if(lmu.chipset != null){
						lmu.map = m; maps.add(lmu);
					}
				}catch(Exception e){}
			}
		}catch(Exception e){
			JOptionPane.showMessageDialog(c, "Could not read RPGMaker project at \""+from.getAbsolutePath()+"\".", "Unable to Import!", JOptionPane.ERROR_MESSAGE);
			return;
		} try{
			if(to.exists() || !to.mkdirs()) throw new Exception(); MapEditor e = MapEditor.instance; 
			Project p = new Project(to, e); File code = new File("project/com"), project = p.getFile();
			if(code.exists()){
				Resource.copyDir(code, new File(project, "com")); p.refresh();
			} p.init(16);
			File f = new File(project, "images"); Resource images = Folder.create(f, e); p.add(images);
			f = new File(images.getFile(), "chipsets"); Resource chipsets = Folder.create(f, e); images.add(chipsets);
			f = new File(images.getFile(), "cropped"); Resource cropped = Folder.create(f, e); images.add(cropped);
			f = new File(project, "tilesets"); Resource tilesets = Folder.create(f, e); p.add(tilesets);
			f = new File(project, "autotiles"); Resource autotiles = Folder.create(f, e); p.add(autotiles);
			f = new File(project, "maps"); Resource map = Folder.create(f, e); p.add(map);
			AutotileFormat autotile = AutotileFormat.create(p, e, p, "RM2KAuto", getAutoTileFormat());
			AutotileFormat water = AutotileFormat.create(p, e, p, "RM2KWater", getWaterFormat());
			AutotileFormat deepwater = AutotileFormat.create(p, e, p, "RM2KDeepWater", getDeepWaterFormat());
			for(LDBReader.Chipset chipset : db.chipsets){
				if(chipset == null) continue;
				File file = getAvailable(chipsets.getFile(), chipset.name, "."+Image.EXT);
				chipset._image = Image.createImage(new Graphic(chipset.chipset), file, e, p); String name = chipset._image.getName();
				CroppedImage crop = CroppedImage.create(p, chipset._image, cropped, name, 4*16*3, 0, 18*16, 16*16);
				chipsets.add(chipset._image);
				TileResource t = Tileset.createTileset(tilesets, e, p, name, crop);
				chipset.tileset = t; //TODO: Water animations
				int j = 1; crop = CroppedImage.create(p, chipset._image, cropped, name+"_"+j, 0, 0, 16*3, 16*8);
				t = AutoTile.createAutoTile(autotiles, e, p, water, name+"_"+j, crop); chipset.autotiles.add(t); j++;
				chipset.autotiles.add(null); j++; //TODO: water type 2
				t = AutoTile.createAutoTile(autotiles, e, p, deepwater, name+"_"+j, crop); chipset.autotiles.add(t); j++;
				//TODO: animated tiles.
				chipset.autotiles.add(null); j++; chipset.autotiles.add(null); j++; chipset.autotiles.add(null); j++;
				for(int y=2; y<4; y++) for(int x=0; x<2; x++){
					crop = CroppedImage.create(p, chipset._image, cropped, name+"_"+j, x*16*3, y*16*4, 16*3, 16*4);
					t = AutoTile.createAutoTile(autotiles, e, p, autotile, name+"_"+j, crop);
					chipset.autotiles.add(t); j++;
				} for(int y=0; y<4; y++) for(int x=2; x<4; x++){
					crop = CroppedImage.create(p, chipset._image, cropped, name+"_"+j, x*16*3, y*16*4, 16*3, 16*4);
					t = AutoTile.createAutoTile(autotiles, e, p, autotile, name+"_"+j, crop);
					chipset.autotiles.add(t); j++;
				}
			} int sz = maps.size(), i=0; for(LMUReader lmu : maps){
				World w = new World(lmu.width, lmu.height); BasicTilemap tilemap = ((BasicTilemap)lmu.chipset.tileset.getTilemap());
				for(int y=0; y<lmu.height; y++){
					for(int x=0; x<lmu.width; x++){
						Cell cell = w.addCell(x, y);
						int idx = (y*lmu.width+x)*2; int dx = lmu.lower.get(idx); int dy = lmu.lower.get(idx+1);
						if(dx >= 12 && dy >= 0) cell.setTile(tilemap.getTile(dx-12, dy), 0, false);
						else if(dx == -1 && dy >= 0){
							TileResource auto = lmu.chipset.autotiles.get(dy);
							if(auto != null) cell.setTile(auto.getTilemap().getTile(0), 0, false);
						} dx = lmu.upper.get(idx); dy = lmu.upper.get(idx+1);
						if(dx >= 12 && dy >= 0) cell.setTile(tilemap.getTile(dx-12, dy), 1, false);
					}
				} w.updateNeighbors(); Resource parent; String n = MapEditor.safeName(lmu.map.name);
				if(lmu.map.parentMap == null) parent = map;
				else{
					if(lmu.map.parentMap.resource != null) parent = lmu.map.parentMap.resource.getParent();
					else parent = map;
				} if(lmu.map.children.size() > 0){
					File file = getAvailable(parent.getFile(), n, "");
					Resource folder = Folder.create(file, e); parent.add(folder); parent = folder;
				} File file = getAvailable(parent.getFile(), n, "."+Map.EXT); n = file.getName(); System.out.println(n+" "+i+"/"+sz); i++;
				Map m = Map.createMap(parent, e, p, n.substring(0, n.length()-Map.EXT.length()-1), w); lmu.map.resource = m;
			} MapEditor.instance.getBrowser().addProject(p);
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not create a new project at \""+to.getAbsolutePath()+"\".", "Unable to Import!", JOptionPane.ERROR_MESSAGE);
		}
	}
}
