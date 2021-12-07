package com.glotitude.jtlm;

import java.util.List;
import java.util.Map;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);
		R visitExpressionStmt(Expression stmt);
		R visitBindingStmt(Binding stmt);
		R visitEmitStmt(Emit stmt);
		R visitIfStmt(If stmt);
		R visitReturnStmt(Return stmt);
		R visitVarStmt(Var stmt);
		R visitWhileStmt(While stmt);
		R visitForStmt(For stmt);
	}

	abstract <R> R accept(Visitor<R> visitor);

	static class Block extends Stmt {
		final List<Stmt> statements;

		Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}
	}

	static class Expression extends Stmt {
		final Expr expression;

		Expression(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
	}

	static class Binding extends Stmt {
		final Token eventName;
		final Token functionName;
		final List<Token> params;
		final List<Stmt> body;

		Binding(Token eventName, Token functionName, List<Token> params, List<Stmt> body) {
			this.eventName = eventName;
			this.functionName = functionName;
			this.params = params;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBindingStmt(this);
		}
	}

	static class Emit extends Stmt {
		final Token eventName;
		final Expr payload;

		Emit(Token eventName, Expr payload) {
			this.eventName = eventName;
			this.payload = payload;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitEmitStmt(this);
		}
	}

	static class If extends Stmt {
		final Expr condition;
		final Stmt thenBranch;
		final Stmt elseBranch;

		If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}
	}

	static class Return extends Stmt {
		final Token keyword;
		final Expr value;

		Return(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}
	}

	static class Var extends Stmt {
		final Token name;
		final Expr initializer;

		Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}
	}

	static class While extends Stmt {
		final Expr condition;
		final Stmt body;

		While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
	}

	static class For extends Stmt {
		final Token name;
		final Expr iterable;
		final Stmt body;

		For(Token name, Expr iterable, Stmt body) {
			this.name = name;
			this.iterable = iterable;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitForStmt(this);
		}
	}

}
