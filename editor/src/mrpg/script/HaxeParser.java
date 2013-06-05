package mrpg.script;

import java.io.File;

import javax.swing.text.Element;

import mrpg.editor.resource.Project;
import mrpg.editor.resource.Script;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

public class HaxeParser extends AbstractParser {
	private DefaultParseResult result;
	private Project project; private Script script;
	public HaxeParser(Project p, Script s){
		project = p; script = s; result = new DefaultParseResult(this);
	}
	private void getPath(StringBuilder b, File f){
		if(f.equals(project.getFile())) return;
		getPath(b, f.getParentFile()); b.append(f.getName()); if(f.isDirectory()) b.append("/");
	}
	/*
	 * TODO: recompile projects periodically if modified in separate thread
	 * (and disable building while compiling, which should also be in a separate thread w/progress bar).
	 * Make sure project is out of date before recompiling manually
	 * Add haxe autocomplete. Make sure linux and mac versions work
	 */
	public ParseResult parse(RSyntaxDocument doc, String style){
		result.clearNotices();
		Element root = doc.getDefaultRootElement();
		result.setParsedLines(0, root.getElementCount()-1);
		if(project.lastCompile == null) return result;
		try {
			if(!script.isModified() && script.getFile().lastModified() <= project.lastCompile.time){
				StringBuilder b = new StringBuilder(); b.append("../"); getPath(b, script.getFile());
				String id = b.toString();
				for(String s : project.lastCompile.messages){
					String[] bits = s.split(":", 4);
					if(bits.length >= 4 && bits[0].equals(id)) try{
						int line = Integer.parseInt(bits[1])-1;
						Element elem = root.getElement(line);
						int offs = elem.getStartOffset();
						String chrs[] = bits[2].trim().substring(11).split("-");
						int st = Integer.parseInt(chrs[0]), end = Integer.parseInt(chrs[1]);
						offs += st; int len = (end-st)+1;
						DefaultParserNotice pn = new DefaultParserNotice(this, bits[3].trim(), line, offs, len);
						pn.setLevel(ParserNotice.ERROR); result.addNotice(pn);
					}catch(Exception ex){}
				}
			}
		}catch(Exception e){} return result;
	}
}
