package planning;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import fol.Atom;
import fol.BackwardChaining;
import fol.Binding;
import fol.Sentence;
import fol.Substitution;
import fol.Term;
import fol.Variable;
import search.*;

/** WARNING: This code currently has several errors and should not be used.
 * Casts the backward state-space search algorithm for automated planning as
 * an ordinary search problem. 
 */
// TODO: fix backward-state-space search!!!
public class BackwardStateSpaceSearch extends StateSpaceSearch {

	BackPlanState initialState;                 // the goals of the planning problem
	List<Atom> goals;                               // the initial state of the planning problem
	ActionSchema[] actions;
	Substitution solutionBindings = null;          // If the problem is solved, the bindings used
	BackwardChaining goalsKB;
	
	/** Given a planning problem, transforms it into a search problem
	 * using backward state-space search. 
	 * @param problem A planning problem definition.
	 */
	public BackwardStateSpaceSearch(PlanningProblem problem) {
	  // the initial state consists of the goal literals of the problem
		List<Literal> literals = Arrays.asList(problem.getGoals());
		initialState = new BackPlanState(literals);
		// the goal is to find a state that is entailed by the initial state 
		goals = Arrays.asList(problem.getInitialState());
		goalsKB = new BackwardChaining(goals);
		actions = problem.getActions();
	}

	/** Return the initial state of the problem (i.e., the state with
	 * the set of planning goals to achieve. */
	public State getInitialState() {
		return initialState;
	}

	/** Get the successors of the input state. To simplify costing, instead of only
	 * returning <action,successor> pairs, we return step costs as well. This
	 * eliminates the need for a separate step cost function that takes a node,
	 * action and child state as input. The action is simply a string, and these
	 * strings are used for displaying the steps of the solution. For backward
	 * state-space search, the successors are determined by relevant actions,
	 * and the states are regressions of the goal over the actions.  */
	public List<Successor> getSuccessors(State s) {
		// find all of the relevant actions and compute their predecessors
		BackPlanState bps = (BackPlanState)s;
		ArrayList<Successor> successors = new ArrayList<>();
		Set<BoundAction> relActions = relevantActions(bps, actions);
		for (BoundAction relAct : relActions) {
			Successor suc = new Successor(predecessor(bps, relAct), relAct, 1);
			successors.add(suc);
		}
		// TODO: consider replacing findAdditionalSpecializedActions with a method here that looks at
		// successors and determines if an additional binding (possibly with variables not appearing
		// in the action schemata), can lead to a predecessor state with fewer literals (by essentially
		// matching a state literal with a positive effect, where one or the other has variables in it)
		
		return successors;
	}

	/** Returns true if the parameter is a goal state. */
	// Issue: When two goals can be achieved by a single action (e.g. Fly(p1,x,JFK) achieves Unload(C1,x,JFK)'s precond At(p1,JFK)
	// and At(P1,JFK)), we only delete one of them currently. This seems to lead to infinite loops. Is it necessary to
	// go back and test for removing goals whenever we extend the bindings or find a candidate solution?
	// Alternatively, we could try creating extra relevant actions for each additional binding with something from the add list
	public boolean goalTest(State currentState) {
		// the goal is true if the current (predecessor) state is satisfied by the initial state
		// this means that each goal unifies with something in the initial state, and none of
		// the antibindings are violated...
		BackPlanState bps = (BackPlanState)currentState;
		Set<Literal> predGoals = bps.getLiterals();
		List<Substitution> candBinds = satisfy(new ArrayList<>(predGoals), goals);
		for (Substitution solution : candBinds) {                  // test the bindings for consistency
		  // must compare anti-bindings to all bindings that led to this state
		  Substitution compSub = new Substitution(bps.getBindings());
		  compSub.compose(solution);
			if (isConsistent(compSub, bps.getAntiBindings())) {
				solutionBindings = compSub;
				System.out.println("Bindings: " + compSub.toString());
				return true;
			}
		}
		return false;          // if we get here, we did not find a consistent set of bindings to satisfy the predecessor state
	}


