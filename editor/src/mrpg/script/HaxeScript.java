package mrpg.script;

import haxe.root.Reflect;
import hscript.Expr;
import hscript.Interp;
import hscript.Parser;

public class HaxeScript {
	@SuppressWarnings("unchecked")
	public static void test(){
		String script = "var obj = {name:\"heh\", test:4, square:function(this){return this.test*this.test;}}; Reflect.setField(obj, \"test\", 8); return obj.square(obj);";
		Parser parser = new Parser(); Expr program = parser.parseString(script); Interp interp = new Interp();
		interp.variables.set("Reflect",Reflect.class); System.out.println(interp.execute(program));
	}
}
