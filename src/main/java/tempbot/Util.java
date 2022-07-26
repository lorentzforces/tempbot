package tempbot;

import static tempbot.Constants.COMPARISON_EPSILON;

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

	public static void
	logToStdOut(String message) {
		System.out.println(message);
	}

	public static void
	panicToStdErr(String message) {
		System.err.println(message);
		System.exit(1);
	}

	public static void
	panicToStdErr(String message, Throwable t) {
		System.err.println(message);
		t.printStackTrace();
		System.exit(1);
	}

}
