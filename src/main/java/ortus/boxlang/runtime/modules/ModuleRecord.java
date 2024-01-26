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
package ortus.boxlang.runtime.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.interop.DynamicObject;
import ortus.boxlang.runtime.runnables.IClassRunnable;
import ortus.boxlang.runtime.runnables.RunnableLoader;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.ThisScope;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.services.ModuleService;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.util.EncryptionUtil;

/**
 * This class represents a module record
 */
public class ModuleRecord {

	/**
	 * Unique internal ID for the module
	 */
	public final String			id							= UUID.randomUUID().toString();

	/**
	 * The name of the module
	 */
	public Key					name;

	/**
	 * The version of the module
	 */
	public String				version						= "1.0.0";

	/**
	 * The author of the module
	 */
	public String				author						= "";

	/**
	 * The description of the module
	 */
	public String				description					= "";

	/**
	 * The web URL of the module
	 */
	public String				webURL						= "";

	/**
	 * The mapping of the module
	 */
	public String				mapping						= "";

	/**
	 * If the module is disabled for activation
	 */
	public boolean				disabled					= false;

	/**
	 * Flag to indicate if the module has been activated or not
	 */
	public boolean				activated					= false;

	/**
	 * The settings of the module
	 */
	public Struct				settings					= new Struct();

	/**
	 * The interceptors of the module
	 */
	public List<Struct>			interceptors				= List.of();

	/**
	 * The custom interception points of the module
	 */
	public List<String>			customInterceptionPoints	= List.of();

	/**
	 * The physical path of the module
	 */
	public Path					physicalPath;

	/**
	 * The physical path of the module but in string format. Used by BoxLang code mostly
	 */
	public String				modulePath;

	/**
	 * The invocation path of the module which is a composition of the
	 * {@link ModuleService#MODULE_MAPPING_PREFIX} and the module name.
	 * Example: {@code /bxModules/MyModule} is the mapping for the module
	 * the invocation path would be {@code bxModules.MyModule}
	 */
	public String				invocationPath				= "";

	/**
	 * The timestamp when the module was registered
	 */
	public Instant				registeredOn;

	/**
	 * The time it took in ms to register the module
	 */
	public long					registrationTime			= 0;

	/**
	 * The timestamp when the module was activated
	 */
	public Instant				activatedOn;

	/**
	 * The time it took in ms to activate the module
	 */
	public long					activationTime				= 0;

	/**
	 * The class loader for the module
	 */
	public ClassLoader			classLoader					= null;

	/**
	 * The descriptor for the module
	 */
	public IClassRunnable		moduleConfig;

	/**
	 * --------------------------------------------------------------------------
	 * Private Properties
	 * --------------------------------------------------------------------------
	 */

	/**
	 * This prefix is used a virtual package name for the module
	 */
	private static final String	MODULE_PACKAGE_NAME			= "ortus.boxlang.runtime.modules.";

	/**
	 * --------------------------------------------------------------------------
	 * Constructors
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Constructor
	 *
	 * @param name         The name of the module
	 * @param physicalPath The physical path of the module
	 */
	public ModuleRecord( Key name, String physicalPath ) {
		// Beautiful name
		this.name			= name;
		// Path to the module in string and Path formats
		this.modulePath		= physicalPath;
		this.physicalPath	= Paths.get( physicalPath );
		// Register the automatic mapping by convention: /bxModules/{name}
		this.mapping		= ModuleService.MODULE_MAPPING_PREFIX + name.getName();
		// Register the invocation path by convention: bxModules.{name}
		this.invocationPath	= ModuleService.MODULE_MAPPING_INVOCATION_PREFIX + name.getName();
	}

	/**
	 * --------------------------------------------------------------------------
	 * Loaders
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Load the ModuleConfig.bx from disk, construct it and store it
	 * Then populate the module record with the information from the descriptor
	 *
	 * @param context The current context of execution
	 *
	 * @return The ModuleRecord
	 */
	public ModuleRecord loadDescriptor( IBoxContext context ) {
		Path	descriptorPath	= physicalPath.resolve( ModuleService.MODULE_DESCRIPTOR );
		String	packageName		= MODULE_PACKAGE_NAME + this.name.getNameNoCase() + EncryptionUtil.hash( Instant.now() + id + physicalPath.toString() );

		// Load the Class, Construct it and store it
		this.moduleConfig = ( IClassRunnable ) DynamicObject.of(
		    RunnableLoader.getInstance().loadClass( descriptorPath, packageName, context )
		).invokeConstructor( context )
		    .getTargetInstance();

		// Nice References
		ThisScope		thisScope		= this.moduleConfig.getThisScope();
		VariablesScope	variablesScope	= this.moduleConfig.getVariablesScope();

		// Store the descriptor information
		this.version		= ( String ) thisScope.getOrDefault( Key.version, "1.0.0" );
		this.author			= ( String ) thisScope.getOrDefault( Key.author, "" );
		this.description	= ( String ) thisScope.getOrDefault( Key.description, "" );
		this.webURL			= ( String ) thisScope.getOrDefault( Key.webURL, "" );
		this.disabled		= ( Boolean ) thisScope.getOrDefault( Key.disabled, false );

		// Do we have a custom mapping to override?
		if ( thisScope.containsKey( Key.mapping ) &&
		    thisScope.get( Key.mapping ) instanceof String castedMapping &&
		    !castedMapping.isBlank() ) {
			this.mapping		= ModuleService.MODULE_MAPPING_PREFIX + castedMapping;
			this.invocationPath	= ModuleService.MODULE_MAPPING_INVOCATION_PREFIX + castedMapping;
		}

		// Verify the internal config structures exist, else default them
		if ( !variablesScope.containsKey( Key.settings ) ) {
			variablesScope.put( Key.settings, new Struct() );
		}
		if ( !variablesScope.containsKey( Key.interceptors ) ) {
			variablesScope.put( Key.interceptors, Array.of() );
		}
		if ( !variablesScope.containsKey( Key.customInterceptionPoints ) ) {
			variablesScope.put( Key.customInterceptionPoints, Array.of() );
		}

		// DI: Incorporate a few injections into the variables scope
		// variablesScope.put( Key.moduleRecord, this );
		// variablesScope.put( Key.boxRuntime, BoxRuntime.getInstance() );

		this.registeredOn = Instant.now();

		return this;
	}

	/**
	 * --------------------------------------------------------------------------
	 * Getters
	 * --------------------------------------------------------------------------
	 */

	/**
	 * If the module is disabled for activation
	 *
	 * @return {@code true} if the module is disabled for activation, {@code false} otherwise
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * If the module is activated
	 *
	 * @return {@code true} if the module is activated, {@code false} otherwise
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * Get a string representation of the module record
	 */
	public String toString() {
		return asStruct().toString();
	}

	/**
	 * Get a struct representation of the module record
	 *
	 * @return A struct representation of the module record
	 */
	public IStruct asStruct() {
		return Struct.of(
		    "activatedOn", activatedOn,
		    "activationTime", activationTime,
		    "activated", activated,
		    "author", author,
		    "customInterceptionPoints", Array.copyOf( customInterceptionPoints ),
		    "description", description,
		    "disabled", disabled,
		    "Id", id,
		    "interceptors", Array.copyOf( interceptors ),
		    "invocationPath", invocationPath,
		    "mapping", mapping,
		    "modulePath", modulePath,
		    "name", name,
		    "physicalPath", physicalPath.toString(),
		    "registeredOn", registeredOn,
		    "registrationTime", registrationTime,
		    "settings", settings,
		    "version", version,
		    "webURL", webURL
		);
	}

}
