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

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.bifs.MemberDescriptor;
import ortus.boxlang.runtime.context.ClassBoxContext;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.IReferenceable;
import ortus.boxlang.runtime.runnables.IClassRunnable;
import ortus.boxlang.runtime.scopes.IntKey;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.IType;
import ortus.boxlang.runtime.types.exceptions.ApplicationException;
import ortus.boxlang.runtime.types.exceptions.BoxLangException;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.exceptions.KeyNotFoundException;
import ortus.boxlang.runtime.types.exceptions.NoFieldException;
import ortus.boxlang.runtime.types.exceptions.NoMethodException;
import ortus.boxlang.runtime.types.meta.BoxMeta;
import ortus.boxlang.runtime.types.meta.GenericMeta;

/**
 * This class is used to provide a way to dynamically and efficiently interact with the java layer from the within a BoxLang environment.
 *
 * This class is not in charge of casting the results. That is up to the caller to determine.
 * We basically just invoke and return the results!
 *
 * To create a new class invoker you can use the following:
 *
 * <pre>{@code
 *
 * ClassInvoker target = new ClassInvoker( String.class );
 * ClassInvoker target = ClassInvoker.of( String.class );
 * ClassInvoker target = new ClassInvoker( new String() );
 * ClassInvoker target = ClassInvoker.of( new String() );
 * }
 * </pre>
 *
 * You can then use the following methods to invoke methods on the class:
 * - {@code invokeConstructor( Object... args )} - Invoke a constructor on the class, and store the instance for future method calls
 * - {@code invokeStaticMethod( String methodName, Object... args )} - Invoke a static method on the class
 * - {@code invoke( String methodName, Object... args )} - Invoke a method on the instance of the class
 */
public class DynamicJavaInteropService {

	/**
	 * --------------------------------------------------------------------------
	 * Public Properties
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Helper for all class utility methods from apache commons lang 3
	 */
	public static final Class<ClassUtils>							CLASS_UTILS			= ClassUtils.class;

	/**
	 * --------------------------------------------------------------------------
	 * Private Properties
	 * --------------------------------------------------------------------------
	 */

	private static Set<Key>											exceptionKeys		= new HashSet<>( Arrays.asList(
	    BoxLangException.messageKey,
	    BoxLangException.detailKey,
	    BoxLangException.typeKey,
	    BoxLangException.tagContextKey,
	    ApplicationException.ExtendedInfoKey
	) );

	/**
	 * This is a map of primitive types to their native Java counterparts
	 * so we can do the right casting for primitives
	 */
	private static final Map<Class<?>, Class<?>>					PRIMITIVE_MAP;

	/**
	 * This is the method handle lookup
	 *
	 * @see https://docs.oracle.com/javase/11/docs/api/java/lang/invoke/MethodHandles.Lookup.html
	 */
	private static final MethodHandles.Lookup						METHOD_LOOKUP;

	/**
	 * This caches the method handles for the class so we don't have to look them up every time
	 */
	private static final ConcurrentHashMap<String, MethodRecord>	methodHandleCache	= new ConcurrentHashMap<>( 32 );

	/**
	 * Name of key to get length of native arrays
	 */
	private static Key												lengthKey			= Key.of( "length" );

	/**
	 * Empty arguments array
	 */
	public static final Object[]									EMPTY_ARGS			= new Object[] {};

	/**
	 * This enables or disables the method handles cache
	 */
	private static Boolean											handlesCacheEnabled	= true;

	/**
	 * Static Initializer
	 */
	static {
		METHOD_LOOKUP	= MethodHandles.lookup();
		PRIMITIVE_MAP	= Map.of(
		    Boolean.class, boolean.class,
		    Byte.class, byte.class,
		    Character.class, char.class,
		    Short.class, short.class,
		    Integer.class, int.class,
		    Long.class, long.class,
		    Float.class, float.class,
		    Double.class, double.class
		);
	}

	/**
	 * --------------------------------------------------------------------------
	 * Setters & Getters
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Verify if the handles cache is enabled or not
	 *
	 * @return the handlesCacheEnabled flag
	 */
	public static Boolean isHandlesCacheEnabled() {
		return handlesCacheEnabled;
	}

	/**
	 * @param enabled Enable or not the handles cache
	 *
	 * @return The Dynamic Object
	 */
	public static void setHandlesCacheEnabled( Boolean enabled ) {
		handlesCacheEnabled = enabled;
	}

