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
package ortus.boxlang.runtime.bifs.global.system;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ortus.boxlang.compiler.parser.BoxSourceType;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.ContainerBoxContext;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.runnables.IClassRunnable;
import ortus.boxlang.runtime.runnables.ITemplateRunnable;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.DateTime;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.IType;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.exceptions.ExceptionUtil;

@BoxBIF
@BoxBIF( alias = "writeDump" )
public class Dump extends BIF {

	private static final ThreadLocal<Set<Integer>>		dumpedObjects		= ThreadLocal.withInitial( HashSet::new );

	private static final ConcurrentMap<String, String>	dumpTemplateCache	= new ConcurrentHashMap<>();

	BoxRuntime											runtime				= BoxRuntime.getInstance();

	/**
	 * Constructor
	 */
	public Dump() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "any", Key.var )
			// TODO:
			// output
			// format
			// abort
			// label
			// metainfo
			// top
			// show
			// hide
			// keys
			// expand
			// showUDFs
		};
	}

	/**
	 * Pretty print a variable to the buffer
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.var The variable to dump
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		String	posInCode			= "";
		Key		posInCodeKey		= Key.of( "posInCode" );
		String	templateBasePath	= "/dump/html/";
		Object	target				= arguments.get( Key.var );
		String	dumpTemplate		= null;
		String	name				= "Class.bxm";

		if ( target == null ) {
			name = "Null.bxm";
		} else if ( target instanceof Throwable ) {
			name = "Throwable.bxm";
		} else if ( target instanceof IScope ) {
			name = "Struct.bxm";
		} else if ( target instanceof Key ) {
			name = "Key.bxm";
		} else if ( target instanceof DateTime ) {
			name = "DateTime.bxm";
		} else if ( target instanceof IClassRunnable ) {
			name = "BoxClass.bxm";
		} else if ( target instanceof ITemplateRunnable itr ) {
			target	= itr.getRunnablePath();
			name	= "ITemplateRunnable.bxm";
		} else if ( target instanceof IStruct ) {
			name = "Struct.bxm";
		} else if ( target instanceof IType ) {
			name = target.getClass().getSimpleName() + ".bxm";
		} else if ( target instanceof String ) {
			name = "String.bxm";
		} else if ( target instanceof Number ) {
			name = "Number.bxm";
		} else if ( target instanceof Boolean ) {
			name = "Boolean.bxm";
		} else if ( target.getClass().isArray() ) {
			name = "NativeArray.bxm";
		}
		// Get the set of dumped objects for this thread
		Set<Integer>	dumped			= dumpedObjects.get();
		boolean			outerDump		= dumped.isEmpty();
		Integer			thisHashCode	= System.identityHashCode( target );
		if ( !dumped.add( thisHashCode ) ) {
			// The target object has already been dumped in this thread, so return to prevent recursion
			// TODO: Move to template
			context.writeToBuffer( "<div>Recursive reference</div>" );
			return null;
		}
		try {

			dumpTemplate = getDumpTemplate( templateBasePath + name, templateBasePath );

			// Just using this so I can have my own variables scope to use.
			IBoxContext dumpContext = new ContainerBoxContext( context );
			// This is expensive, so only do it on the outer dump
			if ( outerDump ) {
				Array tagContext = ExceptionUtil.getTagContext( 1 );
				if ( tagContext.size() > 0 ) {
					IStruct thisTag = ( IStruct ) tagContext.get( 0 );
					posInCode = thisTag.getAsString( Key.template ) + ":" + thisTag.get( Key.line );

				}
			}
			dumpContext.getScopeNearby( VariablesScope.name ).put( posInCodeKey, posInCode );
			dumpContext.getScopeNearby( VariablesScope.name ).put( Key.var, target );
			runtime.executeSource( dumpTemplate, dumpContext, BoxSourceType.BOXTEMPLATE );
		} finally {
			dumped.remove( thisHashCode );
			if ( outerDump ) {
				dumpedObjects.remove();
			}
		}
		return null;
	}

	private String getDumpTemplate( String dumpTemplatePath, String templateBasePath ) {
		return dumpTemplateCache.computeIfAbsent( dumpTemplatePath, key -> computeDumpTemplate( key, templateBasePath ) );
	}

	private String computeDumpTemplate( String dumpTemplatePath, String templateBasePath ) {
		InputStream	dumpTemplate	= null;
		URL			url				= this.getClass().getResource( "" );
		boolean		runningFromJar	= url.getProtocol().equals( "jar" );

		if ( runningFromJar ) {
			dumpTemplate = this.getClass().getResourceAsStream( dumpTemplatePath );
		} else {
			Path filePath = Path.of( "src/main/resources" + dumpTemplatePath );
			if ( Files.exists( filePath ) ) {
				try {
					dumpTemplate = Files.newInputStream( filePath );
				} catch ( IOException e ) {
					throw new BoxRuntimeException( dumpTemplatePath + " not found", e );
				}
			}
		}

		if ( dumpTemplate == null ) {
			dumpTemplatePath = templateBasePath + "Class.bxm";

			if ( runningFromJar ) {
				dumpTemplate = this.getClass().getResourceAsStream( dumpTemplatePath );
			} else {
				Path templatePath = Path.of( "src/main/resources" + dumpTemplatePath );

				if ( Files.exists( templatePath ) ) {
					try {
						dumpTemplate = Files.newInputStream( templatePath );
					} catch ( IOException e ) {
						throw new BoxRuntimeException( dumpTemplatePath + " not found", e );
					}
				}
			}
		}

		if ( dumpTemplate == null ) {
			throw new BoxRuntimeException( "Could not load dump template: " + dumpTemplatePath );
		}

		try ( Scanner s = new Scanner( dumpTemplate ).useDelimiter( "\\A" ) ) {
			return s.hasNext() ? s.next() : "";
		}
	}
}
