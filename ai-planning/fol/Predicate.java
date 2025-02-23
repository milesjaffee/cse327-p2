package fol;

/** A predicate symbol in first-order logic that is used to identify a relation. */ 
public class Predicate {

	private String symbol;
	private int arity;
	
	/** Constructs a new predicate with a given symbol and arity. The arity determines how
	 * many terms the predicate has. 
	 * @param symbol
	 * @param arity
	 */
	public Predicate(String symbol, int arity) {
		this.symbol = symbol;
		this.arity = arity;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public int getArity() {
		return arity;
	}
	
	public String toString() {
		return symbol + "/" + arity;
	}
}
