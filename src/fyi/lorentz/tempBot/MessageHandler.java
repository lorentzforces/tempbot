package fyi.lorentz.tempBot;

import fyi.lorentz.tempBot.service.TemperatureProcessor;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MessageHandler implements IListener<MessageReceivedEvent> {

    @Override
    public void
    handle(MessageReceivedEvent event) {
        String inputText = event.getMessage().getFormattedContent();

        TemperatureProcessor tempProcessor = new TemperatureProcessor(inputText);
        String output = tempProcessor.processMessage();

        if(output.length() > 0) {
            event.getChannel().sendMessage(output);
        }
    }
}
