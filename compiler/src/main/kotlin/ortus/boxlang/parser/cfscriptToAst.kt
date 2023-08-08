package ortus.boxlang.parser

import com.strumenta.kolasu.model.ReferenceByName
import com.strumenta.kolasu.parsing.withParseTreeNode
import org.antlr.v4.runtime.ParserRuleContext

private fun ParserRuleContext.astConversionNotImplemented() = NotImplementedError(
	"""
		|AST conversion not implemented
		|node: ${this.javaClass.simpleName}
		|with children: ${this.children.joinToString(separator = "${System.lineSeparator()}\t") { "${it.javaClass.simpleName}" }}
		|for: <<
		|${this.text}
		|>>
	""".trimMargin("|")
)

// TODO: partially implemented
fun CFParser.ScriptContext.toAst(): BoxScript {
	val statements = mutableListOf<BoxStatement>()

	if (this.component() != null)
		statements += this.component().toAst()

	this.functionOrStatement().forEach { statements += it.toAst() }

	return BoxScript(statements)
}

fun CFParser.FunctionOrStatementContext.toAst(): BoxStatement {
	return when {
		this.function() != null -> this.function().toAst()
		this.statement() != null -> this.statement().toAst()
		else -> throw this.astConversionNotImplemented()
	}
}

fun CFParser.StatementContext.toAst(): BoxStatement {
	return when {
		this.simpleStatement() != null -> this.simpleStatement().toAst()
		this.if_() != null -> this.if_().toAst()
		else -> throw this.astConversionNotImplemented()
	}
}

fun CFParser.IfContext.toAst() = BoxIfStatement(
	condition = this.expression().toAst(),
	body = this.ifStmtBlock?.toAst() ?: listOf(this.ifStmt.toAst()),
	elseStatement = this.elseStmtBlock?.toAst() ?: this.elseStmt?.let { listOf(it.toAst()) }
)

fun CFParser.StatementBlockContext.toAst() = this.statement().map { it.toAst() }

fun CFParser.SimpleStatementContext.toAst(): BoxStatement {
	return when {
		this.assignment() != null -> this.assignment().toAst()
		this.methodInvokation() != null -> BoxExpressionStatement(this.methodInvokation().toAst())
		else -> throw this.astConversionNotImplemented()
	}
}

fun CFParser.AssignmentContext.toAst() = BoxAssignment(
	left = this.assignmentLeft().toAst(),
	right = this.assignmentRight().toAst()
)

fun CFParser.AssignmentLeftContext.toAst(): BoxExpression {
	return when {
		this.assignmentLeft() == null -> this.accessExpression().toAst()
		else -> throw this.astConversionNotImplemented()
	}
}

fun CFParser.AssignmentRightContext.toAst() = this.expression().toAst()

fun CFParser.ExpressionContext.toAst(): BoxExpression {
	return when {
		this.literalExpression() != null -> this.literalExpression().toAst()
		this.accessExpression() != null -> this.accessExpression().toAst()
		this.objectExpression() != null -> this.objectExpression().toAst()
		this.methodInvokation() != null -> this.methodInvokation().toAst()
		this.EQ() != null ||
			this.GT() != null ||
			this.GTE() != null ||
			this.LT() != null ||
			this.LTE() != null ||
			this.NEQ() != null -> BoxComparisonExpression(
			left = this.expression()[0].toAst(),
			right = this.expression()[1].toAst(),
			op = this.EQ()?.let { BoxComparisonOperator.Equal } ?: this.GT()?.let { BoxComparisonOperator.GreaterThan }
			?: this.GTE()?.let { BoxComparisonOperator.GreaterEqualsThan }
			?: this.LT()?.let { BoxComparisonOperator.LessThan }
			?: this.LTE()?.let { BoxComparisonOperator.LessEqualThan }
			?: this.NEQ()?.let { BoxComparisonOperator.NotEqual } ?: throw this.astConversionNotImplemented()
		)

		this.AMPERSAND() != null -> BoxBinaryExpression(
			left = this.expression()[0].toAst(),
			right = this.expression()[1].toAst(),
			op = this.AMPERSAND()?.let { BoxBinaryOperator.Concat } ?: throw this.astConversionNotImplemented()
		)

		else -> throw this.astConversionNotImplemented()
	}
}

