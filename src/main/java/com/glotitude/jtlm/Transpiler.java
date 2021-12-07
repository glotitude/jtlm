package com.glotitude.jtlm;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Transpiler implements Expr.Visitor<Object>, Stmt.Visitor<Void>, Closeable {
    final Environment globals = new Environment();
    private Environment environment = globals;
    final Map<String, List<TlmCallable>> eventBinding = new HashMap<>();

    private final PrintWriter printWriter;

    Transpiler(String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        printWriter = new PrintWriter(fileWriter);
        printWriter.close();
    }

    public void close() {
        printWriter.close();
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
            // todo here we are starting runtime
            // or one level upper with copies of environments
        } catch (RuntimeError error) {
            Tlm.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        // todo add comparison for strings
        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                if (left instanceof List && right instanceof List) {
                    // fixme check if it work
                    List<Object> result = new ArrayList<>();
                    result.addAll((List) left);
                    result.addAll((List) right);
                    return result;
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers, strings or arrays.");
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof TlmCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions.");
        }

        TlmCallable function = (TlmCallable)callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return null;
        // fixme
//        return function.call(this, arguments);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }
        ;

        // unreachable
        return null;
    }

    @Override
    public Object visitArrayExpr(Expr.Array expr) {
        return expr.values.stream().map(this::evaluate).collect(Collectors.toList());
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitDictExpr(Expr.Dict expr) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Expr> value : expr.values.entrySet()) {
            result.put(value.getKey(), evaluate(value.getValue()));
        }

        return result;
    }

    @Override
    public Object visitRangeExpr(Expr.Range expr) {
        List<Double> result = new ArrayList<>();
        Double lowerBound = (Double) evaluate(expr.lowerBound);
        Double upperBound = (Double) evaluate(expr.upperBound);
        for (Double i = lowerBound; i < upperBound; i++) {
            result.add(i);
        }

        return result;
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        // todo add checks for empty arrays, strings and other types
        return true;
    }

    private Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitEmitStmt(Stmt.Emit stmt) {
        Event event = new Event(stmt.eventName.lexeme, evaluate(stmt.payload));
        // cyclic dependency is evil but I don't see better way to do this in current architecture
        Tlm.runtime.addEvent(event);

        return null;
    }

    @Override
    public Void visitBindingStmt(Stmt.Binding stmt) {
        TlmFunction function = new TlmFunction(stmt, environment);

        // we don't need unnamed functions in our scope it's ambiguous
        if (!stmt.functionName.lexeme.equals("_")) {
            environment.define(stmt.functionName.lexeme, function);
        }

        if (eventBinding.containsKey(stmt.eventName.lexeme)) {
            eventBinding.get(stmt.eventName.lexeme).add(function);
        } else {
            List<TlmCallable> callables = new ArrayList<>();
            callables.add(function);
            eventBinding.put(stmt.eventName.lexeme, callables);
        }

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        @SuppressWarnings("unchecked")
        List<Double> iterable = (List<Double>) evaluate(stmt.iterable);
        for (Double i: iterable) {
            Environment e = new Environment(environment);
            e.define(stmt.name.lexeme, i);
            executeBlock(stmt.body, e);
        }

        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    void executeBlock(Stmt statement, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            execute(statement);
        } finally {
            this.environment = previous;
        }
    }
}
