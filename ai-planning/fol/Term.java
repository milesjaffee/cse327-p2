package fol;

/** A constant, variable, or function applied to a list of terms. */
// TO-DO: add in functions...
public abstract class Term {

	public Term() {
	}
	
	/** Give a list of bindings, will return the substitution that matches this
	 * term.
	 */
	public abstract Term substitute(Substitution sub);
	
	public Substitution unify(Term term, Substitution theta) {
		
		if (theta == null)                        // can't bind if unify has already failed...
			return null;
		
		// try replacing both terms with their substitutions, and relying on compose to do the right thing
		Term subThis = substitute(theta);
		Term subTerm = term.substitute(theta);

		if (subThis.equals(subTerm))                 // if they are equivalent after substituting then they match trivially
			return theta;
		if (subThis instanceof Variable) {              // if (theta)this is a variable, then add {(theta)this/subTerm} to theta
			Substitution newTheta = new Substitution(theta);     // make a copy of theta... 
			newTheta.compose(new Substitution(new Binding((Variable)subThis, subTerm)));
			return newTheta;
		} else if (subTerm instanceof Variable) {   // if (theta)term is a variable, then add {(theta)subTerm/subThis} to theta
			Substitution newTheta = new Substitution(theta);     // make a copy of theta... 
			newTheta.compose(new Substitution(new Binding((Variable)subTerm, subThis)));
			return newTheta;
		} else if (subThis instanceof Constant && subTerm instanceof Constant) {
			// must be different constant value because Term.equals(Term) failed above
			return null;
		} else {
			System.out.println("WARNING: Term.unify() reached unexpected case"); 
		}
		return null;
	}
}
