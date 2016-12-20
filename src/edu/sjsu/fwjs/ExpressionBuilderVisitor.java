package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser.ExprContext;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression>{
    @Override
    public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=0; i<ctx.stat().size(); i++) {
            Expression exp = visit(ctx.stat(i));
            if (exp != null) stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    @Override
    public Expression visitBareExpr(FeatherweightJavaScriptParser.BareExprContext ctx) {
        return visit(ctx.expr());
    }
    
    /* Function Declaration and Application */
    public Expression visitFuncDec(FeatherweightJavaScriptParser.FuncDecContext ctx) {
    	List<String> params = new ArrayList<String>();
    	
    	// Convert TerminalNode list of params to String.
    	List<TerminalNode> nodeList = ctx.params().ID();
    	int listSize = nodeList.size();
    	for(int i = 0; i < listSize; i++) {
    		params.add(String.valueOf(nodeList.get(i)));
    	}
    	
    	Expression body = visit(ctx.block());
    	return new FunctionDeclExpr(params, body);
    }
    
    @SuppressWarnings("null")
	public Expression visitFuncApp(FeatherweightJavaScriptParser.FuncAppContext ctx) {
    	Expression exp = visit(ctx.expr());
    	List<Expression> args = new ArrayList<Expression>();
    	
    	// Convert ExprContext list of args to Expression.
    	List<ExprContext> expList = ctx.args().expr();
    	int listSize = expList.size();
    	for(int i = 0; i < listSize; i++) {
    		args.add(visit(expList.get(i)));
    	}
    	
    	return new FunctionAppExpr(exp, args);
    }

    /* IF, WHILE, PRINT */
    @Override
    public Expression visitIfThenElse(FeatherweightJavaScriptParser.IfThenElseContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block(0));
        Expression els = visit(ctx.block(1));
        return new IfExpr(cond, thn, els);
    }

    @Override
    public Expression visitIfThen(FeatherweightJavaScriptParser.IfThenContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block());
        return new IfExpr(cond, thn, null);
    }
    
    @Override
    public Expression visitWhile(FeatherweightJavaScriptParser.WhileContext ctx) {
    	Expression cond = visit(ctx.expr());
    	Expression body = visit(ctx.block());
		return new WhileExpr(cond, body);
    }
    
    @Override
    public Expression visitPrint(FeatherweightJavaScriptParser.PrintContext ctx) {
    	Expression expr = visit(ctx.expr());
    	return new PrintExpr(expr);
    }

    /* Literals */
    @Override
    public Expression visitInt(FeatherweightJavaScriptParser.IntContext ctx) {
        int val = Integer.valueOf(ctx.INT().getText());
        return new ValueExpr(new IntVal(val));
    }
    
    @Override
    public Expression visitBool(FeatherweightJavaScriptParser.BoolContext ctx) {
    	boolean val = Boolean.valueOf(ctx.BOOL().getText());
    	return new ValueExpr(new BoolVal(val));
    }
    
    @Override
    public Expression visitNull(FeatherweightJavaScriptParser.NullContext ctx) {
    	return new ValueExpr(new NullVal());
    }
    
    /* Variable Dec and Update */
    @Override
    public Expression visitVarDec(FeatherweightJavaScriptParser.VarDecContext ctx) {
    	String varName = String.valueOf(ctx.ID().getText());
    	Expression exp = visit(ctx.expr());
    	return new VarDeclExpr(varName, exp);
    }
    
    @Override
    public Expression visitAssign(FeatherweightJavaScriptParser.AssignContext ctx) {
    	String varName = String.valueOf(ctx.ID().getText());
    	Expression exp = visit(ctx.expr());
    	return new AssignExpr(varName, exp);
    }
    
    @Override
    public Expression visitVarRef(FeatherweightJavaScriptParser.VarRefContext ctx) {
    	String varName = String.valueOf(ctx.ID().getText());
    	return new VarExpr(varName);
    }

    /* Parens and Block */
    @Override
    public Expression visitParens(FeatherweightJavaScriptParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitFullBlock(FeatherweightJavaScriptParser.FullBlockContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=1; i<ctx.getChildCount()-1; i++) {
            Expression exp = visit(ctx.getChild(i));
            stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }
    
    /* Operations */
    public Expression visitMulDivMod(FeatherweightJavaScriptParser.MulDivModContext ctx) {
    	Expression exp1 = visit(ctx.expr(0));
    	Expression exp2 = visit(ctx.expr(1));
    	Op op = getEnum(String.valueOf(ctx.op.getText()));
    	return new BinOpExpr(op, exp1, exp2);
    }
    
    public Expression visitAddSub(FeatherweightJavaScriptParser.AddSubContext ctx) {
    	Expression exp1 = visit(ctx.expr(0));
    	Expression exp2 = visit(ctx.expr(1));
    	Op op = getEnum(String.valueOf(ctx.op.getText()));
    	return new BinOpExpr(op, exp1, exp2);
    }
    
    public Expression visitCompare(FeatherweightJavaScriptParser.CompareContext ctx) {
    	Expression exp1 = visit(ctx.expr(0));
    	Expression exp2 = visit(ctx.expr(1));
    	Op op = getEnum(String.valueOf(ctx.op.getText()));
    	return new BinOpExpr(op, exp1, exp2);
    }

    public Op getEnum(String enumVal) {  	
    	switch(enumVal) {
    	case "+":
    		return Op.ADD;
    	case "-":
    		return Op.SUBTRACT;
    	case "*":
    		return Op.MULTIPLY;
    	case "/":
    		return Op.DIVIDE;
    	case "%":
    		return Op.MOD;
    	case ">":
    		return Op.GT;
    	case ">=":
    		return Op.GE;
    	case "<":
    		return Op.LT;
    	case "<=":
    		return Op.LE;
    	case "==":
    		return Op.EQ;
    	default:
    		return null;
    	}
    }

    /**
     * Converts a list of expressions to one sequence expression,
     * if the list contained more than one expression.
     */
    private Expression listToSeqExp(List<Expression> stmts) {
        if (stmts.isEmpty()) return null;
        Expression exp = stmts.get(0);
        for (int i=1; i<stmts.size(); i++) {
            exp = new SeqExpr(exp, stmts.get(i));
        }
        return exp;
    }

    @Override
    public Expression visitSimpBlock(FeatherweightJavaScriptParser.SimpBlockContext ctx) {
        return visit(ctx.stat());
    }
}