	/** Returns the h(n) value for state parameter. Lower h(n) values are
	 * estimated to be closer to the goal. For backward state-space search, we use 
	 * the number of literals in the state description that are not satisfied by the
	 * initial planning state as the heuristic. 
	 */
	// TO-DO: This should really work by doing a backward-chaining search where it counts the minimum number
	// of subgoals fulfilled at each point where it fails. The number of of subgoals unfulfilled are simply the
	// goals left on the stack that were in the original problem (after unwinding the substitutions)
	public int getHeuristicValue(State state) {
		// int h = 0;

		BackPlanState bps = (BackPlanState)state;
		Set<Literal> predGoals = bps.getLiterals();
		// TO-DO: need to fix to handle negative literals
		satisfyWithAntiBindings(new ArrayList<>(predGoals), bps.getAntiBindings(), goals);         // we don't care about the returned results, only the side-effect...
		// minFailedGoals is an instance variable that is set by satisfyWithAntiBindings
		return minFailedGoals;
	}

	/** Builds the path from the initial state to the current node.  Because this is backward state-space search,
	 * we need to reverse the path. This is done by adding each item to the end of the list and then recursively following
	 * parent links to the root.  */
	public List<search.Action> extractPath(SearchNode node) {
		ArrayList<search.Action> path = new ArrayList<>();
		return extractPath(node,path);
	}

	/** Recursive helper for extractPath(SearchNode) */
	private List<search.Action> extractPath(SearchNode node, List<search.Action> path) {
		if (node != null && node.getAction() != null) {                  // remember the root's action will be null
			BoundAction bndAct = (BoundAction)node.getAction();
			// Note: do we still need to build the substitutions in this way, or does composition give us
			// a complete set of bindings at the goal state?
			Substitution fullBindings = new Substitution(bndAct.getBindings());    // make a copy
			if (solutionBindings != null)
				fullBindings.union(solutionBindings);
			path.add(new BoundAction(bndAct.getAction(), fullBindings));
			return extractPath(node.getParent(), path);
		} else 
			return path;
	}

	/** Find all actions that are relevant to at least one of the literals in the predecessor
	 * state description s. The variables are bound as necessary, and bindings that are prohibited
	 * in order to be consistent with the other literals are also identified. If the state has
	 * a literal that cannot be achieved (i.e., no relevant actions and it does not appear in the
	 * initial state of the problem), then all actions are pruned to avoid unnecessary search.
	 * @param s
	 * @param actions
	 * @return
	 */
	public HashSet<BoundAction> relevantActions(BackPlanState s, ActionSchema[] actions) {
		// for each goal (atom), find the actions that achieve that goal
		Set<Literal> allGoals = s.getLiterals();
		HashSet<BoundAction> allRelevant = new HashSet<BoundAction>();
		
		for (Literal goal : allGoals) {  // find actions that are relevant to a single goal
		  List<BoundAction> relActs = relevantActions(goal, allGoals, actions);
		  if (relActs.size() == 0) {  // if a goal has no relevant actions, might be able to prune this branch
		    if (goal.isPositive()) {
		      Atom posGoal = goal.getAtom();
		      if (unifyFromList(goals.toArray(new Atom[0]), posGoal).size() == 0) {  // could not unify with anything in the initial state
		        allRelevant.clear();        // prune all option from this state because one goal will never be achieved
		        System.out.println("Pruning state due to unachievable literal " + goal);
		        break;
		      }
		    } else {
		      System.out.println("Negative literals not supported");
		    }
		  }
			allRelevant.addAll(relActs);
		}
		// once we have all actions that are relevant to a single goal, see if there are ways to specialize them
		// so that they apply to more goals (find additional bindings that lead to additional matches between the
		// add-list and the goal literals). Add these new actions as appropriate...
		Set<BoundAction> addSpecActs = findAdditionalSpecializedActions(allRelevant, allGoals);
		if (!addSpecActs.isEmpty()) {
			System.out.println("Found some specialized acts:");
			System.out.println(addSpecActs);
		}
		allRelevant.addAll(addSpecActs);
		return allRelevant;
	}

