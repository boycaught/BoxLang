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
package ortus.boxlang.runtime.components.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.BoxComponent;
import ortus.boxlang.runtime.components.Component;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.ExpressionInterpreter;
import ortus.boxlang.runtime.dynamic.casters.CastAttempt;
import ortus.boxlang.runtime.dynamic.casters.GenericCaster;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.operators.EqualsEquals;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Query;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.util.ListUtil;
import ortus.boxlang.runtime.validation.Validator;

@BoxComponent( requiresBody = true )
public class Loop extends Component {

	/**
	 * --------------------------------------------------------------------------
	 * Constructor(s)
	 * --------------------------------------------------------------------------
	 */

	public Loop() {
		super();
		declaredAttributes = new Attribute[] {
		    new Attribute( Key.array, "array" ),
		    new Attribute( Key.item, "string", Set.of( Validator.NON_EMPTY ) ),
		    new Attribute( Key.index, "string", Set.of( Validator.NON_EMPTY ) ),
		    // I think dates are allowed here, so numeric may be too strict
		    new Attribute( Key.to, "numeric", Set.of( Validator.requires( Key.index ) ) ),
		    new Attribute( Key.from, "numeric" )
			/*
			 * 
			 * 
			 * step
			 * query
			 * condition
			 * group
			 * groupCaseSensitive
			 * startRow
			 * endrow
			 * list
			 * delimiters
			 * collection
			 * item
			 * array
			 * characters
			 * file
			 * times
			 */
		};
	}

	/**
	 * Different items are required based on loop type. Items listed as required may not be depending on your loop type. Loop forms: [query] [condition]
	 * [index + from + to ] [index + list] [collection + item ]
	 * *
	 *
	 * @param context        The context in which the Component is being invoked
	 * @param attributes     The attributes to the Component
	 * @param body           The body of the Component
	 * @param executionState The execution state of the Component
	 *
	 */
	public BodyResult _invoke( IBoxContext context, IStruct attributes, ComponentBody body, IStruct executionState ) {
		Array	array	= attributes.getAsArray( Key.array );
		String	item	= attributes.getAsString( Key.item );
		String	index	= attributes.getAsString( Key.index );
		Double	to		= attributes.getAsDouble( Key.to );
		Double	from	= attributes.getAsDouble( Key.from );

		if ( array != null ) {
			return _invokeArray( context, array, item, index, body, executionState );
		}
		if ( to != null && from != null ) {
			return _invokeRange( context, from, to, index, body, executionState );
		}
		throw new BoxRuntimeException( "CFLoop attributes not implemented yet! " + attributes.asString() );
		// return DEFAULT_RETURN;
	}

	private BodyResult _invokeRange( IBoxContext context, Double from, Double to, String index, ComponentBody body, IStruct executionState ) {
		// Loop over array, executing body every time
		for ( int i = from.intValue(); i <= to.intValue(); i++ ) {
			// Set the index and item variables
			ExpressionInterpreter.setVariable( context, index, i );
			// Run the code inside of the output loop
			BodyResult bodyResult = processBody( context, body );
			// IF there was a return statement inside our body, we early exit now
			if ( bodyResult.isEarlyExit() ) {
				if ( bodyResult.isContinue() ) {
					continue;
				} else if ( bodyResult.isBreak() ) {
					break;
				} else {
					return bodyResult;
				}
			}
		}
		return DEFAULT_RETURN;
	}

	/**
	 * Loop over array with optional item and index
	 *
	 * @param context        The context in which the Component is being invoked
	 * @param array          The array to loop over
	 * @param item           The name of the variable to hold the current item
	 * @param index          The name of the variable to hold the current index
	 * @param body           The body of the Component
	 * @param executionState The execution state of the Component
	 *
	 * @return The result of the loop body execution
	 */
	private BodyResult _invokeArray( IBoxContext context, Array array, String item, String index, ComponentBody body, IStruct executionState ) {
		// If no item is provided, use the index as the item
		if ( item == null && index != null ) {
			item	= index;
			index	= null;
		}
		// Loop over array, executing body every time
		for ( int i = 0; i < array.size(); i++ ) {
			// Set the index and item variables
			if ( index != null ) {
				ExpressionInterpreter.setVariable( context, index, i + 1 );
			}
			if ( item != null ) {
				ExpressionInterpreter.setVariable( context, item, array.get( i ) );
			}
			// Run the code inside of the output loop
			BodyResult bodyResult = processBody( context, body );
			// IF there was a return statement inside our body, we early exit now
			if ( bodyResult.isEarlyExit() ) {
				if ( bodyResult.isContinue() ) {
					continue;
				} else if ( bodyResult.isBreak() ) {
					break;
				} else {
					return bodyResult;
				}
			}
		}
		return DEFAULT_RETURN;
	}

