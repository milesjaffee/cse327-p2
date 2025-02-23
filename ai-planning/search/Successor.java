package search;

/** A triple consisting of <successr-state, action, step-cost> that 
 * is used as the output of the SearchProblem.getSuccessors() method.
 * @author Jeff Heflin
 *
 */
public class Successor {

   protected State state;
   protected Action action;
   protected int stepCost;
   
   /** Create a new Successor */
   public Successor(State state, Action action, int stepCost) {
      this.state = state;
      this.action = action;
      this.stepCost = stepCost;
   }
   
   /** Get the successor's state. */
   public State getState() {
      return state;
   }
   
   /** Get the successor's action. */
   public Action getAction() {
      return action;
   }
   
   /** Get the step cost of moving to the successor via the action. */
   public int getStepCost() {
      return stepCost;
   }
   
   public String toString() {
	   return "<" + action + ", " + state + ">\n";
   }
}