	/** A helper method for relevantActions(BackPlanSate, Action[]). Returns all actions that are
	 * relevant to the specific goal. 
	 * 
	 * @param goal
	 * @param allGoals
	 * @param actions
	 * @return
	 */
	private static List<BoundAction> relevantActions(Literal goal, Set<Literal> allGoals, ActionSchema[] actions) {
		// find actions that have the goal as an effect, without undoing any other goals 
		System.out.println("Achieving: " + goal);
		List<BoundAction> allRelevant = new ArrayList<BoundAction>();
		for (ActionSchema a:actions) {
			// if the goal literal is positive, it must unify with something in the add-list
			// if it is negative, then it must unify with something in the delete-list (not yet implemented)
			List<Substitution> allBindings = null;
			if (goal.isPositive())
				allBindings = unifyFromList(a.getAddList(),goal.getAtom());
			else {
				System.out.println("Negative goal literals not yet implemented!");
			}
			if (!allBindings.isEmpty()) {
			  // The following will also prune any actions that undoes a goal
				List<BoundAction> newActs = createBoundActionsFromSubstitutions(a, allGoals, allBindings);
				allRelevant.addAll(newActs);
			}
		}
		System.out.println("   " + allRelevant);
		return allRelevant;
	}

	/** Given an action, the set of goals, and a set of substitutions that make the action
	 * relevant to a goal, create one or more bound actions that are relevant to the goal.
	 * Will prune actions that are inconsistent with a goal and create anti-bindings as necessary.
	 * @param act
	 * @param allGoals
	 * @param allBindings
	 */
	private static List<BoundAction> createBoundActionsFromSubstitutions(ActionSchema act, Set<Literal> allGoals, List<Substitution> allBindings) {
		// apply the substitutions to define the actions
		List<BoundAction> newActs = new ArrayList<BoundAction>();
		for (Substitution binds : allBindings) {
			List<Substitution> antiBinding = testConsistency(act, binds, allGoals);
			if (antiBinding != null) {          // null means the action is always inconsistent; do not add it!!!!
				if (antiBinding.isEmpty()) {     // testConsistency returns an empty list when there are no possible inconsistencies              
					BoundAction boundAct = new BoundAction(act, binds); 
					boundAct.standardizeApart();
					newActs.add(boundAct);						      								 
				} else if  (antiBinding.get(0).getBindings().length > 0) {     // it returns a list with an empty substitution when the action is always inconsistent
					// if there are multiple sets in antiBinding, these are ORs
					// must create a distinct relevant action for each...
					for (Substitution anti:antiBinding) {
						// make sure that each bound action gets a different substitution object
						// this is to make sure that future changes to one does not effect the others...
						Substitution bindsCopy = new Substitution(binds);
						BoundAction boundAct = new BoundAction(act, bindsCopy, anti);    
						boundAct.standardizeApart();                     // this usually ends up modifying the bindsCopy from above
						newActs.add(boundAct);
					}
				}
			}
		}
		return newActs;
	}
	
