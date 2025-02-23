package planning;

import fol.Atom;
import fol.Constant;
import fol.Predicate;
import fol.Variable;
import search.SearchMethod;
import search.SearchProblem;
import search.UniformCost;

/** A planning problem that has a PDDL-like description. To specify the problem,
 * you need to provide an initial state, a conjunction of goals, and an array
 * of action schema.
 * 
 * To define state, goals, and schemas, you will need to use various classes from
 * the fol package: Predicate, Constant, Variable, Atom and the Literal class from
 * planning. For example, to define the action schema MoveToTable() from the 
 * blocks world domain, you could write:
 * 
 * <pre>
 * public static final Predicate On = new Predicate("On",2);
 * public static final Predicate Clear = new Predicate("Clear",1);
 * public static final Predicate Block = new Predicate("Block",1);
 *
 * public static final Variable x = new Variable("x");
 * public static final Variable y = new Variable("y");

 * public static final ActionSchema moveToTableAct = 
 *   new ActionSchema("MoveToTable", new Variable[]{x, y},
 *         new Literal[]{new Literal(On,x,y), new Literal(Clear, x)},
 *         new Atom[]{new Atom(On,x,Table), new Atom(Clear, y)},
 *         new Atom[]{new Atom(On,x,y)})
 * </pre>
 *         
 * A typical usage of this class would be:
 * 
 * <pre>
 *  PlanningProblem myprob = new PlanningProblem(initialState, goal, actions);
 *  SearchProblem problem = new ForwardStateSpaceSearch(myprob);
 *  SearchMethod search = search = new UniformCost(problem);
 * </pre>
 */
public class PlanningProblem {

	private Atom[] initialState;
	private Literal[] goals;
	private ActionSchema[] actions;
	
	/** Define a planning problem given 1) an array of Atoms describing the initial state,
	 * an array of Literals describing the conditions of the goal state, and an array
	 * of ActionSchema describing the actions. 
	 * @param initialState
	 * @param goals
	 * @param actions
	 */
	public PlanningProblem(Atom[] initialState, Literal[] goals, ActionSchema[] actions) {
		this.initialState = initialState;
		this.goals = goals;
		this.actions = actions;
	}
	
	public Atom[] getInitialState() {
		return initialState;
	}
	
	public Literal[] getGoals() {
		return goals;
	}
	
	public ActionSchema[] getActions() {
		return actions;
	}
	
}
