package fyi.lorentz.tempbot;

import static fyi.lorentz.tempbot.Constants.COMPARISON_EPSILON;

public final class Util {

	private Util() {
		// private constructor
	}

	// as with standard compare methods, returns -1
	public static boolean doublesAreEqual(double a, double b) {
		if (
				Double.isNaN(a)
				|| Double.isNaN(b)
				|| Double.isInfinite(a)
				|| Double.isInfinite(b)
		) {
			return false;
		}

		if (Math.abs(a - b) > COMPARISON_EPSILON) {
			return false;
		}

		return true;
	}

}