	// *** called by relevantActions() ***
	/** Given a set of Literals and the initial set of relevant actions, will extends the relevant actions
	 * with any actions that can be specialized so that some of their preconditions are satisfied by
	 * the current state. This will ensure that if two goals have a common action that leads to both
	 * then that action will be found. This motivated by the example goal At(C1,JFK) and At(P1,JFK) where
	 * both the actions Unload(C1,p,JFK) and Unload(C1,P1,JFK) should be explored.  Using
	 * only the first Unload will create an additional At(p,JFK).
	 * @param allRelevant
	 * @param allGoals
	 * @return
	 */
	// TODO: This currently can't solve the At(P1,JFK), At(C1,JFK) problem. Search is infinite (2/1/2025)
	// DEBUGGING HISTORY: 
	// Changed to alt strategy that tries to unify previously found relevant actions
  // E.g., If we have Fly(p, x, JFK) and Fly(P1, y, JFK) as actions to achieve At(p,JFK) and At(P1,JFK),
  // create a third action Fly(P1, y, JFK) but with the additional sub p=P1.
	// 1/2/2025: Did the following, but seems to be creating some incorrect actions. Found the incorrect solution:
	//        Fly(P1,LAX,JFK), Load(C1,P1,LAX), Unload(C1,P1,JFK),  Fly(P1,LAX,JFK)
	// 2/1/2025: Fixed isConsstent() to remove the incorrect solution, but back to infinite search
	// 2/2/2025: Applied bindings to new states to get a solution with one extra (bad?) action, But the output needs to replace vars with bindings!!!!
	private static Set<BoundAction> findAdditionalSpecializedActions(Set<BoundAction> allRelevant, Set<Literal> allGoals) {
	  
	  Set<BoundAction> newRelActs = new HashSet<>();
	  ArrayList<BoundAction> relList = new ArrayList(allRelevant);
		for (int i=0; i < relList.size(); i++) { 
		  BoundAction boundAct = relList.get(i);
      for (int j=i+1; j < relList.size(); j++) {  // only try to match with subsequent actions
        BoundAction candAct = relList.get(j);
        Substitution theta = boundAct.unify(candAct);
        if (theta != null && isConsistent(theta, boundAct.getAntiBindings()) && isConsistent(theta, candAct.getAntiBindings())) {
          Substitution newSub = new Substitution(boundAct.getBindings());
          newSub.compose(theta);
          Substitution newAnti = new Substitution(boundAct.getAntiBindings());
          newAnti.union(candAct.getAntiBindings());
          newRelActs.add(new BoundAction(boundAct.action, newSub, newAnti));
        }
      }
		  

/*
			// strategy: remove the achieved goals from the state, then try to unify the effects
			// with the remaining goals, constrained by the existing unifier
			Set<Literal> newLits = new HashSet<>(allGoals);
			for (Atom delLit:boundAct.getAddList()) {
				newLits.remove(new Literal(delLit));
			}
			List<Substitution> allBindings = null;
			ActionSchema a = boundAct.getAction();
			for (Literal goal:newLits) {
				if (goal.isPositive())
					// BUG? need to make sure that the returned bindings have at most one binding per variable
					allBindings = unifyFromList(Arrays.asList(a.getPreconditions()), goal.getAtom(), boundAct.getBindings());   // BUG: This is finding new bindings for goals like Airport(_g3)
				else {
					System.out.println("Negative goal literals not yet implemented!");
				}
				if (!allBindings.isEmpty()) {                      // empty means unify failed!
					// TO-DO: maybe we should check if the new bindings are just renamings of the original ones...
					// But I actually think we need some renamings to make the algorithm work correctly
				  // TODO: 10/1/24: a binding in allBindings is often a superset of the bindings for the act. This appears to do nothing
				  while (allBindings.contains(boundAct.getBindings()))
				    allBindings.remove(boundAct.getBindings());           // if any of the bindings are the same as the one originally used, remove it
					List<BoundAction> newActs = createBoundActionsFromSubstitutions(a, allGoals, allBindings);    // BUG: this is creating inconsistent actions like Fly(C1,JFK,JFK)...
					// TODO: 10/1/24: Need to avoid adding the same action twice, or adding actions that are already in allRelevant
					newRelActs.addAll(newActs);
				}
			}
    */
		}
		return newRelActs;
	}
	
	
	
