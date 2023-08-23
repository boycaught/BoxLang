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
package ortus.boxlang.runtime.dynamic;

import ortus.boxlang.runtime.types.*;
import ortus.boxlang.runtime.types.exceptions.KeyNotFoundException;
import ortus.boxlang.runtime.dynamic.Referencer;
import ortus.boxlang.runtime.scopes.*;

import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

public class ReferencerTest {

	@DisplayName( "It can assign to a scope" )
	@Test
	void testItCanAssignToAScope() {
		Key		key				= Key.of( "brad" );
		IScope	variablesScope	= new VariablesScope();
		Referencer.set( variablesScope, key, "Wood" );
		assertThat( variablesScope.get( key ) ).isEqualTo( "Wood" );
		assertThat( variablesScope.get( Key.of( "BRAD" ) ) ).isEqualTo( "Wood" );
	}

	@DisplayName( "It can dereference from a scope" )
	@Test
	void testItCanDereferenceFromAScope() {
		Key		key				= Key.of( "brad" );
		IScope	variablesScope	= new VariablesScope();
		variablesScope.put( key, "Wood" );
		assertThat( Referencer.get( variablesScope, key, false ) ).isEqualTo( "Wood" );
		assertThrows( KeyNotFoundException.class, () -> Referencer.get( variablesScope, Key.of( "nonExistent" ), false ) );
	}

	@DisplayName( "It can safely dereference from a scope" )
	@Test
	void testItCanSafelyDereferenceFromAScope() {
		Key		key				= Key.of( "brad" );
		IScope	variablesScope	= new VariablesScope();
		variablesScope.put( key, "Wood" );
		assertThat( Referencer.get( variablesScope, key, true ) ).isEqualTo( "Wood" );
		assertThat( Referencer.get( variablesScope, Key.of( "nonExistent" ), true ) ).isNull();
		assertThat( Referencer.get( null, Key.of( "doesn't matter" ), true ) ).isNull();
	}

	@DisplayName( "It can assign to a struct" )
	@Test
	void testItCanAssignToAStruct() {
		Key		key		= Key.of( "brad" );
		Struct	struct	= new Struct();
		Referencer.set( struct, key, "Wood" );
		assertThat( struct.get( key ) ).isEqualTo( "Wood" );
		assertThat( struct.get( Key.of( "BRAD" ) ) ).isEqualTo( "Wood" );
	}

	@DisplayName( "It can assign deeply" )
	@Test
	void testItCanAssignDeeply() {
		IScope	scope	= new VariablesScope();
		Key		foo		= Key.of( "foo" );
		Key		bar		= Key.of( "bar" );
		Key		baz		= Key.of( "baz" );

		Referencer.setDeep( scope, true, foo, bar, baz );

		assertThat( scope.get( foo ) instanceof Map ).isTrue();
		assertThat( ( ( Map ) scope.get( foo ) ).get( "bar" ) instanceof Map ).isTrue();
		assertThat( ( ( Map ) ( ( Map ) scope.get( foo ) ).get( "bar" ) ).get( "baz" ) ).isEqualTo( true );
	}

	@DisplayName( "It can dereference from a struct" )
	@Test
	void testItCanDereferenceFromAStruct() {
		Key		key		= Key.of( "brad" );
		Struct	struct	= new Struct();
		struct.put( key, "Wood" );
		assertThat( Referencer.get( struct, key, false ) ).isEqualTo( "Wood" );
		assertThrows( KeyNotFoundException.class, () -> Referencer.get( struct, Key.of( "nonExistent" ), false ) );
	}

	@DisplayName( "It can safely dereference from a struct" )
	@Test
	void testItCanSafelyDereferenceFromAStruct() {
		Key		key		= Key.of( "brad" );
		Struct	struct	= new Struct();
		struct.put( key, "Wood" );
		assertThat( Referencer.get( struct, key, true ) ).isEqualTo( "Wood" );
		assertThat( Referencer.get( struct, Key.of( "nonExistent" ), true ) ).isNull();
		assertThat( Referencer.get( null, Key.of( "doesn't matter" ), true ) ).isNull();
	}

}
