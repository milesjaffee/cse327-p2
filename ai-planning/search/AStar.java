package search;

/** The A* Search method. Implements best-first search with f(n) = g(n) + h(n). */
public class AStar extends SearchMethod {

  public AStar(SearchProblem problem) {
    super(problem);
  }

  public AStar(SearchProblem problem, long timeOut) {
	  super(problem, timeOut);
  }

  public AStar(SearchProblem problem, boolean verbose) {
	    super(problem,verbose);
	  }

	  public AStar(SearchProblem problem, long timeOut, boolean verbose) {
		  super(problem, timeOut, verbose);
	  }

  /** Set the f(n) value for the node. f(n) = g(n) + h(n) */
  public void setNodeTotalCost(SearchNode node) {
    node.setEvalFunCost(node.getPathCost()
                      + problem.getHeuristicValue(node.getState()));
  }

}
