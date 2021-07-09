package tempbot;

import static tempbot.Constants.COMPARISON_EPSILON;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Util {

	private static final Logger logger = LogManager.getLogger();

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
	panic(String errorMessage) {
		logger.fatal(errorMessage);
		System.exit(1);
	}

	public static void
	panic(String errorMessage, Throwable t) {
		logger.fatal(errorMessage, t);
		System.exit(1);
	}

}
