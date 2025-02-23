package planning;

import java.util.List;

import fol.Atom;
import fol.Binding;
import fol.Predicate;
import fol.Sentence;
import fol.Term;
import fol.Substitution;

/** An atomic sentence or its negations. This is mostly using in planning. */
public class Literal {
	private boolean negative;     // true if the literal is negative
	private Atom atom;
	
	public Literal(Predicate pred, Term term) {
		this(new Atom(pred,term));
	}

	public Literal(Predicate pred, Term term1, Term term2) {
		this(new Atom(pred,term1,term2));
	}

	public Literal(boolean negative, Predicate pred, Term term) {
		this(negative, new Atom(pred,term));
	}

	public Literal(boolean negative, Predicate pred, Term term1, Term term2) {
		this(negative, new Atom(pred,term1,term2));
	}

	/** Create a new positive literal from an atom. */
	public Literal(Atom atom) {          
		this(false, atom);
	}
	
	public Literal(boolean negative, Atom atom) {
		this.atom = atom;
		this.negative = negative;
	}
	
	public Atom getAtom() {
		return atom;
	}
	
	public boolean isNegative() {
		return negative;
	}
	
	public boolean isPositive() {
		return !negative;
	}
	
	public Literal substitute(Substitution bindings) {
		return new Literal(negative, atom.substitute(bindings));
	}
	
	/** Unify this literal with another one. Returns null if the two literals
	 * do not unify. */
	public Substitution unify(Literal lit, Substitution theta) {
		if (negative == lit.negative) {   // the two literals must have the same polarity
			return atom.unify(lit.atom, theta);   // and the atoms must unify
		} else
			return null;           // fail!
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Literal) {
			Literal other = (Literal)obj;
			if (negative == other.negative && atom.equals(other.atom))
				return true;
		}
		return false;
	}

	// literals must have hashCodes if they are to be used in HashSet's
	public int hashCode() {
		int hash = atom.hashCode();
		hash = 7 * hash + Boolean.hashCode(negative);
		return hash;
	}
	
	public String toString() {
		if (negative)
			return "!" + atom;
		else
			return atom.toString();		
	}
}