	/**
	 * --------------------------------------------------------------------------
	 * Invokers
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Invokes the constructor for the class with the given arguments and returns the instance of the object
	 *
	 * @param targetClass The Class that you want to invoke a constructor on
	 * @param args        The arguments to pass to the constructor
	 *
	 * @return The instance of the class
	 */
	public static <T> T invokeConstructor( IBoxContext context, Class<T> targetClass, Object... args ) {

		// Thou shalt not pass!
		if ( isInterface( targetClass ) ) {
			throw new BoxRuntimeException( "Cannot invoke a constructor on an interface" );
		}

		// Unwrap any ClassInvoker instances
		unWrapArguments( args );
		// Method signature for a constructor is void (Object...)
		MethodType		constructorType	= MethodType.methodType( void.class, argumentsToClasses( args ) );
		// Define the bootstrap method
		MethodHandle	constructorHandle;
		try {
			constructorHandle = METHOD_LOOKUP.findConstructor( targetClass, constructorType );
		} catch ( NoSuchMethodException | IllegalAccessException e ) {
			throw new BoxRuntimeException( "Error getting constructor for class " + targetClass.getName(), e );
		}
		// Create a callsite using the constructor handle
		CallSite		callSite			= new ConstantCallSite( constructorHandle );
		// Bind the CallSite and invoke the constructor with the provided arguments
		// Invoke Dynamic tries to do argument coercion, so we need to convert the arguments to the right types
		MethodHandle	constructorInvoker	= callSite.dynamicInvoker();
		try {
			T thisInstance = ( T ) constructorInvoker.invokeWithArguments( args );

			// If this is a Box Class, some additional initialization is needed
			if ( thisInstance instanceof IClassRunnable cfc ) {

				// This class context is really only used while boostrapping the pseudoConstructor. It will NOT be used as a parent
				// context once the CFC is initialized. Methods called on this CFC will have access to the variables/this scope via their
				// FunctionBoxContext, but their parent context will be whatever context they are called from.
				IBoxContext classContext = new ClassBoxContext( context, cfc );

				// Bootstrap the pseudoConstructor
				cfc.pseudoConstructor( classContext );

				// Call constructor
				// TODO: look for initMethod annotation
				if ( cfc.dereference( Key.init, true ) != null ) {
					Object result = cfc.dereferenceAndInvoke( classContext, Key.init, new Object[] {}, false );
					// CF returns the actual result of the constructor, but I'm not sure it makes sense or if people actually ever
					// return anything other than "this".
					if ( result != null ) {
						// This cast will fail if the init returns something like a string
						return ( T ) result;
					}
				}
			}
			return thisInstance;
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Throwable e ) {
			throw new BoxRuntimeException( "Error invoking constructor for class " + targetClass.getName(), e );
		}
	}

	/**
	 * Invokes the no-arg constructor for the class with the given arguments and returns the instance of the object
	 *
	 * @param targetClass The Class that you want to invoke a constructor on
	 *
	 * @return The instance of the class
	 */
	public static <T> T invokeConstructor( IBoxContext context, Class<T> targetClass ) {
		return invokeConstructor( context, targetClass, EMPTY_ARGS );
	}

	/**
	 * Invoke can be used to invoke public methods on instances, or static methods on classes/interfaces.
	 *
	 * If it's determined that the method handle is static, then the target instance is ignored.
	 * If it's determined that the method handle is not static, then the target instance is used.
	 *
	 * @param targetClass The Class that you want to invoke a method on
	 * @param methodName  The name of the method to invoke
	 * @param safe        Whether the method should throw an error or return null if it doesn't exist
	 * @param arguments   The arguments to pass to the method
	 *
	 * @return The result of the method invocation
	 */
	public static Object invoke( Class<?> targetClass, String methodName, Boolean safe, Object... arguments ) {
		return invoke( targetClass, null, methodName, safe, arguments );
	}

	/**
	 * Invoke can be used to invoke public methods on instances, or static methods on classes/interfaces.
	 *
	 * If it's determined that the method handle is static, then the target instance is ignored.
	 * If it's determined that the method handle is not static, then the target instance is used.
	 *
	 * @param targetInstance The instance to call the method on
	 * @param methodName     The name of the method to invoke
	 * @param safe           Whether the method should throw an error or return null if it doesn't exist
	 * @param arguments      The arguments to pass to the method
	 *
	 * @return The result of the method invocation
	 */
	public static Object invoke( Object targetInstance, String methodName, Boolean safe, Object... arguments ) {
		return invoke( targetInstance.getClass(), targetInstance, methodName, safe, arguments );
	}

	/**
	 * Invoke can be used to invoke public methods on instances, or static methods on classes/interfaces.
	 *
	 * If it's determined that the method handle is static, then the target instance is ignored.
	 * If it's determined that the method handle is not static, then the target instance is used.
	 *
	 * @param targetClass    The Class that you want to invoke a method on
	 * @param targetInstance The instance to call the method on, or null if it's static
	 * @param safe           Whether the method should throw an error or return null if it doesn't exist
	 * @param methodName     The name of the method to invoke
	 * @param arguments      The arguments to pass to the method
	 *
	 * @return The result of the method invocation
	 */
	public static Object invoke( Class<?> targetClass, Object targetInstance, String methodName, Boolean safe, Object... arguments ) {
		// Verify method name
		if ( methodName == null || methodName.isEmpty() ) {
			throw new BoxRuntimeException( "Method name cannot be null or empty." );
		}

		// Unwrap any ClassInvoker instances
		unWrapArguments( arguments );

		// Get the invoke dynamic method handle from our cache and discovery techniques
		MethodRecord methodRecord;
		try {
			methodRecord = getMethodHandle( targetClass, methodName, argumentsToClasses( arguments ) );
		} catch ( RuntimeException e ) {
			if ( safe ) {
				return null;
			} else {
				throw new BoxRuntimeException( "Error getting method for class " + targetClass.getName(), e );
			}
		}

		// If it's not static, we need a target instance
		if ( !methodRecord.isStatic() && targetInstance == null ) {
			throw new BoxRuntimeException(
			    "You can't call invoke on a null target instance. Use [invokeStatic] instead or set the target instance manually or via the constructor."
			);
		}

		// Discover and Execute it baby!
		try {
			return methodRecord.isStatic()
			    ? methodRecord.methodHandle().invokeWithArguments( arguments )
			    : methodRecord.methodHandle().bindTo( targetInstance ).invokeWithArguments( arguments );
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Throwable e ) {
			throw new BoxRuntimeException( "Error invoking method " + methodName + " for class " + targetClass.getName(), e );
		}
	}

