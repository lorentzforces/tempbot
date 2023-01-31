package tempbot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;
import tempbot.config.ClientConfig;
import tempbot.config.ConfigLoadException;
import tempbot.config.ConfigLoader;

import static tempbot.Constants.CONFIG_FILE_NAME;
import static tempbot.Util.logToStdOut;
import static tempbot.Util.panicToStdErr;

public class Bot {

	public static void
	main(String[] args) {
		ClientConfig config = loadClientConfig();
		setLogSettings(config);

		try {
			awaitLoginAndRegisterHandlers(config);
		} catch (InterruptedException e) {
			Logger.error("Initialization interrupted, exiting");
		}
		Logger.info("Bot client initialization complete");
	}

	private static void
	awaitLoginAndRegisterHandlers(ClientConfig config) throws InterruptedException {
		var processor = ProcessorData.createProcesser();
		var jda = JDABuilder
			.createDefault(config.secret)
			.enableIntents( GatewayIntent.MESSAGE_CONTENT)
			.build();
		Logger.info("Built JDA client and awaiting connection");
		jda.awaitStatus(Status.CONNECTED);
		Logger.info("Connected, initializing message handlers");
		jda.addEventListener(new MessageHandler(processor, jda.getSelfUser().getId()));
	}

	private static ClientConfig
	loadClientConfig() {
		logToStdOut(String.format("Attempting to load configuration from %s", CONFIG_FILE_NAME));
		Path configFilePath = FileSystems.getDefault().getPath(CONFIG_FILE_NAME);

		ClientConfig result = null;
		try {
			InputStream clientConfigFile =
				Files.newInputStream(configFilePath, StandardOpenOption.READ);

			result = ConfigLoader.loadConfigurationFromFile(clientConfigFile, CONFIG_FILE_NAME);
		}
		catch (IOException e) {
			panicToStdErr(e.getMessage(), e);
		}
		catch (ConfigLoadException e) {
			panicToStdErr(e.getMessage());
		}

		logToStdOut(String.format("Loaded client configuration from %s", CONFIG_FILE_NAME));
		return result;
	}

	private static void
	setLogSettings(ClientConfig config) {
		Configuration.replace(LogConfigurer.configureLogging(
			config.logLevel,
			config.logOutput,
			config.logFormat
		));
	}

}
