package mrpg.script;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

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
	public static void init(){
		((AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance()).putMapping(ScriptTextPane.SYNTAX_STYLE_HAXE, "org.fife.ui.rsyntaxtextarea.modes.HaxeTokenMaker");
		FoldParserManager.get().addFoldParserMapping(ScriptTextPane.SYNTAX_STYLE_HAXE, new CurlyFoldParser());
	}
	public static void destroy(){if(instance != null) instance.dispose();}
}
