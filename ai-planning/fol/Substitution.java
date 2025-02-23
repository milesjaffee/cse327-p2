package fol;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/** A substitution is a set of bindings of variables to terms. The unify 
 * operation returns a substitution and any first-order logic query with
 * variables will return a (possibly empty) set of substitutions.
 * @author heflin
 *
 */
public class Substitution {

	private Set<Binding> bindings = new TreeSet<>();
	
	/** Create an empty substitution. */
	public Substitution() {
		// no op...
	}
	
	/** Create a substitution from a list of bindings. */
	public Substitution(List<Binding> bindings) {
		this.bindings = new TreeSet<Binding>(bindings);
	}

	/** Create a substitution from a sequence of Binding parameters. */
	public Substitution(Binding... bindings) {
		this.bindings = new TreeSet<Binding>(Arrays.asList(bindings));
	}
	
	/** A copy constructor that creates a new substitution from an existing one. */
	public Substitution(Substitution sub) {
		bindings = new TreeSet<Binding>(sub.bindings);
	}
	
	/** Add a binding to the substitution. */
	public void addBinding(Binding bind) {
		bindings.add(bind);
	}

	/** Add a binding to the substitution. */
	public void addBinding(Variable var, Term t) {
		addBinding(new Binding(var,t)); 
	}

	/** Remove a binding from the substitution. */
	public void removeBinding(Binding bind) {
		bindings.remove(bind);
	}
	
	/** Return an array of the bindings. 
	 * @return */
	public Binding[] getBindings() {
		return bindings.toArray(new Binding[0]);
	}
	
	/** Return the number of binding pairs contained in this substitution. */
	public int getNumBindings() {
		return bindings.size();
	}
	
	/** Return the substitute for the specified variable, or null if there
	 * is none.
	 * @param var
	 * @return
	 */
	public Term lookupSubstitute(Variable var) {
		for (Binding bind:bindings) {
			if (bind.getVar().equals(var))
				return bind.getSub();
		}
		return null;            // no binding was found for var
	}
	
	/** Combines the substitution parameter with the current one, removing duplicates.
	 * The effect is the same as applying this substitution and then applying lambda.
	 * TODO: ??? when possible, replace variables with constants or function terms...
	 * @param lambda
	 */
	public void compose(Substitution lambda) {
		Set<Binding> newBindings = new TreeSet<>();
		for (Binding b:bindings) {
			Variable var = b.getVar();
			Term val = b.getSub();
			Term newVal = val.substitute(lambda);  // apply the substitution, often newVal will just be val
			newBindings.add(new Binding(var, newVal));
		}
		// now add in any bindings from lambda that don't apply to variables already bound in this substitution
		for (Binding b:lambda.bindings) {
			Variable var = b.getVar();
			if (lookupSubstitute(var) == null)
				newBindings.add(b);
		}
		bindings = newBindings;
	}
	
	/** Adds all of the bindings from the parameter to this substitution, removing
	 * any duplicates. 
	 * @param lambda
	 */
	public void union(Substitution lambda) {
		bindings.addAll(lambda.bindings);
	}
	
	/** Replaces all occurrences of one term with another. This can be used to
	 * update a substitution subsequent to renaming a variable, e.g., if the 
	 * variable was replaced with a GenSym. */
	public void replaceOccurencesOf(Term oldVal, Term newVal) {
		Set<Binding> newBindings = new TreeSet<>();
		Binding[] oldBindings = bindings.toArray(new Binding[0]);
		for (int i=0; i < oldBindings.length; i++)  {          
			Binding b = oldBindings[i];
			Variable newVar = b.getVar();     // default the new binding to be the same as the old
			Term newSub = b.getSub();
			if (oldVal.equals(newVar))
				newVar = (Variable)newVal;    // this will throw a class cast exception if this replacement involves putting a constant in the var position
			if (oldVal.equals(newSub))
				newSub = newVal;
			newBindings.add(new Binding(newVar,newSub));
		}
		bindings = newBindings;
	}
	
	/** A substitution is a renaming if all of the variables are bound to distinct variables. */
	public boolean isRenaming() {
		Set<Variable> subVars = new HashSet<>();
		for (Binding bind : bindings) {
			Term t = bind.getSub();
			if (t instanceof Variable && !subVars.contains(t)) 
				subVars.add((Variable)t);
			else
				return false;
		}
		return true;
	}
	
	/** Two Substitutions are equal if they contain the same set of bindings. */
	public boolean equals(Object o) {
		if (o instanceof Substitution) {
			Substitution otherSub = (Substitution)o;
			return bindings.equals(otherSub.bindings);
		}
		return false;
	}
	
	public String toString() {
		return bindings.toString();
	}
}
