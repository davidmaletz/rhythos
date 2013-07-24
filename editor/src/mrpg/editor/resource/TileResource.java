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

import java.io.File;

import mrpg.editor.MapEditor;
import mrpg.editor.WorkspaceBrowser;
import mrpg.world.Tilemap;

public abstract class TileResource extends Resource {
	private static final long serialVersionUID = 6072902526037159097L;
	protected TileResource(File f, MapEditor e){super(f,e);}
	public abstract ImageResource getImage();
	public abstract Tilemap getTilemap();
	public boolean isCompatible(Project p){return p.tile_size == WorkspaceBrowser.getProject(this).tile_size;}
}
