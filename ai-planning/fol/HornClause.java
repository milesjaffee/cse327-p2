package fol;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

public class HornClause extends Implies {

	private Atom head;
	private List<Atom> body;
	
	public HornClause(Atom head, List<Atom> body) {
		super(null, head);             // should we turn the body into a multi-conjunction?
		this.head = head;
		this.body = body;
	}

	/** Constructs a new Horn clause from a sequence of Atoms, where the first is the head, and the
	 * rest are the body.
	 * @param head
	 * @param bodyArray
	 */
	public HornClause(Atom head, Atom... bodyArray) {
		this(head, Arrays.asList(bodyArray));
	}

	
	public Atom getHead() {
		return head;
	}
	
	public List<Atom> getBody() {
		return body;
	}
	
	public Sentence substitute(Substitution bindings) {
		List<Atom> subBody = new ArrayList<>(body.size());
		for (Atom b: body)
			subBody.add((Atom)b.substitute(bindings));
		return new HornClause((Atom)head.substitute(bindings), subBody);
	}

	@Override
	public Set<Variable> getVars() {
		Set<Variable> varSet = head.getVars();
		for (Atom atom:body) {
			varSet.addAll(atom.getVars());              
		           // this will change the set returned by head.getVars(), but that's okay as long as we create a new one with every call
		}
		return varSet;
	}

	public String toString() {
		String s = "";
		for (int i=0; i < body.size(); i++) {
			if (i < body.size() - 1)
				s = s + body.get(i) + " ^ ";
			else
				s = s + body.get(i);
		}
		s = s + " => " + head;
		return s;
	}
}
