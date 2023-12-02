/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.runtime.types;

import java.time.LocalDateTime;
import java.util.List;

import ortus.boxlang.runtime.context.FunctionBoxContext;
import ortus.boxlang.runtime.loader.ImportDefinition;
import ortus.boxlang.runtime.runnables.IBoxRunnable;
import ortus.boxlang.runtime.scopes.Key;

public class SampleUDF extends UDF {

	Object				returnVal	= null;

	// These are not static just because this is a test class that is always transient! Do not copy this implementation.
	private Key			name;
	private Argument[]	arguments;
	private String		returnType;
	private Access		access;
	private Struct		annotations;
	private Struct		documentation;

	public Key getName() {
		return name;
	}

	public Argument[] getArguments() {
		return arguments;
	}

	public String getReturnType() {
		return returnType;
	}

	public Struct getAnnotations() {
		return annotations;
	}

	public Struct getDocumentation() {
		return documentation;
	}

	public Access getAccess() {
		return access;
	}

	@Override
	public long getRunnableCompileVersion() {
		return 0;
	}

	@Override
	public LocalDateTime getRunnableCompiledOn() {
		return null;
	}

	@Override
	public Object getRunnableAST() {
		return null;
	}

	public IBoxRunnable getDeclaringRunnable() {
		return null;
	}

	public List<ImportDefinition> getImports() {
		return null;
	}

	public SampleUDF( Access access, Key name, String returnType, Argument[] arguments, Object returnVal ) {
		this( access, name, returnType, arguments, returnVal, Struct.EMPTY, Struct.EMPTY );
	}

	public SampleUDF( Access access, Key name, String returnType, Argument[] arguments, Object returnVal, Struct annotations ) {
		this( access, name, returnType, arguments, returnVal, annotations, Struct.EMPTY );
	}

	public SampleUDF( Access access, Key name, String returnType, Argument[] arguments, Object returnVal, Struct annotations, Struct documentation ) {
		super();
		this.access			= access;
		this.name			= name;
		this.returnType		= returnType;
		this.arguments		= arguments;
		this.returnVal		= returnVal;
		this.annotations	= annotations;
		this.documentation	= documentation;
	}

	@Override
	public Object _invoke( FunctionBoxContext context ) {
		return returnVal;
	}
}