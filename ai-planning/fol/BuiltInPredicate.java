package fol;

public abstract class BuiltInPredicate extends Predicate {

	// use an anonymous class to define a BuiltInPredicate
	public static BuiltInPredicate lt = new BuiltInPredicate("lt",2) {
		public boolean evaluate(Term[] args) {
			if (args[0] instanceof Numeric && args[1] instanceof Numeric) {
				double leftVal = ((Numeric)args[0]).getValue();
				double rightVal = ((Numeric)args[1]).getValue();
				return leftVal < rightVal;
			}
			System.out.println("Evaluating built-in with non-numers: " + args[0] + " or " + args[1]);
			return false;
		}
	};

	public static BuiltInPredicate lte = new BuiltInPredicate("lte",2) {
		public boolean evaluate(Term[] args) {
			if (args[0] instanceof Numeric && args[1] instanceof Numeric) {
				double leftVal = ((Numeric)args[0]).getValue();
				double rightVal = ((Numeric)args[1]).getValue();
				return leftVal <= rightVal;
			}
			System.out.println("Evaluating built-in with non-numers: " + args[0] + " or " + args[1]);
			return false;
		}
	};

	public static BuiltInPredicate gt = new BuiltInPredicate("gt",2) {
		public boolean evaluate(Term[] args) {
			if (args[0] instanceof Numeric && args[1] instanceof Numeric) {
				double leftVal = ((Numeric)args[0]).getValue();
				double rightVal = ((Numeric)args[1]).getValue();
				return leftVal > rightVal;
			}
			System.out.println("Evaluating built-in with non-numers: " + args[0] + " or " + args[1]);
			return false;
		}
	};

	public static BuiltInPredicate gte = new BuiltInPredicate("gte",2) {
		public boolean evaluate(Term[] args) {
			if (args[0] instanceof Numeric && args[1] instanceof Numeric) {
				double leftVal = ((Numeric)args[0]).getValue();
				double rightVal = ((Numeric)args[1]).getValue();
				return leftVal >= rightVal;
			}
			System.out.println("Evaluating built-in with non-numers: " + args[0] + " or " + args[1]);
			return false;
		}
	};

	// TO-DO: what should happen when eq is used with non-numbers? Especially if one is a variable?
	public static BuiltInPredicate eq = new BuiltInPredicate("eq",2) {
		public boolean evaluate(Term[] args) {
			if (args[0] instanceof Numeric && args[1] instanceof Numeric) {
				double leftVal = ((Numeric)args[0]).getValue();
				double rightVal = ((Numeric)args[1]).getValue();
				return leftVal == rightVal;
			}
			System.out.println("Evaluating built-in with non-numers: " + args[0] + " or " + args[1]);
			return false;
		}
	};

	public BuiltInPredicate(String symbol, int arity) {
		super(symbol,arity);
	}
	
	public abstract boolean evaluate(Term[] args);
}


