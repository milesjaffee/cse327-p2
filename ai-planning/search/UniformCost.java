package search;

/** The Uniform Cost Search method. Implements best-first search with f(n) = g(n) */
public class UniformCost extends SearchMethod {
  
  public UniformCost(SearchProblem problem) {
    super(problem);
  }
  
  /** Set the f(n) value for the node. f(n) = g(n) */
  public void setNodeTotalCost(SearchNode node) {
    node.setEvalFunCost(node.getPathCost());
  }
  
}
