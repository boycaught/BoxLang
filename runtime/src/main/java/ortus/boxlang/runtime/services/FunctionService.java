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
package ortus.boxlang.runtime.services;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ortus.boxlang.runtime.events.InterceptorState;
import ortus.boxlang.runtime.functions.FunctionNamespace;
import ortus.boxlang.runtime.functions.BIF;
import ortus.boxlang.runtime.functions.FunctionDescriptor;
import ortus.boxlang.runtime.interop.DynamicObject;
import ortus.boxlang.runtime.loader.IClassResolver;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.KeyNotFoundException;
import ortus.boxlang.runtime.util.ClassDiscovery;
import ortus.boxlang.runtime.util.Timer;

/**
 * The {@code FunctionService} is in charge of managing the runtime's built-in functions.
 * It will also be used by the module services to register functions.
 */
public class FunctionService extends BaseService {

	/**
	 * --------------------------------------------------------------------------
	 * Private Properties
	 * --------------------------------------------------------------------------
	 */

	private static final String					FUNCTIONS_PACKAGE	= "ortus.boxlang.runtime.functions";

	/**
	 * Logger
	 */
	private static final Logger					logger				= LoggerFactory.getLogger( FunctionService.class );

	/**
	 * Singleton instance
	 */
	private static FunctionService				instance;

	/**
	 * The set of global functions registered with the service
	 */
	private static Map<Key, FunctionDescriptor>	globalFunctions		= new ConcurrentHashMap<>();

	/**
	 * The set of namespaced functions registered with the service
	 */
	private static Map<Key, FunctionNamespace>	namespaces			= new ConcurrentHashMap<>();

	/**
	 * --------------------------------------------------------------------------
	 * Constructor(s)
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Constructor
	 */
	private FunctionService() {
	}

	/**
	 * Get an instance of the service
	 *
	 * @return The singleton instance
	 */
	public static synchronized FunctionService getInstance() {
		if ( instance == null ) {
			instance = new FunctionService();
		}
		return instance;
	}

	/**
	 * --------------------------------------------------------------------------
	 * Runtime Service Event Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * The startup event is fired when the runtime starts up
	 */
	public static void onStartup() {
		logger.info( "FunctionService.onStartup()" );
		// Load global functions
		try {
			loadGlobalFunctions();
		} catch ( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Cannot load global functions", e );
		}
	}

	/**
	 * The configuration load event is fired when the runtime loads its configuration
	 */
	public static void onConfigurationLoad() {
		logger.info( "FunctionService.onConfigurationLoad()" );
	}

	/**
	 * The shutdown event is fired when the runtime shuts down
	 */
	public static void onShutdown() {
		logger.info( "FunctionService.onShutdown()" );
	}

	/**
	 * --------------------------------------------------------------------------
	 * Global Function Methods
	 * --------------------------------------------------------------------------
	 */

	public static long getGlobalFunctionCount() {
		return globalFunctions.size();
	}

	public static Set<String> getGlobalFunctionNames() {
		return globalFunctions.keySet().stream().map( Key::getName ).collect( Collectors.toSet() );
	}

	public static Boolean hasGlobalFunction( String name ) {
		return globalFunctions.containsKey( Key.of( name ) );
	}

	public static FunctionDescriptor getGlobalFunction( String name ) throws KeyNotFoundException {
		FunctionDescriptor target = globalFunctions.get( Key.of( name ) );
		if ( target == null ) {
			throw new KeyNotFoundException(
			    String.format(
			        "The global function [%s] does not exist.",
			        name
			    ) );
		}
		return target;
	}

	public static FunctionDescriptor getGlobalFunctionDescriptor( String name ) {
		return globalFunctions.get( Key.of( name ) );
	}

	public static void registerGlobalFunction( FunctionDescriptor descriptor ) throws IllegalArgumentException {
		if ( hasGlobalFunction( descriptor.name ) ) {
			throw new IllegalArgumentException( "Global function " + descriptor.name + " already exists" );
		}
		globalFunctions.put( Key.of( descriptor.name ), descriptor );
	}

	public static void registerGlobalFunction( String name, BIF function, String module ) throws IllegalArgumentException {
		if ( hasGlobalFunction( name ) ) {
			throw new IllegalArgumentException( "Global function " + name + " already exists" );
		}

		globalFunctions.put(
		    Key.of( name ),
		    new FunctionDescriptor(
		        name,
		        ClassUtils.getCanonicalName( function.getClass() ),
		        module,
		        null,
		        true,
		        DynamicObject.of( function )
		    )
		);
	}

	public static void unregisterGlobalFunction( String name ) {
		globalFunctions.remove( Key.of( name ) );
	}

	public static void loadGlobalFunctions() throws IOException {
		globalFunctions = ClassDiscovery
		    .getClassFilesAsStream( FUNCTIONS_PACKAGE + ".global" )
		    .collect(
		        Collectors.toConcurrentMap(
		            value -> Key.of( ClassUtils.getShortClassName( value ) ),
		            value -> new FunctionDescriptor(
		                ClassUtils.getShortClassName( value ),
		                value,
		                null,
		                null,
		                true,
		                null
		            )
		        )
		    );
	}

	public static long getNamespaceCount() {
		return namespaces.size();
	}
}
