package ortus.boxlang.java

import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.modules.ModuleDeclaration
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.IfStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.VoidType
import com.strumenta.kolasu.model.children
import com.strumenta.kolasu.model.processNodesOfType
import ortus.boxlang.parser.*
import com.github.javaparser.ast.expr.Expression as JExpression


class BoxToJavaMapper(
	private val boxAstRoot: BoxScript,
	private val fileName: String? = null
) {
	private val cu = CompilationUnit()

	fun toJava(): CompilationUnit =
		if (boxAstRoot.body.any { it !is BoxComponent })
			SingleScriptTemplate(
				boxAstRoot.body.filter { it !is BoxComponent },
				cu,
				fileName
			).toJava()
		else
			cu
}

class SingleScriptTemplate(
	private val scriptStatements: List<BoxStatement>,
	private val cu: CompilationUnit,
	private val fileName: String? = null
) {
	inner class ExecutionContextType : ClassOrInterfaceType("ExecutionContext") {
		init {
			cu.addImport("ortus.boxlang.runtime.ExecutionContext")
		}
	}

	private val executionContextType = ExecutionContextType()
	private val iTemplateType = ClassOrInterfaceType("ITemplate")
		.apply { cu.addImport("ortus.boxlang.runtime.dynamic.ITemplate") }
	private val iScopeType = ClassOrInterfaceType("IScope")
		.apply { cu.addImport("ortus.boxlang.runtime.scopes.IScope") }
	private val executionContextParameter = Parameter(executionContextType, "context")
	private val invokeMethodDeclaration = MethodDeclaration()
		.apply { name = SimpleName("invoke") }
		.apply {
			setBody(
				BlockStmt().apply {
					addStatement(
						ExpressionStmt(AssignExpr(
							VariableDeclarationExpr(iScopeType, "variablesScope"),
							MethodCallExpr(NameExpr("context"), "getVariablesScope"),
							AssignExpr.Operator.ASSIGN
						)))
				}
			)
		}
		.apply { addModifier(Modifier.Keyword.PUBLIC) }
		.apply { parameters = NodeList(executionContextParameter) }
		.apply { type = VoidType() }
		.apply { addThrownException(Throwable::class.java) }
	private val classDefinition = ClassOrInterfaceDeclaration()
		.apply { addMember(invokeMethodDeclaration) }
		.apply { fileName?.let { setName(it.replace(Regex("""(.*)\.([^.]+)"""), """$1\$$2""")) } }
		.apply { addModifier(Modifier.Keyword.PUBLIC) }
		.apply { addImplementedType(iTemplateType) }

	fun toJava(): CompilationUnit {
		scriptStatements.forEach {
			invokeMethodDeclaration.body.orElseThrow().addStatement(it.toJava())
		}
		return cu
			.apply { addType(classDefinition) }
	}
}

sealed class ScopeNameExpr(name: String) : NameExpr(name) {
	fun constructSetExpression(fieldName: String, value: JExpression) = ScopeSetExpression(this, fieldName, value)
	fun constructGetExpression(fieldName: String) = ScopeGetExpression(this, fieldName)
}

class VariablesScopeNameExpr : ScopeNameExpr("variablesScope")

class ScopeSetExpression(
	scopeNameExpression: ScopeNameExpr,
	fieldName: String,
	value: JExpression
) : MethodCallExpr(scopeNameExpression, "put", NodeList(fieldName.toKeyOf(), value))

class ScopeGetExpression(
	val scopeNameExpression: ScopeNameExpr,
	private val fieldName: String
) : MethodCallExpr(scopeNameExpression, "get", NodeList(fieldName.toKeyOf())) {
	fun toSetExpression(value: JExpression) = ScopeSetExpression(
		scopeNameExpression,
		fieldName,
		value)
}

fun String.toKeyOf() = MethodCallExpr(
	NameExpr("Key"),
	"of",
	NodeList(StringLiteralExpr(this))
)

fun NameExpr.toKeyOf() = this.nameAsString.toKeyOf()

fun BoxScript.toJava(): com.github.javaparser.ast.CompilationUnit {
	val packageDeclaration = PackageDeclaration()

	val imports = NodeList<ImportDeclaration>()

	val statements = NodeList<TypeDeclaration<*>>()

	// When we have statements at the root level that are not contained in a Component
	// we want to define a Java class and map those statements
	// into an invoke() method
	//
	statements += this.body
		.filter { it !is BoxComponent }
		.takeIf { it.isNotEmpty() }
		?.fold(
			initial = BlockStmt(),
			operation = { block, statement -> block.apply { this.addStatement(statement.toJava()) } }
		)
		?.let { block ->
			ClassOrInterfaceDeclaration()
				.apply {
					this.addMethod("invoke")
						.apply { this.setBody(block) }
				}
		}

	// When Components are defined
	//
	this.processNodesOfType(
		BoxComponent::class.java,
		{ component -> statements.add(component.toJava()) }
	)

	val module = ModuleDeclaration()
	return CompilationUnit(packageDeclaration, imports, statements, null)
}

fun BoxComponent.toJava(): ClassOrInterfaceDeclaration {
	val classDeclaration = ClassOrInterfaceDeclaration()
	if (!this.identifier.isNullOrBlank())
		classDeclaration.name = SimpleName(this.identifier)
	this.functions.forEach {
		classDeclaration.addMethod(it.name)
	}
	return classDeclaration
}

fun BoxStatement.toJava(): Statement = when (this) {
	is BoxAssignment -> this.toJava()
	is BoxIfStatement -> this.toJava()
	is BoxExpressionStatement -> this.toJava()
	else -> throw this.notImplemented()
}

fun BoxExpressionStatement.toJava(): ExpressionStmt = ExpressionStmt(
	this.expression.toJava()
)

