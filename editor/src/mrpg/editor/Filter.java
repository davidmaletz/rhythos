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