	/** Calculate the predecessor of a state assuming a particular action was applied to arrive
	 * at the current state, by regressing from g over a. In particular, the positive literals
	 * are the positive literals of g, minus the add list of a, plus the positive preconditions
	 * of a. The negative literals are the negative sliterals of g, minus the delete list of a,
	 *  plus the negative preconditions of a. 
	 * @param state
	 * @param action
	 * @return
	 */
	public static BackPlanState predecessor(BackPlanState state, BoundAction action) {
		// first apply the bindings of the action to the state description
	  // this is necessary to properly support specialized actions
    Set<Literal> newLits = new HashSet<>();
    for (Literal lit:state.getLiterals()) {
      Literal subLit = lit.substitute(action.getBindings());
      newLits.add(subLit);
    }
    
	  // add the preconditions and remove the addList, be sure to carry antibindings forward
		// NOTE: If we allow for variables to be bound later, we'll also need to apply the
		// substitutions from the action to the literals carried back from the current state
		for (Atom addLit:action.getAddList()) {
			newLits.remove(new Literal(addLit));
		}
    // TODO: does not currently handle removing the delete list from the negative goals 
		for (Literal posPreLit:action.getPreconditions()) {
		  // since all preconditions are just added, doesn't matter whether they are negative or positive
		  newLits.add(posPreLit);
		}

		Substitution bindings = new Substitution(state.getBindings());
		bindings.compose(action.getBindings());
		
		Substitution antiBindings = new Substitution(action.getAntiBindings());       // make a copy of the anti-bindings for the action
		antiBindings.union(state.getAntiBindings());                              // add the antibinding to those of the state
		return new BackPlanState(newLits, bindings, antiBindings);
	}

	
	// below we redefine satisfy to count the number of unfulfilled goals..
	private static int minFailedGoals;         // as of the most recent call to satisfy...
	
	
	/** Determine if a collection of atoms describing a state satisfy a list of goal 
	 *  literals. If so, returns a list of all sets of bindings that make it possible. 
	 *  If the goal is not satisfied, then returns an empty list.If it is satisfied without 
	 *  bindings, then it returns a list containing an empty list. This method will
	 *  update the class variable minFailedGoals with the smallest number of original goals that
	 *  the algorithm was unable to prove. */ 
	public static List<Substitution> satisfyWithAntiBindings(List<Literal> goalLits, Substitution antiBinds, Collection<Atom> state) {
		// do this recursively. unify first precond with an atom, then recurse on rest of goal literals
		// use an int to keep track of location in list? also need current bindings....
		minFailedGoals = goalLits.size();
		return satisfyWithAntiBindings(goalLits, antiBinds, 0, state, new Substitution(), minFailedGoals);
	}

