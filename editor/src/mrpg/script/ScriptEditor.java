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
import mrpg.editor.WorkspaceBrowser;
import mrpg.editor.resource.Project;
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
		if(script != null && script.isModified()) try{script.save(); MapEditor.instance.updateSaveButtons();}catch(Exception ex){}
	}
	public void setScript(Script s){
		if(script != null) script.setScript(textPane.getText());
		script = s; textPane.loadDocument(script.getScript(), script.isModified(), this); save.setEnabled(script.isModified());
	}
	public static void show(Script script){
		if(instance == null) instance = new ScriptEditor();
		instance.textPane.setParser(new HaxeParser(WorkspaceBrowser.getProject(script), script));
		instance.setScript(script); instance.setVisible(true);
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
	public static void compiled(Project p){
		if(instance != null && WorkspaceBrowser.getProject(instance.script) == p){
			instance.textPane.reparse();
		}
	}
	public static void init(){
		((AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance()).putMapping(ScriptTextPane.SYNTAX_STYLE_HAXE, "org.fife.ui.rsyntaxtextarea.modes.HaxeTokenMaker");
		FoldParserManager.get().addFoldParserMapping(ScriptTextPane.SYNTAX_STYLE_HAXE, new CurlyFoldParser());
	}
	public static void destroy(){if(instance != null) instance.dispose(); HaxeCompiler.destroy();}
}
