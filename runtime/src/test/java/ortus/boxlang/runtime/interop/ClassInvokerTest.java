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
package ortus.boxlang.runtime.interop;

import ortus.boxlang.runtime.types.IType;

import java.lang.String;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.Duration;

import TestCases.interop.InvokeDynamicFields;
import TestCases.interop.PrivateConstructors;

import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClassInvokerTest {

	@DisplayName( "It can create class invokers of instances" )
	@Test
	void testItCanBeCreatedWithAnInstance() {
		ClassInvoker target = ClassInvoker.of( this );
		assertThat( target.getTargetClass() ).isEqualTo( this.getClass() );
		assertThat( target.getTargetInstance() ).isEqualTo( this );
		assertThat( target.isInterface() ).isFalse();
	}

	@DisplayName( "It can create class invokers of classes" )
	@Test
	void testItCanBeCreatedWithAClass() {
		ClassInvoker target = ClassInvoker.of( String.class );
		assertThat( target.getTargetClass() ).isEqualTo( String.class );
		assertThat( target.isInterface() ).isFalse();
	}

	@DisplayName( "It can create class invokers of interfaces" )
	@Test
	void testItCanBeCreatedWithAnInterface() {
		ClassInvoker target = new ClassInvoker( IType.class );
		assertThat( target.isInterface() ).isTrue();
	}

	@DisplayName( "It can call a constructor with one argument" )
	@Test
	void testItCanCallConstructorsWithOneArgument() throws Throwable {
		ClassInvoker target = new ClassInvoker( String.class );
		target.invokeConstructor( "Hello World" );
		assertThat( target.getTargetClass() ).isEqualTo( String.class );
		assertThat( target.getTargetInstance() ).isEqualTo( "Hello World" );
	}

	@DisplayName( "It can call a constructor with many arguments" )
	@Test
	void testItCanCallConstructorsWithManyArguments() throws Throwable {
		ClassInvoker target = new ClassInvoker( LinkedHashMap.class );
		System.out.println( int.class );
		target.invokeConstructor( 16, 0.75f, true );
		assertThat( target.getTargetClass() ).isEqualTo( LinkedHashMap.class );
	}

	@DisplayName( "It can call a constructor with no arguments" )
	@Test
	void testItCanCallConstructorsWithNoArguments() throws Throwable {
		ClassInvoker target = new ClassInvoker( String.class );
		target.invokeConstructor();
		assertThat( target.getTargetClass() ).isEqualTo( String.class );
		assertThat( target.getTargetInstance() ).isEqualTo( "" );
	}

	@DisplayName( "It can call instance methods with no arguments" )
	@Test
	void testItCanCallMethodsWithNoArguments() throws Throwable {
		ClassInvoker myMapInvoker = new ClassInvoker( HashMap.class );
		myMapInvoker.invokeConstructor();
		assertThat( myMapInvoker.invoke( "size" ).get() ).isEqualTo( 0 );
		assertThat( ( Boolean ) myMapInvoker.invoke( "isEmpty" ).get() ).isTrue();
	}

	@DisplayName( "It can call instance methods with many arguments" )
	@Test
	void testItCanCallMethodsWithManyArguments() throws Throwable {
		ClassInvoker myMapInvoker = new ClassInvoker( HashMap.class );
		myMapInvoker.invokeConstructor();
		myMapInvoker.invoke( "put", "name", "luis" );
		assertThat( myMapInvoker.invoke( "size" ).get() ).isEqualTo( 1 );
		assertThat( myMapInvoker.invoke( "get", "name" ).get() ).isEqualTo( "luis" );
	}

	@DisplayName( "It can call static methods on classes" )
	@Test
	void testItCanCallStaticMethods() throws Throwable {
		ClassInvoker	myInvoker	= ClassInvoker.of( Duration.class );
		Duration		results		= ( Duration ) myInvoker.invokeStatic( "ofSeconds", new Object[] { 120 } ).get();
		assertThat( results.toString() ).isEqualTo( "PT2M" );
	}

	@DisplayName( "It can call methods on interfaces" )
	@Test
	void testItCanCallMethodsOnInterfaces() throws Throwable {
		ClassInvoker	myInvoker	= ClassInvoker.of( List.class );
		List			results		= ( List ) myInvoker.invokeStatic( "of", new Object[] { "Hello" } ).get();
		assertThat( results.toString() ).isEqualTo( "[Hello]" );
		assertThat( results ).isNotEmpty();
	}

	@DisplayName( "It can create a class with private constructors" )
	@Test
	void testItCanCreateWithPrivateConstructors() throws Throwable {
		ClassInvoker myInvoker = ClassInvoker.of( PrivateConstructors.class );
		assertThat( myInvoker ).isNotNull();
		// Now call it via normal `invoke()`
		myInvoker.invoke( "getInstance" );
	}

	@DisplayName( "It can get public fields" )
	@Test
	void testItCanGetPublicFields() throws Throwable {
		ClassInvoker myInvoker = ClassInvoker.of( InvokeDynamicFields.class );
		myInvoker.invokeConstructor();
		assertThat( myInvoker.getField( "name" ).get() ).isEqualTo( "luis" );
	}

	@DisplayName( "It can get non-existent field with a default value" )
	@Test
	void testItCanGetPublicFieldsWithADefaultValue() throws Throwable {
		ClassInvoker myInvoker = ClassInvoker.of( InvokeDynamicFields.class );
		myInvoker.invokeConstructor();
		assertThat( myInvoker.getField( "InvalidFieldBaby", "sorry" ).get() ).isEqualTo( "sorry" );
	}

	@DisplayName( "It can get static public fields" )
	@Test
	void testItCanGetStaticPublicFields() throws Throwable {
		ClassInvoker myInvoker = ClassInvoker.of( InvokeDynamicFields.class );
		assertThat( ( String ) myInvoker.getField( "HELLO" ).get() ).isEqualTo( "Hello World" );
		assertThat( ( Integer ) myInvoker.getField( "MY_PRIMITIVE" ).get() ).isEqualTo( 42 );
	}

	@DisplayName( "It can throw an exception when getting an invalid field" )
	@Test
	void testItCanThrowExceptionForInvalidFields() {
		NoSuchFieldException exception = assertThrows( NoSuchFieldException.class, () -> {
			ClassInvoker myInvoker = ClassInvoker.of( InvokeDynamicFields.class );
			myInvoker.invokeConstructor();
			myInvoker.getField( "InvalidField" );
		} );
		assertEquals( "InvalidField", exception.getMessage() );
	}

	@DisplayName( "It can get set values on public fields" )
	@Test
	void testItCanSetPublicFields() throws Throwable {
		ClassInvoker myInvoker = ClassInvoker.of( InvokeDynamicFields.class );
		myInvoker.invokeConstructor();

		myInvoker.setField( "name", "Hola Tests" );

		assertThat( myInvoker.getField( "name" ).get() ).isEqualTo( "Hola Tests" );
	}

	@DisplayName( "It can get all the fields of a class" )
	@Test
	void testItCanGetAllFields() throws Throwable {
		ClassInvoker	myInvoker	= ClassInvoker.of( InvokeDynamicFields.class );
		Field[]			fields		= myInvoker.getFields();
		assertThat( fields ).isNotEmpty();
		assertThat( fields.length ).isEqualTo( 3 );
	}

	@DisplayName( "It can get all the field names of a class" )
	@Test
	void testItCanGetAllFieldNames() throws Throwable {
		ClassInvoker	myInvoker	= ClassInvoker.of( InvokeDynamicFields.class );
		List<String>	names		= myInvoker.getFieldNames();
		assertThat( names ).isNotEmpty();
		assertThat( names.size() ).isEqualTo( 3 );
		assertThat( names ).containsExactly( new Object[] { "name", "HELLO", "MY_PRIMITIVE" } );
	}

	@DisplayName( "It can get all the field names of a class with no case sensitivity" )
	@Test
	void testItCanGetAllFieldNamesNoCase() throws Throwable {
		ClassInvoker	myInvoker	= ClassInvoker.of( InvokeDynamicFields.class );
		List<String>	names		= myInvoker.getFieldNamesNoCase();
		assertThat( names ).isNotEmpty();
		assertThat( names.size() ).isEqualTo( 3 );
		assertThat( names ).containsExactly( new Object[] { "NAME", "HELLO", "MY_PRIMITIVE" } );

	}

	@DisplayName( "It can verify if a field with a specific name exists" )
	@Test
	void testItCanCheckForFields() throws Throwable {
		ClassInvoker myInvoker = ClassInvoker.of( InvokeDynamicFields.class );

		assertThat(
		        myInvoker.hasField( "name" )
		).isTrue();

		assertThat(
		        myInvoker.hasField( "NaMe" )
		).isFalse();
		assertThat(
		        myInvoker.hasFieldNoCase( "NaMe" )
		).isTrue();

		assertThat(
		        myInvoker.hasField( "bogus" )
		).isFalse();

	}

}