	public static List<Substitution> satisfyWithAntiBindings(List<Literal> goalLits, Substitution antiBinds, int first, Collection<Atom> state, Substitution theta, int unprovenGoals) {
		List<Substitution> answers = new ArrayList<Substitution>();
		if (first == goalLits.size()) {                // base case: we've considered every goal 
			if (unprovenGoals == 0)              // if we've matched every literal, theta is one answer
				answers.add(theta);                   // if no bindings are required, theta will be an empty substitution
			if (unprovenGoals < minFailedGoals)			// update minFailedGoals if we've found a smaller number
				minFailedGoals = unprovenGoals;
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
					// test to make sure the substitution is consistent with the antiBindings
					if (isConsistent(newTheta, antiBinds)) {
						List<Substitution> result = satisfyWithAntiBindings(goalLits, antiBinds, first+1, state, newTheta, unprovenGoals - 1);   // recursion
						if (!result.isEmpty()) {
							answers.addAll(result);
						}
					}	
				} 
			} else {
				System.out.println("Negative literals not yet handled");
			}
		}
		if (answers.isEmpty()) { // if we were unable to match this goal, consider an option where we skip it altogether
			// note, if answers is not empty, the unproven goals should be 0, so no need to keep looking for alternative options
			// look to see how many other goals could be proven, used in the heuristic
			
			// try to prune unnecessary calls, i.e., if the remaining goals are fewer than those needed to beat the current best, don't do this...
			int goalsRemaining = goalLits.size() - first;
			int distanceFromMin = unprovenGoals - minFailedGoals;
			if (goalsRemaining > distanceFromMin) {
				List<Substitution> unused = satisfyWithAntiBindings(goalLits, antiBinds, first+1, state, theta, unprovenGoals);         // we only care about updates to minFailedGoals here
			}
		}	
		return answers;
	}
	
		

	/** Unify each of a set of options from with an array of atoms with a target atom. Returns all bindings
	 * that result in a match. If there are no matches, returns an empty list. If there is
	 * a match without the need for bindings, this is returned as a list with an
	 * empty list of bindings. The variables in the array of atoms will be given precedence in
	 * resulting substitutions. */
	// Note, it may be the case that multiple items in the add list of a single action
	// match with the same goal. E.g., consider, an action that might clear multiple
	// blocks. Each of these could unify with Clear(B)...
	private static List<Substitution> unifyFromList(Atom[] targets, Atom atom) {
		// currently used in: relevantActions()
		// this is redundant with the method below, but is kept for efficiency since
		// otherwise we would have to convert each Atom to a literal and then insert
		// it into a collection
		List<Substitution> result = new ArrayList<>(); 
		for (Atom candidate : targets) {
			Substitution temp = candidate.unify(atom);
			          /* in Atom.unify(), the parameter provides the replacement terms for substitutions.
			           Since the targets are taken from action descriptions, these are the values that
			 		     we want to replace in our substitutions */
			if (temp != null) {
				result.add(temp);
			}
		}
		return result;
	}


	/** Unify a single atom with a Collection of literals. Returns all bindings
	 * that result in a match. If there are no matches, returns an empty list. If there is
	 * a match without the need for bindings, this is returned as a list with an
	 * empty list of bindings. */
	// Note, it may be the case that multiple items in the add list of a single action
	// match with the same goal. E.g., consider, an action that might clear multiple
	// blocks. Each of these could unify with Clear(B)...
	private static List<Substitution> unifyWithList(Atom atom, Collection<Literal> targets) {
		// currently used in: testInconsistency()
		List<Substitution> result = new ArrayList<>(); 
		for (Literal candidate : targets) {
			if (candidate.isPositive()) {
				Substitution temp = atom.unify(candidate.getAtom());	
				if (temp != null) {
					result.add(temp);
				}
			} else 
				System.out.println("unifyWithList(): Neg. literals not yet supported!");
		}
		return result;
	}


	/** Unify a single atom with a Collection of literals. Returns all bindings
	 * that result in a match. If there are no matches, returns an empty list. If there is
	 * a match without the need for bindings, this is returned as a list with an
	 * empty list of bindings. */
	// Note, it may be the case that multiple items in the add list of a single action
	// match with the same goal. E.g., consider, an action that might clear multiple
	// blocks. Each of these could unify with Clear(B)...
	private static List<Substitution> unifyFromList(Collection<Literal> targets, Atom atom, Substitution theta) {
		// currently used in: findAdditionalSpecializedActions
		List<Substitution> result = new ArrayList<>(); 
		for (Literal candidate : targets) {
			if (candidate.isPositive()) {
				// for findAdditionalSpecializedActions() we want to preserve the variables
				// from the targets, which are the variables in the action schema
				Substitution temp = candidate.getAtom().unify(atom, theta);	
				if (temp != null) {
					result.add(temp);
				}
			} else 
				System.out.println("unifyWithList(): Neg. literals not yet supported!");
		}
		return result;
	}

	/** Given an action, a set of bindings for it, and a set of goals, determines if the action
	 * is consistent with the goals. If it is always inconsistent with a goal, then returns 
	 * null. If it is consistent as long as certain bindings are 
	 * prohibited, then those bindings (called anti-bindings here) are returned. If it is always 
	 * consistent, then return an empty list. If there are different combinations of anti-bindings, each
	 * of these is a separate Substitution object.
	 * @param a
	 * @param binds
	 * @param allGoals
	 * @return
	 */
	// BUG: This method needs to distinguish between two different bindings for the same goal.
	// These should be treated as ORs not ANDs, since an inconsistency will only happen if both
	// bindings occur. We can then use these ORs to produce different options for actions...
	// JDH 2/23/18: Did I mean if the same goal resulted in a binding for each of two or more
	// variables? If so, I think it has been fixed below for bind.size() > 1...
	public static List<Substitution> testConsistency(ActionSchema a, Substitution binds, Set<Literal> allGoals) {
		List<Substitution> antiBindings = new ArrayList<>();
		Atom[] delList = a.getDeleteList();
		for (Atom delAtom : delList) {
			Atom delLit = delAtom.substitute(binds);
			List<Substitution> matchBinds = unifyWithList(delLit, allGoals);   // BUG? What if goals are negative?
			// if matchBinds is empty, then there are no matches, and the action is safe (consistent)
			for (Substitution bind : matchBinds) {  
				// if matchBinds contains a substitution with no variables, that means the action is always 
				// inconsistent with the goals. Therefore, we must return null
				if (bind.getNumBindings() == 0)
					return null;
				else if (bind.getNumBindings() == 1) {                 // only one variable is bound, extend the existing antBindings
					Binding theBinding = bind.getBindings()[0];       // we know there is exactly one binding, get it
					
					if (antiBindings.isEmpty())           // if there are no anti-bindings yet, create one with the found inconsistency...
						antiBindings.add(bind);
					else {
						for (Substitution oneList:antiBindings) {         // for anti-bindings, the same variable could be paired with different values
							oneList.addBinding(theBinding);                // so we have to add this to each possible set
						}
					}
				} else {                // need to replicate the anti-bindings and extend them with each option
					if (antiBindings.isEmpty()) {               // must start brand new lists with each extension
						for (Binding extension:bind.getBindings()) {
							Substitution extendedList = new Substitution();
							extendedList.addBinding(extension);
							antiBindings.add(extendedList);
						}
					} else {
						List<Substitution> newAntiBinds = new ArrayList<>();
						for (Substitution oldAntiList : antiBindings) {         // one new list for each combo of old and an extension
							for (Binding extension:bind.getBindings()) {
								// make a copy of the existing lists before extending them
								Substitution extendedList = new Substitution(oldAntiList); 
								extendedList.addBinding(extension);
								newAntiBinds.add(extendedList);
							}
						}
						antiBindings = newAntiBinds;
					}

				}
			}
		}
		return antiBindings;
	}

	/** Returns true if a list of bindings is consistent with a list of prohibited bindings
	 * (called the anti-bindings). 
	 * @param bindings - Should be a list of concrete bindings
	 * @param antiBindings - Pairs of terms that cannot be equal. Could by X/a or X/Y
	 * @return
	 */
	private static boolean isConsistent(Substitution bindings, Substitution antiBindings) {
		for (Binding anti : antiBindings.getBindings()) {             // check each anti-binding 
		  // we assume each anti is of form antiVar/antiBan
			Variable antiVar = anti.getVar();

			// if it is possible to unify the antiBinding with its substitution, then we have an inconsistency
			// Substitution result = antiVar.unify(anti.getSub(), bindings);
			// if (result != null)            // if we find a unifier, bindings are not consistent
			//	return false;

	    // Note, the following is need to use this with non-ground anti-binding.
		  // E.g., For the test case of At(P1,JFK), At(C1,JFK), there is an antibinding g1 != g11 that must be caught
			// to avoid incorrect solutions
			Term antiBan = anti.getSub();
			// Note: this will not terminate if bindings has X/Y and Y/X, but I don't think that is possible
			while (antiBan != null && antiBan instanceof Variable) { // if the antiBan is a variable, must replace with the substitute for this variable
        antiBan = bindings.lookupSubstitute((Variable)antiBan);
			}
			if (antiBan != null) {     // if the ban was a variable not present in bindings, it is not inconsistent 
        Term bind = bindings.lookupSubstitute(antiVar);          // find a binding for the anti-variable, there should be only one!!!
        if (bind != null) {
          if (antiBan.equals(bind))  // if bound to the same value, we have an inconsistency
            return false;
        } 
      }
		}
		return true;            // if no problems were detected, the bindings are consistent
	} 

