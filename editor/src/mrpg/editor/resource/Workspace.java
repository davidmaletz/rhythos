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

import javax.swing.Icon;

import mrpg.editor.MapEditor;
import mrpg.editor.resource.Project;


public class Workspace extends Resource {
	private static final long serialVersionUID = -781096720788373713L;
	public Workspace(MapEditor e){super(null, e);}
	
	public boolean canAddChildren(){return false;}
	public long getId(){return 0;}
	public Icon getIcon(){return null;}
	public boolean canDelete(){return false;}
	protected void read(File f) throws Exception {throw new Exception();}
	
	public int getProjectCount(){return getChildCount();}
	public Project getProject(int i){return (Project)getChild(i);}
	public File copy(File f, Project p) throws Exception {throw new Exception();}
	public String getExt(){return null;}
}
