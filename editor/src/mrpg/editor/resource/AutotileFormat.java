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
package mrpg.editor.resource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JPanel;

import mrpg.editor.Filter;
import mrpg.editor.MapEditor;
import mrpg.editor.ResourceChooser;
import mrpg.world.AutoTileFormat;

public class AutotileFormat extends TypedResource {
	private static final long serialVersionUID = -3381982539985690245L;
	public static final String EXT = "afmt", TYPE = "af"; private static final short VERSION=1;
	private AutoTileFormat format;
	public AutotileFormat(File f, MapEditor editor){super(f, editor);}
	public AutoTileFormat getFormat(){return format;}
	public Icon getIcon(){return TileResource.icon;}
	public void writeInner(DataOutputStream out) throws Exception {format.write(out);}
	public void readInner(DataInputStream in) throws Exception {format = new AutoTileFormat(in);}
	public String getType(){return TYPE;}
	public short getVersion(){return VERSION;}
	public TypedResource.Properties getProperties(){return new Properties(this, "Auto Tile Format Properties");}
	protected void read(File f) throws Exception {MapEditor.deferRead(this, MapEditor.DEF_MEDIA);}
	public String getExt(){return EXT;}
	public static void register() throws Exception {
		Resource.register("Auto Tile Format Files", AutotileFormat.EXT, AutotileFormat.TYPE, AutotileFormat.class);
	}
	
	public static AutotileFormat choose(Resource root, Resource selected){
		ResourceChooser c = new ResourceChooser(root, selected, FILTER);
		c.setVisible(true); return (AutotileFormat)c.getSelectedResource();
	}
	private static class FFilter extends JPanel implements Filter {
		private static final long serialVersionUID = 907354882348925575L;
		public FFilter(){}
		public boolean filter(Resource r){String ext = r.getExt(); return ext == null || ext == EXT;}
		public JPanel getPreview(){return this;}
		public boolean showPreview(Resource r){
			return r.getExt() != null;
		}
	} public static final Filter FILTER = new FFilter();
}
