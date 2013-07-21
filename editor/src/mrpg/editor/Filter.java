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

import javax.swing.JPanel;

import mrpg.editor.resource.Resource;

public interface Filter {
	/**
	 * Determines whether the resource should be included in this filter.
	 * @param r The resource in question.
	 * @return True if the resource should be included, otherwise false.
	 */
	public boolean filter(Resource r);
	/**
	 * Gets the preview panel for all resources in this filter.
	 * @return A JPanel with the proper size and components to display a preview for all resources in this filter.
	 */
	public JPanel getPreview();
	/**
	 * Shows a preview of a selected resource.
	 * @param r The selected resource to show a preview of.
	 * @return True if the resource can be chosen, otherwise false (for example, if you can select but not choose a folder.
	 */
	public boolean showPreview(Resource r);
}
