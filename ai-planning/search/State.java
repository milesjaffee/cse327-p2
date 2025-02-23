package search;

/** A generic class for representing a State. This must be
 * extended by a class that describes the state for a specific
 * problem. Note, it is important to implement the equals()
 * method to ensure that states can be compared on equality
 * of description, and not just the default comparison that
 * two references point to the same object in memory. Also,
 * there will need to be problem specific methods for setting
 * and modifying a state. 
 * @author Jeff Heflin
 *
 */
public abstract class State {

//	public State(State s) {};
	public abstract boolean equals(Object obj);
	public abstract String toString();
	// public abstract void print();
}
