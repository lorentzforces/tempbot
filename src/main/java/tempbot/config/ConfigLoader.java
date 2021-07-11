package tempbot.config;

import java.io.InputStream;
import tempbot.Constants.LogFormat;
import tempbot.Constants.LogLevel;
import tempbot.Constants.LogOutput;

public class ConfigLoader {

	private
	ConfigLoader() { /* private constructor */ }

	public static ClientConfig
	loadConfigurationFromFile(InputStream configFile, String readableFileName) throws ConfigLoadException {
		ConfigPropertyFetcher fetcher = new ConfigPropertyFetcher(configFile, readableFileName);

		ClientConfig config = new ClientConfig();
		config.clientId = fetcher.requireConfigProperty(String.class, "clientId");
		config.secret = fetcher.requireConfigProperty(String.class, "secret");
		config.logLevel = fetcher.requireConfigProperty(LogLevel.class, "loggingLevel");
		config.logOutput = fetcher.requireConfigProperty(LogOutput.class, "logOutput");
		config.logFormat = fetcher.requireConfigProperty(LogFormat.class, "logFormat");

		return config;
	}

}
