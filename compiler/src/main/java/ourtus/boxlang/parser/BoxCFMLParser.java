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
package ourtus.boxlang.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.io.input.BOMInputStream;
import ortus.boxlang.parser.CFMLLexer;
import ortus.boxlang.parser.CFMLParser;
import ourtus.boxlang.ast.*;

import java.io.File;
import java.io.IOException;

public class BoxCFMLParser extends BoxAbstractParser {

	public BoxCFMLParser() {
		super();
	}

	@Override
	protected ParserRuleContext parserFirstStage( File file ) throws IOException {
		BOMInputStream inputStream = getInputStream( file );

		CFMLLexer lexer = new CFMLLexer( CharStreams.fromStream( inputStream ) );
		CFMLParser parser = new CFMLParser( new CommonTokenStream( lexer ) );
		addErrorListeners( lexer, parser );
		return parser.htmlDocument();
	}

	@Override
	protected BoxScript parseTreeToAst( File file, ParserRuleContext parseTree ) throws IOException {
		Position position = new Position( new Point( parseTree.start.getLine(), parseTree.start.getCharPositionInLine() ),
			new Point( parseTree.stop.getLine(), parseTree.stop.getCharPositionInLine() ), new SourceFile( file ) );
		String sourceText = ""; // TODO: extract from parse tree
		// TODO: also pass statements
		return new BoxScript( position, sourceText );
	}

	public ParsingResult parse( File file ) throws IOException {
		CFMLParser.HtmlDocumentContext parseTree = ( CFMLParser.HtmlDocumentContext ) parserFirstStage( file );
		BoxScript ast = parseTreeToAst( file, parseTree );
		return new ParsingResult( ast, issues );
	}
}
