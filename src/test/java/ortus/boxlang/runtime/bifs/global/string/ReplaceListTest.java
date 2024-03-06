
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

package ortus.boxlang.runtime.bifs.global.string;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class ReplaceListTest {

	static BoxRuntime	instance;
	IBoxContext			context;
	IScope				variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance = BoxRuntime.getInstance( true );
	}

	@AfterAll
	public static void teardown() {
	}

	@BeforeEach
	public void setupEach() {
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@DisplayName( "It tests the BIF ReplaceList" )
	@Test
	public void testBif() {
		instance.executeSource(
		    """
		    myList="xxxabcdxxxabcd";
		    result = ReplaceList( myList , "a,b,c,d", "0,1,2,3");
		    """,
		    context );
		assertThat( "xxx0123xxx0123" ).isEqualTo( variables.get( result ) );
		instance.executeSource(
		    """
		    myList="xxxabcdxxxabcd";
		    result = ReplaceList( myList , "", "");
		    """,
		    context );
		assertThat( "xxxabcdxxxabcd" ).isEqualTo( variables.get( result ) );
		instance.executeSource(
		    """
		    myList="xxxabcdxxxabcd";
		    result = ReplaceList( myList , "a", "1,2,3");
		    """,
		    context );
		assertThat( "xxx1bcdxxx1bcd" ).isEqualTo( variables.get( result ) );
		instance.executeSource(
		    """
		    myList="xxxabcdxxxabcd";
		    result = ReplaceList( myList , "a,b,c,d", "11");
		    """,
		    context );
		assertThat( "xxx11xxx11" ).isEqualTo( variables.get( result ) );

	}

	@DisplayName( "It tests the BIF ReplaceListNoCase" )
	@Test
	public void testBifNoCase() {
		instance.executeSource(
		    """
		    myList="xxxaBcDxxxAbcD";
		    result = ReplaceListNoCase( myList , "a,b,c,d", "0,1,2,3");
		    """,
		    context );
		assertThat( "xxx0123xxx0123" ).isEqualTo( variables.get( result ) );
		instance.executeSource(
		    """
		    myList="xxxabcdxxxAbcd";
		    result = ReplaceListNoCase( myList , "a", "1,2,3");
		    """,
		    context );
		assertThat( "xxx1bcdxxx1bcd" ).isEqualTo( variables.get( result ) );
		instance.executeSource(
		    """
		    myList="xxxabCdxxxaBcd";
		    result = ReplaceListNoCase( myList , "a,b,c,d", "11");
		    """,
		    context );
		assertThat( "xxx11xxx11" ).isEqualTo( variables.get( result ) );

	}

	@DisplayName( "It tests the member function for ReplaceList" )
	@Test
	public void testMemberFunction() {
		instance.executeSource(
		    """
		    myList="xxxabcdxxxabcd";
		    result = myList.ReplaceList( "a,b,c,d", "0,1,2,3");
		    """,
		    context );
		assertThat( "xxx0123xxx0123" ).isEqualTo( variables.get( result ) );
	}

	@DisplayName( "It tests the member function for ReplaceListNoCase" )
	@Test
	public void testMemberFunctionNoCase() {
		instance.executeSource(
		    """
		    myList="xxxaBcDxxxAbcD";
		    result = myList.ReplaceListNoCase( "a,b,c,d", "0,1,2,3");
		    """,
		    context );
		assertThat( "xxx0123xxx0123" ).isEqualTo( variables.get( result ) );
	}

}
