package search;

import java.util.ArrayList;
import java.util.List;

/** A problem defined for use with an AI search algorithm. This class
 * must be extended with a specific class to define the problem, which
 * implements all of the abstract methods.
 * @author Jeff Heflin, Lehigh University (heflin@cse.lehigh.edu)
 *
 */
public abstract class SearchProblem {
  
  /** Return the initial state of the problem. */
  public abstract State getInitialState();
  
  /** Get the successors of the input state. To simplify costing, instead of only
   * returning <action,successor> pairs, we return step costs as well. This
   * eliminates the need for a separate step cost function that takes a node,
   * action and child state as input. The action is simply a string, and these
   * strings are used for displaying the steps of the solution. */
  public abstract List<Successor> getSuccessors(State s);
  
  /** Returns true if the parameter is a goal state. */
  public abstract boolean goalTest(State currentState);
  
  /** Returns the h(n) value for state parameter. Lower h(n) values are
   * estimated to be closer to the goal.
   */
  public abstract int getHeuristicValue(State state);
    
  /** Builds the path from the initial state to the current node.  This is done by recursively following
   * parent links to the root, and then add the actions that generated each node to an ArrayList. */
  public List<Action> extractPath(SearchNode node) {
	  ArrayList<Action> path = new ArrayList<>();
	  return extractPath(node,path);
  }
  
  /** Recursive helper for extractPath(SearchNode) */
  private List<Action> extractPath(SearchNode node, List<Action> path) {
	  if (node != null && node.getAction() != null) {        // remember, the root's action will be null
		  path = extractPath(node.getParent(), path);
		  path.add(node.getAction());
		  return path;
	  }
	  else 
		  return path;
  }

}
