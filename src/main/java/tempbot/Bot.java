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
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import tempbot.engine.Processor;

import static tempbot.Constants.CONFIG_FILENAME;

public class Bot {

	private static final Logger logger = LogManager.getLogger();

	public static void
	main(String[] args) {
		try {
			ClientConfig clientConfig = loadClientConfigYaml();

			Processor processor = ProcessorData.createProcesser();

			DiscordClient client = DiscordClientBuilder.create(clientConfig.secret).build();

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
		catch (IOException e) {
			logger.fatal("Error reading client configuration " + CONFIG_FILENAME, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static ClientConfig
	loadClientConfigYaml() throws IOException {
		Path configFilePath = FileSystems.getDefault().getPath(CONFIG_FILENAME);
		InputStream clientConfigFile =
				Files.newInputStream(configFilePath, StandardOpenOption.READ);
		Load yamlLoader = new Load(
				LoadSettings.builder().setLabel("Tempbot configuration file").build()
		);
		Map<String, String> configProperties =
				(Map<String, String>) yamlLoader.loadFromInputStream(clientConfigFile);

		ClientConfig result = new ClientConfig();
		result.secret = fetchStringConfigProperty(configProperties, "secret");
		result.clientId = fetchStringConfigProperty(configProperties, "clientId");
		return result;
	}

	@SuppressWarnings("unchecked")
	private static String
	fetchStringConfigProperty(
			Map<String, ?> properties,
			String propertyName
	) {
		String propertyValue = null;
		try {
			propertyValue = (String) properties.get(propertyName);
		}
		catch (ClassCastException e) {
			logger.fatal(
					"Property type mismatch in " + CONFIG_FILENAME + " : "
					+ e.getMessage());
			System.exit(1);
		}

		if (propertyValue == null) {
			logger.fatal(
					"Could not find property " + propertyName
					+ " in configuration "
					+ CONFIG_FILENAME);
			System.exit(1);
		}

		return propertyValue;
	}

}
