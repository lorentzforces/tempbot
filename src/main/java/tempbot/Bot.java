package tempbot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;
import tempbot.config.ClientConfig;
import tempbot.config.ConfigLoadException;
import tempbot.config.ConfigLoader;
import tempbot.engine.UserInputProcessor;

import static tempbot.Constants.CONFIG_FILE_NAME;
import static tempbot.Util.logToStdOut;
import static tempbot.Util.panicToStdErr;

public class Bot {

	public static void
	main(final String[] args) {
		final var config = loadClientConfig();
		setLogSettings(config);

		try {
			awaitLoginAndRegisterHandlers(config);
		} catch (final InterruptedException e) {
			Logger.error("Initialization interrupted, exiting");
		}
		Logger.info("Bot client initialization complete");

		// The call to JDABuilder::build spins up a concurrent executor which continues
		// awaiting/executing until instructed (or forced to shut down, such as from a ctrl-c
		// interrupt). Thus, returning from main does not exit the program.
	}

	private static void
	awaitLoginAndRegisterHandlers(final ClientConfig config) throws InterruptedException {
		final var jda = JDABuilder
			.createDefault(config.secret())
			.enableIntents(GatewayIntent.MESSAGE_CONTENT)
			.build();
		Logger.info("Built JDA client and awaiting connection");
		jda.awaitStatus(Status.CONNECTED);
		Logger.info("Connected, initializing message handlers");

		final var inputHandler = new BotEventHandler(
			new UserInputProcessor(ProcessorData.createAllDimensions())
		);
		jda.addEventListener(inputHandler);

		// TODO: operationalize this a bit by updating global commands when in production mode, and
		// only using guild commands when in testing mode
		final var helpCommand =
			Commands.slash(
				"help",
				"read help about TempBot or any of the things it can convert between"
			);


		// for now we're adding it as a command to all guilds
		jda.getGuildCache().stream().forEach(guild -> guild.updateCommands().addCommands(helpCommand).queue());
	}

	private static ClientConfig
	loadClientConfig() {
		logToStdOut(String.format("Attempting to load configuration from %s", CONFIG_FILE_NAME));
		final Path configFilePath = FileSystems.getDefault().getPath(CONFIG_FILE_NAME);

		ClientConfig result = null;
		try {
			final InputStream clientConfigFile =
				Files.newInputStream(configFilePath, StandardOpenOption.READ);

			result = ConfigLoader.loadConfigurationFromFile(clientConfigFile, CONFIG_FILE_NAME);
		}
		catch (final IOException e) {
			panicToStdErr(e.getMessage(), e);
		}
		catch (final ConfigLoadException e) {
			panicToStdErr(e.getMessage());
		}

		logToStdOut(String.format("Loaded client configuration from %s", CONFIG_FILE_NAME));
		return result;
	}

	private static void
	setLogSettings(final ClientConfig config) {
		Configuration.replace(LogConfigurer.configureLogging(
			config.logLevel(),
			config.logOutput(),
			config.logFormat()
		));
	}
}
