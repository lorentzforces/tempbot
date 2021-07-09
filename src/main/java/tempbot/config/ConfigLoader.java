package tempbot.config;

import java.io.InputStream;

public class
ConfigLoader {

	private
	ConfigLoader() { /* private constructor */ }

	public static ClientConfig
	loadConfigurationFromFile(InputStream configFile) throws ConfigLoadException {
		ConfigPropertyFetcher fetcher = new ConfigPropertyFetcher(configFile, "test input file");

		ClientConfig config = new ClientConfig();
		config.clientId = fetcher.fetchConfigProperty(String.class, "clientId").get();
		config.secret = fetcher.fetchConfigProperty(String.class, "secret").get();

		return config;
	}

}
