package org.vanted.addons.matrix.utils;

import org.graffiti.util.Pair;

public class EqPair<T, V> extends Pair<T, V>{

	public EqPair(T val1, V val2) {
		super(val1, val2);
	}
	
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj instanceof EqPair) {
			EqPair sndPair = (EqPair) obj;
			if(getFst() != null ? !getFst().equals(sndPair.getFst()) : sndPair.getFst() != null) return false;
            return getSnd() != null ? getSnd().equals(sndPair.getSnd()) : sndPair.getSnd() == null;
        }
		return false;
	}

    @Override
    public int hashCode() {
        return this.getFst().hashCode() * 13 + (this.getSnd() == null ? 0 : this.getSnd().hashCode());
    }
}
