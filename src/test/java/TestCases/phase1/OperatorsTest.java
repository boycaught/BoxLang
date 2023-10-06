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
package TestCases.phase1;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingBoxContext;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class OperatorsTest {

	static BoxRuntime	instance;
	static IBoxContext	context;

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingBoxContext( instance.getRuntimeContext() );
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		context.getScopeNearby( VariablesScope.name ).clear();
	}

	@DisplayName( "string concat" )
	@Test
	public void testStringConcat() {
		Object result = instance.executeStatement( "'brad' & 'wood'", context );
		assertThat( result ).isEqualTo( "bradwood" );

	}

	@DisplayName( "string contains" )
	@Test
	public void testStringContains() {
		Object result = instance.executeStatement( "\"Brad Wood\" contains \"Wood\"", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "\"Brad Wood\" contains \"luis\"", context );
		assertThat( result ).isEqualTo( false );

		result = instance.executeStatement( "\"Brad Wood\" DOES NOT CONTAIN \"Luis\"", context );
		assertThat( result ).isEqualTo( true );
	}

	@DisplayName( "math addition" )
	@Test
	public void testMathAddition() {
		Object result = instance.executeStatement( "5+5", context );
		assertThat( result ).isEqualTo( 10 );

		result = instance.executeStatement( "'5'+'2'", context );
		assertThat( result ).isEqualTo( 7 );
	}

	@DisplayName( "math subtraction" )
	@Test
	public void testMathSubtraction() {
		Object result = instance.executeStatement( "6-5", context );
		assertThat( result ).isEqualTo( 1 );
	}

	@DisplayName( "math negation" )
	@Test
	public void testMathNegation() {
		Object result = instance.executeStatement( "-5", context );
		assertThat( result ).isEqualTo( -5 );

		result = instance.executeStatement( "-(5+5)", context );
		assertThat( result ).isEqualTo( -10 );

		// Operator precedence is probably wrong
		result = instance.executeStatement( "-5+5", context );
		assertThat( result ).isEqualTo( 0 );
	}

	@DisplayName( "math division" )
	@Test
	public void testMathDivision() {
		Object result = instance.executeStatement( "10/5", context );
		assertThat( result ).isEqualTo( 2 );
	}

	@DisplayName( "math int dividsion" )
	@Test
	public void testMathIntDivision() {
		Object result = instance.executeStatement( "10\\3", context );
		assertThat( result ).isEqualTo( 3 );
	}

	@DisplayName( "math multiplication" )
	@Test
	public void testMathMultiplication() {
		Object result = instance.executeStatement( "10*5", context );
		assertThat( result ).isEqualTo( 50 );
	}

	@DisplayName( "math power" )
	@Test
	public void testMathPower() {
		Object result = instance.executeStatement( "2^3", context );
		assertThat( result ).isEqualTo( 8 );
	}

	@DisplayName( "logical and" )
	@Test
	public void testLogicalAnd() {
		Object result = instance.executeStatement( "true and true", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "true && true", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "true and false", context );
		assertThat( result ).isEqualTo( false );

		result = instance.executeStatement( "true && false", context );
		assertThat( result ).isEqualTo( false );
	}

	@DisplayName( "logical or" )
	@Test
	public void testLogicalOr() {
		Object result = instance.executeStatement( "true or true", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "true || true", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "true or false", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "true || false", context );
		assertThat( result ).isEqualTo( true );
	}

	@DisplayName( "logical not" )
	@Test
	public void testLogicalNot() {
		Object result = instance.executeStatement( "!true", context );
		assertThat( result ).isEqualTo( false );

		result = instance.executeStatement( "!false", context );
		assertThat( result ).isEqualTo( true );
	}

	@DisplayName( "logical xor" )
	@Test
	public void testLogicalXOR() {
		// Failing due to case of class. Acronyms are always upper case, so it's `XOR.invoke()`
		Object result = instance.executeStatement( "true xor false", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "true xor true", context );
		assertThat( result ).isEqualTo( false );

		result = instance.executeStatement( "false xor false", context );
		assertThat( result ).isEqualTo( false );
	}

	@DisplayName( "elvis" )
	@Test
	public void testElvis() {

		instance.executeSource(
		    """
		    tmp = "brad"
		    result = tmp ?: 'default'
		    """,
		    context );
		assertThat( context.getScopeNearby( VariablesScope.name ).dereference( Key.of( "result" ), false ) ).isEqualTo( "brad" );

		Object result = instance.executeStatement( "null ?: 'default'", context );
		assertThat( result ).isEqualTo( "default" );

		result = instance.executeStatement( "foo ?: 'default'", context );
		assertThat( result ).isEqualTo( "default" );

		result = instance.executeStatement( "foo.bar.baz ?: 'default'", context );
		assertThat( result ).isEqualTo( "default" );

		result = instance.executeStatement( "foo['bar'] ?: 'default'", context );
		assertThat( result ).isEqualTo( "default" );

		result = instance.executeStatement( "foo['bar'].baz ?: 'default'", context );
		assertThat( result ).isEqualTo( "default" );

	}

	@DisplayName( "ternary" )
	@Test
	public void testTernary() {

		Object result = instance.executeStatement( "true ? 'itwastrue' : 'itwasfalse'", context );
		assertThat( result ).isEqualTo( "itwastrue" );

		result = instance.executeStatement( "FALSE ? 'itwastrue' : 'itwasfalse'", context );
		assertThat( result ).isEqualTo( "itwasfalse" );

		// scoped tmp lookup is failing, technically no issue with ternary
		instance.executeSource(
		    """
		    tmp = true;
		    result = tmp ? 'itwastrue' : 'itwasfalse'
		    """,
		    context );
		assertThat( context.getScopeNearby( VariablesScope.name ).dereference( Key.of( "result" ), false ) ).isEqualTo( "itwastrue" );

	}

	@DisplayName( "instanceOf" )
	@Test
	public void testInstanceOf() {

		Object result = instance.executeStatement( "true instanceOf 'Boolean'", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "'brad' instanceOf 'java.lang.String'", context );
		assertThat( result ).isEqualTo( true );

	}

	@DisplayName( "castAs" )
	@Test
	public void testCastAs() {

		// variable sdf should not exist, therefore an error needs to be thrown
		assertThrows( Throwable.class, () -> instance.executeStatement( "5 castAs sdf", context ) );

		// castAs keyword doesn't seem to be implemented-- the parser is just directly returning the 5 as a literal
		Object result = instance.executeStatement( "5 castAs 'String'", context );
		assertThat( result ).isEqualTo( "5" );
		assertThat( result.getClass().getName() ).isEqualTo( "java.lang.String" );
	}

	@DisplayName( "assert" )
	@Test
	public void testAssert() {

		Object result = instance.executeStatement( "assert true", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "assert 5==5", context );
		assertThat( result ).isEqualTo( true );

		assertThrows( Throwable.class, () -> instance.executeStatement( "assert 5==6", context ) );

	}

	@DisplayName( "comparison equality" )
	@Test
	public void testComparisonEquality() {

		Object result = instance.executeStatement( "5==5", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "'5'==5", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "'brad'=='brad'", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "'brad'==5", context );
		assertThat( result ).isEqualTo( false );

	}

	@DisplayName( "comparison strict equality" )
	@Test
	public void testComparisonStrictEquality() {

		Object result = instance.executeStatement( "5===5", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "'5'===5", context );
		assertThat( result ).isEqualTo( false );

		result = instance.executeStatement( "'brad'==='brad'", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "'brad'===5", context );
		assertThat( result ).isEqualTo( false );

	}

	@DisplayName( "comparison greater than" )
	@Test
	public void testGreaterThan() {

		Object result = instance.executeStatement( "6 > 5", context );
		assertThat( result ).isEqualTo( true );

		// not implemented
		result	= instance.executeStatement( "6 GREATER THAN 5", context );

		result	= instance.executeStatement( "'B' > 'A'", context );
		assertThat( result ).isEqualTo( true );

		// not implemented
		result = instance.executeStatement( "'B' greater than 'A'", context );
		assertThat( result ).isEqualTo( true );
	}

	@DisplayName( "comparison greater than equal" )
	@Test
	public void testGreaterThanEqual() {

		Object result = instance.executeStatement( "10 >= 5", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "10 GTE 5", context );
		assertThat( result ).isEqualTo( true );

		// not implemented
		result = instance.executeStatement( "10 GREATER THAN OR EQUAL TO 5", context );
		assertThat( result ).isEqualTo( true );

		// not implemented
		result = instance.executeStatement( "10 GE 5", context );
		assertThat( result ).isEqualTo( true );
	}

	@DisplayName( "comparison less than" )
	@Test
	public void testLessThan() {

		Object result = instance.executeStatement( "5 < 10", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "5 LT 10", context );
		assertThat( result ).isEqualTo( true );

		// not implemented
		result = instance.executeStatement( "5 LESS THAN 10", context );
		assertThat( result ).isEqualTo( true );

	}

	@DisplayName( "comparison less than equal" )
	@Test
	public void testLessThanEqual() {

		Object result = instance.executeStatement( "5 <= 10", context );
		assertThat( result ).isEqualTo( true );

		result = instance.executeStatement( "5 LTE 10", context );
		assertThat( result ).isEqualTo( true );

		// not implemented
		result = instance.executeStatement( "5 LESS THAN OR EQUAL TO 10", context );
		assertThat( result ).isEqualTo( true );

		// not implemented
		result = instance.executeStatement( "5 LE 10", context );
		assertThat( result ).isEqualTo( true );

	}

}
