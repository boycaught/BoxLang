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

package ortus.boxlang.runtime.bifs.global.decision;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class IsFileObjectTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IScope		variables;

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		variables.clear();
	}

	@Disabled( "fileOpen() not implemented, and File.createTempFile throws a ClassNotFoundBoxLangException" )
	@DisplayName( "It detects file objects" )
	@Test
	public void testTrueConditions() {
		instance.executeSource(
		    """
			import Java.io.File;

		    fromFileOpen = isFileObject( fileOpen( "./brad.txt" ) );
			fromJavaFile = isFileObject( File.createTempFile( "brad", ".txt" ) );
		    """,
		    context );
		assertThat( ( Boolean ) variables.dereference( Key.of( "fromFileOpen" ), false ) ).isTrue();
		assertThat( ( Boolean ) variables.dereference( Key.of( "fromJavaFile" ), false ) ).isTrue();
	}

	@DisplayName( "It returns false for non-file objects" )
	@Test
	public void testFalseConditions() {
		instance.executeSource(
		    """
		    aBool = isFileObject( true );
		    aString = isFileObject( "" );
		    aFilePath = isFileObject( "./test.csv" );
		      """,
		    context );
		assertThat( ( Boolean ) variables.dereference( Key.of( "aBool" ), false ) ).isFalse();
		assertThat( ( Boolean ) variables.dereference( Key.of( "aString" ), false ) ).isFalse();
		assertThat( ( Boolean ) variables.dereference( Key.of( "aFilePath" ), false ) ).isFalse();
	}

}
