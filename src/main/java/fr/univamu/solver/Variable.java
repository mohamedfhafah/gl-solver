package fr.univamu.solver;

public class Variable extends Interval {

	final private String name;
	final private boolean named;

	public Variable(int _number) {
		named = false;
		name = "_" + _number;
	}

	public Variable(String _name) {
		named = true;
		name = _name;
	}

	public Variable domain(int min, int max) {
		reduce(min, max);
		return this;
	}

	public Variable domain(int value) {
		return domain(value, value);
	}

	public int getFixedValue() {
		if (isFixed()) {
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
