package mrpg.editor;

import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

public class BasicMenuItem implements MenuItem {
	public String text, icon; public int keyCode, modifiers; public ActionListener listener;
	public BasicMenuItem(String t, String i, int k, int m, ActionListener l){
		text = t; icon = i; keyCode = k; modifiers = m; listener = l;
	}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		if(listener == null) listener = MapEditor.instance.getBrowser();
		if(allowAccelerator) return MapEditor.createMenuItemIcon(text, icon, keyCode, modifiers, listener);
		else return MapEditor.createMenuItemIcon(text, icon, listener);
	}
	public JComponent getToolbarItem(){
		if(listener == null) listener = MapEditor.instance.getBrowser();
		return MapEditor.createToolbarButton(icon, text, listener);
	}
}
