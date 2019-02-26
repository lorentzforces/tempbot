package fyi.lorentz.tempBot;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class Bot {

    private static final Logger logger = LogManager.getLogger();

    private static final String CONFIG_FILENAME = "client.json";

    public static void
    main(String[] args) {
        try {
            ClientConfig clientConfig = loadClientConfig();

            DiscordClientBuilder builder = new DiscordClientBuilder(clientConfig.token);
            DiscordClient client = builder.build();

            client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message ->
                    message.getAuthor().map(
                        user -> !user.isBot()
                    ).orElse(false))
                .subscribe(message -> {
                    MessageHandler handler = new MessageHandler();
                    handler.handle(message);
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
