package ortus.boxlang.parser

private fun findAttribute(name: String, htmlElementContext: CFMLParser.HtmlElementContext): String {
	val attribute = htmlElementContext.htmlAttribute()
		.filter { it.htmlAttributeName().text.equals(name, true) }
		.firstOrNull()
	attribute?.apply {
		return this.htmlAttributeValue()
			.text.replace("\"", "")
	}
	return ""
}

private fun getAttributeValue(attribute: String) =
	attribute.substring(1, attribute.length - 1)

fun CFMLParser.HtmlDocumentContext.toAst(): CFScript {
	val statements = mutableListOf<Statement>()
	this.htmlElements().forEach {
		when {
			it is CFMLParser.HtmlElementsContext -> {
				if (it.htmlElement().htmlTagName(0).text.equals("cfcomponent", true)) {
					val name = findAttribute("name", it.htmlElement())
					// TODO: assume one is present
					statements += it.htmlElement().htmlTagName(0).toAst(name, this.htmlElements())
				}

			}
			// TODO: cfmlElements
		}
	}

	this.cfmlElement().forEach { element ->
		when {
			element.cfcomponent() != null -> {
				statements += element.cfcomponent().toAst()
			}

			element.cfscript() != null -> {
				val scriptAst = element.cfscript().toAst()
				scriptAst.body.forEach { statements += it }
			}
		}
	}
	return CFScript(statements)
}

fun CFMLParser.CfscriptContext.toAst(): CFScript {
	val scriptCode = this.CFSCRIPT_BODY().text.removeSuffix("</cfscript>")
	val cfParser = CFKolasuParser() //<-- TODO: externalise
	val result = cfParser.parse(scriptCode)
	return result.root!! //<-- TODO: handle errors
}

fun CFMLParser.CfcomponentContext.toAst() = Component(
	identifier = this.htmlAttribute()
		.firstOrNull { it.htmlAttributeName().text.lowercase() == "name" }
		?.htmlAttributeValue()?.text?.let { getAttributeValue(it) } ?: "",
	functions = this.cfmlElement()
		.filter { it.cffunction() != null }
		.map { it.cffunction().toAst() }
)

fun CFMLParser.CffunctionContext.toAst() = FunctionDefinition(
	name = this.htmlAttribute()
		.firstOrNull { it.htmlAttributeName().text.lowercase() == "name" }
		?.htmlAttributeValue()?.text?.let { getAttributeValue(it) } ?: "",
	returnType = this.htmlAttribute()
		.firstOrNull { it.htmlAttributeName().text.lowercase() == "returntype" }
		?.htmlAttributeValue()?.text?.let { getAttributeValue(it) } ?: ""
)

private fun CFMLParser.HtmlTagNameContext.toAst(identifier: String, htmlElements: MutableList<CFMLParser.HtmlElementsContext>): Statement {
	val functions = mutableListOf<FunctionDefinition>()
	htmlElements.forEach {
		when {
			it is CFMLParser.HtmlElementsContext -> {
				if (it.htmlElement().htmlTagName(0).text.equals("cffunction", true)) {
					val name = findAttribute("name", it.htmlElement())
					val returnType = findAttribute("returntype", it.htmlElement())
					functions += FunctionDefinition(name, returnType, emptyList())
				}

			}
		}
	}
	return Component(
		identifier,
		functions
	)
}

