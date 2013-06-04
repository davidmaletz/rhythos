package mrpg.script;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;

import mrpg.editor.MapEditor;

import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchDialogSearchContext;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;
import org.fife.ui.rtextarea.SearchEngine;

public class ScriptTextPane extends RTextScrollPane implements HyperlinkListener, ActionListener {
	private static final long serialVersionUID = 9050172784894489328L;
	public static final String SYNTAX_STYLE_HAXE = "text/haxe";
	private MySyntaxTextArea textArea;
	
	public ScriptTextPane(int w, int h){
		super(createTextArea(), true); textArea = (MySyntaxTextArea)getTextArea();
		textArea.addHyperlinkListener(this); textArea.addParser(new HaxeParser());
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider); ac.install(textArea);
		Gutter gutter = getGutter();
		gutter.setBookmarkingEnabled(true);
		gutter.setBookmarkIcon(MapEditor.getIcon("bookmark"));
		setPreferredSize(new Dimension(w,h));
	}
	public void setText(String text) {
		textArea.setText(text); textArea.setCaretPosition(0); textArea.discardAllEdits();
	}
	private static RSyntaxTextArea createTextArea() {
		RSyntaxTextArea textArea = new MySyntaxTextArea();
		textArea.setTabSize(4);
		textArea.setCaretPosition(0);
		textArea.requestFocusInWindow();
		textArea.setMarkOccurrences(false);
		textArea.setCodeFoldingEnabled(true);
		textArea.setClearWhitespaceLinesEnabled(false);
		textArea.setSyntaxEditingStyle(SYNTAX_STYLE_HAXE);
		//textArea.setWhitespaceVisible(true);
		//textArea.setPaintMatchedBracketPair(true);
		return textArea;
	}
	private CompletionProvider createCompletionProvider() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "showMessage", "Shows a message dialog with the passed formatted string."));
		provider.addCompletion(new BasicCompletion(provider, "showChoice", "Shows a choice selection dialog with the passed options."));

		/*TODO: add auto-complete options.
		provider.addCompletion(new BasicCompletion(provider, "abstract"));
		provider.addCompletion(new BasicCompletion(provider, "assert"));
		provider.addCompletion(new BasicCompletion(provider, "break"));
		provider.addCompletion(new BasicCompletion(provider, "case"));
		provider.addCompletion(new BasicCompletion(provider, "catch"));
		provider.addCompletion(new BasicCompletion(provider, "class"));
		provider.addCompletion(new BasicCompletion(provider, "const"));
		provider.addCompletion(new BasicCompletion(provider, "continue"));
		provider.addCompletion(new BasicCompletion(provider, "default"));
		provider.addCompletion(new BasicCompletion(provider, "do"));
		provider.addCompletion(new BasicCompletion(provider, "else"));
		provider.addCompletion(new BasicCompletion(provider, "enum"));
		provider.addCompletion(new BasicCompletion(provider, "extends"));
		provider.addCompletion(new BasicCompletion(provider, "final"));
		provider.addCompletion(new BasicCompletion(provider, "finally"));
		provider.addCompletion(new BasicCompletion(provider, "for"));
		provider.addCompletion(new BasicCompletion(provider, "goto"));
		provider.addCompletion(new BasicCompletion(provider, "if"));
		provider.addCompletion(new BasicCompletion(provider, "implements"));
		provider.addCompletion(new BasicCompletion(provider, "import"));
		provider.addCompletion(new BasicCompletion(provider, "instanceof"));
		provider.addCompletion(new BasicCompletion(provider, "interface"));
		provider.addCompletion(new BasicCompletion(provider, "native"));
		provider.addCompletion(new BasicCompletion(provider, "new"));
		provider.addCompletion(new BasicCompletion(provider, "package"));
		provider.addCompletion(new BasicCompletion(provider, "private"));
		provider.addCompletion(new BasicCompletion(provider, "protected"));
		provider.addCompletion(new BasicCompletion(provider, "public"));
		provider.addCompletion(new BasicCompletion(provider, "return"));
		provider.addCompletion(new BasicCompletion(provider, "static"));
		provider.addCompletion(new BasicCompletion(provider, "strictfp"));
		provider.addCompletion(new BasicCompletion(provider, "super"));
		provider.addCompletion(new BasicCompletion(provider, "switch"));
		provider.addCompletion(new BasicCompletion(provider, "synchronized"));
		provider.addCompletion(new BasicCompletion(provider, "this"));
		provider.addCompletion(new BasicCompletion(provider, "throw"));
		provider.addCompletion(new BasicCompletion(provider, "throws"));
		provider.addCompletion(new BasicCompletion(provider, "transient"));
		provider.addCompletion(new BasicCompletion(provider, "try"));
		provider.addCompletion(new BasicCompletion(provider, "void"));
		provider.addCompletion(new BasicCompletion(provider, "volatile"));
		provider.addCompletion(new BasicCompletion(provider, "while"));*/
		return provider;
	}
	public JMenu createViewMenu(){
		JMenu menu = new JMenu("View");
		JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(new CodeFoldingAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new ViewLineHighlightAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new ViewLineNumbersAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new AnimateBracketMatchingAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new BookmarksAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new WordWrapAction());
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new ToggleAntiAliasingAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new TabLinesAction());
		menu.add(cbItem);
		return menu;
	}
	public JMenu createSearchMenu(){
		JMenu menu = new JMenu("Search");
		menu.add(new JMenuItem(new ShowFindDialogAction()));
		menu.add(new JMenuItem(new ShowReplaceDialogAction()));
		menu.add(new JMenuItem(new GoToLineAction()));
		return menu;
	}
	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;
	private Window owner;
	public void initSearchDialogs(Dialog d) {
		owner = d; findDialog = new FindDialog(d, this);
		replaceDialog = new ReplaceDialog(d, this);
		replaceDialog.setSearchContext(findDialog.getSearchContext());
	}
	public void initSearchDialogs(Frame f) {
		owner = f; findDialog = new FindDialog(f, this);
		replaceDialog = new ReplaceDialog(f, this);
		replaceDialog.setSearchContext(findDialog.getSearchContext());
	}
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		SearchDialogSearchContext context = findDialog.getSearchContext();
		if (FindDialog.ACTION_FIND.equals(command)) {
			if (!SearchEngine.find(textArea, context)) {
				UIManager.getLookAndFeel().provideErrorFeedback(textArea);
			}
		} else if (ReplaceDialog.ACTION_REPLACE.equals(command)) {
			if (!SearchEngine.replace(textArea, context)) {
				UIManager.getLookAndFeel().provideErrorFeedback(textArea);
			}
		} else if (ReplaceDialog.ACTION_REPLACE_ALL.equals(command)) {
			int count = SearchEngine.replaceAll(textArea, context);
			JOptionPane.showMessageDialog(null, count
					+ " occurrences replaced.");
		}
	}
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
			URL url = e.getURL();
			if (url==null) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			}
			else {
				try{Desktop.getDesktop().browse(url.toURI());}catch(Exception ex){}
			}
		}
	}
	private class AnimateBracketMatchingAction extends AbstractAction {
		private static final long serialVersionUID = 8261076216280862718L;
		public AnimateBracketMatchingAction() {
			putValue(NAME, "Animate Bracket Matching");
		}
		public void actionPerformed(ActionEvent e) {
			textArea.setAnimateBracketMatching(
						!textArea.getAnimateBracketMatching());
		}
	}
	private class BookmarksAction extends AbstractAction {
		private static final long serialVersionUID = -764210212338830406L;
		public BookmarksAction() {
			putValue(NAME, "Bookmarks");
		}
		public void actionPerformed(ActionEvent e) {
			setIconRowHeaderEnabled(!isIconRowHeaderEnabled());
		}
	}
	private class CodeFoldingAction extends AbstractAction {
		private static final long serialVersionUID = 1088597282761061994L;
		public CodeFoldingAction() {
			putValue(NAME, "Code Folding");
		}
		public void actionPerformed(ActionEvent e) {
			textArea.setCodeFoldingEnabled(!textArea.isCodeFoldingEnabled());
		}
	}
	private class TabLinesAction extends AbstractAction {
		private static final long serialVersionUID = -7796352932297988470L;
		private boolean selected;
		public TabLinesAction() {
			putValue(NAME, "Tab Lines");
		}
		public void actionPerformed(ActionEvent e) {
			selected = !selected;
			textArea.setPaintTabLines(selected);
		}
	}
	private class ToggleAntiAliasingAction extends AbstractAction {
		private static final long serialVersionUID = 6084634739172516759L;
		public ToggleAntiAliasingAction() {
			putValue(NAME, "Anti-Aliasing");
		}
		public void actionPerformed(ActionEvent e) {
			textArea.setAntiAliasingEnabled(!textArea.getAntiAliasingEnabled());
		}
	}
	private class ViewLineHighlightAction extends AbstractAction {
		private static final long serialVersionUID = -6323023498538895804L;
		public ViewLineHighlightAction() {
			putValue(NAME, "Current Line Highlight");
		}
		public void actionPerformed(ActionEvent e) {
			textArea.setHighlightCurrentLine(
					!textArea.getHighlightCurrentLine());
		}
	}
	private class ViewLineNumbersAction extends AbstractAction {
		private static final long serialVersionUID = 3393534952794484599L;
		public ViewLineNumbersAction() {
			putValue(NAME, "Line Numbers");
		}
		public void actionPerformed(ActionEvent e) {
			setLineNumbersEnabled(!getLineNumbersEnabled());
		}
	}
	private class WordWrapAction extends AbstractAction {
		private static final long serialVersionUID = -2552189079689556221L;
		public WordWrapAction() {
			putValue(NAME, "Word Wrap");
		}
		public void actionPerformed(ActionEvent e) {
			textArea.setLineWrap(!textArea.getLineWrap());
		}
	}
	private class GoToLineAction extends AbstractAction {
		private static final long serialVersionUID = -4716340028675686542L;
		public GoToLineAction() {
			super("Go To Line...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, c));
		}
		public void actionPerformed(ActionEvent e) {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			GoToDialog dialog; if(owner instanceof Dialog) dialog = new GoToDialog((Dialog)owner);
			else dialog = new GoToDialog((Frame)owner);
			dialog.setMaxLineNumberAllowed(textArea.getLineCount());
			dialog.setVisible(true);
			int line = dialog.getLineNumber();
			if (line>0) {
				try {
					textArea.setCaretPosition(textArea.getLineStartOffset(line-1));
				} catch (BadLocationException ble) { // Never happens
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
					ble.printStackTrace();
				}
			}
		}
	}
	private class ShowFindDialogAction extends AbstractAction {
		private static final long serialVersionUID = -1987487736084782095L;
		public ShowFindDialogAction() {
			super("Find...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
		}
		public void actionPerformed(ActionEvent e) {
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			findDialog.setVisible(true);
		}
	}
	private class ShowReplaceDialogAction extends AbstractAction {
		private static final long serialVersionUID = -6974058078209198657L;
		public ShowReplaceDialogAction() {
			super("Replace...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
		}
		public void actionPerformed(ActionEvent e) {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			replaceDialog.setVisible(true);
		}
	}
	public static interface ModifiedListener {
		public void setModified(boolean modified);
	}
	public String getText(){return textArea.getText();}
	public void loadDocument(String doc){textArea.loadDocument(doc, false, null);}
	public void loadDocument(String doc, boolean modified){textArea.loadDocument(doc, modified, null);}
	public void loadDocument(String doc, boolean modified, ModifiedListener l){textArea.loadDocument(doc, modified, l);}
	public void onSave(){textArea.save();}
	private static class MySyntaxTextArea extends RSyntaxTextArea {
		private static final long serialVersionUID = -3837261840564134591L;
		private MyUndoManager manager;
		protected RUndoManager createUndoManager(){
			int last = 0; ModifiedListener l = null;
			if(manager != null){getDocument().removeDocumentListener(manager); last = manager.lastSave; l = manager.listener;}
			manager = new MyUndoManager(this); getDocument().addDocumentListener(manager);
			manager.lastSave = last; manager.listener = l; return manager;
		}
		public void loadDocument(String doc, boolean modified, ModifiedListener l){
			manager.loadDocument(modified, l); setText(doc); discardAllEdits();
		}
		public void save(){manager.save(); manager.endInternalAtomicEdit();}
	}
	private static class MyUndoManager extends RUndoManager implements DocumentListener {
		private static final long serialVersionUID = -56221406556737313L;
		private int lastSave; private ModifiedListener listener;
		public MyUndoManager(RSyntaxTextArea t){super(t);}
		public void loadDocument(boolean modified, ModifiedListener l){
			lastSave = (modified)?-1:0; listener = l;
		}
		public void save(){lastSave = edits.indexOf(editToBeUndone())+1;}
		public void updateActions(){
			super.updateActions(); if(listener != null) listener.setModified(lastSave != edits.indexOf(editToBeUndone())+1);
		}
		public void changedUpdate(DocumentEvent e){}
		public void insertUpdate(DocumentEvent e){if(listener != null) listener.setModified(true);}
		public void removeUpdate(DocumentEvent e){if(listener != null) listener.setModified(true);}
	}
}
