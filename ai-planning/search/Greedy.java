package search;

/** The Greedy Best-First Search method. Implements best-first search with f(n) = h(n). */
public class Greedy extends SearchMethod {
  
  public Greedy(SearchProblem problem) {
    super(problem);
  }
  
  /** Set the f(n) value for the node. f(n) = h(n) */
  public void setNodeTotalCost(SearchNode node) {
    node.setEvalFunCost(problem.getHeuristicValue(node.getState()));
  }
  
}
