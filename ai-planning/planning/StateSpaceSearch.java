package planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fol.Atom;
import fol.Binding;
import fol.Substitution;
import search.SearchProblem;

public abstract class StateSpaceSearch extends SearchProblem {

	/** Determine if a collection of atoms describing a state satisfy a list of goal 
	 *  literals. If so, returns a list of all sets of bindings that make it possible. 
	 *  If the goal is not satisfied, then returns an empty list.If it is satisfied without 
	 *  bindings, then it returns a list containing an empty list.  */ 
	public static List<Substitution> satisfy(List<Literal> goalLits, Collection<Atom> state) {
		// do this recursively. unify first precond with an atom, then recurse on rest of goal literals
		// use an int to keep track of location in list? also need current bindings....
		return satisfy(goalLits, 0, state, new Substitution());
	}

	/** Recursive helper method for satisfy(List<>,Collection<>). Will attempt to match the next goalLit
	 * (identified by first) using the current substitution theta. */
	static List<Substitution> satisfy(List<Literal> goalLits, int first, Collection<Atom> state, Substitution theta) {
		List<Substitution> answers = new ArrayList<Substitution>();
		if (first == goalLits.size()) {                // base case, if we've matched every literal, theta is one answer
			answers.add(theta);                   // if no bindings are required, theta will be an empty substitution
			return answers;
		}
		Literal currentLit = goalLits.get(first);
		// System.out.println("Unifying with " + currentLit);

		for (Atom atom:state) {
			if (currentLit.isPositive()) {
				// POSSIBLE IMPROVEMENT: could we be more efficient if we bind the current literal with theta before attempting to unify it???
				// This might allow us to avoid some backtracking...
				Substitution newTheta = atom.unify(currentLit.getAtom(), theta);
				if (newTheta != null) {
					List<Substitution> result = satisfy(goalLits, first+1, state, newTheta);   // recursion
					if (!result.isEmpty()) {
						answers.addAll(result);
					}
				}
			} else {
				System.out.println("Negative literals not yet handled");
			}
		}
		return answers;
	}

}
