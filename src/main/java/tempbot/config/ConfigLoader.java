package tempbot.config;

import java.io.InputStream;
import tempbot.Constants.LoggingLevel;

public class
ConfigLoader {

	private
	ConfigLoader() { /* private constructor */ }

	public static ClientConfig
	loadConfigurationFromFile(InputStream configFile) throws ConfigLoadException {
		ConfigPropertyFetcher fetcher = new ConfigPropertyFetcher(configFile, "test input file");

		ClientConfig config = new ClientConfig();
		config.clientId = fetcher.requireConfigProperty(String.class, "clientId");
		config.secret = fetcher.requireConfigProperty(String.class, "secret");
		config.loggingLevel = fetcher.requireConfigProperty(LoggingLevel.class, "loggingLevel");

		return config;
	}

}
