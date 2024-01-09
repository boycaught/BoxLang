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
package ortus.boxlang.runtime.config.segments;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ortus.boxlang.runtime.config.util.PlaceholderHelper;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Struct;

/**
 * The runtime configuration for the BoxLang runtime
 */
public class RuntimeConfig {

	/**
	 * A struct of mappings for the runtime
	 */
	public Struct				mappings			= new Struct();

	/**
	 * The directory where the modules are located by default:
	 * {@code /{user-home}/modules}
	 */
	public String				modulesDirectory	= System.getProperty( "user.home" ) + "/modules";

	/**
	 * The cache configurations for the runtime
	 */
	public Struct				caches				= new Struct();

	/**
	 * Logger
	 */
	private static final Logger	logger				= LoggerFactory.getLogger( RuntimeConfig.class );

	/**
	 * --------------------------------------------------------------------------
	 * Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Processes the configuration struct. Each segment is processed individually from the initial configuration struct.
	 *
	 * @param config the configuration struct
	 *
	 * @return the configuration
	 */
	public RuntimeConfig process( Struct config ) {

		// Process mappings
		if ( config.containsKey( "mappings" ) ) {
			if ( config.get( "mappings" ) instanceof Map<?, ?> castedMap ) {
				castedMap.forEach( ( key, value ) -> {
					this.mappings.put( ( String ) key, PlaceholderHelper.resolve( value ) );
				} );
			} else {
				logger.warn( "The [runtime.mappings] configuration is not a JSON Object, ignoring it." );
			}
		}

		// Process Modules
		if ( config.containsKey( "modulesDirectory" ) ) {
			this.modulesDirectory = PlaceholderHelper.resolve( ( String ) config.get( "modulesDirectory" ) );
		}

		// Process cache configurations
		if ( config.containsKey( "caches" ) ) {
			if ( config.get( "caches" ) instanceof Map<?, ?> castedCaches ) {
				// Process each cache configuration
				castedCaches
				    .entrySet()
				    .forEach( entry -> {
					    if ( entry.getValue() instanceof Map<?, ?> castedMap ) {
						    CacheConfig cacheConfig = new CacheConfig( ( String ) entry.getKey() ).process( new Struct( castedMap ) );
						    this.caches.put( cacheConfig.name, cacheConfig );
					    } else {
						    logger.warn( "The [runtime.caches.{}] configuration is not a JSON Object, ignoring it.", entry.getKey() );
					    }
				    } );
			} else {
				logger.warn( "The [runtime.caches] configuration is not a JSON Object, ignoring it." );
			}
		}

		return this;
	}

	/**
	 * Returns the configuration as a struct
	 * 
	 * @return Struct
	 */
	public Struct asStruct() {
		return Struct.of(
		    Key.mappings, this.mappings,
		    Key.modulesDirectory, this.modulesDirectory,
		    Key.caches, this.caches
		);
	}

}
