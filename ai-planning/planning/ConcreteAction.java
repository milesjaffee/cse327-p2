package planning;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import fol.Constant;
import fol.Substitution;
import fol.Variable;
import fol.Atom;
import fol.Binding;

/** A specific action where all of the parameters of the action schema are bound to
 * constants.
 */
public class ConcreteAction extends BoundAction {
	
	public ConcreteAction(ActionSchema action, Substitution bindings) {
		super(action,bindings);
		// should we check that all of the bindings are constants?
	}
	
	public String toString() {
		String temp = action.getName() + "(";
		for (int i=0; i < arguments.length-1; i++)
			temp = temp + arguments[i] + ",";
		if (arguments.length != 0)
			temp = temp + arguments[arguments.length-1];
		return temp + ")";
	}
}
