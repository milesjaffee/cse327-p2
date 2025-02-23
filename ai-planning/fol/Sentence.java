package fol;

import java.util.List;
import java.util.TreeSet;
import java.util.Set;

public abstract class Sentence {

	public abstract Sentence substitute(Substitution bindings);

	public abstract Set<Variable> getVars();
	
	public Substitution unify(Sentence otherSentence) {
		return unify(otherSentence, new Substitution());
	}
	
	public abstract Substitution unify(Sentence otherSentence, Substitution theta);

}
