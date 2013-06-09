package mrpg.editor;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

public class Menu implements MenuItem {
	public String text, icon; public final ArrayList<MenuItem> children;
	public Menu(){children = new ArrayList<MenuItem>();}
	public Menu(String title, String icon){text = title; this.icon = icon; children = new ArrayList<MenuItem>();}
	public boolean isEmpty(){return children == null || children.isEmpty();}
	public Menu getMenu(String title){
		for(MenuItem o : children){if(o != null && o instanceof Menu && ((Menu)o).text.equals(title)) return (Menu)o;}
		return null;
	}
	public Menu addMenu(String title, String icon){
		Menu ret = getMenu(title);
		if(ret == null){ret = new Menu(title, icon); children.add(ret);}
		return ret;
	}
	public void addItem(String text, String icon, ActionListener listener){addItem(text, icon, 0, 0, listener);}
	public void addItem(String text, String icon, int keyCode, int modifiers, ActionListener listener){
		children.add(new BasicMenuItem(text, icon, keyCode, modifiers, listener));
	}
	public void addItem(MenuItem i){children.add(i);}
	public void addSeparator(){children.add(null);}
	public JMenuItem getMenuItem(){return getMenuItem(true);}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		JMenu inner = new JMenu(text); if(icon != null) inner.setIcon(MapEditor.getIcon(icon));
		for(MenuItem o : children){
			if(o == null) inner.addSeparator();
			else inner.add(o.getMenuItem(allowAccelerator));
		} return inner;
	}
	public JMenuBar getMenuBar(){return getMenuBar(true);}
	public JMenuBar getMenuBar(boolean allowAccelerator){
		JMenuBar bar = new JMenuBar();
		for(MenuItem o : children){
			if(o != null) bar.add(o.getMenuItem(allowAccelerator));
		} return bar;
	}
	public JComponent getToolbarItem(){return getMenuItem(false);}
	public JToolBar getToolbar(){
		JToolBar bar = new JToolBar(); bar.setFloatable(false); bar.setRollover(false);
		for(MenuItem o : children){
			if(o == null) bar.addSeparator();
			else bar.add(o.getToolbarItem());
		} return bar;
	}
}
