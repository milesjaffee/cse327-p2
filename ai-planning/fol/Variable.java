package fol;

import java.util.List;
import java.util.ArrayList;

public class Variable extends Term {

	public static Variable x = new Variable("x");
	public static Variable y = new Variable("y");
	public static Variable z = new Variable("z");
	
	private static int gensym = 0;
	private String symbol;
	
	/** Create a new, unique variable. */
	public Variable() {
		this.symbol = "_g" + gensym;
		gensym++;
	}
	
	public Variable(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	/** Give a list of bindings, will return the substitution that matches this
	 * variable. If there is no such substitution, just returns the variable.
	 * @param sub A set of bindings that could be applied to the variable 
	 * @return The variable or the substituted term
	 */
	public Term substitute(Substitution sub) {
		for (Binding b: sub.getBindings()) {
			if (b.getVar().equals(this)) 
				return b.getSub();
		}
		return this;
	}
	
	// trying to replace this with a simpler Term.unify()
	/** Attempt to unify the current variable with the term parameter, taking into account existing
	 * substitutions specified by theta. Will return null if the unify fails, will return theta
	 * if nothing is needed to make them unify. 
	 * @param term
	 * @param theta
	 * @return
	 */
	/*
	public Substitution unify(Term term, Substitution theta) {

		Variable otherVar = null;
		Term boundValue = null;
		Term otherValue = null;
		
		if (theta == null)                        // can't bind if unify has already failed...
			return null;
		
		if (term instanceof Variable) {
			otherVar = (Variable)term;
		}
		for (Binding b:theta.getBindings()) {                   // look for this variable and the term we are matching with in subsitutions
			if (b.getVar().equals(this))
				boundValue = b.getSub();
			if (b.getVar().equals(otherVar))
				otherValue = b.getSub();
		}
		
		// KNOWN BUG: this doesn't handle function terms!!!!
		
		if (this.equals(term))                              // if it is the same variable, binds trivially
			return theta;
		else if (boundValue != null && boundValue instanceof Constant && term instanceof Constant) {
			// can't bind if term is a constant, and this var is already bound to a different constant
			if (!boundValue.equals(term))            
				return null;
			else                                 // if the term is already the same as the bound value, return existing theta (trivial success)
				return theta;
		} else if (boundValue != null && boundValue instanceof Constant && otherValue instanceof Constant) {
			// can't bind if they are already bound to different constants
			if (!boundValue.equals(otherValue))     
				return null;	
			else                                 // if the term is already bound to the same constant as this variable, return existing theta (trivial success)
				return theta;
		} else if (boundValue != null && otherVar != null)         // if this variable is bound and the other term is a variable, try to bind it to the same value
			//TO-DO: what do we really need this for???
			return otherVar.unify(boundValue, theta);               // BUG? I think this breaks the way the substitutions are supposed to work, e.g. with {a/_g2} and trying to unify x1 with a
		// 2/28/18: I don't think we need the next two lines...
		// else if (otherVar != null && otherValue != null)        // if the other term is a variable that is bound, try to bind this variable to the same value
		//  	return unify(otherValue, theta);
		// KNOWN BUG: we are omitting the OCCUR-CHECK for now!!!!
		// We should check if we try to unify a variable with a term that contains it. Otherwise, we could have unsound
		// inference. E.g., consider lessthan(X,next(X)) and lessthan(Y,Y); these will unify with {X/next(X),Y/next(X)}
		// see Poole and Mackworth pp. 518
		else {  // either this variable is unbound, or it is bound to a variable and the other term is a constant
			Substitution newTheta = new Substitution(theta);     // make a copy of theta... 
			// if this variable is already bound to a variable, then we need to replace that binding with a binding
			// of the original target variable to the current target
			if (boundValue != null) {
				Variable boundVar = (Variable)boundValue;
				newTheta.removeBinding(new Binding(this, boundValue));
				newTheta.addBinding(new Binding(boundVar,term));
			}
			newTheta.compose(new Substitution(new Binding(this, term)));
			return newTheta;
		}
	}
	*/
	
	/** Two variables are considered the same if they have the same symbol.
	 * Note, equals() does not test if two variables are in different 
	 * sentences (and thus necessarily distinct)
	 */
	public boolean equals(Object o) {
		if (o instanceof Variable) {
			if (symbol.equals(((Variable)o).symbol))
				return true;
		}
		return false;
	}
	
	public int hashCode() {
		return symbol.hashCode();
	}
	
	public String toString() {
		return symbol;
	}

	public static void main(String[] args) {
		// test unify
		Variable x = new Variable("x");
		Variable y = new Variable("y");
		Constant a = new Constant("a");
		Constant b = new Constant("b");
		Substitution  subya = new Substitution();
		subya.addBinding(new Binding(y,a));
		Substitution subxayb = new Substitution();
		subxayb.addBinding(new Binding(x,a));
		subxayb.addBinding(new Binding(y,b));
		
		System.out.println(x.unify(a, new Substitution()));
		System.out.println(x.unify(y, new Substitution()));
		System.out.println(y.unify(b, subya));
		System.out.println(x.unify(y, subya));
		System.out.println(x.unify(y, subxayb));

	}
}
