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
package ortus.boxlang.transpiler;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.runtime.types.exceptions.ApplicationException;

/**
 * BoxLang AST transpiler interface
 */
public interface ITranspiler {

	TranspiledCode transpile( BoxNode node ) throws ApplicationException;

	String compileJava( CompilationUnit cu, String outputPath, List<String> classPath ) throws ApplicationException;

	void run( String fqn, List<String> classPath ) throws Throwable;

}
