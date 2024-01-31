/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import ortus.boxlang.ast.BoxBufferOutput;
import ortus.boxlang.ast.BoxClass;
import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.ast.BoxScript;
import ortus.boxlang.ast.BoxScriptIsland;
import ortus.boxlang.ast.BoxStatement;
import ortus.boxlang.ast.BoxTemplate;
import ortus.boxlang.ast.Issue;
import ortus.boxlang.ast.Point;
import ortus.boxlang.ast.Position;
import ortus.boxlang.ast.expression.BoxFQN;
import ortus.boxlang.ast.expression.BoxIdentifier;
import ortus.boxlang.ast.expression.BoxNull;
import ortus.boxlang.ast.expression.BoxStringInterpolation;
import ortus.boxlang.ast.expression.BoxStringLiteral;
import ortus.boxlang.ast.statement.BoxAccessModifier;
import ortus.boxlang.ast.statement.BoxAnnotation;
import ortus.boxlang.ast.statement.BoxArgumentDeclaration;
import ortus.boxlang.ast.statement.BoxBreak;
import ortus.boxlang.ast.statement.BoxContinue;
import ortus.boxlang.ast.statement.BoxDocumentationAnnotation;
import ortus.boxlang.ast.statement.BoxExpression;
import ortus.boxlang.ast.statement.BoxFunctionDeclaration;
import ortus.boxlang.ast.statement.BoxIfElse;
import ortus.boxlang.ast.statement.BoxImport;
import ortus.boxlang.ast.statement.BoxInclude;
import ortus.boxlang.ast.statement.BoxProperty;
import ortus.boxlang.ast.statement.BoxRethrow;
import ortus.boxlang.ast.statement.BoxReturn;
import ortus.boxlang.ast.statement.BoxReturnType;
import ortus.boxlang.ast.statement.BoxSwitch;
import ortus.boxlang.ast.statement.BoxSwitchCase;
import ortus.boxlang.ast.statement.BoxThrow;
import ortus.boxlang.ast.statement.BoxTry;
import ortus.boxlang.ast.statement.BoxTryCatch;
import ortus.boxlang.ast.statement.BoxType;
import ortus.boxlang.ast.statement.BoxWhile;
import ortus.boxlang.ast.statement.tag.BoxOutput;
import ortus.boxlang.ast.statement.tag.BoxTag;
import ortus.boxlang.parser.antlr.CFMLLexer;
import ortus.boxlang.parser.antlr.CFMLParser;
import ortus.boxlang.parser.antlr.CFMLParser.ArgumentContext;
import ortus.boxlang.parser.antlr.CFMLParser.AttributeContext;
import ortus.boxlang.parser.antlr.CFMLParser.AttributeValueContext;
import ortus.boxlang.parser.antlr.CFMLParser.BoxImportContext;
import ortus.boxlang.parser.antlr.CFMLParser.BreakContext;
import ortus.boxlang.parser.antlr.CFMLParser.CaseContext;
import ortus.boxlang.parser.antlr.CFMLParser.CatchBlockContext;
import ortus.boxlang.parser.antlr.CFMLParser.ComponentContext;
import ortus.boxlang.parser.antlr.CFMLParser.ContinueContext;
import ortus.boxlang.parser.antlr.CFMLParser.FunctionContext;
import ortus.boxlang.parser.antlr.CFMLParser.GenericOpenCloseTagContext;
import ortus.boxlang.parser.antlr.CFMLParser.GenericOpenTagContext;
import ortus.boxlang.parser.antlr.CFMLParser.IncludeContext;
import ortus.boxlang.parser.antlr.CFMLParser.OutputContext;
import ortus.boxlang.parser.antlr.CFMLParser.PropertyContext;
import ortus.boxlang.parser.antlr.CFMLParser.RethrowContext;
import ortus.boxlang.parser.antlr.CFMLParser.ReturnContext;
import ortus.boxlang.parser.antlr.CFMLParser.ScriptContext;
import ortus.boxlang.parser.antlr.CFMLParser.SetContext;
import ortus.boxlang.parser.antlr.CFMLParser.StatementContext;
import ortus.boxlang.parser.antlr.CFMLParser.StatementsContext;
import ortus.boxlang.parser.antlr.CFMLParser.SwitchContext;
import ortus.boxlang.parser.antlr.CFMLParser.TemplateContext;
import ortus.boxlang.parser.antlr.CFMLParser.TextContentContext;
import ortus.boxlang.parser.antlr.CFMLParser.ThrowContext;
import ortus.boxlang.parser.antlr.CFMLParser.TryContext;
import ortus.boxlang.parser.antlr.CFMLParser.WhileContext;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class BoxCFMLParser extends BoxAbstractParser {

	public BoxCFMLParser() {
		super();
	}

	public BoxCFMLParser( int startLine, int startColumn ) {
		super( startLine, startColumn );
	}

	@Override
	protected ParserRuleContext parserFirstStage( InputStream inputStream ) throws IOException {
		CFMLLexer	lexer	= new CFMLLexer( CharStreams.fromStream( inputStream ) );
		CFMLParser	parser	= new CFMLParser( new CommonTokenStream( lexer ) );
		addErrorListeners( lexer, parser );
		return parser.template();
	}

	@Override
	protected BoxNode parseTreeToAst( File file, ParserRuleContext parseTree ) throws IOException {
		TemplateContext		template	= ( TemplateContext ) parseTree;

		List<BoxStatement>	statements	= new ArrayList<>();
		if ( template.boxImport() != null ) {
			statements.addAll( toAst( file, template.boxImport() ) );
		}
		if ( template.component() != null ) {
			return toAst( file, template.component(), statements );
		}
		if ( template.interface_() != null ) {
			throw new BoxRuntimeException( "tag interface parsing not implemented yet" );
		}
		if ( template.statements() != null ) {
			statements.addAll( toAst( file, template.statements() ) );
		}
		return new BoxTemplate( statements, getPosition( parseTree ), getSourceText( parseTree ) );
	}

	private BoxNode toAst( File file, ComponentContext node, List<BoxStatement> importStatements ) {
		List<BoxImport>						imports			= new ArrayList<>();
		List<BoxStatement>					body			= new ArrayList<>();
		List<BoxAnnotation>					annotations		= new ArrayList<>();
		// This will be empty in tags
		List<BoxDocumentationAnnotation>	documentation	= new ArrayList<>();
		List<BoxProperty>					properties		= new ArrayList<>();

		for ( BoxStatement importStatement : importStatements ) {
			imports.add( ( BoxImport ) importStatement );
		}
		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		if ( node.statements() != null ) {
			body.addAll( toAst( file, node.statements() ) );
		}
		for ( CFMLParser.PropertyContext annotation : node.property() ) {
			properties.add( toAst( file, annotation ) );
		}

		return new BoxClass( imports, body, annotations, documentation, properties, getPosition( node ), getSourceText( node ) );
	}

	private BoxProperty toAst( File file, PropertyContext node ) {
		List<BoxAnnotation>					annotations		= new ArrayList<>();
		// This will be empty in tags
		List<BoxDocumentationAnnotation>	documentation	= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		return new BoxProperty( annotations, documentation, getPosition( node ), getSourceText( node ) );
	}

	private List<BoxImport> toAst( File file, List<BoxImportContext> imports ) {
		List<BoxImport> boxImports = new ArrayList<>();
		for ( var boxImport : imports ) {
			boxImports.add( toAst( file, boxImport ) );
		}
		return boxImports;
	}

	private BoxImport toAst( File file, BoxImportContext node ) {
		String				name		= null;
		String				prefix		= null;
		BoxIdentifier		alias		= null;
		List<BoxAnnotation>	annotations	= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		BoxExpr nameSearch = findExprInAnnotations( annotations, "name", true, null, "import", getPosition( node ) );
		name = getBoxExprAsString( nameSearch, "name", false );
		BoxFQN nameFQN = new BoxFQN( name, nameSearch.getPosition(), nameSearch.getSourceText() );

		prefix = getBoxExprAsString( findExprInAnnotations( annotations, "prefix", false, null, null, null ), "prefix", false );

		BoxExpr aliasSearch = findExprInAnnotations( annotations, "alias", false, null, null, null );
		if ( aliasSearch != null ) {
			alias = new BoxIdentifier( getBoxExprAsString( aliasSearch, "alias", false ),
			    aliasSearch.getPosition(),
			    aliasSearch.getSourceText() );
		}

		if ( prefix != null ) {
			name = prefix + ":" + name;
		}
		return new BoxImport( nameFQN, alias, getPosition( node ), getSourceText( node ) );
	}

	private List<BoxStatement> toAst( File file, StatementsContext node ) {
		List<BoxStatement> statements = new ArrayList<>();
		if ( node.children != null ) {
			for ( var child : node.children ) {
				if ( child instanceof StatementContext statement ) {
					if ( statement.genericCloseTag() != null ) {
						String	tagName		= statement.genericCloseTag().tagName().getText();
						// see if statements list has a BoxTag with this name
						int		size		= statements.size();
						boolean	foundStart	= false;
						int		removeAfter	= -1;
						// loop backwards checking for a BoxTag with this name
						for ( int i = size - 1; i >= 0; i-- ) {
							BoxStatement boxStatement = statements.get( i );
							if ( boxStatement instanceof BoxTag boxTag ) {
								if ( boxTag.getName().equalsIgnoreCase( tagName ) && boxTag.getBody() == null ) {
									foundStart = true;
									// slice all statements from this position to the end and set them as the body of the start tag
									boxTag.setBody( new ArrayList<>( statements.subList( i + 1, size ) ) );
									boxTag.getPosition().setEnd( getPosition( statement.genericCloseTag() ).getEnd() );
									boxTag.setSourceText( getSourceText( boxTag.getSourceStartIndex(), statement.genericCloseTag() ) );
									removeAfter = i;
									break;
								}
							}
						}
						// remove all items in list after removeAfter index
						if ( removeAfter >= 0 ) {
							statements.subList( removeAfter + 1, size ).clear();
						}
						if ( !foundStart ) {
							issues.add( new Issue( "Found end tag [" + tagName + "] without matching start tag", getPosition( statement.genericCloseTag() ) ) );
						}

					} else {
						statements.add( toAst( file, statement ) );
					}
				} else if ( child instanceof TextContentContext textContent ) {
					statements.add( toAst( file, textContent ) );
				} else if ( child instanceof ScriptContext script ) {
					if ( script.scriptBody() != null ) {
						statements.add(
						    new BoxScriptIsland(
						        parseCFStatements( script.scriptBody().getText(), getPosition( script.scriptBody() ) ),
						        getPosition( script.scriptBody() ),
						        getSourceText( script.scriptBody() )
						    )
						);
					}
				}
			}
		}
		return statements;
	}

	private BoxStatement toAst( File file, StatementContext node ) {
		if ( node.output() != null ) {
			return toAst( file, node.output() );
		} else if ( node.set() != null ) {
			return toAst( file, node.set() );
		} else if ( node.if_() != null ) {
			return toAst( file, node.if_() );
		} else if ( node.try_() != null ) {
			return toAst( file, node.try_() );
		} else if ( node.function() != null ) {
			return toAst( file, node.function() );
		} else if ( node.return_() != null ) {
			return toAst( file, node.return_() );
		} else if ( node.while_() != null ) {
			return toAst( file, node.while_() );
		} else if ( node.break_() != null ) {
			return toAst( file, node.break_() );
		} else if ( node.continue_() != null ) {
			return toAst( file, node.continue_() );
		} else if ( node.include() != null ) {
			return toAst( file, node.include() );
		} else if ( node.rethrow() != null ) {
			return toAst( file, node.rethrow() );
		} else if ( node.throw_() != null ) {
			return toAst( file, node.throw_() );
		} else if ( node.switch_() != null ) {
			return toAst( file, node.switch_() );
		} else if ( node.genericOpenCloseTag() != null ) {
			return toAst( file, node.genericOpenCloseTag() );
		} else if ( node.genericOpenTag() != null ) {
			return toAst( file, node.genericOpenTag() );
		}
		throw new BoxRuntimeException( "Statement node " + node.getClass().getName() + " parsing not implemented yet. " + node.getText() );

	}

	private BoxStatement toAst( File file, GenericOpenCloseTagContext node ) {
		List<BoxAnnotation> attributes = new ArrayList<>();
		for ( var attr : node.attribute() ) {
			attributes.add( toAst( file, attr ) );
		}
		return new BoxTag( node.tagName().getText(), attributes, List.of(), node.getStart().getStartIndex(), getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, GenericOpenTagContext node ) {
		List<BoxAnnotation> attributes = new ArrayList<>();
		for ( var attr : node.attribute() ) {
			attributes.add( toAst( file, attr ) );
		}
		// Body may get set later, if we find an end tag
		return new BoxTag( node.tagName().getText(), attributes, null, node.getStart().getStartIndex(), getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, SwitchContext node ) {
		BoxExpr				expression;
		List<BoxAnnotation>	annotations	= new ArrayList<>();
		List<BoxSwitchCase>	cases		= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		expression = findExprInAnnotations( annotations, "expression", true, null, "switch", getPosition( node ) );

		if ( node.switchBody() != null && node.switchBody().children != null ) {
			for ( var c : node.switchBody().children ) {
				if ( c instanceof CFMLParser.CaseContext caseNode ) {
					cases.add( toAst( file, caseNode ) );
					// We're willing to overlook text, but not other CF tags
				} else if ( ! ( c instanceof CFMLParser.TextContentContext ) ) {
					issues.add( new Issue( "Switch body can only contain case statements - ", getPosition( ( ParserRuleContext ) c ) ) );
				}
			}
		}
		return new BoxSwitch( expression, cases, getPosition( node ), getSourceText( node ) );
	}

	private BoxSwitchCase toAst( File file, CaseContext node ) {
		BoxExpr	value		= null;
		BoxExpr	delimiter	= null;

		// Only check for these on case nodes, not default case
		if ( !node.CASE().isEmpty() ) {
			List<BoxAnnotation> annotations = new ArrayList<>();

			for ( var attr : node.attribute() ) {
				annotations.add( toAst( file, attr ) );
			}

			value		= findExprInAnnotations( annotations, "value", true, null, "case", getPosition( node ) );
			delimiter	= findExprInAnnotations( annotations, "delimiter", false, new BoxStringLiteral( ",", null, null ), "case", getPosition( node ) );
		}

		List<BoxStatement> statements = new ArrayList<>();
		if ( node.statements() != null ) {
			statements.addAll( toAst( file, node.statements() ) );
		}

		// In tag mode, the break is implied
		statements.add( new BoxBreak( null, null ) );

		return new BoxSwitchCase( value, delimiter, statements, getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, ThrowContext node ) {
		BoxExpr				object			= null;
		BoxExpr				type			= null;
		BoxExpr				message			= null;
		BoxExpr				detail			= null;
		BoxExpr				errorcode		= null;
		BoxExpr				extendedinfo	= null;

		List<BoxAnnotation>	annotations		= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		for ( var annotation : annotations ) {
			if ( annotation.getKey().getValue().equalsIgnoreCase( "object" ) ) {
				object = annotation.getValue();
			}
			if ( annotation.getKey().getValue().equalsIgnoreCase( "type" ) ) {
				type = annotation.getValue();
			}
			if ( annotation.getKey().getValue().equalsIgnoreCase( "message" ) ) {
				message = annotation.getValue();
			}
			if ( annotation.getKey().getValue().equalsIgnoreCase( "detail" ) ) {
				detail = annotation.getValue();
			}
			if ( annotation.getKey().getValue().equalsIgnoreCase( "errorcode" ) ) {
				errorcode = annotation.getValue();
			}
			if ( annotation.getKey().getValue().equalsIgnoreCase( "extendedinfo" ) ) {
				extendedinfo = annotation.getValue();
			}
		}

		return new BoxThrow( object, type, message, detail, errorcode, extendedinfo, getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, RethrowContext node ) {
		return new BoxRethrow( getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, IncludeContext node ) {
		BoxExpr				template;
		List<BoxAnnotation>	annotations	= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		template = findExprInAnnotations( annotations, "template", true, null, "include", getPosition( node ) );

		// TODO: Add runOnce support
		return new BoxInclude( template, getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, ContinueContext node ) {
		return new BoxContinue( getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, BreakContext node ) {
		return new BoxBreak( getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, WhileContext node ) {
		BoxExpr				condition;
		List<BoxStatement>	body		= new ArrayList<>();
		List<BoxAnnotation>	annotations	= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		BoxExpr conditionSearch = findExprInAnnotations( annotations, "condition", true, null, "while", getPosition( node ) );
		condition = parseCFExpression(
		    getBoxExprAsString(
		        conditionSearch,
		        "condition",
		        false
		    ),
		    conditionSearch.getPosition()
		);

		if ( node.statements() != null ) {
			body.addAll( toAst( file, node.statements() ) );
		}

		return new BoxWhile( condition, body, getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, ReturnContext node ) {
		BoxExpr expr;
		if ( node.expression() != null ) {
			expr = parseCFExpression( node.expression().getText(), getPosition( node.expression() ) );
		} else {
			expr = new BoxNull( null, null );
		}
		return new BoxReturn( expr, getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, FunctionContext node ) {
		BoxReturnType						returnType		= null;
		String								name			= null;
		List<BoxStatement>					body			= new ArrayList<>();
		List<BoxArgumentDeclaration>		args			= new ArrayList<>();
		List<BoxAnnotation>					annotations		= new ArrayList<>();
		List<BoxDocumentationAnnotation>	documentation	= new ArrayList<>();
		BoxAccessModifier					modifier		= null;

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		name = getBoxExprAsString( findExprInAnnotations( annotations, "name", true, null, "function", getPosition( node ) ), "name", false );

		String accessText = getBoxExprAsString( findExprInAnnotations( annotations, "function", false, null, null, null ), "access", true );
		if ( accessText != null ) {
			accessText = accessText.toLowerCase();
			if ( accessText.equals( "public" ) ) {
				modifier = BoxAccessModifier.Public;
			} else if ( accessText.equals( "private" ) ) {
				modifier = BoxAccessModifier.Private;
			} else if ( accessText.equals( "remote" ) ) {
				modifier = BoxAccessModifier.Remote;
			} else if ( accessText.equals( "package" ) ) {
				modifier = BoxAccessModifier.Package;
			}
		}

		BoxExpr	returnTypeSearch	= findExprInAnnotations( annotations, "returnType", false, null, null, null );
		String	returnTypeText		= getBoxExprAsString( returnTypeSearch, "returnType", true );
		if ( returnTypeText != null ) {
			returnTypeText = returnTypeText.toLowerCase();
			BoxType type = null;
			if ( returnTypeText.equals( "boolean" ) ) {
				type = BoxType.Boolean;
			}
			if ( returnTypeText.equals( "numeric" ) ) {
				type = BoxType.Numeric;
			}
			if ( returnTypeText.equals( "string" ) ) {
				type = BoxType.String;
			}
			// TODO: Add rest of types or make dynamic
			if ( type != null ) {
				returnType = new BoxReturnType( type, null, returnTypeSearch.getPosition(), returnTypeSearch.getSourceText() );
			} else {
				returnType = new BoxReturnType( BoxType.Fqn, returnTypeText, returnTypeSearch.getPosition(), returnTypeSearch.getSourceText() );
			}
		}

		for ( var arg : node.argument() ) {
			args.add( toAst( file, arg ) );
		}

		body.addAll( toAst( file, node.statements() ) );

		return new BoxFunctionDeclaration( modifier, name, returnType, args, annotations, documentation, body, getPosition( node ), getSourceText( node ) );
	}

	private BoxArgumentDeclaration toAst( File file, ArgumentContext node ) {
		Boolean								required		= false;
		String								type			= "Any";
		String								name			= "undefined";
		BoxExpr								expr			= null;
		List<BoxAnnotation>					annotations		= new ArrayList<>();
		List<BoxDocumentationAnnotation>	documentation	= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		name		= getBoxExprAsString( findExprInAnnotations( annotations, "name", true, null, "function", getPosition( node ) ), "name", false );

		required	= BooleanCaster.cast(
		    getBoxExprAsString(
		        findExprInAnnotations( annotations, "required", false, null, null, null ),
		        "required",
		        false
		    )
		);

		expr		= findExprInAnnotations( annotations, "default", false, null, null, null );
		type		= getBoxExprAsString( findExprInAnnotations( annotations, "type", false, new BoxStringLiteral( "Any", null, null ), null, null ), "type",
		    false );

		return new BoxArgumentDeclaration( required, type, name, expr, annotations, documentation, getPosition( node ), getSourceText( node ) );
	}

	private BoxAnnotation toAst( File file, AttributeContext attribute ) {
		BoxFQN	name	= new BoxFQN( attribute.attributeName().getText(), getPosition( attribute.attributeName() ),
		    getSourceText( attribute.attributeName() ) );
		BoxExpr	value;
		if ( attribute.attributeValue() != null ) {
			value = toAst( file, attribute.attributeValue() );
		} else {
			value = new BoxStringLiteral( "", null, null );
		}
		return new BoxAnnotation( name, value, getPosition( attribute ), getSourceText( attribute ) );
	}

	private BoxExpr toAst( File file, AttributeValueContext node ) {
		if ( node.identifier() != null ) {
			return new BoxStringLiteral( node.identifier().getText(), getPosition( node ),
			    getSourceText( node ) );
		} else {
			return toAst( file, node.quotedString() );
		}
	}

	private BoxStatement toAst( File file, TryContext node ) {
		List<BoxStatement>	tryBody		= toAst( file, node.statements() );
		List<BoxTryCatch>	catches		= node.catchBlock().stream().map( it -> toAst( file, it ) ).toList();
		List<BoxStatement>	finallyBody	= new ArrayList<>();
		if ( node.finallyBlock() != null ) {
			finallyBody.addAll( toAst( file, node.finallyBlock().statements() ) );
		}
		return new BoxTry( tryBody, catches, finallyBody, getPosition( node ), getSourceText( node ) );
	}

	private BoxTryCatch toAst( File file, CatchBlockContext node ) {
		BoxExpr			exception	= new BoxIdentifier( "cfcatch", null, null );
		List<BoxExpr>	catchTypes;

		var				typeSearch	= node.attribute().stream()
		    .filter( ( it ) -> it.attributeName().TAG_NAME().getText().equalsIgnoreCase( "type" ) && it.attributeValue() != null ).findFirst();
		if ( typeSearch.isPresent() ) {
			BoxExpr type;
			if ( typeSearch.get().attributeValue().identifier() != null ) {
				type = new BoxStringLiteral( typeSearch.get().attributeValue().identifier().getText(), getPosition( typeSearch.get().attributeValue() ),
				    getSourceText( typeSearch.get().attributeValue() ) );
			} else {
				type = toAst( file, typeSearch.get().attributeValue().quotedString() );
			}
			catchTypes = List.of( type );
		} else {
			catchTypes = List.of( new BoxFQN( "any", null, null ) );
		}

		List<BoxStatement> catchBody = toAst( file, node.statements() );

		return new BoxTryCatch( catchTypes, exception, catchBody, getPosition( node ), getSourceText( node ) );
	}

	private BoxExpr toAst( File file, CFMLParser.QuotedStringContext node ) {
		String quoteChar = node.getText().substring( 0, 1 );
		if ( node.interpolatedExpression().isEmpty() ) {
			String s = node.getText();
			// trim leading and trailing quote
			s = s.substring( 1, s.length() - 1 );
			return new BoxStringLiteral(
			    escapeStringLiteral( quoteChar, s ),
			    getPosition( node ),
			    getSourceText( node )
			);

		} else {
			List<BoxExpr> parts = new ArrayList<>();
			node.children.forEach( it -> {
				if ( it != null && it instanceof CFMLParser.QuotedStringPartContext str ) {
					parts.add( new BoxStringLiteral( escapeStringLiteral( quoteChar, getSourceText( str ) ),
					    getPosition( str ),
					    getSourceText( str ) ) );
				}
				if ( it != null && it instanceof CFMLParser.InterpolatedExpressionContext interp ) {
					parts.add( parseCFExpression( interp.expression().getText(), getPosition( interp.expression() ) ) );
				}
			} );
			return new BoxStringInterpolation( parts, getPosition( node ), getSourceText( node ) );
		}
	}

	/**
	 * Escape double up quotes and pounds in a string literal
	 * 
	 * @param quoteChar the quote character used to surround the string
	 * @param string    the string to escape
	 * 
	 * @return the escaped string
	 */
	private String escapeStringLiteral( String quoteChar, String string ) {
		String escaped = string.replace( "##", "#" );
		return escaped.replace( quoteChar + quoteChar, quoteChar );
	}

	private BoxIfElse toAst( File file, CFMLParser.IfContext node ) {
		// if condition will always exist
		BoxExpr				condition	= parseCFExpression( node.ifCondition.getText(), getPosition( node.ifCondition ) );
		List<BoxStatement>	thenBody	= new ArrayList<>();
		List<BoxStatement>	elseBody	= new ArrayList<>();

		// Then body will always exist
		thenBody.addAll( toAst( file, node.thenBody ) );

		if ( node.ELSE() != null ) {
			elseBody.addAll( toAst( file, node.elseBody ) );
		}

		// Loop backward over elseif conditions, each one becoming the elseBody of the next.
		for ( int i = node.elseIfCondition.size() - 1; i >= 0; i-- ) {
			int		stopIndex;
			Point	end	= new Point( node.elseIfTagClose.get( i ).getLine(),
			    node.elseIfTagClose.get( i ).getCharPositionInLine() );
			stopIndex = node.elseIfTagClose.get( i ).getStopIndex();
			if ( node.elseThenBody.get( i ).statement().size() > 0 ) {
				end			= new Point( node.elseThenBody.get( i ).statement( node.elseThenBody.get( i ).statement().size() - 1 ).getStop().getLine(),
				    node.elseThenBody.get( i ).statement( node.elseThenBody.get( i ).statement().size() - 1 ).getStop().getCharPositionInLine() );
				stopIndex	= node.elseThenBody.get( i ).statement( node.elseThenBody.get( i ).statement().size() - 1 ).getStop().getStopIndex();
			}
			Position	pos				= new Position(
			    new Point( node.ELSEIF( i ).getSymbol().getLine(), node.ELSEIF( i ).getSymbol().getCharPositionInLine() - 3 ),
			    end );
			BoxExpr		thisCondition	= parseCFExpression( node.elseIfCondition.get( i ).getText(), getPosition( node.elseIfCondition.get( i ) ) );
			elseBody = List.of( new BoxIfElse( thisCondition, toAst( file, node.elseThenBody.get( i ) ), elseBody, pos,
			    getSourceText( node, node.ELSEIF().get( i ).getSymbol().getStartIndex() - 3, stopIndex ) ) );
		}

		// If there were no elseif's, the elsebody here will be the <cfelse>. Otherwise, it will be the last elseif.
		return new BoxIfElse( condition, thenBody, elseBody, getPosition( node ), getSourceText( node ) );
	}

	private BoxStatement toAst( File file, SetContext set ) {
		// In tags, a <bx:set ...> tag is an Expression Statement.
		return new BoxExpression( parseCFExpression( set.expression().getText(), getPosition( set.expression() ) ), getPosition( set ), getSourceText( set ) );
	}

	private BoxStatement toAst( File file, OutputContext node ) {
		List<BoxStatement>	statements	= new ArrayList<>();
		List<BoxAnnotation>	annotations	= new ArrayList<>();

		for ( var attr : node.attribute() ) {
			annotations.add( toAst( file, attr ) );
		}

		BoxExpr	query				= findExprInAnnotations( annotations, "query", false, null, null, null );
		BoxExpr	group				= findExprInAnnotations( annotations, "group", false, null, null, null );
		BoxExpr	groupCaseSensitive	= findExprInAnnotations( annotations, "groupCaseSensitive", false, null, null, null );
		BoxExpr	startRow			= findExprInAnnotations( annotations, "startRow", false, null, null, null );
		BoxExpr	maxRows				= findExprInAnnotations( annotations, "maxRows", false, null, null, null );
		BoxExpr	encodeFor			= findExprInAnnotations( annotations, "encodeFor", false, null, null, null );

		if ( node.statements() != null ) {
			statements.addAll( toAst( file, node.statements() ) );
		}

		return new BoxOutput( statements, query, group, groupCaseSensitive, startRow, maxRows, encodeFor, getPosition( node ), getSourceText( node ) );
	}

	/**
	 * A helper function to find a specific annotation by name and return the value expression
	 * 
	 * @param annotations       the list of annotations to search
	 * @param name              the name of the annotation to find
	 * @param required          whether the annotation is required. If required, and not present a parsing Issue is created.
	 * @param defaultValue      the default value to return if the annotation is not found. Ignored if requried is false.
	 * @param containingTagName the name of the tag that contains the annotation, used in error handling
	 * @param position          the position of the tag, used in error handling
	 * 
	 * @return the value expression of the annotation, or the default value if the annotation is not found
	 * 
	 */
	private BoxExpr findExprInAnnotations( List<BoxAnnotation> annotations, String name, boolean required, BoxExpr defaultValue, String containingTagName,
	    Position position ) {
		var search = annotations.stream().filter( ( it ) -> it.getKey().getValue().equalsIgnoreCase( name ) ).findFirst();
		if ( search.isPresent() ) {
			return search.get().getValue();
		} else if ( !required ) {
			return defaultValue;
		} else {
			issues.add( new Issue( "Missing " + name + " attribute on " + containingTagName + " tag", position ) );
			return new BoxNull( null, null );
		}

	}

	/**
	 * A helper function to take a BoxExpr and return the value expression as a string.
	 * If the expression is not a string literal, an Issue is created.
	 * 
	 * @param expr       the expression to get the value from
	 * @param name       the name of the attribute, used in error handling
	 * @param allowEmpty whether an empty string is allowed. If not allowed, an Issue is created.
	 * 
	 * @return the value of the expression as a string, or null if the expression is null
	 */
	private String getBoxExprAsString( BoxExpr expr, String name, boolean allowEmpty ) {
		if ( expr == null ) {
			return null;
		}
		if ( expr instanceof BoxStringLiteral str ) {
			if ( !allowEmpty && str.getValue().trim().isEmpty() ) {
				issues.add( new Issue( "Attribute [" + name + "] cannot be empty", expr.getPosition() ) );
			}
			return str.getValue();
		} else {
			issues.add( new Issue( "Attribute [" + name + "] attribute must be a string literal", expr.getPosition() ) );
			return "";
		}
	}

	private BoxStatement toAst( File file, TextContentContext node ) {
		BoxExpr expression = null;
		// No interpolated expressions, only string
		if ( node.interpolatedExpression().isEmpty() ) {
			expression = new BoxStringLiteral( escapeStringLiteral( node.getText() ), getPosition( node ), getSourceText( node ) );
		} else {
			List<BoxExpr> expressions = new ArrayList<>();
			for ( var child : node.children ) {
				if ( child instanceof CFMLParser.InterpolatedExpressionContext intrpexpr && intrpexpr.expression() != null ) {
					// parse the text between the hash signs as a CF expression
					expressions.add( parseCFExpression( intrpexpr.expression().getText(), getPosition( intrpexpr ) ) );
				} else if ( child instanceof CFMLParser.NonInterpolatedTextContext strlit ) {
					expressions.add( new BoxStringLiteral( escapeStringLiteral( strlit.getText() ), getPosition( strlit ), getSourceText( strlit ) ) );
				}
			}
			expression = new BoxStringInterpolation( expressions, getPosition( node ), getSourceText( node ) );
		}
		return new BoxBufferOutput( expression, getPosition( node ), getSourceText( node ) );
	}

	/**
	 * Escape pounds in a string literal
	 *
	 * @param string the string to escape
	 *
	 * @return the escaped string
	 */
	private String escapeStringLiteral( String string ) {
		return string.replace( "##", "#" );
	}

	public ParsingResult parse( File file ) throws IOException {
		BOMInputStream				inputStream	= getInputStream( file );

		CFMLParser.TemplateContext	parseTree	= ( CFMLParser.TemplateContext ) parserFirstStage( inputStream );
		BoxNode						ast			= parseTreeToAst( file, parseTree );
		return new ParsingResult( ast, issues );
	}

	public ParsingResult parse( String code ) throws IOException {
		InputStream					inputStream	= IOUtils.toInputStream( code, StandardCharsets.UTF_8 );
		CFMLParser.TemplateContext	parseTree	= ( CFMLParser.TemplateContext ) parserFirstStage( inputStream );
		BoxNode						ast			= parseTreeToAst( file, parseTree );
		return new ParsingResult( ast, issues );
	}

	public BoxExpr parseCFExpression( String code, Position position ) {
		try {
			ParsingResult result = new BoxCFParser( position.getStart().getLine(), position.getStart().getColumn() ).parseExpression( code );
			if ( result.getIssues().isEmpty() ) {
				return ( BoxExpr ) result.getRoot();
			} else {
				// Add these issues to the main parser
				issues.addAll( result.getIssues() );
				return new BoxNull( null, null );
			}
		} catch ( IOException e ) {
			issues.add( new Issue( "Error parsing interpolated expression " + e.getMessage(), position ) );
			return new BoxNull( null, null );
		}
	}

	public List<BoxStatement> parseCFStatements( String code, Position position ) {
		try {
			ParsingResult result = new BoxCFParser( position.getStart().getLine(), position.getStart().getColumn() ).parse( code );
			if ( result.getIssues().isEmpty() ) {
				BoxNode root = result.getRoot();
				if ( root instanceof BoxScript script ) {
					return script.getStatements();
				} else if ( root instanceof BoxStatement statement ) {
					return List.of( statement );
				} else {
					// Could be a BoxClass, which we may actually need to support if there is a .cfc file with a top-level <cfscript> node containing a
					// component.
					issues.add( new Issue( "Unexpected root node type [" + root.getClass().getName() + "] in script island.", position ) );
					return List.of();
				}
			} else {
				// Add these issues to the main parser
				issues.addAll( result.getIssues() );
				return List.of( new BoxExpression( new BoxNull( null, null ), null, null ) );
			}
		} catch ( IOException e ) {
			issues.add( new Issue( "Error parsing interpolated expression " + e.getMessage(), position ) );
			return List.of();
		}
	}
}