	/**
	 * Invokes a static method with the given name and arguments on a class or an interface
	 *
	 * @param targetClass The class you want to invoke a method on
	 * @param methodName  The name of the method to invoke
	 * @param arguments   The arguments to pass to the method
	 *
	 * @return The result of the method invocation
	 */
	public static Object invokeStatic( Class<?> targetClass, String methodName, Object... arguments ) {

		// Verify method name
		if ( methodName == null || methodName.isEmpty() ) {
			throw new BoxRuntimeException( "Method name cannot be null or empty." );
		}

		// Unwrap any ClassInvoker instances
		unWrapArguments( arguments );

		// Discover and Execute it baby!
		try {
			return getMethodHandle( targetClass, methodName, argumentsToClasses( arguments ) )
			    .methodHandle()
			    .invokeWithArguments( arguments );
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Throwable e ) {
			throw new BoxRuntimeException( "Error invoking method " + methodName + " for class " + targetClass.getName(), e );
		}
	}

	/**
	 * --------------------------------------------------------------------------
	 * Field Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Get the value of a public or public static field on a class or instance
	 *
	 * @param targetClass The class you want to look for a field on
	 * @param fieldName   The name of the field to get
	 *
	 * @return The value of the field wrapped in an Optional
	 */
	public static Optional<Object> getField( Class<?> targetClass, String fieldName ) {
		return getField( targetClass, ( Object ) null, fieldName );
	}

	/**
	 * Get the value of a public or public static field on a class or instance
	 *
	 * @param targetInstance The instance you want to look for a field on
	 * @param fieldName      The name of the field to get
	 *
	 * @return The value of the field wrapped in an Optional
	 */
	public static Optional<Object> getField( Object targetInstance, String fieldName ) {
		return getField( targetInstance.getClass(), targetInstance, fieldName );
	}

	/**
	 * Get the value of a public or public static field on a class or instance
	 *
	 * @param targetClass    The class you want to look for a field on
	 * @param targetInstance The instance you want to look for a field on
	 * @param fieldName      The name of the field to get
	 *
	 * @return The value of the field wrapped in an Optional
	 */
	public static Optional<Object> getField( Class<?> targetClass, Object targetInstance, String fieldName ) {
		// Discover the field with no case sensitivity
		Field			field	= findField( targetClass, fieldName );
		// Now get the method handle for the field to execute
		MethodHandle	fieldHandle;
		try {
			fieldHandle = METHOD_LOOKUP.unreflectGetter( field );
		} catch ( IllegalAccessException e ) {
			throw new BoxRuntimeException( "Error getting field " + fieldName + " for class " + targetClass.getName(), e );
		}
		Boolean isStatic = Modifier.isStatic( field.getModifiers() );

		// If it's not static, we need a target instance
		if ( !isStatic && targetInstance == null ) {
			throw new BoxRuntimeException(
			    "You are trying to get a public field but there is not instance set on the invoker, please make sure the [invokeConstructor] has been called."
			);
		}

		try {
			return Optional.ofNullable(
			    isStatic
			        ? fieldHandle.invoke()
			        : fieldHandle.invoke( targetInstance )
			);
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Throwable e ) {
			throw new BoxRuntimeException( "Error getting field " + fieldName + " for class " + targetClass.getName(), e );
		}
	}

	/**
	 * Get the value of a public or public static field on a class or instance but if it doesn't exist
	 * return the default value passed in.
	 *
	 * @param targetInstance The instance you want to look for a field on
	 * @param fieldName      The name of the field to get
	 * @param defaultValue   The default value to return if the field doesn't exist
	 *
	 *
	 * @return The value of the field or the default value wrapped in an Optional
	 */
	public static Optional<Object> getField( Object targetInstance, String fieldName, Object defaultValue ) {
		return getField( targetInstance.getClass(), fieldName, defaultValue );
	}

	/**
	 * Get the value of a public or public static field on a class or instance but if it doesn't exist
	 * return the default value passed in.
	 *
	 * @param targetClass  The class you want to look for a field on
	 * @param fieldName    The name of the field to get
	 * @param defaultValue The default value to return if the field doesn't exist
	 *
	 *
	 * @return The value of the field or the default value wrapped in an Optional
	 */
	public static Optional<Object> getField( Class<?> targetClass, String fieldName, Object defaultValue ) {
		return getField( targetClass, null, fieldName, defaultValue );
	}

