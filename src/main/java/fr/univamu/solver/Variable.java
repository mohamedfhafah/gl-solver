package fr.univamu.solver;

public class Variable extends Interval {

    static private long anonymousCounter = 0;
	final private String name;
	final private boolean named;

	public Variable() {
		named = false;
		name = "_" + (++anonymousCounter);
	}

	public Variable(String _name) {
		named = true;
		name = _name;
	}

	public int getFixedValue() {
		if (isOneValue()) {
			return (getMin());
		}
		throw new IllegalStateException("variable not fixed: " + this);
	}

	public String toString() {
		return (String.format("%s%s", this.name, super.toString()));
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public String getName() {
		return name;
	}

	public boolean isNamed() {
		return named;
	}

}
