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
package ortus.boxlang.ast.statement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.BoxStatement;
import ortus.boxlang.ast.Position;
import ortus.boxlang.ast.expression.BoxIdentifier;

/**
 * AST Node representing a if statement
 */
public class BoxTryCatch extends BoxStatement {

	private final BoxIdentifier					exception;
	private final List<BoxStatement>			catchBody;
	private final List<BoxCatchExceptionType>	catchTypes;

	public BoxTryCatch( List<BoxCatchExceptionType> catchTypes, BoxExpr exception, List<BoxStatement> catchBody, Position position, String sourceText ) {
		super( position, sourceText );
		if ( exception instanceof BoxIdentifier exp ) {
			this.exception = exp;
		} else {
			throw new IllegalStateException( "Exception must be a BoxIdentifier" );
		}
		this.exception.setParent( this );
		this.catchBody = Collections.unmodifiableList( catchBody );
		this.catchBody.forEach( arg -> arg.setParent( this ) );

		this.catchTypes = catchTypes;

	}

	public List<BoxStatement> getCatchBody() {
		return catchBody;
	}

	public BoxIdentifier getException() {
		return exception;
	}

	public List<BoxCatchExceptionType> getCatchTypes() {
		return this.catchTypes;
	}

	public BoxCatchExceptionType getType() {
		return catchTypes.get( 0 );
	}

	public BoxExpr getName() {
		return catchTypes.get( 0 ).getName();
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "exception", exception.toMap() );
		map.put( "catchBody", catchBody.stream().map( BoxStatement::toMap ).collect( Collectors.toList() ) );
		map.put( "catchTypes", catchTypes.stream().map( BoxStatement::toMap ).collect( Collectors.toList() ) );

		return map;
	}
}