	/**
	 * Get the value of a public or public static field on a class or instance but if it doesn't exist
	 * return the default value passed in.
	 *
	 * @param targetClass    The class you want to look for a field on
	 * @param targetInstance The instance you want to look for a field on
	 * @param fieldName      The name of the field to get
	 * @param defaultValue   The default value to return if the field doesn't exist
	 *
	 *
	 * @return The value of the field or the default value wrapped in an Optional
	 */
	public static Optional<Object> getField( Class<?> targetClass, Object targetInstance, String fieldName, Object defaultValue ) {
		try {
			return getField( targetClass, targetInstance, fieldName );
		} catch ( BoxLangException e ) {
			return Optional.ofNullable( defaultValue );
		}
	}

	/**
	 * Set the value of a public or public static field on a class or instance
	 *
	 * @param targetClass The class you want to look for a field on
	 * @param fieldName   The name of the field to set
	 * @param value       The value to set the field to
	 *
	 * @return The class invoker
	 */
	public static void setField( Class<?> targetClass, String fieldName, Object value ) {
		setField( targetClass, null, fieldName, value );
	}

	/**
	 * Set the value of a public or public static field on a class or instance
	 *
	 * @param targetInstance The instance you want to look for a field on
	 * @param fieldName      The name of the field to set
	 * @param value          The value to set the field to
	 *
	 * @return The class invoker
	 */
	public static void setField( Object targetInstance, String fieldName, Object value ) {
		setField( targetInstance.getClass(), targetInstance, fieldName, value );
	}

	/**
	 * Set the value of a public or public static field on a class or instance
	 *
	 * @param targetClass    The class you want to look for a field on
	 * @param targetInstance The instance you want to look for a field on
	 * @param fieldName      The name of the field to set
	 * @param value          The value to set the field to
	 *
	 * @return The class invoker
	 */
	public static void setField( Class<?> targetClass, Object targetInstance, String fieldName, Object value ) {
		// Discover the field with no case sensitivity
		Field			field	= findField( targetClass, fieldName );
		MethodHandle	fieldHandle;
		try {
			fieldHandle = METHOD_LOOKUP.unreflectSetter( field );
		} catch ( IllegalAccessException e ) {
			throw new BoxRuntimeException( "Error setting field " + fieldName + " for class " + targetClass.getName(), e );
		}
		Boolean isStatic = Modifier.isStatic( field.getModifiers() );

		// If it's not static, we need a target instance, verify it's not null
		if ( !isStatic && targetInstance == null ) {
			throw new BoxRuntimeException(
			    "You are trying to set a public field but there is not instance set on the invoker, please make sure the [invokeConstructor] has been called."
			);
		}

		try {
			if ( isStatic ) {
				fieldHandle.invokeWithArguments( value );
			} else {
				fieldHandle.bindTo( targetInstance ).invokeWithArguments( value );
			}
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Throwable e ) {
			throw new BoxRuntimeException( "Error setting field " + fieldName + " for class " + targetClass.getName(), e );
		}
	}

	/**
	 * Find a field by name with no case-sensitivity (upper case) in the class
	 *
	 * @param targetClass The class to find the field in
	 * @param fieldName   The name of the field to find
	 *
	 * @return The field if discovered
	 */
	public static Field findField( Class<?> targetClass, String fieldName ) {
		return getFieldsAsStream( targetClass )
		    .filter( target -> target.getName().equalsIgnoreCase( fieldName ) )
		    .findFirst()
		    .orElseThrow( () -> new NoFieldException(
		        String.format( "No such field [%s] found in the class [%s].", fieldName, targetClass.getName() )
		    ) );
	}

	/**
	 * Verifies if the class has a public or public static field with the given name
	 *
	 * @param targetClass The class to check
	 * @param fieldName   The name of the field to check
	 *
	 * @return True if the field exists, false otherwise
	 */
	public static Boolean hasField( Class<?> targetClass, String fieldName ) {
		return getFieldNames( targetClass ).contains( fieldName );
	}

	/**
	 * Verifies if the class has a public or public static field with the given name and no case-sensitivity (upper case)
	 *
	 * @param targetClass The class to check
	 * @param fieldName   The name of the field to check
	 *
	 * @return True if the field exists, false otherwise
	 */
	public static Boolean hasFieldNoCase( Class<?> targetClass, String fieldName ) {
		return getFieldNamesNoCase( targetClass ).contains( fieldName.toUpperCase() );
	}

	/**
	 * Get an array of fields of all the public fields for the given class
	 *
	 * @param targetClass The class to get the fields for
	 *
	 * @return The fields in the class
	 */
	public static Field[] getFields( Class<?> targetClass ) {
		return targetClass.getFields();
	}

	/**
	 * Get a stream of fields of all the public fields for the given class
	 *
	 * @param targetClass The class to get the fields for
	 *
	 * @return The stream of fields in the class
	 */
	public static Stream<Field> getFieldsAsStream( Class<?> targetClass ) {
		return Stream.of( getFields( targetClass ) );
	}

	/**
	 * Get a list of field names for the given class with case-sensitivity
	 *
	 * @param targetClass The class to get the fields for
	 *
	 * @return A list of field names
	 */
	public static List<String> getFieldNames( Class<?> targetClass ) {
		return getFieldsAsStream( targetClass )
		    .map( Field::getName )
		    .toList();

	}