//	/** Given a list of bindings, returns the one that use a specific variable. */
//	public static Binding varLookup(List<Binding> bindings, Variable var) {
//		for (Binding bind:bindings) {
//			if (bind.getVar().equals(var)){
//				return bind;
//			}
//		}
//		return null;
//	}
	
	/** A state for backward state-space search. Maintains the list of goals we need
	 * to achieve in order to have found a solution and a set of anti-bindings:
	 * values that the variables cannot take if the relevant actions are to be
	 * consistent with the goals they achieve. */
	public static class BackPlanState extends State {

		private Set<Literal> literals;                 // these literals may contain variables
		private Substitution bindings;                // the bindings that were used for the literals
		                  // note: we need these to check antibinding because we are now substituting in when creating 
		private Substitution antiBindings;            // values that are prohibited for the variables

		public BackPlanState(Collection<Literal> literals) {
      // 2/3/25: avoid creating useless objects we'll use null instead 
		  //of an empty list for the antiBindings (2/3/25: Does this make sense?)
			this(literals, new Substitution(), null);
		}

		public BackPlanState(Collection<Literal> literals, Substitution antiBindings) {
	     this(literals, new Substitution(), antiBindings);
		}

    public BackPlanState(Collection<Literal> literals, Substitution bindings, Substitution antiBindings) {
      if (literals instanceof Set)
        this.literals = (Set<Literal>)literals;
      else
        this.literals = new HashSet<>(literals);
      this.bindings = bindings;
      this.antiBindings = antiBindings;
    }

		public Set<Literal> getLiterals() {
			return literals;
		}

		/** Returns the bindings. Lazily creates a new empty substitution
		 * and records it if necessary. 
		 * @return
		 */
    public Substitution getBindings() {
      if (bindings == null)
        bindings = new Substitution();
      return bindings;
    }

		 /** Returns the antibindings. Lazily creates a new empty substitution
		  * and records it if necessary.
		  * @return
		  */
		public Substitution getAntiBindings() {
      if (antiBindings == null)
        antiBindings = new Substitution();
      return antiBindings;
		}

		/** Two backward-planning states are considered equivalent if one is simply a variable
		 * renaming of the other. This must consider the literals, and the anti-bindings.
		 * Note, this version of equals will only help prune nodes from the search if we are
		 * doing a graph search, but the current version of SearchMethod does a tree search.
		 */
		public boolean equals(Object obj) {
			if (obj instanceof BackPlanState) {
				BackPlanState bps = (BackPlanState)obj;
				List<Literal> litList = new ArrayList<>(literals);
				List<Literal> candidates = new ArrayList<>(bps.literals); 
				Substitution theta = new Substitution();
				Substitution newTheta = renamingTestHelper(litList, candidates, 0, theta);
				if (newTheta != null) {            // one state's literals are simply a renaming of the other...
					// now check anti-bindings
					// consider antibinding x != y and x2 != y2 and unifier {x/x2, y/y2}
					// build a new substitution, where newTheta is applied to both sides of all bindings
					Substitution thetaAnti = new Substitution();
					for (Binding bind : antiBindings.getBindings()) {
						// apply newTheta to each anti-binding
						Binding newBind = new Binding((Variable)(bind.getVar().substitute(newTheta)), bind.getSub().substitute(newTheta));
						thetaAnti.addBinding(newBind);
					}
					if (thetaAnti.equals(bps.antiBindings))   // the bindings are Sets, so this will work even if the "orders" are different
						return true;
					else                           // two states with equivalent literals but different antiBindings are not the same!!!!
						return false;
				} else                         // two states with different literals are not the same
					return false;
			}
			return false;
		}

		/** Test whether one lists of literals is just a renaming of another. If it is, Will return a unifier
		 * consisting of only unique variables. Otherwise returns null. The method makes no assumptions
		 * about the relative ordering of literals within the lists.
		 * @param list1
		 * @param list2
		 * @param start
		 * @param theta
		 * @return
		 */
	   private Substitution renamingTestHelper(List<Literal> list1, List<Literal> list2, int start, Substitution theta) {

	   	if (start >= list1.size())             // base case: if we've matched all of the literals in list1
	   		return theta;
			for (Literal possMatch : list2) {
				Substitution newTheta = list1.get(start).unify(possMatch, theta);
				if (newTheta != null && newTheta.isRenaming()) { // if we have a match that is only a renaming, we are one step closer to being equal
					List<Literal> newList2 = new ArrayList<>(list2);
					newList2.remove(possMatch);
					newTheta = renamingTestHelper(list1, newList2, start+1, newTheta);
					if (newTheta != null)
						return newTheta;
				}
			}	
			return null;          // if it gets here, no renamings were found.
	   }	
						
	  /** Return a string that contains the goal literals and anti-bindings. */
		public String toString() {
			String temp = literals.toString();
			if (antiBindings != null) {
				for (Binding anti : antiBindings.getBindings()) {
					temp = temp + ", " + anti.getVar() + " != " + anti.getSub();
				}
			}
			return temp;
		}
	}
	
	
}