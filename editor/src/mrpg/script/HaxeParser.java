package mrpg.script;

import javax.swing.text.Element;

import haxe.lang.HaxeException;
import hscript.Parser;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

public class HaxeParser extends AbstractParser {
	private Parser parser;
	private DefaultParseResult result;
	public HaxeParser(){
		parser = new Parser(); result = new DefaultParseResult(this);
	}
	private static String getDesc(Throwable t){
		if(t instanceof HaxeException){
			Object o = ((HaxeException)t).obj;
			if(o instanceof hscript.Error){
				hscript.Error e = (hscript.Error)o; switch(e.index){
				case 0: return "Invalid Character '"+(char)((Integer)e.params.__get(0)).intValue()+"'";
				case 1: return "Unexpected '"+e.params.__get(0)+"'";
				case 2: return "Unterminated String";
				case 3: return "Unterminated Comment";
				case 4: return "Unknown Variable '"+e.params.__get(0)+"'";
				case 5: return "Invalid Iterator '"+e.params.__get(0)+"'";
				case 6: return "Invalid Operation '"+e.params.__get(0)+"'";
				case 7: return "Invalid Access '"+e.params.__get(0)+"'";
				}
			} return o.toString();
		} else return t.getMessage();
	}
	public ParseResult parse(RSyntaxDocument doc, String style){
		result.clearNotices();
		Element root = doc.getDefaultRootElement();
		result.setParsedLines(0, root.getElementCount()-1);
		try{
			parser.parseString(doc.getText(0, doc.getLength()));
		}catch(Throwable t){
			int line = parser.line-1; Element elem = root.getElement(line);
			int offs = elem.getStartOffset();
			int len = elem.getEndOffset()-offs;
			if (line==root.getElementCount()-1) {
				len++;
			} DefaultParserNotice pn = new DefaultParserNotice(this, getDesc(t), line, offs, len);
			pn.setLevel(ParserNotice.ERROR); result.addNotice(pn);
		} return result;
	}
}
