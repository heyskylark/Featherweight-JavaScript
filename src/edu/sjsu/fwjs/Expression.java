package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment env) {
        return env.resolveVar(varName);
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;
    public PrintExpr(Expression exp) {
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2;
    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    public Value evaluate(Environment env) {
    	// Eval e1 and e2. Should be IntVal.
    	int v1 = ((IntVal)e1.evaluate(env)).toInt();
    	int v2 = ((IntVal) e2.evaluate(env)).toInt();
    	
    	// Switch to determine right Op
    	switch(op) {
    	case ADD:
    		return new IntVal(v1 + v2);
    	case SUBTRACT:
    		return new IntVal(v1 - v2);
    	case MULTIPLY:
    		return new IntVal(v1 * v2);
    	case DIVIDE:
    		return new IntVal(v1 / v2);
    	case MOD:
    		return new IntVal(v1 % v2);
    	case GT:
    		return new BoolVal(v1 > v2);
    	case GE:
    		return new BoolVal(v1 >= v2);
    	case LT:
    		return new BoolVal(v1 < v2);
    	case LE:
    		return new BoolVal(v1 <= v2);
    	case EQ:
    		return new BoolVal(v1 == v2);
    	default:
    		return new NullVal();
    	}
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els;
    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }
    public Value evaluate(Environment env) {
    	// Take a BoolVal to determine which expression to return.
    	// Return evaluated thn or els expression.
        if(((BoolVal)cond.evaluate(env)).toBoolean()) {
        	return thn.evaluate(env);
        } else if(els != null) {
        	return els.evaluate(env);
        }
        
		return new NullVal();
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;
    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public Value evaluate(Environment env) {
    	Value bodyVal = new NullVal();	// Body value to return
    	
    	// Do while loop while condition holds.
        while(((BoolVal) cond.evaluate(env)).toBoolean()) {
        	bodyVal = body.evaluate(env);
        }
        
        //Will return NullVal if bodyVal was never evaluated.
        return bodyVal;
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;
    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Value evaluate(Environment env) {
    	// Evaluate first expression then return evaluated second expression.
        e1.evaluate(env);
        return e2.evaluate(env);
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;
    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
    	// createVar creates variable in the local scope if it does not exist.
    	env.createVar(varName, exp.evaluate(env));
    	return env.resolveVar(varName);
    }
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;
    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }
    public Value evaluate(Environment env) {
    	// Update the var in env.
    	// Will go to global scope if var is not found.
        env.updateVar(varName, e.evaluate(env));
        // Returns the var from env after updating.
        return env.resolveVar(varName);
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public Value evaluate(Environment env) {
    	// Creates a new ClosureVal and returns it.
        return new ClosureVal(params, body, env);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }
    public Value evaluate(Environment env) {
    	ClosureVal val = (ClosureVal) f.evaluate(env);	// Evaluate f expression to get ClosureVal
    	List<Value> evalArgs = new ArrayList<Value>();	// List to hold evaluated values.
    	
    	// Add evaluated Expressions from args to evalArgs to be used in the function.
    	for(int i = 0; i < args.size(); i++) {
    		evalArgs.add(args.get(i).evaluate(env));
    	}
    	// Apply the evaluated Expressions to the val function.
    	return val.apply(evalArgs);
    }
}

