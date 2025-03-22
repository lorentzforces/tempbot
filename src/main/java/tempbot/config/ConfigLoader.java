package tempbot.config;

import java.io.InputStream;
import tempbot.Constants.CommandScope;
import tempbot.Constants.LogFormat;
import tempbot.Constants.LogLevel;
import tempbot.Constants.LogOutput;

import static tempbot.config.ConfigPropertyFetcher.fetchConfigEnvVar;

public class ConfigLoader {

	private
	ConfigLoader() { /* private constructor */ }

	public static ClientConfig
	loadConfigurationFromFile(
		final InputStream configFile,
		final String readableFileName
	) throws ConfigLoadException {
		final var fetcher = new ConfigPropertyFetcher(configFile);

		try {
			return ClientConfig.builder()
				.secret(fetcher.requireConfigProperty(String.class, "secret"))
				.logLevel(fetcher.requireConfigProperty(LogLevel.class, "loggingLevel"))
				.logOutput(fetcher.requireConfigProperty(LogOutput.class, "logOutput"))
				.logFormat(fetcher.requireConfigProperty(LogFormat.class, "logFormat"))
				.commandScope(fetcher.requireConfigProperty(CommandScope.class, "commandScope"))
				.build();
		} catch (ConfigLoadException e) {
			throw new ConfigLoadException(
				String.format("Error loading configuration: %s", readableFileName),
				e
			);
		}
	}

	// TODO: not everything has to be env-var-able
	public static ClientConfig
	applyEnvVarsToConfig(final ClientConfig config) throws ConfigLoadException {
		try {
			final var secret = fetchConfigEnvVar(String.class, "TEMPBOT_SECRET");
			final var loggingLevel = fetchConfigEnvVar(LogLevel.class, "TEMPBOT_LOG_LEVEL");
			final var logOutput = fetchConfigEnvVar(LogOutput.class, "TEMPBOT_LOG_OUTPUT");
			final var logFormat = fetchConfigEnvVar(LogFormat.class, "TEMPBOT_LOG_FORMAT");
			final var commandScope = fetchConfigEnvVar(CommandScope.class, "TEMPBOT_COMMAND_SCOPE");

			return ClientConfig.builder()
				.secret(secret.orElse(config.secret()))
				.logLevel(loggingLevel.orElse(config.logLevel()))
				.logOutput(logOutput.orElse(config.logOutput()))
				.logFormat(logFormat.orElse(config.logFormat()))
				.commandScope(commandScope.orElse(config.commandScope()))
				.build();
		} catch (ConfigLoadException e) {
			throw new ConfigLoadException("Error loading configuration value from env var", e);
		}
	}

}
