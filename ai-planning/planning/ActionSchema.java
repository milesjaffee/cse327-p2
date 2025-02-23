package planning;

import fol.Variable;
import fol.Atom;

/** An action schema defined for a planning problem. Typically, the schema has a set of
 * variables that must be bound in order for it to be part of a solution. Thus, it is
 * different from search.Action. The subclass ConcreteAction is the planning equivalent of
 * search.Action.
 * @author heflin
 *
 */
public class ActionSchema {

	private String name;
	private Variable[] parameters;
	private Literal[] preconditions;
	private Atom[] addList;
	private Atom[] deleteList;
	
	/** Constructor for an action schema. The caller must provide a name, parameters,
	 * preconditions, add list and delete list. The addList and deleteList simplify the
	 * handling of effects: addList are the positive effects while deleteList are the 
	 * negative effects. 
	 * @param name Description of the schema
	 * @param parameters An array of variables that parameterize the schema
	 * @param preconditions A lists of literals that must be true for the action to be applicable
	 * @param addList A list of atoms that will be added to the state after performing the action
	 * @param deleteList A list of atoms that will be deleted from the state after performing the action
	 */
	public ActionSchema(String name, Variable[] parameters, Literal[] preconditions,
				Atom[] addList, Atom[] deleteList) {
		this.name = name;
		this.parameters = parameters;
		this.preconditions = preconditions;
		this.addList = addList;
		this.deleteList = deleteList;
	}

	/** Returns the name of the schema. */
	public String getName() {
		return name;
	}
	
	/** Returns the parameters of the schema. */
	public Variable[] getParameters() {
		return parameters;
	}
	
	/** Returns the preconditions of the schema. */
	public Literal[] getPreconditions() {
		return preconditions;
	}
	
	/** Returns the add list of the schema. These are the positive literals in its effects. */
	public Atom[] getAddList() {
		return addList;
	}
	
	/** Returns the delete list of the schema. These are the negative literals in its effects. */
	public Atom[] getDeleteList() {
		return deleteList;
	}

	/** Returns the name and parameters of the schema. */
	public String toString() {
		String temp = name + "(";
		for (int i=0; i < parameters.length-1; i++)
			temp = temp + parameters[i] + ",";
		if (parameters.length != 0)
			temp = temp + parameters[parameters.length-1];
		return temp + ")";
	}
}
