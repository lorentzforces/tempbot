package fyi.lorentz.tempBot;

import discord4j.core.object.entity.Message;

import fyi.lorentz.tempBot.service.TemperatureProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler {

    private static final Logger logger = LogManager.getLogger(MessageHandler.class);

    public void
    handle(Message message) {
        handleTemperatures(message);
    }

    private void
    handleTemperatures(Message message) {
        String inputText = message.getContent().orElse("");

        TemperatureProcessor tempProcessor = new TemperatureProcessor(inputText);
        String output = tempProcessor.processMessage();

        if (output.length() > 0) {
            message.getChannel().subscribe(channel -> {
                channel.createMessage(output).subscribe();
            });
        }
    }

}
