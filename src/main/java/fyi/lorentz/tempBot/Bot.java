package fyi.lorentz.tempBot;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.json.JSONException;
import org.json.JSONObject;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.Permissions;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

public class Bot {

    private static final Logger logger = LogManager.getLogger();

    private static final String CONFIG_FILENAME = "client.json";

    public static void
    main(String[] args) {
        try {
            ClientConfig clientConfig = loadClientConfig();

            System.out.println(
                    "Authorization Link: "
                    + getAuthorizationLink(clientConfig.clientId));

            IDiscordClient botClient =
                    new ClientBuilder()
                    .withToken(clientConfig.token)
                    .registerListener(new MessageHandler())
                    .build();

            botClient.login();
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

    private static String
    getAuthorizationLink(String clientId) {
        String baseUrl = "https://discordapp.com/api/oauth2/authorize?";
        StringBuilder authUrl = new StringBuilder(baseUrl);
        authUrl.append("client_id=").append(clientId);
        authUrl.append("&");
        authUrl.append("scope=bot");
        authUrl.append("&");
        authUrl.append("permissions=");
        authUrl.append(Integer.toString(calculatePermissions()));

        return authUrl.toString();
    }

    private static int
    calculatePermissions() {
        EnumSet<Permissions> botPermissions = EnumSet.of(
                Permissions.READ_MESSAGES,
                Permissions.SEND_MESSAGES,
                Permissions.READ_MESSAGE_HISTORY,
                Permissions.EMBED_LINKS);

        return Permissions.generatePermissionsNumber(botPermissions);
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
