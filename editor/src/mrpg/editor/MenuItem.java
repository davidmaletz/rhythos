package mrpg.editor;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

public interface MenuItem {
	public JMenuItem getMenuItem(boolean allowAccelerator);
	public JComponent getToolbarItem();
}
