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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tempbot.config.ClientConfig;
import tempbot.config.ConfigLoadException;
import tempbot.config.ConfigLoader;
import tempbot.engine.Processor;

import static tempbot.Constants.CONFIG_FILE_NAME;
import static tempbot.Util.panic;

public class Bot {

	private static final Logger logger = LogManager.getLogger();

	public static void
	main(String[] args) {
		ClientConfig config = loadClientConfig();
		// NOCHECKIN
		// TODO: initialize logging based on config
		DiscordClient client = DiscordClientBuilder.create(config.secret).build();
		registerDiscordHandlersAndBlock(client);
	}

	private static ClientConfig
	loadClientConfig() {
		Path configFilePath = FileSystems.getDefault().getPath(CONFIG_FILE_NAME);

		ClientConfig result = null;
		try {
			InputStream clientConfigFile =
					Files.newInputStream(configFilePath, StandardOpenOption.READ);

			result = ConfigLoader.loadConfigurationFromFile(clientConfigFile);
		}
		catch (IOException e) {
			panic(e.getMessage(), e);
		}
		catch (ConfigLoadException e) {
			panic(e.getMessage());
		}

		return result;
	}

	private static void
	registerDiscordHandlersAndBlock(DiscordClient client) {
		Processor processor = ProcessorData.createProcesser();

		client.withGateway(gatewayClient -> {
			gatewayClient.getEventDispatcher().on(ReadyEvent.class).subscribe(
					ready -> {
						logger.info("Bot client connected");
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
							// why the fuck is this necessary
							User me = messageCreateEvent.getClient().getSelf().block();
							// lack of a guild id means a private message without
							// requiring another API call for channel information
							new MessageHandler(processor, me).handle(
									message,
									!messageCreateEvent.getGuildId().isPresent()
							);
						}
					});

			logger.info("Bot client initialization complete");
			return gatewayClient.onDisconnect();
		}).block();
	}

}
