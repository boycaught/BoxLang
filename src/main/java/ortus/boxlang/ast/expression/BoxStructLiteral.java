package ortus.boxlang.ast.expression;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.Position;

/**
 * A struct literal comes in two forms, ordered and unordered (default).
 * The unordered struct uses curly braces `{}` like a JSON object.
 * The ordered struct, uses square brackets `[]` like an array literal.
 * The difference is structs use a comma-delimited list of key/value pairs.
 * Note, key/value pairs ANYWHERE in Boxlang can either be specified as `
 * foo=bar` OR `foo : bar`. This goes for strut literals, function parameters,
 * or class/UDF metadata.
 */
public class BoxStructLiteral extends BoxExpr {

	private final BoxStructType	type;
	private final List<BoxExpr>	values;

	/**
	 * Creates the AST node for Struct Literals
	 *
	 * @param values     initialization values
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxStructLiteral( BoxStructType type, List<BoxExpr> values, Position position, String sourceText ) {
		super( position, sourceText );
		this.type	= type;
		this.values	= Collections.unmodifiableList( values );
		this.values.forEach( arg -> arg.setParent( this ) );
	}

	public List<BoxExpr> getValues() {
		return values;
	}

	public BoxStructType getType() {
		return type;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "type", type.toString() );
		map.put( "values", values.stream().map( BoxExpr::toMap ).collect( Collectors.toList() ) );
		return map;
	}
}