	// TODO: refactor to get just what it needs
	public BodyResult _invokeAQuery( IBoxContext context, IStruct attributes, ComponentBody body, IStruct executionState ) {
		Object queryOrName = attributes.get( Key.query );
		// Short circuit if there's no query
		if ( queryOrName == null ) {
			BodyResult bodyResult = processBody( context, body );
			// IF there was a return statement inside our body, we early exit now
			if ( bodyResult.isEarlyExit() ) {
				return bodyResult;
			}
			return DEFAULT_RETURN;
		}

		String				group				= attributes.getAsString( Key.group );
		// TODO: Use this
		Boolean				groupCaseSensitive	= attributes.getAsBoolean( Key.groupCaseSensitive );
		Integer				startRow			= attributes.getAsInteger( Key.startRow );
		Integer				maxRows				= attributes.getAsInteger( Key.maxRows );
		// TODO: Use this
		String				encodefor			= attributes.getAsString( Key.encodefor );

		CastAttempt<String>	queryName			= StringCaster.attempt( queryOrName );
		Query				theQuery;
		// If the input is a string, get a variable in the context of that name
		if ( queryName.wasSuccessful() ) {
			// check not empty
			if ( queryName.get().isEmpty() ) {
				throw new IllegalArgumentException( "The query name cannot be empty" );
			}
			queryOrName = ExpressionInterpreter.getVariable( context, queryName.get(), false );
		}
		// See if what we have is a query!
		theQuery = ( Query ) GenericCaster.cast( context, queryOrName, "query" );
		int	iStartRow	= ( startRow == null ) ? 0 : ( startRow - 1 );
		int	iEndRow		= ( maxRows == null ) ? ( theQuery.size() - 1 ) : ( maxRows + iStartRow - 1 );

		// TODO: Throw exceptions if incoming data is out of bounds?
		iEndRow = Math.min( iEndRow, theQuery.size() - 1 );

		// If there's nothing to loop over, exit stage left
		if ( iEndRow <= iStartRow ) {
			return DEFAULT_RETURN;
		}

		boolean					isGrouped			= group != null;
		Key[]					groupKeys			= null;
		Object[]				lastGroupValues		= null;
		Map<String, Boolean>	isSameGroup			= new HashMap<>();
		int						currentGroupEndRow	= 0;
		isSameGroup.put( "value", true );

		if ( isGrouped ) {
			groupKeys		= ListUtil.asList( group, "," )
			    .stream()
			    .map( ( c ) -> Key.of( ( ( String ) c ).trim() ) )
			    .toArray( Key[]::new );

			lastGroupValues	= new Object[ groupKeys.length ];
			// Calculate the group values for the first row
			lastGroupValues	= getGroupValuesForRow( theQuery, groupKeys, lastGroupValues, iStartRow, isSameGroup );
		}
		// This allows query references to know what row we're on and for unscoped column references to work
		context.registerQueryLoop( theQuery, iStartRow );
		try {
			for ( int i = iStartRow; i <= iEndRow; i++ ) {
				// System.out.println( "i: " + i );
				if ( isGrouped ) {
					if ( i < iEndRow ) {
						lastGroupValues = getGroupValuesForRow( theQuery, groupKeys, lastGroupValues, i + 1, isSameGroup );
					} else {
						isSameGroup.put( "value", false );
					}
					if ( isSameGroup.get( "value" ) ) {
						// System.out.println( "Same group" );
						continue;
					} else {
						// TODO: handle nested output with groups
						currentGroupEndRow = i;
					}
				}
				// Run the code inside of the output loop
				BodyResult bodyResult = processBody( context, body );
				// IF there was a return statement inside our body, we early exit now
				if ( bodyResult.isEarlyExit() ) {
					if ( bodyResult.isContinue() ) {
						continue;
					} else if ( bodyResult.isBreak() ) {
						break;
					} else {
						return bodyResult;
					}
				}
				// Next row, please!
				context.registerQueryLoop( theQuery, i + 1 );
			}
		} finally {
			// This query is DONE!
			context.unregisterQueryLoop( theQuery );
		}
		return DEFAULT_RETURN;
	}

	private static Object[] getGroupValuesForRow( Query query, Key[] groupKeys, Object[] lastGroupValues, int row, Map<String, Boolean> isSameGroup ) {
		Object[] thisGroupValues = new Object[ groupKeys.length ];
		isSameGroup.put( "value", true );
		// System.out.println( "calc group data for Row: " + row );
		for ( int j = 0; j < groupKeys.length; j++ ) {
			thisGroupValues[ j ] = query.getCell( groupKeys[ j ], row );
			// TODO: Use case sensitive flag. Perhaps this needs to use compare?
			if ( !EqualsEquals.invoke( thisGroupValues[ j ], lastGroupValues[ j ] ) ) {
				// System.out.println( "Row: " + row + " is not the same as last row key " + groupKeys[ j ].getName() + " " + thisGroupValues[ j ] + " != "
				// + lastGroupValues[ j ] );
				isSameGroup.put( "value", false );
			}
		}
		return thisGroupValues;
	}
}
