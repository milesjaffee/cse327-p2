package fol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


/** Defines a first-order logic atom. The general form is p(t1, t2, ...) where
 * p is the predicate and each t_i is a term. The number of terms is determined by
 * the arity of the predicate. A term can be a constant or a variable.
 */
public class Atom extends Sentence {

	private Predicate pred;
	private Term[] terms;

//	public Atom(Predicate pred, Term[] terms) {
//		this.pred = pred;
//		this.terms = terms;
//		if (pred.getArity() != terms.length)
//			System.out.println(pred.getSymbol() + "(" + terms + ") is not well-formed");
//	}
	
	/** Create an atom using the predicate, and all remaining arguments as terms. */
	public Atom(Predicate pred, Term... terms) {  
		this.pred = pred;
		this.terms = terms;
		if (pred.getArity() != terms.length)
			System.out.println(pred.getSymbol() + "(" + terms + ") is not well-formed");
	}
	
	public Predicate getPred() {
		return pred;
	}
	
	public Term[] getTerms() {
		return terms;
	}
	
	/** Is the atom free of variables? */
	public boolean isGround() {
		for (Term t : terms) {
			if (t instanceof Variable)
				return false;
		}
		return true;
	}
	
	// use covariant return types (Java 5.0+) to avoid casting
	public Atom substitute(Substitution sub) {
		
		Term[] newTerms = new Term[terms.length];
		for (int i=0; i < terms.length; i++) {
			newTerms[i] = terms[i];                   // by default, we keep the term the same
			if (terms[i] instanceof Variable) {       // if it is a variable, look for a suitable binding
				for (Binding b: sub.getBindings()) {
					if (b.getVar().equals(terms[i])) 
						newTerms[i] = b.getSub();
				}
			}
		}
		return new Atom(pred, newTerms);
	}
	
	// BUG: sometimes adds a redundant binding if theta already has necessary binding
	public Substitution unify(Sentence sentence, Substitution theta) {
		// TO-DO: consider implementing the version from Poole and Mackworth, p. 513....
		/* ALT: 
		 * We can compute the MGU using the disagreement set Dk = {e1, e2}:
		 * 				 the pair of expressions where two clauses first disagree.
		 * REPEAT UNTIL no more disagreement ! found MGU.
		 * IF either e1 or e2 is a variable V and the other is some term (or a
		 *           variable) t, then choose V = t as substitution.
		 * Then substitute to obtain Sk+1 and find disagreement set Dk+1.
		 * ELSE unification is not possible
		 */
		if (theta == null)                        // if already impossible, fail automatically
			return null;
		if (!(sentence instanceof Atom))          // atoms can only match atoms
			return null;
		else {
			Atom otherAtom = (Atom)sentence;
			if (!pred.equals(otherAtom.pred))
				return null;
			for (int i=0; i < terms.length; i++) {
				theta = terms[i].unify(otherAtom.terms[i], theta);
				if (theta == null)                                        // early exit if one of the terms fails to unify...
					return null;
				// TO-DO: handle functions
				// default is to not change theta
			}
			return theta;       // theta is either set to null, or extended by loop above
		}
	}
	
	public Set<Variable> getVars() {
		Set<Variable> varSet = new HashSet<>();
		for (Term t: terms) {
			if (t instanceof Variable)
				varSet.add((Variable)t);
		}
		return varSet;
	}
	
	public String toString() {
		String temp = pred.getSymbol() + "(";
		for (int i=0; i < terms.length-1; i++) {
			temp = temp + terms[i] + ",";
		}
		if (terms.length > 0)
			temp = temp + terms[terms.length-1];
		temp = temp + ")";
		return temp;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Atom) {
			Atom a = (Atom)obj;
			if (pred.equals(a.pred)) {
				for (int i=0; i < terms.length; i++) {
					if (!terms[i].equals(a.terms[i]))
						return false;
				}
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		int hash = 3;
		hash = 7 * hash + pred.hashCode();
		for (Term t : terms)
			hash = 7 * hash + t.hashCode(); 
		return hash;
	}
	
	public static void unifyTestCase(Atom atom1, Atom atom2, Substitution theta) {
		if (theta == null) {
			System.out.print("Unifying " + atom1 + " with " + atom2 + ": ");
			System.out.println(atom1.unify(atom2));
		} else {
			System.out.print("Unifying " + atom1 + " with " + atom2 + " where " + theta + ": ");
			System.out.println(atom1.unify(atom2, theta));
		}
	}

	public static void main(String[] args) {
		// test unify
		Variable x = new Variable("x");
		Variable y = new Variable("y");
		Variable z = new Variable("z");
		Constant a = new Constant("a");
		Constant b = new Constant("b");
		Predicate p = new Predicate("p",2);
		Predicate q = new Predicate("q",2);
		
		Atom pxy = new Atom(p, x, y);
		Atom qab = new Atom(q, a, b);
		Atom paa = new Atom(p, a, a);
		Atom pab = new Atom(p, a, b);
		Atom pxa = new Atom(p, x, a);
		Atom pby = new Atom(p, b, y);
		Atom pya = new Atom(p, y, a);
		
		unifyTestCase(pxy, qab, null);
		unifyTestCase(pxy, pab, null);
		unifyTestCase(pxy, paa, null);
		unifyTestCase(pxa, pab, null);
		unifyTestCase(pxa, pby, null);
		unifyTestCase(pab, pxa, null);

		// test handling of binding when an existing substitution has a variable bound to another variable
		Substitution sub = new Substitution(new Binding(y,z));
		unifyTestCase(pxa, pxy, sub);

		sub = new Substitution(new Binding(y,z));
		unifyTestCase(pxa, pya, sub);

		sub = new Substitution(new Binding(x,a), new Binding(y,b));
		unifyTestCase(pxa, pya, sub);

		sub = new Substitution(new Binding(x,z), new Binding(y,b));
		unifyTestCase(pxa, pya, sub);

	}
}
