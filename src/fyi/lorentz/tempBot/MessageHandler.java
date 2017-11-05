package fyi.lorentz.tempBot;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MessageHandler implements IListener<MessageReceivedEvent> {

    @Override
    public void
    handle(MessageReceivedEvent event) {
        String author = event.getAuthor().getName();
        String message = event.getMessage().getContent();

        System.out.println(author + ": " + message);
    }
}
