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
package ortus.boxlang.compiler.ast;

import java.util.List;
import java.util.Map;

import ortus.boxlang.compiler.ast.statement.BoxAnnotation;
import ortus.boxlang.compiler.ast.statement.BoxDocumentationAnnotation;
import ortus.boxlang.compiler.ast.statement.BoxImport;
import ortus.boxlang.compiler.ast.statement.BoxProperty;

/**
 * Root node for a Class
 */
public class BoxClass extends BoxNode {

	private final List<BoxStatement>				body;
	private final List<BoxImport>					imports;
	private final List<BoxAnnotation>				annotations;
	private final List<BoxDocumentationAnnotation>	documentation;
	private final List<BoxProperty>					properties;

	/**
	 * Creates an AST for a Class
	 *
	 * @param statements list of the statements nodes
	 * @param position   position within the source code
	 * @param sourceText source code
	 *
	 * @see Position
	 * @see BoxStatement
	 */
	public BoxClass( List<BoxImport> imports, List<BoxStatement> body, List<BoxAnnotation> annotations,
	    List<BoxDocumentationAnnotation> documentation, List<BoxProperty> properties, Position position,
	    String sourceText ) {
		super( position, sourceText );
		this.body = body;
		this.body.forEach( arg -> arg.setParent( this ) );
		this.annotations = annotations;
		this.annotations.forEach( arg -> arg.setParent( this ) );
		this.documentation = documentation;
		this.documentation.forEach( arg -> arg.setParent( this ) );
		this.imports = imports;
		this.imports.forEach( arg -> arg.setParent( this ) );
		this.properties = properties;
		this.properties.forEach( arg -> arg.setParent( this ) );
	}

	public List<BoxStatement> getBody() {
		return body;
	}

	public List<BoxAnnotation> getAnnotations() {
		return annotations;
	}

	public List<BoxDocumentationAnnotation> getDocumentation() {
		return documentation;
	}

	public List<BoxImport> getImports() {
		return imports;
	}

	public List<BoxProperty> getProperties() {
		return properties;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "imports", imports.stream().map( BoxImport::toMap ).collect( java.util.stream.Collectors.toList() ) );
		map.put( "body", body.stream().map( BoxStatement::toMap ).collect( java.util.stream.Collectors.toList() ) );
		map.put( "annotations", annotations.stream().map( BoxAnnotation::toMap ).collect( java.util.stream.Collectors.toList() ) );
		map.put( "documentation", documentation.stream().map( BoxDocumentationAnnotation::toMap ).collect( java.util.stream.Collectors.toList() ) );
		map.put( "properties", properties.stream().map( BoxProperty::toMap ).collect( java.util.stream.Collectors.toList() ) );
		return map;
	}
}
