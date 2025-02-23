package fol;

import java.util.List;
import java.util.Set;

public class Or extends Sentence {

	private Sentence left;
	private Sentence right;
	
	public Or(Sentence left, Sentence right) {
		this.left = left;
		this.right = right;
	}
	
	public Sentence getLeft() {
		return left;
	}
	
	public Sentence getRight() {
		return right;
	}
	
	@Override
	public Sentence substitute(Substitution bindings) {
		return new Or(left.substitute(bindings), right.substitute(bindings));
	}

	@Override
	public Substitution unify(Sentence otherSentence, Substitution theta) {
		if (otherSentence instanceof Or) {
			Or other = (Or)otherSentence;
			Substitution newTheta = left.unify(other.left, theta);
			newTheta = right.unify(other.right, newTheta);
			return newTheta;
		}
		return null;        // can't unify with a sentence that isn't an OR
	}

	@Override
	public Set<Variable> getVars() {
		Set<Variable> varSet = left.getVars();
		varSet.addAll(right.getVars());              
		           // this will change the set returned by left.getVars(), but that's okay as long as we create a new one with every call
		return varSet;
	}

	public String toString() {
		return left + " v " + right;
	}
}
