package tempbot;

import java.util.HashMap;
import java.util.Map;
import tempbot.Constants.LogFormat;
import tempbot.Constants.LogLevel;
import tempbot.Constants.LogOutput;

import static tempbot.Constants.LOG_FILE_NAME;
import static tempbot.Util.logToStdOut;

public class LogConfigurer {

	protected static final String CONSOLE_WRITER_NAME = "writerConsole";
	protected static final String FILE_WRITER_NAME = "writerFile";

	protected static final String VERBOSE_FORMAT =
		"{date} {class-name}:{line} {level} {pipe} {message}";
	protected static final String CONCISE_FORMAT = "{date} {level} {pipe} {message}";

	/**
	 * Outputs Tinylog configuration, ready for consumption by
	 * {@link org.tinylog.configuration.Configuration#replace}
	 */
	public static Map<String, String>
	configureLogging(LogLevel logLevel, LogOutput logOutput, LogFormat logFormat) {
		printLoggingConfiguration(logLevel, logOutput, logFormat);
		Map<String, String> configuredProperties = new HashMap<>();

		configuredProperties.put("level", logLevel.name());

		switch (logOutput) {
			case CONSOLE_AND_FILE -> {
				addConsoleWriter(configuredProperties, logFormat);
				addFileWriter(configuredProperties, logFormat);
			}
			case CONSOLE -> addConsoleWriter(configuredProperties, logFormat);
			case FILE -> addFileWriter(configuredProperties, logFormat);
		}

		return configuredProperties;
	}

	private static void
	printLoggingConfiguration(LogLevel logLevel, LogOutput logOutput, LogFormat logFormat) {
		logToStdOut("Initializing tinylog with the following parameters:");
		logToStdOut(String.format("    log level: %s", logLevel.toString()));
		logToStdOut(String.format("    log output: %s", logOutput.toString()));
		logToStdOut(String.format("    log format: %s", logFormat.toString()));
	}

	private static void
	addConsoleWriter(Map<String, String> configuredProperties, LogFormat logFormat) {
		configuredProperties.put(CONSOLE_WRITER_NAME, "console");
		configuredProperties.put(CONSOLE_WRITER_NAME + ".format", getFormatString(logFormat));
	}

	private static void
	addFileWriter(Map<String, String> configuredProperties, LogFormat logFormat) {
		configuredProperties.put(FILE_WRITER_NAME, "file");
		configuredProperties.put(FILE_WRITER_NAME + ".file", LOG_FILE_NAME);
		configuredProperties.put(FILE_WRITER_NAME + ".format", getFormatString(logFormat));
	}

	private static String getFormatString(LogFormat logFormat) {
		return logFormat.equals(LogFormat.DEV) ? VERBOSE_FORMAT : CONCISE_FORMAT;
	}

}
