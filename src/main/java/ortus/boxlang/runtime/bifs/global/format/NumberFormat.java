
package ortus.boxlang.runtime.bifs.global.format;

import java.util.Locale;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.bifs.BoxMember;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.DoubleCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.BoxLangType;
import ortus.boxlang.runtime.util.LocalizationUtil;

@BoxBIF
@BoxMember( type = BoxLangType.NUMERIC )

public class NumberFormat extends BIF {

	/**
	 * Constructor
	 */
	public NumberFormat() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "any", Key.number ),
		    new Argument( false, "string", Key.mask )
		};
	}

	/**
	 * Formats a number with an optional format mask
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.number The number to be formatted
	 *
	 * @argument.mask The formatting mask to apply
	 *
	 * @argument.locale Note used by standard NumberFormat but used by LSNumberFormat
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		Key						bifMethodKey	= arguments.getAsKey( BIF.__functionName );
		double					value			= DoubleCaster.cast( arguments.get( Key.number ) );
		String					format			= null;
		Locale					locale			= LocalizationUtil.parseLocaleOrDefault(
		    arguments.getAsString( Key.locale ),
		    ( Locale ) context.getConfigItem( Key.locale, Locale.getDefault() )
		);
		java.text.NumberFormat	formatter		= LocalizationUtil.localizedDecimalFormatter(
		    locale,
		    LocalizationUtil.numberFormatPatterns.getAsString( LocalizationUtil.DEFAULT_NUMBER_FORMAT_KEY )
		);
		if ( bifMethodKey.equals( Key.dollarFormat ) ) {
			formatter = ( java.text.NumberFormat ) LocalizationUtil.commonNumberFormatters.get( "USD" );
		} else {
			format = arguments.getAsString( Key.mask );
		}

		// Currency-specific arguments
		String type = arguments.getAsString( Key.type );
		if ( type != null ) {
			formatter = ( java.text.NumberFormat ) LocalizationUtil.localizedCurrencyFormatter( locale, type );
			// Format parsing
		} else if ( format != null ) {
			if ( format.equals( "$" ) ) {
				format = "USD";
			}
			Key formatKey = Key.of( format );
			if ( LocalizationUtil.commonNumberFormatters.containsKey( formatKey ) ) {
				formatter = ( java.text.NumberFormat ) LocalizationUtil.commonNumberFormatters.get( formatKey );
			} else if ( LocalizationUtil.numberFormatPatterns.containsKey( formatKey ) ) {
				formatter = LocalizationUtil.localizedDecimalFormatter( locale, format );
			} else if ( format.equals( "ls$" ) ) {
				formatter = LocalizationUtil.localizedCurrencyFormatter( locale );
			} else {
				format = format.replaceAll( "9", "0" )
				    .replaceAll( "_", "#" );
				if ( format.substring( 0, 1 ).equals( "L" ) ) {
					format = format.substring( 1, format.length() );
				} else if ( format.substring( 0, 1 ).equals( "C" ) ) {
					format = format.substring( 1, format.length() ).replace( "0", "#" );
				}
				formatter = LocalizationUtil.localizedDecimalFormatter( locale, format );
			}
		}

		return formatter.format( value );
	}

}
