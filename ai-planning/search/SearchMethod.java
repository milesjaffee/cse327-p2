package search;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/** A generic algorithm for best-first search. This class must be extended by 
 * a specific algorithm that implements the setNodeTotalCost() method. The
 * search method is implemented as a tree-search, with the exception that 
 * it will not return to state matching the parent of the expanded node.
 * 
 * @author Jeff Heflin, Lehigh University (heflin@cse.lehigh.edu)
 *
 */
public abstract class SearchMethod {
  
  SearchProblem problem;
  FrontierQueue frontier;
  int expandedCount = 0;
  int generatedCount = 1;
  protected boolean verbose = false;
  long timeOut = 10000;              // time out in milliseconds
  
  public SearchMethod(SearchProblem problem) {
	  this(problem,false);
  }
  
  /** Initialize a search method with a custom time out. */
  public SearchMethod(SearchProblem problem, long timeOut) {
	  this(problem,timeOut,false);
  }

  /** Initialize a search method with a custom time out. */
  public SearchMethod(SearchProblem problem, long timeOut, boolean verbose) {
	  this(problem,verbose);
	  this.timeOut = timeOut;
  }

  public SearchMethod(SearchProblem problem, boolean verbose) {
	  this.problem = problem;
	  this.verbose = verbose;
	  // initialize root with the initial state
	  SearchNode rootNode = new SearchNode(problem.getInitialState(),null,null,0);
	  frontier = new FrontierQueue();
	  frontier.insert(rootNode);
  }
  

  /** Perform a search on the problem used to initialize the class. Terminates 
   *  when solutions is found, there are no nodes left to expand, or the
   *  timeout is reached (currently set at 10,000 ms) */
  public void search() {
    boolean foundGoal = false;
    SearchNode nextNode=null, nextChild;
    Iterator<SearchNode> children;
    
    long startTime = System.currentTimeMillis();
    while (foundGoal == false && frontier.isEmpty() == false &&
          timeOut >= System.currentTimeMillis() - startTime) {
      if (verbose) {
         System.out.print("Frontier f(n) values:");
         frontier.displayQueue();
      }
      nextNode = (SearchNode)(frontier.removeFirst());
      if (verbose) {
         System.out.println("Expanding " + nextNode.toString() + "...");
      }
      expandedCount++;
      if (problem.goalTest(nextNode.getState()) == false) {
        children = expand(nextNode).iterator();
        while (children.hasNext()) {
          nextChild = (SearchNode)(children.next());
          setNodeTotalCost(nextChild);
          // if (verbose)
          //   System.out.println("  Inserting " + nextChild);
          frontier.insert(nextChild);
          generatedCount++;
        }
      }
      else
        foundGoal = true;
      if (verbose)
         System.out.println();
    }
    long endTime = System.currentTimeMillis();
    if (foundGoal) {
      if (verbose) {
         System.out.println("  GOAL!");
         System.out.println();
      }
      System.out.println("Solution:");
      printSolution(nextNode);
      System.out.println();
      System.out.println("Path Cost = " + nextNode.getPathCost());
    }
    else {
       if (frontier.isEmpty())
          System.out.println("Could not solve problem.");
       else
          System.out.println("Could not solve problem in < " + timeOut + "ms!!!");
    }

    System.out.println("Nodes Expanded = " + expandedCount);
    System.out.println("Nodes Generated = " + generatedCount);
    System.out.println("Total time(ms) = " + (endTime - startTime));
  } 
  
  /** Expand a search node and return a list of children nodes. In order to
   * prune obviously bad moves, the algorithm does not generates children that
   * have states identical to that of the parent (i.e., nodes that would
   * undo the previous move). In order to save memory, it does not avoid
   * generating states that have already been expanded or that are on the
   * frontier, i.e., it is basically a tree-search as opposed to a graph
   * search. */
  public List<SearchNode> expand(SearchNode node) {
     State thisState = null, nextState = null;
     Successor successor = null;
     SearchNode parent;    // parent of the search node being expanded
     SearchNode newNode;

     thisState = node.getState();
      
     List<Successor> sucStates = problem.getSuccessors(thisState);
     for (int i=0; i<sucStates.size(); i++) {
        successor = sucStates.get(i);
        nextState = successor.getState();
        parent = node.getParent();
        State parentState = null;
        if (parent != null) {
           parentState = parent.getState();
        }
        // do not return to state prior to the current one
        if (parent == null || !nextState.equals(parentState)) {
           newNode = new SearchNode(successor.getState(), node, 
                 successor.getAction(), node.getPathCost() + successor.getStepCost());
           node.addChild(newNode);
        }
     }
     return node.getChildren();
  }

  /** Set the f(n) value for the node */
  public abstract void setNodeTotalCost(SearchNode node);
  
  /** Output the solution to the problem. This is done by recursively following
   * parent links to the root, and then printing the actions that generated
   * each node. */
  public void printSolution(SearchNode node) {
    List<Action> path = getPath(node);
    for (Action action:path) {
   	 System.out.println("  " + action);
    }
  }
  
  public List<Action> getPath(SearchNode node) {
	  /*
	  ArrayDeque<Action> path = new ArrayDeque<>();
	  while (node != null) {
		  if (node.getAction() != null)
			  path.addFirst(node.getAction());  
		  node = node.getParent();
	  }
	  return path.toArray(new Action[path.size()]);
	  */
	  // use the path extraction defined in the search problem
	  // this allows us to (on the rare occasion that it is needed) customize how the solution is extracted
	  return problem.extractPath(node);
  }
}
