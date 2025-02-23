package fol;

import java.util.List;
import java.util.Set;

public class Implies extends Sentence {

	Sentence antecedent;
	Sentence consequent;
	
	public Implies(Sentence antecedent, Sentence consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}
	
	@Override
	public Sentence substitute(Substitution bindings) {
		return new Implies(antecedent.substitute(bindings), consequent.substitute(bindings));
	}

	@Override
	public Substitution unify(Sentence otherSentence, Substitution theta) {
		if (otherSentence instanceof Implies) {
			Implies otherImplies = (Implies)otherSentence;
			Substitution newTheta = consequent.unify(otherImplies.consequent, theta);
			newTheta = antecedent.unify(otherImplies.antecedent, newTheta);
			return newTheta;
		}
		return null;        // can't unify with a sentence that isn't an implies
	}
	
	@Override
	public Set<Variable> getVars() {
		Set<Variable> varSet = antecedent.getVars();
		varSet.addAll(consequent.getVars());              
		           // this will change the set returned by antecedent.getVars(), but that's okay as long as we create a new one with every call
		return varSet;
	}


	public String toString() {
		return antecedent + " => " + consequent;
	}

}
