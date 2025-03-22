package tempbot;

public final class Constants {

	// number of decimal places of precision for display
	public static final int PRECISION = 3;
	// we set our comparison epsilon range to half the smallest precision (basically rounding)
	public static final double COMPARISON_EPSILON = Math.pow(0.1d, PRECISION) / 2;

	public static final int MAX_CONVERSIONS = 10;

	public static final String CONFIG_FILE_NAME = "client.yml";

	public enum LogLevel {
		ERROR, WARN, INFO, DEBUG, TRACE
	}

	public enum LogOutput {
		CONSOLE, FILE, CONSOLE_AND_FILE
	}

	public enum CommandScope {
		GUILD, GLOBAL
	}

	public static final String LOG_FILE_NAME = "tempbot.log";

	public enum LogFormat {
		DEV, NON_DEV
	}

}
