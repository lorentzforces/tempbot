package fyi.lorentz.tempBot;

import fyi.lorentz.tempBot.service.TemperatureProcessor;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;

public class MessageHandler implements IListener<MessageReceivedEvent> {

    private static final ReactionEmoji angryFace = ReactionEmoji.of("\uD83D\uDE20");
    private static final ReactionEmoji thinkingFace = ReactionEmoji.of("\uD83E\uDD14");

    @Override
    public void
    handle(MessageReceivedEvent event) {
        handleTemperatures(event);
        handleBadMentions(event);
    }

    private void
    handleTemperatures(MessageReceivedEvent event) {
        String inputText = event.getMessage().getFormattedContent();

        TemperatureProcessor tempProcessor = new TemperatureProcessor(inputText);
        String output = tempProcessor.processMessage();

        if(output.length() > 0) {
            event.getChannel().sendMessage(output);
        }
    }

    private void
    handleBadMentions(MessageReceivedEvent event) {
        IMessage message = event.getMessage();

        if(message.mentionsEveryone()) {
            message.addReaction(angryFace);
        }

        if(message.mentionsHere()) {
            message.addReaction(thinkingFace);
        }
    }
}
