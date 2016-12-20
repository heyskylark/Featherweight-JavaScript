package edu.sjsu.fwjs;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptLexer;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

import java.io.FileInputStream;
import java.io.InputStream;

public class Interpreter {

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if (args.length>0) inputFile = args[0];
        InputStream is = System.in;
        if (inputFile!=null) is = new FileInputStream(inputFile);

        ANTLRInputStream input = new ANTLRInputStream(is);
        FeatherweightJavaScriptLexer lexer = new FeatherweightJavaScriptLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        FeatherweightJavaScriptParser parser = new FeatherweightJavaScriptParser(tokens);
        ParseTree tree = parser.prog(); // parse
        
        ExpressionBuilderVisitor builder = new ExpressionBuilderVisitor();
        Expression prog = builder.visit(tree);
        prog.evaluate(new Environment());
    }

}
