package fr.univamu.solver;

import java.util.function.Consumer;

public class Variable extends Interval {

    static private long anonymousCounter = 0;
	final private String name;
	final private boolean named;
	private Consumer<Variable> observer = null;

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

	/**
	 * Définit l'observateur qui sera notifié quand le domaine de cette variable change.
	 *
	 * @param observer le Consumer qui sera appelé avec cette variable quand reduce() modifie le domaine
	 */
	public void setObserver(Consumer<Variable> observer) {
		this.observer = observer;
	}

	/**
	 * Retourne l'observateur actuel de cette variable.
	 *
	 * @return l'observateur, ou null s'il n'y en a pas
	 */
	public Consumer<Variable> getObserver() {
		return observer;
	}

	/**
	 * Réduit le domaine de cette variable et notifie l'observateur si des changements ont été faits.
	 * Surcharge la méthode de Interval pour ajouter la notification.
	 *
	 * @param newMin la nouvelle borne inférieure
	 * @param newMax la nouvelle borne supérieure
	 * @return true si le domaine a été modifié, false sinon
	 */
	@Override
	public boolean reduce(int newMin, int newMax) {
		boolean changed = super.reduce(newMin, newMax);
		if (changed && observer != null) {
			observer.accept(this);
		}
		return changed;
	}

	/**
	 * Réduit le domaine de cette variable selon un autre intervalle et notifie l'observateur si nécessaire.
	 * Surcharge la méthode de Interval pour ajouter la notification.
	 *
	 * @param i l'intervalle selon lequel réduire
	 * @return true si le domaine a été modifié, false sinon
	 */
	@Override
	public boolean reduce(Interval i) {
		boolean changed = super.reduce(i);
		if (changed && observer != null) {
			observer.accept(this);
		}
		return changed;
	}

}
