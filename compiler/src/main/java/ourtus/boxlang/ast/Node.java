/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ourtus.boxlang.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a Node in the AST
 */
public class Node {

	protected Position			position;
	private final String		sourceText;
	protected Node				parent	= null;
	private final List<Node>	children;
	private Node				originator;

	/**
	 * AST node constructor
	 * 
	 * @param position   the position within the source code that originated the node
	 * @param sourceText the original source code that represented by the node
	 */
	public Node( Position position, String sourceText ) {
		this.position	= position;
		this.sourceText	= sourceText;
		this.children	= new ArrayList<>();
	}

	/**
	 * Returns the position in code that the node represents
	 * 
	 * @return a Position instance
	 * 
	 * @see Position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Returns the source code that originated the Node
	 * 
	 * @return the snipped of the source code
	 */
	public String getSourceText() {
		return sourceText;
	}

	/**
	 * Set the parent and the children of the Node
	 * 
	 * @param parent an instance of the parent code
	 */
	public void setParent( Node parent ) {
		this.parent = parent;
		if ( parent != null ) {
			if ( !parent.children.contains( this ) )
				parent.getChildren().add( this );
		}
	}

	/**
	 * Returns the parent Node of node or null if has no parent
	 * 
	 * @return the parent Node of the current Node
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * Returns the list ov children of the current node
	 * 
	 * @return a list of children Node
	 */
	public List<Node> getChildren() {
		return children;
	}

	public Node getOriginator() {
		return originator;
	}

	/**
	 * Walk the tree
	 * 
	 * @return a list of nodes traversed
	 */
	public List<Node> walk() {
		List<Node> result = new ArrayList<>();
		result.add( this );
		for ( Node node : this.children ) {
			result.addAll( node.walk() );
		}
		return result;
	}
}
