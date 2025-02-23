package fol;

import java.util.List;
import java.util.Hashtable;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.ArrayDeque;

import static fol.Variable.*;

public class BackwardChaining {

	private BackwardChainKB kb;
	private static boolean verbose = false;
	
	public BackwardChaining(List<? extends Sentence> kb) {
		this.kb = new BackwardChainKB(kb);
	}

	/*
	public Substitution entails(Sentence query) {
		List<Atom> goals = new
	}
	*/
	
	/** Given a sequence of atoms representing subgoals, returns a list of
	 * bindings that make the query true. If there are no such bindings, returns
	 * an empty list. If there is a binding that requires no substitutions, then
	 * returns a list containing an empty list.
	 * @param goalsArray
	 * @return
	 */
	public List<Substitution> entails(	Atom... goalsArray) {
		return entails(Arrays.asList(goalsArray));
	}
	

	/** Returns a list of bindings that makes the query true, or null if there are
	 * no such bindings. If there are no such bindings, returns
	 * an empty list. If there is a binding that requires no substitutions, then
	 * returns a list containing an empty list.
	 * @param goals
	 * @return
	 */
	public List<Substitution> entails(List<Atom> goals) {
		Substitution initBind = new Substitution();
		return entails(new ArrayDeque<Atom>(goals), initBind);
	}
	
	public List<Substitution> entails(ArrayDeque<Atom> goals, Substitution theta) {
		if (verbose)
			System.out.println("entails(): " + goals + ", theta=" + theta);
		List<Substitution> answers = new ArrayList<>();
		if (goals.isEmpty()) {
			// POSSIBLE BUG? it may be better to identify the query vars, and only add them (as opposed to filtering out gensym variables)
			answers.add(filterOutGensymVariables(theta));
			return answers;
		}
		Atom subGoal = goals.pop();
		// since we now apply the substitutions to all goals before call entail, the next line is not needed
		// Atom subGoal = (Atom)nextGoal.substitute(theta);                              
		
		// handle built-in predicates
		if (subGoal.getPred() instanceof BuiltInPredicate) {              // must evaluate the predicate
			Term[] args = subGoal.getTerms();                   // note, substitutions must be applied to these terms...
			BuiltInPredicate builtIn = (BuiltInPredicate)subGoal.getPred();
			if (builtIn.evaluate(args))                // if true, continue to evaluate the goals
				return entails(goals, theta);
			else                                       // if false, then backtrack and try new path...
				return answers;                       
		}

		// handle all other predicates
		ArrayList<Sentence> possMatch = kb.getCandidates(subGoal.getPred());       
		if (possMatch != null) {
			for (Sentence s : possMatch) {
				// standardize apart any variables that have already been seen, this is necessary for any cyclic rules
				Set<Variable> matchVars = s.getVars();          // TO-DO: only do the next 6 lines if this set is non-empty...
				if (matchVars.size() > 0) {
					Set<Variable> usedVars = new HashSet<>();
					for (Binding bind:theta.getBindings())
						usedVars.add(bind.getVar());
					usedVars.addAll(subGoal.getVars());       // union of theta variables and subgoal variables
					usedVars.retainAll(matchVars);          // intersect usedVars with matchVars
					if (!usedVars.isEmpty()) {
						s = standardizeApart(s,usedVars);                // we'll only replace the variables in the intersection, this saves one step over the regular standardizeApart
					}
				}
				if (s instanceof Atom) {
					Atom atom = (Atom)s;
					Substitution newBind = atom.unify(subGoal, new Substitution(theta));       // BUG? is putting theta here right?? It probably helps when the substitution above is broken 
					if (newBind != null) {                                                 
						Substitution newTheta = new Substitution(theta);                          // TO-DO:It seems that newTheta always produces the same result as newBind, since new bind was already based on theta 
						newTheta.compose(newBind);
						answers.addAll(entails(applyBindings(goals,newTheta), newTheta));
					}
				} else if (s instanceof HornClause) {
					HornClause hc = (HornClause)s;
					// in order for compose to work correctly the unify method must be applied to subGoal, rather than the head
					Substitution newBind = subGoal.unify(hc.getHead(), new Substitution(theta));       // BUG? is putting theta here right??
					if (newBind != null) {
						Substitution newTheta = new Substitution(theta);                  // BUG? maybe we don't need to do this if we pass theta in above?
						newTheta.compose(newBind);
						List<Atom> body = hc.getBody();
						ArrayDeque<Atom> newGoals = applyBindings(goals,newTheta);   // first apply bindings to existing goals...
						for (int i=body.size()-1; i >= 0; i--)      // push in reverse order to make sure leftmost goal if at the top of the stack
							newGoals.push((Atom)body.get(i).substitute(newTheta));    // then apply to new goals as we push them onto the deque
						answers.addAll(entails(newGoals, newTheta));
					}
				
				} else {
					System.out.println("ERROR: Sentence is not a Horn clause");
				}
				
			}
		}
		return answers;
	}
	
	/** Applies a set of bindings to a deque of goals. */
	public static ArrayDeque<Atom> applyBindings(ArrayDeque<Atom> goals, Substitution theta) {
		ArrayDeque<Atom> newGoals = new ArrayDeque<>();
		for (Atom subgoal : goals)
			newGoals.addLast((Atom)subgoal.substitute(theta));
		return newGoals;
	}
	
