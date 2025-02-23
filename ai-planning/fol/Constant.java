package fol;

/** A constant symbol. */
public class Constant extends Term {
	
	private String symbol;

	public Constant(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	
	/** Give a list of bindings, will return the substitution that matches this
	 * term. For constants, this just returns the term unchanged.
	 * @param sub A set of bindings that could be applied to the term 
	 * @return Always the original constant, as substitutions cannot change constants.
	 */
	public Term substitute(Substitution sub) {
		return this;                // a substitution can never replace a constant...
	}

	/** Two constants are considered the same if they have the same symbol.
	 * Note, equals() does not test if two distinct constants logically
	 * denote the same individual. 
	 */
	public boolean equals(Object o) {
		if (o instanceof Constant) {
			if (symbol.equals(((Constant)o).symbol))
				return true;
		}
		return false;
	}
	
	public String toString() {
		return symbol;
	}

}
