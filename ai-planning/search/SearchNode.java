package search;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/** A node in the search tree. Records the state, parent node, action, path cost,
 * evaluation function cost, and children. 
 * @author Jeff Heflin
 *
 */
public class SearchNode {

   State state;
   SearchNode parent;
   Action action;
   int pathCost;        // g(n)
   int evalFunCost;       // f(n)
   Vector<SearchNode> childNodes;


   /** Create a new node in the search tree. */
   public SearchNode(State state, SearchNode parent, Action action, int pathCost) {
      this.state = state;
      this.parent = parent;
      this.action = action;
      this.pathCost = pathCost;
      childNodes = new Vector<SearchNode>();
   }

   /** Add a child to this node. */
   public void addChild(SearchNode child) {
      childNodes.addElement(child);
   }

   /** Return all children of the node. */
   public List<SearchNode> getChildren() {
      return childNodes;
   }

   /** Get the evaluation function value for this node, i.e., f(n).  */
   public int getEvalFunCost() {
      return evalFunCost;
   }

   /** Get the action that generated this node. */
   public Action getAction() {
      return action;
   }
   
   /** Get the parent of this node. */
   public SearchNode getParent() {
      return parent;
   }
   
   /** Get the path from the root to the current node. */
   public List<Action> getPath() {
   	ArrayList<Action> path = new ArrayList<>();
   	return getPath(this, path);
   }

   /** Recursive helper method for getPath() */ 
   private List<Action> getPath(SearchNode node, List<Action> path) {
   	if (node != null && node.action != null) {                // remember root has a null action
   		path = getPath(node.parent, path);
   		path.add(node.action);
   	}
   	return path;
   }
   
   /** Return the cost of the path from the initial state to this node, i.e., g(n) */
   public int getPathCost() {
      return pathCost;
   }

   /** Get the state associated with this node. */
   public State getState() {
      return state;
   }

   /** Set the evaluation function value for this node, i.e., f(n). */
   public void setEvalFunCost(int evalFunCost) {
      this.evalFunCost = evalFunCost;
   }

   /** Generate some basic information about the node; useful for debugging */
   public String toString() {
      return action + "\t(f(n)=" + getEvalFunCost() + ", g(n)=" + pathCost + "):\n\t" + state.toString() + "\n\t" + getPath();
   }
}