	/**
	 * Get a list of field names for the given class with no case-sensitivity (upper case)
	 *
	 * @param targetClass The class to get the fields for
	 *
	 * @return A list of field names
	 */
	public static List<String> getFieldNamesNoCase( Class<?> targetClass ) {
		return getFieldsAsStream( targetClass )
		    .map( Field::getName )
		    .map( String::toUpperCase )
		    .toList();

	}

	/**
	 * --------------------------------------------------------------------------
	 * Helpers
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Gets the method handle for the given method name and arguments, from the cache if possible
	 * or creates a new one if not found or throws an exception if the method signature doesn't exist
	 *
	 * @param targetClass        The class to get the method handle for
	 * @param methodName         The name of the method to get the handle for
	 * @param argumentsAsClasses The array of arguments as classes to map
	 *
	 * @return The method handle representing the method signature
	 *
	 */
	public static MethodRecord getMethodHandle( Class<?> targetClass, String methodName, Class<?>[] argumentsAsClasses ) {
		// We use the method signature as the cache key
		String			cacheKey		= targetClass.hashCode() + methodName + Arrays.hashCode( argumentsAsClasses );
		MethodRecord	methodRecord	= methodHandleCache.get( cacheKey );

		// Double lock to avoid race-conditions
		if ( methodRecord == null || !handlesCacheEnabled ) {
			synchronized ( methodHandleCache ) {
				if ( methodRecord == null || !handlesCacheEnabled ) {
					methodRecord = discoverMethodHandle( targetClass, methodName, argumentsAsClasses );
					methodHandleCache.put( cacheKey, methodRecord );
				}
			}
		}

		return methodRecord;
	}

	/**
	 * Discovers the method to invoke for the given method name and arguments according to two algorithms:
	 *
	 * 1. Exact Match : Matches the incoming argument class types to the method signature
	 * 2. Discovery : Matches the incoming argument class types to the method signature by discovery of matching method names and argument counts
	 *
	 * @param targetClass        The class to discover the method for
	 * @param methodName         The name of the method to discover
	 * @param argumentsAsClasses The array of arguments as classes to map
	 *
	 * @return The method record representing the method signature and metadata
	 *
	 */
	public static MethodRecord discoverMethodHandle( Class<?> targetClass, String methodName, Class<?>[] argumentsAsClasses ) {
		// Our target we must find using our dynamic rules:
		// - case insensitivity
		// - argument count
		// - argument class asignability
		Method targetMethod = findMatchingMethod( targetClass, methodName, argumentsAsClasses );

		try {
			return new MethodRecord(
			    methodName,
			    targetMethod,
			    METHOD_LOOKUP.unreflect( targetMethod ),
			    Modifier.isStatic( targetMethod.getModifiers() ),
			    argumentsAsClasses.length
			);
		} catch ( IllegalAccessException e ) {
			throw new BoxRuntimeException( "Error getting method handle for method " + methodName + " for class " + targetClass.getName(), e );
		}
	}

	/**
	 * Get a HashSet of methods of all the unique callable method signatures for the given class
	 *
	 * @param targetClass The class to get the methods for
	 *
	 * @return A unique set of callable methods
	 */
	public static Set<Method> getMethods( Class<?> targetClass ) {
		Set<Method> allMethods = new HashSet<>();
		allMethods.addAll( new HashSet<>( List.of( targetClass.getMethods() ) ) );
		allMethods.addAll( new HashSet<>( List.of( targetClass.getDeclaredMethods() ) ) );
		return allMethods;
	}

	/**
	 * Get a stream of methods of all the unique callable method signatures for the given class
	 *
	 * @param targetClass The class to get the methods for
	 *
	 * @return A stream of unique callable methods
	 */
	public static Stream<Method> getMethodsAsStream( Class<?> targetClass ) {
		return getMethods( targetClass ).stream();
	}

	/**
	 * Get a list of method names for the given class
	 *
	 * @param targetClass The class to get the methods for
	 *
	 * @return A list of method names
	 */
	public static List<String> getMethodNames( Class<?> targetClass ) {
		return getMethodsAsStream( targetClass )
		    .parallel()
		    .map( Method::getName )
		    .toList();
	}

	/**
	 * Get a list of method names for the given class with no case-sensitivity (upper case)
	 *
	 * @param targetClass The class to get the methods for
	 *
	 * @return A list of method names with no case
	 */
	public static List<String> getMethodNamesNoCase( Class<?> targetClass ) {
		return getMethodsAsStream( targetClass )
		    .parallel()
		    .map( Method::getName )
		    .map( String::toUpperCase )
		    .toList();
	}

	/**
	 * Verifies if the class has a public or public static method with the given name
	 *
	 * @param targetClass The class to check
	 * @param methodName  The name of the method to check
	 *
	 * @return True if the method exists, false otherwise
	 */
	public static Boolean hasMethod( Class<?> targetClass, String methodName ) {
		return getMethodNames( targetClass ).contains( methodName );
	}

	/**
	 * Verifies if the class has a public or public static method with the given name and no case-sensitivity (upper case)
	 *
	 * @param targetClass The class to check
	 * @param methodName  The name of the method to check
	 *
	 * @return True if the method exists, false otherwise
	 */
	public static Boolean hasMethodNoCase( Class<?> targetClass, String methodName ) {
		return getMethodNamesNoCase( targetClass ).contains( methodName.toUpperCase() );
	}

