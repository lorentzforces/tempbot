package tempbot;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;
import tempbot.config.ClientConfig;
import tempbot.config.ConfigLoadException;
import tempbot.config.ConfigLoader;
import tempbot.engine.Processor;

import static tempbot.Constants.CONFIG_FILE_NAME;
import static tempbot.Util.logToStdOut;
import static tempbot.Util.panicToStdErr;

public class Bot {

	public static void
	main(String[] args) {
		ClientConfig config = loadClientConfig();
		setLogSettings(config);
		DiscordClient client = DiscordClientBuilder.create(config.secret).build();
		registerDiscordHandlersAndBlock(client);
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
		Configuration.replace(LogConfigurer.configureLogging(config.loggingLevel, config.logOutput));
	}

	private static void
	registerDiscordHandlersAndBlock(DiscordClient client) {
		Processor processor = ProcessorData.createProcesser();

		client.withGateway(gatewayClient -> {
			gatewayClient.getEventDispatcher().on(ReadyEvent.class).subscribe(
					ready -> {
						Logger.info("Bot client connected");
					}
			);

			gatewayClient.getEventDispatcher()
					.on(MessageCreateEvent.class)
					.subscribe(messageCreateEvent -> {
						Message message = messageCreateEvent.getMessage();
						boolean shouldProcessMessage =
								message.getAuthor().isPresent()
								&& !message.getAuthor().get().isBot();
						if (shouldProcessMessage) {
							User me = messageCreateEvent.getClient().getSelf().block();
							// lack of a guild id means a private message without
							// requiring another API call for channel information
							new MessageHandler(processor, me).handle(
									message,
									!messageCreateEvent.getGuildId().isPresent()
							);
						}
					});

			Logger.info("Bot client initialization complete");
			return gatewayClient.onDisconnect();
		}).block();
	}

}
