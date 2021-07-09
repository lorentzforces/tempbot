package tempbot;

public final class Constants {

	// number of decimal places of precision for display
	public static final int PRECISION = 3;
	// we set our comparison epsilon range to half the smallest precision (basically rounding)
	public static final double COMPARISON_EPSILON = Math.pow(0.1d, PRECISION + 1) / 2;

	public static final int MAX_CONVERSIONS = 10;

	public static final String CONFIG_FILE_NAME = "client.yml";

	public enum LoggingLevel {
		FATAL, ERROR, WARN, INFO, DEBUG, TRACE
	}

}
