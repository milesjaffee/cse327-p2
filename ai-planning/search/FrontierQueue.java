package search;

import java.util.ArrayList;
import java.util.Iterator;

/** An ordered list of search nodes that have not yet been expanded.
 * @author Jeff Heflin 
 */
public class FrontierQueue {
  
  // Although one might think a LinkedList would be better, since inserts
  // and deletes could occur anywhere in the queue, empirical tests have
  // shown that ArrayList is faster. Perhaps this is due to the ability
  // to efficiently locate a node at a specific location.
  // If performance was of utmost importance, then implementing
  // a priority queue using a heap would be even better (although
  // significantly more complicated).
  ArrayList<SearchNode> queue;
  
  public FrontierQueue() {
    queue = new ArrayList<SearchNode>();
  }
  
  /** Insert a new node into the queue in order of f-cost */ 
  public void insert(SearchNode newNode) {
    
    int i = 0;
    boolean done = false;
    SearchNode qNode;
    
    while (i < queue.size() && !done) {
      qNode = (SearchNode)(queue.get(i));
      // when all is equal we prefer nodes that appear to be closer to the goal
      if (newNode.getEvalFunCost() == qNode.getEvalFunCost() &&        // if f(n) is same, the one with larger g(n) goes first
      		newNode.getPathCost() > qNode.getPathCost()) {
         queue.add(i, newNode);
         done = true;
       } else if (newNode.getEvalFunCost() < qNode.getEvalFunCost()) {
        queue.add(i, newNode);
        done = true;
      }
      else
        i++;
    }
    if (done == false)
      queue.add(newNode);
  }
  
  /** Return true if there are no nodes in the queue. */
  public boolean isEmpty() {
    return queue.isEmpty();
  }
  
  /** Remove the first node from the queue and return it. */
  public SearchNode removeFirst() {
    
    SearchNode temp = queue.get(0);
    queue.remove(0);
    return temp;
  }
  
  /** Display debugging information about the queue to standard output. */
  public void displayQueue() {
    
    System.out.print("{");
    Iterator<SearchNode> nodes = queue.iterator();
    while (nodes.hasNext()) {
      SearchNode next = nodes.next();
      System.out.print(next.getEvalFunCost() + ",");
//      System.out.print(((State)next.getState()).toString() + ", ");
    }
    System.out.println("}");
  }
  
  public String toString() {
	  String s = "{";
	  for (SearchNode next:queue)
		  s = s + next + "\n"; 
	  s = s + "}";
	  return s;
  }
  
}
