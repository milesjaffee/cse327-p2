package search;

import java.util.List;
import java.util.Random;

/** A class used to create a reachable goal state for a problem. 
 * If the actions are reversible, can also be used to generate an
 * initial state from a goal.
 */
public class ProblemGenerator {

   SearchProblem problem;
   
   public ProblemGenerator(SearchProblem problem) {
      this.problem=problem;
   }
   
   /** Given a state init, generates a random state that is
    * step moves away from it. 
    * @param init
    * @param steps
    * @return
    */
   public State generate(State init, int steps) {
      State prevState = null;
      State thisState = init;
      // ArrayList actionSeq = new ArrayList();
      int count = 0;
      
      while (count < steps) {
          List<Successor> sucList = problem.getSuccessors(thisState);
          for (int i=0; i < sucList.size(); i++)
             if (sucList.get(i).getState().equals(prevState))
                sucList.remove(i);
          Random rand = new Random();
          int choose = rand.nextInt(sucList.size());
          prevState = thisState;
          Successor successor = sucList.get(choose);
          thisState = successor.getState(); 
          // actionSeq.add(successor.getAction());
          System.out.println(successor.getAction());
          count++;
      }
      System.out.println(thisState.toString());
      return thisState;      
   }
   
}
