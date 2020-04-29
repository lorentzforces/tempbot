package fyi.lorentz.tempbot;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import fyi.lorentz.tempbot.engine.Processor;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONException;
import org.json.JSONObject;

public class Bot {

    private static final Logger logger = LogManager.getLogger();

    private static final String CONFIG_FILENAME = "client.json";

    public static void
    main(String[] args) {
        try {
            ClientConfig clientConfig = loadClientConfig();

            Processor processor = ProcessorData.createProcesser();

            DiscordClientBuilder builder = new DiscordClientBuilder(clientConfig.token);
            DiscordClient client = builder.build();

            User me = client.getSelf().block();

            System.out.println("clientconfig token: " + clientConfig.token);

            client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .subscribe(messageCreateEvent -> {
                    Message message = messageCreateEvent.getMessage();
                    boolean shouldProcessMessage =
                            message.getAuthor().isPresent()
                            && !message.getAuthor().get().isBot();
                    if (shouldProcessMessage) {
                        // lack of a guild id means a private message without requiring
                        // another API call for channel information
                        new MessageHandler(processor, me).handle(
                                message,
                                !messageCreateEvent.getGuildId().isPresent()
                        );
                    }
                });

            client.login().block();
        }
        catch (IOException e) {
            // TODO: include specific information about what the specific issue
            // with the configuration was
            logger.fatal("Error reading client configuration client.json", e);
        }
        catch (JSONException e) {
            logger.fatal("Could not parse client.json", e);
        }
    }

    private static ClientConfig
    loadClientConfig() throws IOException, JSONException {
        Path configFilePath = FileSystems.getDefault().getPath(CONFIG_FILENAME);
        String clientConfigFile = new String(Files.readAllBytes(configFilePath));
        JSONObject clientConfigJson = new JSONObject(clientConfigFile);

        ClientConfig result = new ClientConfig();
        result.token = clientConfigJson.getString("token");
        result.clientId = clientConfigJson.getString("clientId");
        return result;
    }

}
