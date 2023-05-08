package tempbot.config;

import java.io.InputStream;
import tempbot.Constants.LogFormat;
import tempbot.Constants.LogLevel;
import tempbot.Constants.LogOutput;

public class ConfigLoader {

	private
	ConfigLoader() { /* private constructor */ }

	public static ClientConfig
	loadConfigurationFromFile(final InputStream configFile, final String readableFileName) throws ConfigLoadException {
		final ConfigPropertyFetcher fetcher = new ConfigPropertyFetcher(configFile, readableFileName);

		return ClientConfig.builder()
			.secret(fetcher.requireConfigProperty(String.class, "secret"))
			.logLevel(fetcher.requireConfigProperty(LogLevel.class, "loggingLevel"))
			.logOutput(fetcher.requireConfigProperty(LogOutput.class, "logOutput"))
			.logFormat(fetcher.requireConfigProperty(LogFormat.class, "logFormat"))
			.build();
	}

}
