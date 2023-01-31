package tempbot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
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
		var config = loadClientConfig();
		setLogSettings(config);

		try {
			awaitLoginAndRegisterHandlers(config);
		} catch (InterruptedException e) {
			Logger.error("Initialization interrupted, exiting");
		}
		Logger.info("Bot client initialization complete");

		// The call to JDABuilder::build spins up a concurrent executor which continues
		// awaiting/executing until instructed (or forced to shut down, such as from a ctrl-c
		// interrupt). Thus, returning from main does not exit the program.
	}

	private static void
	awaitLoginAndRegisterHandlers(ClientConfig config) throws InterruptedException {
		var jda = JDABuilder
			.createDefault(config.secret)
			.enableIntents(GatewayIntent.MESSAGE_CONTENT)
			.build();
		Logger.info("Built JDA client and awaiting connection");
		jda.awaitStatus(Status.CONNECTED);
		Logger.info("Connected, initializing message handlers");

		var messageHandler = new MessageHandler(ProcessorData.createProcesser(), jda.getSelfUser().getId());
		var eventHandler = new EventHandler(
			messageHandler
		);
		jda.addEventListener(eventHandler);

		var helpCommand =
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

	// TODO: pull this somewhere that makes sense
	private static class EventHandler extends ListenerAdapter {
		private final MessageHandler messageHandler;

		public EventHandler(MessageHandler messageHandler) {
			this.messageHandler = messageHandler;
		}

		@Override
		public void
		onMessageReceived(MessageReceivedEvent event) {
			if (!event.getAuthor().isBot()) {
				messageHandler.handle(event.getMessage(), event.isFromType(ChannelType.PRIVATE));
			}
		}

		// TODO: operationalize this a bit by updating global commands when in production mode, and
		// only using guild commands when in testing mode
		@Override
		public void
		onSlashCommandInteraction(SlashCommandInteractionEvent event) {
			switch(event.getName()) {
				case "help" -> event.reply(HELP_TEXT).setEphemeral(true).queue();
			}
		}

		private static final String HELP_TEXT =
			"Hi!\n"
			+ "**To see this help text, tag or DM me with \"help\" in the message.**\n"
			+ "If you send a message to me or in a channel I can see, I'll do my best to "
			+ "convert any units I recognize in your message. If the unit you want to see "
			+ "in the results doesn't appear, try mentioning me in the message (I convert to "
			+ "more--usually uncommon--units if you tag me).\n"
			+ "To convert to a specific unit, tag or DM me and write \"*<unit value>* to "
			+ "*<result unit>*\"";
	}

}
