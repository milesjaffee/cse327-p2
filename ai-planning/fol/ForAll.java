package fol;

import java.util.List;
import java.util.Set;

/** A universrally quantified sentence. */
public class ForAll extends Sentence {

	Sentence scope;
	Variable var;
	
	public ForAll(Variable var, Sentence scope) {
		this.var = var;
		this.scope = scope;
		// TO-DO: test if variable is in scope, and all other variables are quantified...
	}
	
	@Override
	public Sentence substitute(Substitution bindings) {
		// if you substitute a constant for the for all variable, should you remove the quantifier?
		return new ForAll(var, scope.substitute(bindings));
	}

	@Override
	public Substitution unify(Sentence otherSentence, Substitution theta) {
		if (otherSentence instanceof ForAll) {
			ForAll other = (ForAll)otherSentence;
			Substitution newTheta = scope.unify(other.scope, theta);
			return newTheta;
		}
		return null;        // can't unify with a sentence that isn't a ForAll
	}
	
	@Override
	public Set<Variable> getVars() {
		return scope.getVars();
	}


	public String toString() {
		return "FORALL " + var + ": " + scope;
	}

}
