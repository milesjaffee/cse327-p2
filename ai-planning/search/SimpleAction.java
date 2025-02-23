package search;

/** A class for atomic search problem actions that are distinguished
 * by a description and that have no other fields or methods.
 * @author heflin
 *
 */
public class SimpleAction extends Action {

	private String description;
	
	public SimpleAction(String description) {
		this.description = description;
	}
	
	public String toString() {
		return description;
	}
}
