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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.context.ScriptingBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Function.Argument;

public class ClosureTest {

	@DisplayName( "can define Closure" )
	@Test
	void testCanDefineClosure() {
		Argument[] args = new Argument[] {
		    new Function.Argument( true, "String", Key.of( "firstName" ), "brad" ),
		    new Function.Argument( true, "String", Key.of( "lastName" ), "wood" )
		};
		new SampleClosure( args, new ScriptingBoxContext(), "Brad" );

	}

	@DisplayName( "can default args" )
	@Test
	void testCanDefaultArgs() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Key			age			= Key.of( "age" );

		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, "brad" ),
		    new Function.Argument( true, "String", lastName, "wood" ),
		    new Function.Argument( false, "String", age, 43 )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure.createArgumentsScope();

		assertThat( argscope.get( firstName ) ).isEqualTo( "brad" );
		assertThat( argscope.get( lastName ) ).isEqualTo( "wood" );
		assertThat( argscope.size() ).isEqualTo( 3 );
		assertThat( argscope.get( age ) ).isEqualTo( 43 );
	}

	@DisplayName( "can process positional args" )
	@Test
	void testCanProcessPositionalArgs() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, "brad" ),
		    new Function.Argument( true, "String", lastName, "wood" )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure.createArgumentsScope( new Object[] { "Luis", "Majano", "Extra" } );

		assertThat( argscope.get( firstName ) ).isEqualTo( "Luis" );
		assertThat( argscope.get( lastName ) ).isEqualTo( "Majano" );
		assertThat( argscope.size() ).isEqualTo( 3 );
		assertThat( argscope.get( Key.of( "3" ) ) ).isEqualTo( "Extra" );
	}

	@DisplayName( "can default missing positional args" )
	@Test
	void testCanDefaultMmissingPositionalArgs() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, "brad" ),
		    new Function.Argument( true, "String", lastName, "wood" )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure.createArgumentsScope( new Object[] { "Luis" } );

		assertThat( argscope.get( firstName ) ).isEqualTo( "Luis" );
		assertThat( argscope.get( lastName ) ).isEqualTo( "wood" );
		assertThat( argscope.size() ).isEqualTo( 2 );
	}

	@DisplayName( "can process named args" )
	@Test
	void testCanProcessNamedArgs() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Key			extra		= Key.of( "extra" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, "brad" ),
		    new Function.Argument( true, "String", lastName, "wood" )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure
		    .createArgumentsScope( Map.of( firstName, "Luis", lastName, "Majano", extra, "Gavin" ) );

		assertThat( argscope.get( firstName ) ).isEqualTo( "Luis" );
		assertThat( argscope.get( lastName ) ).isEqualTo( "Majano" );
		assertThat( argscope.size() ).isEqualTo( 3 );
		assertThat( argscope.get( extra ) ).isEqualTo( "Gavin" );
	}

	@DisplayName( "can reject invalid named arg types" )
	@Test
	void testCanRejectInvalidNamedArgTypes() {
		Key			age		= Key.of( "age" );
		Argument[]	args	= new Argument[] {
		    new Function.Argument( true, "numeric", age, "sdf" )
		};
		Closure		closure	= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );

		// Explicit named arg
		assertThrows( Throwable.class, () -> closure.createArgumentsScope( Map.of( age, "sdf" ) ) );
		// Explicit positional arg
		assertThrows( Throwable.class, () -> closure.createArgumentsScope( new Object[] { "sdf" } ) );
		// Default postiional arg
		assertThrows( Throwable.class, () -> closure.createArgumentsScope() );
		// Default named arg
		assertThrows( Throwable.class, () -> closure.createArgumentsScope( Map.of() ) );
	}

	@DisplayName( "can default missing named args" )
	@Test
	void testCanDefaultMmissingNamedArgs() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, "brad" ),
		    new Function.Argument( true, "String", lastName, "wood" )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure
		    .createArgumentsScope( Map.of( firstName, "Luis" ) );

		assertThat( argscope.get( firstName ) ).isEqualTo( "Luis" );
		assertThat( argscope.get( lastName ) ).isEqualTo( "wood" );
		assertThat( argscope.size() ).isEqualTo( 2 );
	}

	@DisplayName( "can process argumentCollection" )
	@Test
	void testCanProcessArgumentCollection() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Key			extra		= Key.of( "extra" );
		Key			extraExtra	= Key.of( "extraExtra" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, "brad" ),
		    new Function.Argument( true, "String", lastName, "wood" )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure
		    .createArgumentsScope( new HashMap<Key, Object>( Map.of(
		        Function.ARGUMENT_COLLECTION, Map.of( firstName, "Luis", lastName, "Majano", extra, "Gavin" ),
		        extraExtra, "Jorge"
		    ) ) );

		assertThat( argscope.get( firstName ) ).isEqualTo( "Luis" );
		assertThat( argscope.get( lastName ) ).isEqualTo( "Majano" );
		assertThat( argscope.size() ).isEqualTo( 4 );
		assertThat( argscope.get( extra ) ).isEqualTo( "Gavin" );
		assertThat( argscope.get( extraExtra ) ).isEqualTo( "Jorge" );
	}

	@DisplayName( "can override argumentCollection with args" )
	@Test
	void testOverrideArgumentCollectionWithArgs() {
		Key			firstName	= Key.of( "firstName" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, "brad" )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure
		    .createArgumentsScope( new HashMap<Key, Object>( Map.of(
		        Function.ARGUMENT_COLLECTION, Map.of( firstName, "from collection" ),
		        firstName, "top level"
		    ) ) );

		assertThat( argscope.get( firstName ) ).isEqualTo( "top level" );
	}

	@DisplayName( "errors for required arg" )
	@Test
	void testErrorsForRequired() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );

		Argument[]	args		= new Argument[] {
		    new Function.Argument( true, "String", firstName, null ),
		    new Function.Argument( true, "String", lastName, null )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );

		assertThrows( Throwable.class, () -> closure.createArgumentsScope() );
		assertThrows( Throwable.class, () -> closure.createArgumentsScope( new Object[] { "Luis" } ) );
		assertThrows( Throwable.class, () -> closure.createArgumentsScope( Map.of( firstName, "Luis" ) ) );
	}

	@DisplayName( "can process no args" )
	@Test
	void testCanProcessNoArgs() {
		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( false, "String", firstName, "brad" ),
		    new Function.Argument( false, "String", lastName, "wood" )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );
		IScope		argscope	= closure.createArgumentsScope();

		assertThat( argscope.get( firstName ) ).isEqualTo( "brad" );
		assertThat( argscope.get( lastName ) ).isEqualTo( "wood" );
		assertThat( argscope.size() ).isEqualTo( 2 );
	}

	@DisplayName( "can get Closure metadata" )
	@Test
	void testCanGetClosureMetadata() {

		Key			firstName	= Key.of( "firstName" );
		Key			lastName	= Key.of( "lastName" );
		Argument[]	args		= new Argument[] {
		    new Function.Argument( false, "String", Key.of( "firstname" ), "brad", Struct.of( "hint", "First Name" ) ),
		    new Function.Argument( false, "String", lastName, "wood", Struct.of( "hint", "Last Name" ) )
		};
		Closure		closure		= new SampleClosure( args, new ScriptingBoxContext(), "Brad" );

		Struct		meta		= closure.getMetaData();
		assertThat( meta.dereference( Key.of( "name" ), false ) ).isEqualTo( Key.of( "closure" ) );
		assertThat( meta.dereference( Key.of( "returnType" ), false ) ).isEqualTo( "any" );
		assertThat( meta.dereference( Key.of( "output" ), false ) ).isEqualTo( false );
		assertThat( meta.dereference( Key.of( "hint" ), false ) ).isEqualTo( "" );
		assertThat( meta.dereference( Key.of( "access" ), false ) ).isEqualTo( "public" );
		assertThat( meta.dereference( Key.of( "closure" ), false ) ).isEqualTo( true );
		assertThat( meta.dereference( Key.of( "ANONYMOUSCLOSURE" ), false ) ).isEqualTo( true );
		assertThat( meta.dereference( Key.of( "lambda" ), false ) ).isEqualTo( false );
		assertThat( meta.dereference( Key.of( "ANONYMOUSLAMBDA" ), false ) ).isEqualTo( false );

		Array arguments = ( Array ) meta.dereference( Key.of( "parameters" ), false );
		assertThat( arguments.size() ).isEqualTo( 2 );

		Struct arg1 = ( Struct ) arguments.dereference( Key.of( "1" ), false );
		assertThat( arg1.dereference( Key.of( "name" ), false ) ).isEqualTo( firstName );
		assertThat( arg1.dereference( Key.of( "required" ), false ) ).isEqualTo( false );
		assertThat( arg1.dereference( Key.of( "type" ), false ) ).isEqualTo( "String" );
		assertThat( arg1.dereference( Key.of( "default" ), false ) ).isEqualTo( "brad" );
		assertThat( arg1.dereference( Key.of( "hint" ), false ) ).isEqualTo( "First Name" );

		Struct arg2 = ( Struct ) arguments.dereference( Key.of( "2" ), false );
		assertThat( arg2.dereference( Key.of( "name" ), false ) ).isEqualTo( lastName );
		assertThat( arg2.dereference( Key.of( "required" ), false ) ).isEqualTo( false );
		assertThat( arg2.dereference( Key.of( "type" ), false ) ).isEqualTo( "String" );
		assertThat( arg2.dereference( Key.of( "default" ), false ) ).isEqualTo( "wood" );
		assertThat( arg2.dereference( Key.of( "hint" ), false ) ).isEqualTo( "Last Name" );

	}

}
