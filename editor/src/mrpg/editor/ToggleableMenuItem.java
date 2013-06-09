package mrpg.editor;

import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

public class ToggleableMenuItem extends BasicMenuItem {
	private JMenuItem item; private JComponent toolbar; private boolean enabled;
	public ToggleableMenuItem(String t, String i, ActionListener l){this(t,i,0,0,l);}
	public ToggleableMenuItem(String t, String i, int k, int m, ActionListener l){super(t, i, k, m, l); enabled = true;}
	public ToggleableMenuItem(String t, String i, ActionListener l, boolean enabled){this(t,i,0,0,l,enabled);}
	public ToggleableMenuItem(String t, String i, int k, int m, ActionListener l, boolean enabled){
		super(t, i, k, m, l); this.enabled = enabled; 
	}
	public JMenuItem getMenuItem(boolean allowAccelerator){
		if(item == null){
			item = super.getMenuItem(allowAccelerator); item.setEnabled(enabled);
		} return item;
	}
	public JComponent getToolbarItem(){
		if(toolbar == null){
			toolbar = super.getToolbarItem(); toolbar.setEnabled(enabled);
		} return toolbar;
	}
	public void setEnabled(boolean e){
		enabled = e; if(item != null) item.setEnabled(e); if(toolbar != null) toolbar.setEnabled(e);
	}
	public boolean isEnabled(){return enabled;}
}
