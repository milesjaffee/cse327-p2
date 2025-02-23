package fol;

/** The pair of a variable with a term that can be substituted for it. */
public class Binding implements Comparable<Binding> {

	private Variable var;            // the variable
	private Term sub;                // the substitution, a term
	
	public Binding(Variable var, Term sub) {
		this.var = var;
		this.sub = sub;
	}
	
	public Variable getVar() {
		return var;
	}
	
	public Term getSub() { 
		return sub;
	}
	
	/** Two bindings are considered equal if they have the same variable and substitution. */
	public boolean equals(Object obj) {
		if (obj instanceof Binding) {
			Binding otherBind = (Binding)obj;
			return var.equals(otherBind.var) && sub.equals(otherBind.sub);
		}
		return false;
	}

	/** Two bindings are compared using the results of their toString method. 
	 * This method is needed in order to use a TreeSet of bindings. */
	public int compareTo(Binding otherBind) {
		return this.toString().compareTo(otherBind.toString());
	}
	
	public String toString() {
		return var + "/" + sub;
	}
}