	/** Creates a new list of bindings with any gensym variables (i.e., those that start with "_g") removed. */
	public static Substitution filterOutGensymVariables(Substitution bindings) {
		Substitution filtered = new Substitution();
		for (Binding bind : bindings.getBindings()) {
			if (!bind.getVar().getSymbol().startsWith("_g"))
				filtered.addBinding(bind);
		}
		return filtered;
	}

	/** Given a sentence, returns a new sentence where all variables are replaced with new variables
	 * unique to this sentence. 
	 * @param s
	 * @return
	 */
	public static Sentence standardizeApart(Sentence s) {
		Set<Variable> varSet = s.getVars();
		return standardizeApart(s, varSet);	
	}

	/** Given a sentence and a set of variables, returns a new sentence where the selected variables are replaced with new variables
	 * unique to this sentence. 
	 * @param s
	 * @return
	 */
	public static Sentence standardizeApart(Sentence s, Set<Variable> varSet) {
		for (Variable var:varSet) {
			Substitution sub = new Substitution();
			sub.addBinding(new Binding(var, new Variable())); 
			s = s.substitute(sub);                      // note substitute creates a new copy of the sentence with the substitution applied
		}
		return s;
	}
	
	public static void main(String[] args) {
		Predicate parent = new Predicate("parent",2);
		Predicate child = new Predicate("child",2);
		Predicate female = new Predicate("female",1);
		Predicate mother = new Predicate("mother",2);
		Predicate ancestor = new Predicate("ancestor",2);
		// Variable x = new Variable("x");
		// Variable y = new Variable("y");
		Constant Lisa = new Constant("Lisa");
		Constant Homer = new Constant("Homer");
		Constant Marge = new Constant("Marge");
		Constant Abe = new Constant("Abe");
		Constant Someone = new Constant("Someone");
		Constant Someone2 = new Constant("Someone2");
		ArrayList<Sentence> rules = new ArrayList<>(Arrays.asList(new HornClause(new Atom(parent,y,x), new ArrayList<>(Arrays.asList(new Atom(child,x,y)))),
				new HornClause(new Atom(mother,x,y), new ArrayList<>(Arrays.asList(new Atom(parent,x,y), new Atom(female,x)))),
				new HornClause(new Atom(ancestor,x,y), new Atom(parent,x,y)),	
				new HornClause(new Atom(ancestor,x,y), new Atom(parent,x,z), new Atom(ancestor,z,y)),
				new Atom(child,Lisa,Homer),
				new Atom(child,Lisa,Marge),
				new Atom(parent,Abe,Homer),
				new Atom(parent,Someone,Abe),
				new Atom(child,Someone, Someone2),
				new Atom(female,Marge)
				));
		System.out.println(rules);
		BackwardChaining reasoner = new BackwardChaining(rules);
		System.out.println();
		System.out.println("?- parent(x, Lisa)\n" + reasoner.entails(new ArrayList<>(Arrays.asList(new Atom(parent,x, Lisa)))));  
		System.out.println();
		System.out.println("?- mother(x, Lisa)\n" + reasoner.entails(new ArrayList<>(Arrays.asList(new Atom(mother,x,Lisa)))));
		System.out.println();
		System.out.println("?- mother(x, y)\n" + reasoner.entails(new ArrayList<>(Arrays.asList(new Atom(mother,x,y)))));
		System.out.println();
		System.out.println("?- parent(x, y)\n" + reasoner.entails(new ArrayList<>(Arrays.asList(new Atom(parent,x,y)))));
		System.out.println();
		System.out.println("?- ancestor(x,Lisa)\n" + reasoner.entails(new ArrayList<>(Arrays.asList(new Atom(ancestor,x,Lisa)))));
	}
}

class BackwardChainKB {

	/** Indexes Sentences by their predicates. All sentences with the same head predicate will
	 * be stored in an array list with the predicate as its key.
	 */
	Hashtable<Predicate,ArrayList<Sentence>> rules;

	public BackwardChainKB(List<? extends Sentence> kb) {
		rules = new Hashtable<>(kb.size());
		for (Sentence s:kb) {
			s = BackwardChaining.standardizeApart(s);         // BUG? this probably won't work with recursive rules...
			if (s instanceof Atom) {
				Atom atom = (Atom)s;
				addEntry(atom.getPred(), atom);
			} else if (s instanceof HornClause) {
				HornClause hc = (HornClause)s;
				addEntry(hc.getHead().getPred(), hc);
			} else {
				System.out.println("Warning: " + s + " is not a Horn Clause!");
			}
		}
	}
	
	public void addEntry(Predicate pred, Sentence sentence) {
		ArrayList<Sentence> entries = rules.get(pred);
		if (entries == null) {
			entries = new ArrayList<>();
			entries.add(sentence);
			rules.put(pred, entries);
		} else {
			entries.add(sentence);
		}
	}
	
	/** Return all sentences that use the given predicate in their head (including atoms). */
	public ArrayList<Sentence> getCandidates(Predicate pred) {
		return rules.get(pred);
	}

}