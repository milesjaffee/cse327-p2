package fol;

// Number is defined in java.lang, so we need a different name for this class
public class Numeric extends Constant {

	private double value;
	
	public Numeric(double value) {
		super("" + value);
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	/** Two numeric constants are considered the same if they have the same value.
	 */
	public boolean equals(Object o) {
		if (o instanceof Numeric) {
			if (value == ((Numeric)o).value)
				return true;
		}
		return false;
	}
	
}
