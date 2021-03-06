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
package mrpg.editor;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import mrpg.editor.resource.Resource;

public class FilterTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = -9003054607707078379L;
	private final Filter filter;
	public FilterTreeModel(TreeNode root, Filter f){
		super(root); filter = f;
	}
	public Object getChild(Object parent, int index){
		int ct = 0, end = super.getChildCount(parent); for(int i=0; i<end; i++){
			Resource r = (Resource)super.getChild(parent, i);
			if(filter.filter(r)){
				if(ct == index) return r; ct++;
			}
		} throw new ArrayIndexOutOfBoundsException();
	}
	public int getChildCount(Object parent){
		int ct = 0, end = super.getChildCount(parent); for(int i=0; i<end; i++){
			Resource r = (Resource)super.getChild(parent, i);
			if(filter.filter(r)) ct++;
		} return ct;
	}
}
