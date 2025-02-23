package search;

/** This class is used for actions defined for search problems. Usually, the concrete
 * subclass SimpleAction will be sufficient. Any concrete subclasses should override the 
 * toString() method, as this will be used to output the action as part of a solution.
 * @author heflin
 *
 */
public abstract class Action {
   // no fields or methods, the class exists simply for polymorphism
	public abstract String toString();
}
