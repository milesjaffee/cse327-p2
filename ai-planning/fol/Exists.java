package fol;

import java.util.List;
import java.util.Set;

/** An existentially quenatified sentence. */
public class Exists extends Sentence {

	Variable var;
	Sentence scope;
	
	public Exists(Variable var, Sentence scope) {
		this.var = var;
		this.scope = scope;
	}
	
	@Override
	public Sentence substitute(Substitution bindings) {
		return new Exists(var, scope.substitute(bindings));
	}

	@Override
	public Substitution unify(Sentence otherSentence, Substitution theta) {
		if (otherSentence instanceof Exists) {
			Exists other = (Exists)otherSentence;
			Substitution newTheta = scope.unify(other.scope, theta);
			return newTheta;
		}
		return null;        // can't unify with a sentence that isn't an Exists
	}

	@Override
	public Set<Variable> getVars() {
		return scope.getVars();
	}
	

	public String toString() {
		return "EXISTS " + var + ": " + scope;
	}

}