	/**
	 * This method is used to verify if the class has the same method signature as the incoming one with no case-sensitivity (upper case)
	 *
	 * @param targetClass        The class to check
	 * @param methodName         The name of the method to check
	 * @param argumentsAsClasses The parameter types of the method to check
	 *
	 * @return The matched method signature
	 *
	 */
	public static Method findMatchingMethod( Class<?> targetClass, String methodName, Class<?>[] argumentsAsClasses ) {
		return getMethodsAsStream( targetClass )
		    .parallel()
		    .filter( method -> method.getName().equalsIgnoreCase( methodName ) )
		    .filter( method -> hasMatchingParameterTypes( method, argumentsAsClasses ) )
		    .findFirst()
		    .orElseThrow( () -> new NoMethodException(
		        String.format(
		            "No such method [%s] found in the class [%s] using [%d] arguments of types [%s]",
		            methodName,
		            targetClass.getName(),
		            argumentsAsClasses.length,
		            Arrays.toString( argumentsAsClasses )
		        )
		    ) );
	}

	/**
	 * Verifies if the method has the same parameter types as the incoming ones
	 *
	 * @see https://commons.apache.org/proper/commons-lang/javadocs/api-release/index.html
	 *
	 * @param method             The method to check
	 * @param argumentsAsClasses The arguments to check
	 *
	 * @return True if the method has the same parameter types, false otherwise
	 */
	private static boolean hasMatchingParameterTypes( Method method, Class<?>[] argumentsAsClasses ) {
		Class<?>[] methodParams = Arrays
		    .stream( method.getParameters() )
		    .map( Parameter::getType )
		    .toArray( Class<?>[]::new );

		if ( methodParams.length != argumentsAsClasses.length ) {
			return false;
		}

		// Verify assignability including primitive autoboxing
		return ClassUtils.isAssignable( argumentsAsClasses, methodParams );

		// return IntStream.range( 0, methodParameters.length )
		// .allMatch( i -> ClassUtils.isAssignable( argumentsAsClasses[ i ], methodParameters[ i ].getType() ) );
	}

	/**
	 * Utility method to convert a method to a method handle
	 *
	 * @param method The method to convert
	 *
	 * @return The method handle representing the method or an exception if it fails
	 *
	 */
	public static MethodHandle toMethodHandle( Method method ) {
		try {
			return METHOD_LOOKUP.unreflect( method );
		} catch ( IllegalAccessException e ) {
			throw new BoxRuntimeException( "Error creating MethodHandle for method [" + method + "]", e );
		}
	}

	/**
	 * Verifies if the target calss is an interface or not
	 *
	 * @param targetClass The class to check
	 *
	 * @return True if the class is an interface, false otherwise
	 */
	private static boolean isInterface( Class<?> targetClass ) {
		return targetClass.isInterface();
	}

	/**
	 * Converts the argument(s) to a class representation according to Java casting rules
	 *
	 * @param thisArg The argument to convert
	 *
	 * @return The class representation of the argument
	 */
	public static Class<?> argumentToClass( Object thisArg ) {
		// nulls are always null, why is this even a thing?
		if ( thisArg == null ) {
			return Object.class;
		}
		// If it's a primitive, we need to convert it to the wrapper class
		Class<?> clazz = thisArg.getClass();
		return PRIMITIVE_MAP.getOrDefault( clazz, clazz );
	}

	/**
	 * Converts the arguments to an array of classes
	 *
	 * @param args The arguments to convert
	 *
	 * @return The array of classes
	 */
	public static Class<?>[] argumentsToClasses( Object... args ) {
		// Convert the arguments to an array of classes
		return Arrays.stream( args )
		    .map( DynamicObject::argumentToClass )
		    // .peek( clazz -> System.out.println( "argumentToClass -> " + clazz ) )
		    .toArray( Class<?>[]::new );
	}

	/**
	 * Unwrap an object if it's inside a ClassInvoker instance
	 *
	 * @param param The object to unwrap
	 *
	 * @return The target instance or class, depending which one is set
	 */
	public static Object unWrap( Object param ) {
		if ( param instanceof DynamicObject invoker ) {
			if ( invoker.hasInstance() ) {
				return invoker.getTargetInstance();
			} else {
				return invoker.getTargetClass();
			}
		} else {
			return param;
		}
	}

	/**
	 * Unwrap any ClassInvoker instances in the arguments
	 *
	 * @param arguments The arguments to unwrap
	 *
	 * @return The unwrapped arguments
	 */
	private static void unWrapArguments( Object[] arguments ) {
		for ( int j = 0; j < arguments.length; j++ ) {
			arguments[ j ] = unWrap( arguments[ j ] );
		}
	}

	/**
	 * --------------------------------------------------------------------------
	 * Implementation of IReferencable
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Dereference this object by a key and return the value, or throw exception
	 *
	 * @param targetClass The class to dereference and look for the value on
	 * @param name        The name of the key to dereference
	 * @param safe        If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested object
	 */
	@SuppressWarnings( "unchecked" )
	public static Object dereference( Class<?> targetClass, Key name, Boolean safe ) {
		return dereference( targetClass, null, name, safe );
	}