fun BoxObjectAccessExpression.toJava(): JExpression {
	val scope = this.context.toJava()

	return when (scope) {
		is ScopeNameExpr -> scope.constructGetExpression(
			when (this.access) {
				is BoxIdentifier -> this.access.name
				is BoxStringLiteral -> this.access.value
				else -> throw this.notImplemented()
			})

		else -> {
			FieldAccessExpr(
				scope,
				when (this.access) {
					is BoxIdentifier -> this.access.name
					is BoxStringLiteral -> this.access.value
					is BoxObjectAccessExpression -> when {
						this.access.context == null && this.access.access is BoxIdentifier -> this.access.access.name
						else -> throw this.notImplemented()
					}

					else -> throw this.notImplemented()
				})
		}
	}
}

fun FunctionInvokationExpression.toJava(): MethodCallExpr {
	/* TODO: move this toJava() definition inside the SingleScriptTemplate
		so that it knows about "context"
	*/
	val arguments = this.arguments.map { it.toJava() }.toTypedArray()

	return if (this.name.name == "createObject" && arguments.size == 2) {
		MethodCallExpr(
			when {
				arguments[0] is StringLiteralExpr &&
					arguments[0].asStringLiteralExpr().value == "java" -> NameExpr("JavaLoader")

				else -> throw notImplemented()
			},
			"load",
			NodeList(mutableListOf<JExpression>(NameExpr("context")) + arguments.toList().subList(1, arguments.size))
		)
	} else {
		MethodCallExpr(this.name.name, *arguments)
	}
}

fun BoxAssignment.toJava(): ExpressionStmt {
	val assignmentLeftExpression = this.left.toJava()
	val assignmentRightExpression = this.right.toJava()

	val possiblyScopeExpression = when (assignmentLeftExpression) {
		is FieldAccessExpr -> assignmentLeftExpression.scope
		is ScopeGetExpression -> assignmentLeftExpression.scopeNameExpression
		else -> null
	}

	return ExpressionStmt(
		if (possiblyScopeExpression is ScopeNameExpr) {
			when (assignmentLeftExpression) {
				is FieldAccessExpr -> possiblyScopeExpression.constructSetExpression(
					assignmentLeftExpression.nameAsString,
					assignmentRightExpression
				)

				is ScopeGetExpression -> assignmentLeftExpression.toSetExpression(assignmentRightExpression)

				else -> throw notImplemented()
			}
		} else {
			AssignExpr(
				assignmentLeftExpression,
				assignmentRightExpression,
				AssignExpr.Operator.ASSIGN
			)
		}
	)
}

fun BoxIfStatement.toJava(): IfStmt = IfStmt(
	this.condition.toJava(),
	BlockStmt(NodeList(this.body.map { it.toJava() })),
	BlockStmt(NodeList(this.elseStatement?.map { it.toJava() } ?: emptyList<Statement>()))
)

fun BoxExpression.toJava(): JExpression = when (this) {
	is BoxObjectAccessExpression -> this.toJava()
	is BoxIdentifier -> this.toJava()
	is FunctionInvokationExpression -> this.toJava()
	is BoxStringLiteral -> this.toJava()
	is BoxIntegerLiteral -> this.toJava()
	is BoxMethodInvokationExpression -> this.toJava()
	is BoxComparisonExpression -> this.toJava()
	is BoxBinaryExpression -> this.toJava()
	is BoxVariablesScopeExpression -> this.toJava()
	is BoxArrayAccessExpression -> this.toJava()
	else -> throw this.notImplemented()
}

fun BoxArrayAccessExpression.toJava(): JExpression = when (val index = this.index.toJava()) {
	is StringLiteralExpr -> FieldAccessExpr(this.context.toJava(), index.value) // TODO: variables scope case
	else -> ArrayAccessExpr(this.context.toJava(), index)
}

fun BoxBinaryExpression.toJava() = MethodCallExpr(
	NameExpr("Concat"),
	"invoke",
	NodeList(
		NameExpr("context"),
		this.left.toJava().let {
			if (it.isNameExpr)
				it.asNameExpr().toKeyOf()
			else
				it
		},
		this.right.toJava()
	)
)

fun BoxComparisonExpression.toJava() = MethodCallExpr(
	when (this.op) {
		BoxComparisonOperator.Equal -> NameExpr("EqualsEquals")
		else -> throw notImplemented()
	},
	"invoke",
	NodeList(NameExpr("context"), this.left.toJava(), this.right.toJava())
)

fun BoxMethodInvokationExpression.toJava(): MethodCallExpr {
	val scope = this.obj.toJava()

	return if (this.methodName.name == "init") {
		MethodCallExpr(
			scope,
			"invokeConstructor",
			NodeList(ArrayCreationExpr(
				ClassOrInterfaceType("Object"),
				NodeList(ArrayCreationLevel()),
				ArrayInitializerExpr(NodeList(this.arguments.map { it.toJava() }))
			))
		)
	} else {
		MethodCallExpr(
			scope,
			this.methodName.name,
			NodeList(this.arguments.map { it.toJava() })
		)
	}
}

fun BoxVariablesScopeExpression.toJava() = VariablesScopeNameExpr()

fun BoxIdentifier.toJava(): JExpression = NameExpr(this.name)

fun BoxStringLiteral.toJava(): StringLiteralExpr = StringLiteralExpr(this.value)

fun BoxIntegerLiteral.toJava(): IntegerLiteralExpr = IntegerLiteralExpr(this.value)


fun BoxNode.notImplemented() = NotImplementedError(
	"""
		${this.javaClass.simpleName} with children:
		${this.children.map { it.javaClass.simpleName }}
	""".trimIndent()
)