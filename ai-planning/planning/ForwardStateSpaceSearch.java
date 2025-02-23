package planning;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
// import java.util.Arrays;
import fol.Atom;
import search.*;
import fol.Binding;
import fol.Substitution;

/** Casts the forward state-space search algorithm for automated planning as
 * an ordinary search problem. 
 */
public class ForwardStateSpaceSearch extends StateSpaceSearch {

	ForwardPlanState initialState;
	Literal[] goals;
	ActionSchema[] actions;
	
	public ForwardStateSpaceSearch(PlanningProblem problem) {
		List<Atom> literals = Arrays.asList(problem.getInitialState());
		initialState = new ForwardPlanState(new HashSet<Atom>(literals));
		goals = problem.getGoals();
		actions = problem.getActions();
	}
	
	  /** Return the initial state of the problem. */
	  public State getInitialState() {
		  return initialState;
	  }
	  
	  /** Get the successors of the input state. To simplify costing, instead of only
	   * returning <action,successor> pairs, we return step costs as well. This
	   * eliminates the need for a separate step cost function that takes a node,
	   * action and child state as input. The action is simply a string, and these
	   * strings are used for displaying the steps of the solution. */
	  public List<Successor> getSuccessors(State s) {
		  ForwardPlanState fps = (ForwardPlanState)s;
		  ArrayList<Successor> successors = new ArrayList<>();
		  List<ConcreteAction> allApplicable = applicableActions(fps, actions);
		  for (ConcreteAction conAct:allApplicable) {
			  Successor suc = new Successor(result(fps, conAct), conAct, 1);
			  successors.add(suc);
		  }
		  return successors;
	  }
	  
	  /** Returns true if the parameter is a goal state. */
	  // For now, we won't allow variables in the goals
	  public boolean goalTest(State currentState) {
		  ForwardPlanState bps = (ForwardPlanState)currentState;
		  Set<Atom> atoms = bps.getAtoms();
		  for (Literal l:goals) {
			  if (l.isPositive() && !atoms.contains(l.getAtom()))    // positive literal must be in state
				  return false;
			  else if (l.isNegative() && atoms.contains(l.getAtom()))          // negative literal cannot be in state
				  return false;
		  }
		  return true;
	  }
		
	  
	  /** Returns the h(n) value for state parameter. Lower h(n) values are
	   * estimated to be closer to the goal. For forward state-space search,
	   * we'll use the number of unsatisfied sub goals as a heuristic.
	   */
	  public int getHeuristicValue(State state) {
		  int h = 0;
		  
		  ForwardPlanState fps = (ForwardPlanState)state;
		  Set<Atom> atoms = fps.getAtoms();
		  for (Literal l:goals) {
			  if (l.isPositive() && !atoms.contains(l.getAtom()))    // positive literal must be in state
				  h++;
			  else if (l.isNegative() && atoms.contains(l.getAtom()))          // negative literal cannot be in state
				  h++;
		  }
		  return h;
	  }

	  /** Returns a list of concrete actions that can be performed in the given state. An
	   * action is applicable if all of its preconditions are met. Usually, this
	   * requires binding the parameters to constants from the state.
	   * @param s The state the action is performed in
	   * @param actions A list of available action schema in the domain
	   * @return A List of applicable actions. 
	   */
	  public static List<ConcreteAction> applicableActions(ForwardPlanState s, ActionSchema[] actions) {
		  List<ConcreteAction> allApplicable = new ArrayList<ConcreteAction>();
		  for (ActionSchema a:actions) {
			  // System.out.println("Considering action... " + a);
			  // find all bindings that cause preconds to match state
		    // NOTE: in order for this to work properly, all parameters must appear at least
		    // once in the preconditions
			  List<Substitution> allBindings = satisfy(Arrays.asList(a.getPreconditions()), s.getAtoms());
			  // then need to build concrete actions by applying the bindings as substitutions
			  for (Substitution binding:allBindings) {                           // if there were no matches, allBindings is empty
				  ConcreteAction conAct = new ConcreteAction(a, binding);          // may apply an empty binding if no vars
				  allApplicable.add(conAct);
			  }
		  }
		  return allApplicable;
	  }
	  
	  /** Removes all of the delete-list atoms from the previous state and adds all of the 
	   * addList atoms to it.
	   * @param state
	   * @param action
	   * @return
	   */
	  public static ForwardPlanState result(ForwardPlanState state, ConcreteAction action) {
		  Set<Atom> newAtoms = new HashSet<>(state.getAtoms());
		  for (Atom del:action.getDeleteList()) {
			  newAtoms.remove(del);
		  }
		  for (Atom add:action.getAddList()) {
			  newAtoms.add(add);
		  }
		  return new ForwardPlanState(newAtoms);
	  }
	  
	  
	  /** A custom state class to be used with forward planning
	   * The state contains a set of all atoms that are true in the state.
	   * As per database semantics, any atoms not included are assumed to 
	   * be false.
	   */
	  public static class ForwardPlanState extends State {
			
			private Set<Atom> atoms;
			
			public ForwardPlanState(Set<Atom> atoms) {
				this.atoms = new HashSet<>(atoms);
			}
			
			public Set<Atom> getAtoms() {
				return atoms;
			}
			
			public boolean equals(Object obj) {
				if (obj instanceof ForwardPlanState) {
					ForwardPlanState bps = (ForwardPlanState)obj;
					if (atoms.equals(bps.atoms))
						return true;
				}
				return false;
			}
			
			public String toString() {
				return atoms.toString();
			}
		}
}