	/**
	 * Dereference this object by a key and return the value, or throw exception
	 *
	 * @param targetInstance The instance to dereference and look for the value on
	 * @param name           The name of the key to dereference
	 * @param safe           If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested object
	 */
	@SuppressWarnings( "unchecked" )
	public static Object dereference( Object targetInstance, Key name, Boolean safe ) {
		return dereference( targetInstance.getClass(), targetInstance, name, safe );
	}

	/**
	 * Dereference this object by a key and return the value, or throw exception
	 *
	 * @param targetClass    The class to dereference and look for the value on
	 * @param targetInstance The instance to dereference and look for the value on
	 * @param name           The name of the key to dereference
	 * @param safe           If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested object
	 */
	@SuppressWarnings( "unchecked" )
	public static Object dereference( Class<?> targetClass, Object targetInstance, Key name, Boolean safe ) {

		// If the object is referencable, allow it to handle the dereference
		if ( targetInstance != null && targetInstance instanceof IReferenceable ref ) {
			return ref.dereference( name, safe );
		}

		// This check allows us to lazy-create meta for BoxLang types the first time it is requested
		if ( name.equals( BoxMeta.key ) ) {
			if ( targetInstance != null && targetInstance instanceof IType type ) {
				return type.getBoxMeta();
			}
			return new GenericMeta( targetInstance );
		}

		if ( targetInstance instanceof Map ) {
			// If it's a raw Map, then we use the original value as the key
			return ( ( Map<Object, Object> ) targetInstance ).get( name.getOriginalValue() );
			// Special logic so we can treat exceptions as referencable. Possibly move to helper
		} else if ( targetInstance instanceof List list ) {
			Integer index = Array.validateAndGetIntForDerefernce( name, list.size(), safe );
			// non-existant indexes return null when dereferncing safely
			if ( safe && ( index < 1 || index > list.size() ) ) {
				return null;
			}
			return list.get( index - 1 );
		} else if ( targetInstance != null && targetInstance.getClass().isArray() ) {
			Object[] arr = ( ( Object[] ) targetInstance );
			if ( name.equals( lengthKey ) ) {
				return arr.length;
			}

			Integer index = Array.validateAndGetIntForDerefernce( name, arr.length, safe );
			// non-existant indexes return null when dereferncing safely
			if ( safe && ( index < 1 || index > arr.length ) ) {
				return null;
			}
			return arr[ index - 1 ];
		} else if ( targetInstance instanceof Throwable t && exceptionKeys.contains( name ) ) {
			// Throwable.message always delegates through to the message field
			if ( name.equals( BoxLangException.messageKey ) ) {
				return t.getMessage();
			} else {
				// CFML returns "" for Throwable.detail, etc
				return "";
			}
			// Special logic for accessing strings as array. Possibly move to helper
		} else if ( targetInstance instanceof String s && name instanceof IntKey intKey ) {
			Integer index = Array.validateAndGetIntForDerefernce( intKey, s.length(), safe );
			// non-existant indexes return null when dereferncing safely
			if ( safe && ( index < 1 || index > s.length() ) ) {
				return null;
			}
			return s.substring( index - 1, index );
			// Special logic for native arrays. Possibly move to helper
		} else if ( hasField( targetClass, name.getName() ) ) {
			// If we have the field, return it's value, even if it's null
			return getField( targetClass, targetInstance, name.getName() ).orElse( null );
		}

		if ( safe ) {
			return null;
		}

		// Field not found anywhere
		if ( targetInstance != null ) {
			throw new KeyNotFoundException(
			    String.format( "The instance [%s] has no public field [%s]. The allowed fields are [%s]",
			        ClassUtils.getCanonicalName( targetClass ),
			        name.getName(),
			        getFieldNames( targetClass )
			    )
			);
		} else {
			throw new KeyNotFoundException(
			    String.format( "The instance [%s] has no static field [%s]. The allowed fields are [%s]",
			        ClassUtils.getCanonicalName( targetClass ),
			        name.getName(),
			        getFieldNames( targetClass )
			    )
			);
		}
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method)
	 *
	 * @param targetClass         The class to dereference and look for the invocable on
	 * @param context             The IBoxContext in which the function will be executed
	 * @param name                The name of the key to dereference, which becomes the method name
	 * @param positionalArguments The arguments to pass to the invokable
	 * @param safe                If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested return value or null
	 */
	public static Object dereferenceAndInvoke( Class<?> targetClass, IBoxContext context, Key name, Object[] positionalArguments,
	    Boolean safe ) {
		return dereferenceAndInvoke( targetClass, null, context, name, positionalArguments, safe );
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method)
	 *
	 * @param targetInstance      The instance to dereference and look for the invocable on
	 * @param context             The IBoxContext in which the function will be executed
	 * @param name                The name of the key to dereference, which becomes the method name
	 * @param positionalArguments The arguments to pass to the invokable
	 * @param safe                If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested return value or null
	 */
	public static Object dereferenceAndInvoke( Object targetInstance, IBoxContext context, Key name, Object[] positionalArguments,
	    Boolean safe ) {
		return dereferenceAndInvoke( targetInstance.getClass(), targetInstance, context, name, positionalArguments, safe );
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method)
	 *
	 * @param targetClass         The class to dereference and look for the invocable on
	 * @param targetInstance      The instance to dereference and look for the invocable on
	 * @param context             The IBoxContext in which the function will be executed
	 * @param name                The name of the key to dereference, which becomes the method name
	 * @param positionalArguments The arguments to pass to the invokable
	 * @param safe                If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested return value or null
	 */
	public static Object dereferenceAndInvoke( Class<?> targetClass, Object targetInstance, IBoxContext context, Key name, Object[] positionalArguments,
	    Boolean safe ) {

		if ( targetInstance != null && targetInstance instanceof IReferenceable ref ) {
			return ref.dereferenceAndInvoke( context, name, positionalArguments, safe );
		}

		if ( targetInstance != null ) {
			MemberDescriptor memberDescriptor = BoxRuntime.getInstance().getFunctionService().getMemberMethod( name, targetInstance );
			if ( memberDescriptor != null ) {
				return memberDescriptor.invoke( context, targetInstance, positionalArguments );
			}
		}

		if ( safe && !hasMethod( targetClass, name.getName() ) ) {
			return null;
		}

		return invoke( targetClass, targetInstance, name.getName(), safe, positionalArguments );
	}

