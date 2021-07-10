package tempbot;

import java.util.HashMap;
import java.util.Map;
import tempbot.Constants.LogOutput;
import tempbot.Constants.LoggingLevel;

import static tempbot.Constants.LOG_FILE_NAME;
import static tempbot.Util.logToStdOut;

public class LogConfigurer {

	protected static final String CONSOLE_WRITER_NAME = "writerConsole";
	protected static final String FILE_WRITER_NAME = "writerFile";

	/**
	 * Outputs Tinylog configuration
	 */
	public static Map<String, String>
	configureLogging(LoggingLevel logLevel, LogOutput logOutput) {
		printLoggingConfiguration(logLevel, logOutput);
		Map<String, String> configuredProperties = new HashMap<>();

		configuredProperties.put("level", logLevel.name());

		switch (logOutput) {
			case CONSOLE_AND_FILE: {
				addConsoleWriter(configuredProperties);
				addFileWriter(configuredProperties);
			} break;
			case CONSOLE: {
				addConsoleWriter(configuredProperties);
			} break;
			case FILE: {
				addFileWriter(configuredProperties);
			} break;
		}

		return configuredProperties;
	}

	private static void
	printLoggingConfiguration(LoggingLevel logLevel, LogOutput logOutput) {
		logToStdOut("Initializing tinylog with the following parameters:");
		logToStdOut(String.format("    log level: %s", logLevel.toString()));
		logToStdOut(String.format("    log ouptut: %s", logOutput.toString()));
	}

	private static void
	addConsoleWriter(Map<String, String> configuredProperties) {
		configuredProperties.put(CONSOLE_WRITER_NAME, "console");
	}

	private static void
	addFileWriter(Map<String, String> configuredProperties) {
		configuredProperties.put(FILE_WRITER_NAME, "file");
		configuredProperties.put(FILE_WRITER_NAME + ".file", LOG_FILE_NAME);
	}

}
