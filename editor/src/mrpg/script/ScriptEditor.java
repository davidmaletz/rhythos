package mrpg.script;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import mrpg.editor.resource.Script;

public class ScriptEditor extends JFrame {
	private static final long serialVersionUID = 2350041509426603391L;
	private ScriptTextPane textPane; private static ScriptEditor instance;
	private ScriptEditor(){
		super("Script Editor");
		textPane = new ScriptTextPane(700,500); setContentPane(textPane);
		textPane.initSearchDialogs(this); JMenuBar bar = new JMenuBar();
		bar.add(textPane.createViewMenu()); bar.add(textPane.createSearchMenu()); setJMenuBar(bar);
		setDefaultCloseOperation(HIDE_ON_CLOSE); pack();
	}
	public static void show(Script script){
		if(instance == null) instance = new ScriptEditor(); instance.setVisible(true); //TODO: set script text/save/load
	}
}