private fun CFParser.MethodInvokationContext.toAst() = BoxMethodInvokationExpression(
	methodName = ReferenceByName(this.functionInvokation().identifier().text),
	arguments = this.functionInvokation().argumentList()?.argument()?.map { it.toAst() } ?: emptyList(),
	obj = when {
		this.objectExpression() != null -> this.objectExpression().toAst()
		this.accessExpression() != null -> this.accessExpression().toAst()
		else -> throw this.astConversionNotImplemented()
	}
)

private fun CFParser.LiteralExpressionContext.toAst(): BoxLiteralExpression {
	return when {
		this.stringLiteral() != null -> this.stringLiteral().toAst()
		this.integerLiteral() != null -> this.integerLiteral().toAst()
		else -> throw this.astConversionNotImplemented()
	}
}

fun CFParser.ObjectExpressionContext.toAst(): BoxExpression {
	return when {
		this.functionInvokation() != null -> this.functionInvokation().toAst()
		this.identifier() != null -> this.identifier().toAst()
		this.arrayAccess() != null -> this.arrayAccess().toAst().withParseTreeNode(this@toAst) // TODO
		else -> throw this.astConversionNotImplemented()
	}
}

private fun CFParser.FunctionInvokationContext.toAst() = FunctionInvokationExpression(
	name = ReferenceByName(name = this.identifier().text),
	arguments = this.argumentList()?.argument()?.map { it.toAst() } ?: emptyList()
)

private fun CFParser.ArgumentContext.toAst(): BoxExpression {
	// TODO: consider the other possible expressions
	return this.expression()[0].toAst()
}

fun CFParser.AccessExpressionContext.toAst(): BoxExpression {
	return when {
		this.identifier() != null -> this.identifier().toAst()
		this.arrayAccess() != null -> this.arrayAccess().toAst()
		this.objectExpression() != null -> {
			val stack = ArrayDeque<BoxExpression>()
			var acc = this

			while (acc.objectExpression() != null) {
				stack.addLast(acc.objectExpression().toAst())
				acc = acc.accessExpression()
			}

			stack.addLast(acc.toAst())

			fun f(s: ArrayDeque<BoxExpression>): BoxObjectAccessExpression {
				val acc = s.removeLast()
				return BoxObjectAccessExpression(
					access = acc,
					context = if (s.size == 1) s.removeLast() else f(s)
				)
			}

			f(stack)
		}

		else -> throw this.astConversionNotImplemented()
	}
}

private fun CFParser.IdentifierContext.toAst() = when {
	this.reservedKeyword()?.scope() != null -> this.reservedKeyword()!!.scope().toAst()
	else -> BoxIdentifier(this.text)
}

private fun CFParser.ScopeContext.toAst(): BoxScopeExpression {
	return when {
		this.VARIABLES() != null -> BoxVariablesScopeExpression()
		else -> throw this.astConversionNotImplemented()
	}
}

fun CFParser.ArrayAccessContext.toAst(): BoxArrayAccessExpression {
	val context = when {
		this.identifier() != null -> this.identifier().toAst()
		this.arrayAccess() != null -> this.arrayAccess().toAst()
		else -> throw this.astConversionNotImplemented()
	}
	return BoxArrayAccessExpression(
		context = context,
		index = this.arrayAccessIndex().toAst()
	)
}

private fun CFParser.ArrayAccessIndexContext.toAst() = this.expression().toAst()

private fun CFParser.IntegerLiteralContext.toAst() = BoxIntegerLiteral(value = this.text)

fun CFParser.StringLiteralContext.toAst(): BoxStringLiteral {
	val value = StringBuffer()
	this.children.forEach { fragment ->
		when (fragment) {
			OPEN_QUOTE(), CLOSE_QUOTE() -> {}
			else -> value.append(fragment.text)
		}
	}
	return BoxStringLiteral(value.toString())
}

// TODO: partially implemented
fun CFParser.ComponentContext.toAst() = BoxComponent(
	identifier = this.identifier()?.text ?: "",
	functions = this.functionOrStatement()
		.filter { it.function() != null }
		.map { it.function().toAst() }
)

// TODO: partially implemented
fun CFParser.FunctionContext.toAst() = BoxFunctionDefinition(
	name = this.functionSignature().identifier().text,
	returnType = this.functionSignature().returnType()?.text ?: ""
)