	public static Object dereferenceAndInvoke( Class<?> targetClass, IBoxContext context, Key name, Map<Key, Object> namedArguments,
	    Boolean safe ) {
		return dereferenceAndInvoke( targetClass, null, context, name, namedArguments, safe );
	}

	public static Object dereferenceAndInvoke( Object targetInstance, IBoxContext context, Key name, Map<Key, Object> namedArguments,
	    Boolean safe ) {
		return dereferenceAndInvoke( targetInstance.getClass(), targetInstance, context, name, namedArguments, safe );
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method)
	 *
	 * @param targetClass    The class to assign the field on
	 * @param targetInstance The instance to assign the field on
	 * @param name           The name of the key to dereference, which becomes the method name
	 * @param namedArguments The arguments to pass to the invokable
	 * @param safe           If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested return value or null
	 */
	public static Object dereferenceAndInvoke( Class<?> targetClass, Object targetInstance, IBoxContext context, Key name, Map<Key, Object> namedArguments,
	    Boolean safe ) {

		if ( targetInstance != null && targetInstance instanceof IReferenceable ref ) {
			return ref.dereferenceAndInvoke( context, name, namedArguments, safe );
		}

		if ( targetInstance != null ) {
			MemberDescriptor memberDescriptor = BoxRuntime.getInstance().getFunctionService().getMemberMethod( name, targetInstance );
			if ( memberDescriptor != null ) {
				return memberDescriptor.invoke( context, targetInstance, namedArguments );
			}
		}

		throw new BoxRuntimeException( "Methods on Java objects cannot be called with named arguments" );
	}

	/**
	 * Assign a value to a field
	 *
	 * @param targetClass The class to assign the field on
	 * @param name        The name of the field to assign
	 * @param value       The value to assign
	 */
	@SuppressWarnings( "unchecked" )
	public static Object assign( Class<?> targetClass, Key name, Object value ) {
		return assign( targetClass, null, name, value );
	}

	/**
	 * Assign a value to a field
	 *
	 * @param targetInstance The instance to assign the field on
	 * @param name           The name of the field to assign
	 * @param value          The value to assign
	 */
	@SuppressWarnings( "unchecked" )
	public static Object assign( Object targetInstance, Key name, Object value ) {
		return assign( targetInstance.getClass(), targetInstance, name, value );
	}

	/**
	 * Assign a value to a field
	 *
	 * @param targetClass    The class to assign the field on
	 * @param targetInstance The instance to assign the field on
	 * @param name           The name of the field to assign
	 * @param value          The value to assign
	 */
	@SuppressWarnings( "unchecked" )
	public static Object assign( Class<?> targetClass, Object targetInstance, Key name, Object value ) {

		if ( targetInstance != null && targetInstance instanceof IReferenceable ref ) {
			return ref.assign( name, value );
		} else if ( targetInstance != null && targetInstance.getClass().isArray() ) {
			Object[]	arr		= ( ( Object[] ) targetInstance );
			Integer		index	= Array.validateAndGetIntForAssign( name, arr.length, true );
			arr[ index - 1 ] = value;
			return value;
		} else if ( targetInstance instanceof List list ) {
			Integer index = Array.validateAndGetIntForAssign( name, list.size(), false );
			if ( index > list.size() ) {
				// If the index is larger than the array, pad the array with nulls
				for ( int i = list.size(); i < index; i++ ) {
					list.add( null );
				}
			}
			list.set( index - 1, value );
			return value;
		} else if ( targetInstance instanceof Map ) {
			// If it's a raw Map, then we use the original value as the key
			( ( Map<Object, Object> ) targetInstance ).put( name.getOriginalValue(), value );
			return value;
		}

		try {
			setField( targetClass, targetInstance, name.getName(), value );
		} catch ( Throwable e ) {
			// CFML ignores Throwable.foo = "bar"
			if ( targetInstance instanceof Throwable ) {
				return value;
			}
			throw e;
		}
		return value;
	}
}
