package planning;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import fol.Term;
import fol.Variable;
import fol.Atom;
import fol.Binding;
import fol.Constant;
import fol.Substitution;

public class BoundAction extends search.Action {

	protected ActionSchema action;
	protected Term[] arguments;
	private Substitution bindings;               // bindings required for the action
	private Substitution  antiBindings;           // bindings that make the action inconsistent
		
	public BoundAction(ActionSchema action, Substitution bindings, Substitution antiBindings) {
		this.action = action;
		this.bindings = bindings;
		this.antiBindings = antiBindings;
		Variable[] params = action.getParameters();
		// arguments are used for toString() and unifying with other BoundActions
		arguments = new Term[params.length];
		for (int i=0; i < params.length; i++) {
			arguments[i] = (Term)params[i].substitute(bindings);
			// if the argument is a gensym, see if you can substitute again...
			// this is a bit of hack, and it would probably be better to compose the bindings...
			if (arguments[i].toString().startsWith("_g"))
				arguments[i] = ((Variable)arguments[i]).substitute(bindings);
		}
	}

	public BoundAction(ActionSchema action, Substitution bindings) {
		this(action, bindings, new Substitution());
	}

	public ActionSchema getAction() {
		return action;
	}
	
	public Substitution getBindings() {
		return bindings;
	}
	
	public Substitution  getAntiBindings() {
		return antiBindings;
	}
	
	public Term[] getArguments() {
		return arguments;
	}
	
	public List<Literal> getPreconditions() {
		List<Literal> boundPreconds = new ArrayList<>();
		for (Literal lit:action.getPreconditions())
			boundPreconds.add(lit.substitute(bindings));
		return boundPreconds;
	}
	
	public Set<Atom> getDeleteList() {
		Set<Atom> boundAtoms = new HashSet<Atom>();
		for (Atom del:action.getDeleteList()) {
			boundAtoms.add((Atom)del.substitute(bindings));
		}
		return boundAtoms;
	}

	public Set<Atom> getAddList() {
		Set<Atom> boundAtoms = new HashSet<Atom>();
		for (Atom add:action.getAddList()) {
			boundAtoms.add((Atom)add.substitute(bindings));
		}
		return boundAtoms;
	}

	/** Replaces any variables remaining in the action with unique variables. This is to ensure
	 * that each bound action has a distinct set of variables from any other actions that might
	 * occur in the plan. */
	public void standardizeApart() {
		Variable[] params = action.getParameters();
		for (int i=0; i < params.length; i++ ) {                // check each parameter of the action
			Variable var = params[i];
			boolean match = false;
			for (Binding bind:bindings.getBindings()) {        // if there are no bindings for the argument...
				if (var.equals(bind.getVar())) {
					match = true;
					break;
				}
			}
			if (match == false) {                        // need to standardize apart this variable
				Variable newVar = new Variable();
				bindings.addBinding(new Binding(var, newVar));    // because we are modifying bindings here, it is important that each action has its own copy of the bindings!!!
				arguments[i] = newVar;                     // must update the arguments so toString() works correctly.
				if (antiBindings != null) 
					antiBindings.replaceOccurencesOf(var, newVar);
			}
		}
	}
	
	/** Tries to unify the current BoundAction with the parameter. Returns the MGU if they 
	 * unify and null if they don't.
	 * don't unify. 
	 * @param otherAct
	 * @return
	 */
	public Substitution unify(BoundAction otherAct) {
	  Substitution theta = new Substitution();
	  if (otherAct.action.equals(action)) {
	    for (int i=0; i < arguments.length; i++) {
	        theta = arguments[i].unify(otherAct.arguments[i], theta);
	        if (theta == null)                                        // early exit if one of the terms fails to unify...
	          return null;
	    }
	  } else {
	    return null;
	  }
	  return theta;
	}
	
	public String toString() {
		String temp = action.getName() + "(";
		for (int i=0; i < arguments.length-1; i++)
			temp = temp + arguments[i] + ",";
		if (arguments.length != 0)
			temp = temp + arguments[arguments.length-1];
		temp = temp + ")";
		Binding[] antiBindArray = antiBindings.getBindings();
		for (int i=0; i < antiBindArray.length; i++) {
			if (i==0)
				temp = temp + " where ";
			else 
				temp = temp + " and ";
			temp = temp + antiBindArray[i].getVar() + "!=" + antiBindArray[i].getSub();
		}
		return temp;
	}
	
	/** Two bound actions are equivalent if they have the same action schema, same bindings
	 * and same antibindings.
	 */
	public boolean equals(Object o) {
	  if (o instanceof BoundAction) {
	    BoundAction ba = (BoundAction)o;
	    return (this.action.equals(ba.action) && this.bindings.equals(ba.bindings) &&
	        this.antiBindings.equals(ba.antiBindings));
	  } else
	    return false;
	}
	
	// needed so this class can be used with HashSets
	public int hashCode() {
	  return toString().hashCode();
	}
}
