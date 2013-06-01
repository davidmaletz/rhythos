package mrpg.script;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import mrpg.editor.MapEditor;
import mrpg.editor.resource.Script;

public class ScriptEditor extends JFrame implements ScriptTextPane.ModifiedListener, ActionListener {
	private static final long serialVersionUID = 2350041509426603391L;
	private ScriptTextPane textPane; private Script script; private static ScriptEditor instance; private JMenuItem save;
	private ScriptEditor(){
		super("Script Editor");
		textPane = new ScriptTextPane(700,500); setContentPane(textPane);
		textPane.initSearchDialogs(this); JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File"); save = MapEditor.createMenuItemIcon("Save", MapEditor.SAVE, KeyEvent.VK_S, ActionEvent.CTRL_MASK, this);
		menu.add(save); bar.add(menu); bar.add(textPane.createViewMenu()); bar.add(textPane.createSearchMenu()); setJMenuBar(bar);
		setDefaultCloseOperation(HIDE_ON_CLOSE); pack();
	}
	public void setModified(boolean modified){
		if(script != null){script.setModified(modified); save.setEnabled(modified); MapEditor.instance.updateSaveButtons();}
	}
	public void actionPerformed(ActionEvent e) {
		if(script != null && script.isModified()) try{script.save();}catch(Exception ex){}
	}
	public void setScript(Script s){
		if(script != null) script.setScript(textPane.getText());
		script = s; textPane.loadDocument(script.getScript(), script.isModified(), this); save.setEnabled(script.isModified());
	}
	public static void show(Script script){
		if(instance == null) instance = new ScriptEditor(); instance.setScript(script); instance.setVisible(true);
	}
	public static void onSave(Script script){
		if(instance != null && script == instance.script) instance.textPane.onSave();
	}
	public static void loadScript(Script script){
		if(instance != null && script == instance.script) script.setScript(instance.textPane.getText());
	}
	public static void update(Script script){
		if(instance != null && script == instance.script){
			instance.textPane.loadDocument(script.getScript(), script.isModified(), instance); instance.save.setEnabled(script.isModified());
		}
	}
	public static void init(){
		((AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance()).putMapping(ScriptTextPane.SYNTAX_STYLE_HAXE, "org.fife.ui.rsyntaxtextarea.modes.HaxeTokenMaker");
		FoldParserManager.get().addFoldParserMapping(ScriptTextPane.SYNTAX_STYLE_HAXE, new CurlyFoldParser());
	}
	public static void destroy(){if(instance != null) instance.dispose();}
}
