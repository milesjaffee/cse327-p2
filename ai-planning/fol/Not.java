package fol;

import java.util.List;
import java.util.Set;

public class Not extends Sentence {

	private Sentence negated;
	
	public Not(Sentence negated) {
		this.negated = negated;
	}
	
	public Sentence getNegated() {
		return negated;
	}
	
	public Sentence substitute(Substitution bindings) {
		return new Not(negated.substitute(bindings));
	}

	public Substitution unify(Sentence otherSentence, Substitution theta) {
		if (theta == null)
			return null;
		if (!(otherSentence instanceof Not))
			return null;
		else
			return negated.unify(((Not)otherSentence).negated, theta);
	}

	@Override
	public Set<Variable> getVars() {
		return negated.getVars();
	}	


	public String toString() {
		return "~" + negated;
	}

}
