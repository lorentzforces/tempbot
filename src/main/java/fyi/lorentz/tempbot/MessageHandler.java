package fyi.lorentz.tempbot;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import fyi.lorentz.tempbot.engine.Dimension;
import fyi.lorentz.tempbot.engine.ProcessingResult;
import fyi.lorentz.tempbot.engine.Processor;
import fyi.lorentz.tempbot.engine.UnitRangeException;
import fyi.lorentz.tempbot.engine.UnitValue;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static fyi.lorentz.tempbot.Constants.PRECISION;

public class MessageHandler {

    private static final Logger logger = LogManager.getLogger(MessageHandler.class);

    private static final Pattern HELP_PATTERN =
            Pattern.compile("\\bhelp\\b", Pattern.CASE_INSENSITIVE);

    private final Processor processor;
    private final User currentUser;
    private final DecimalFormat format;
    private final Pattern rawBotMentionPattern;

    public MessageHandler(Processor processor, User currentUser) {
        this.processor = processor;
        this.currentUser = currentUser;

        StringBuilder formatString = new StringBuilder("###,###,###,###.");
        for (int i = 0; i < PRECISION; i++) {
            formatString.append("#");
        }
        format = new DecimalFormat(formatString.toString());

        rawBotMentionPattern = Pattern.compile("<@!?" + currentUser.getId().asString() + ">");
    }

    public void
    handle(Message message, boolean isPrivateMessage) {
        String messageContent = message.getContent();
        logger.debug("Message handling: \"" + messageContent + "\"");
        if (messageContent.length() == 0) {
            return;
        }

        boolean botWasMentioned = message.getUserMentionIds().contains(currentUser.getId());
        if (botWasMentioned || isPrivateMessage) {
            handlePotentialHelpResponse(
                    messageContent,
                    message.getAuthor().get()
            );
        }

        List<ProcessingResult> results =
                processor.processMessage(messageContent, botWasMentioned);
        StringBuilder processingOutput = new StringBuilder("");
        formatProcessingResults(results, processingOutput);

        if (processingOutput.length() > 0) {
            message.getChannel().subscribe(channel -> {
                channel.createMessage(processingOutput.toString()).subscribe();
            });
        }
    }

    private void
    handlePotentialHelpResponse(String messageContents, User them) {
        Matcher botMentionMatcher = rawBotMentionPattern.matcher(messageContents);
        String contentsWithoutBotMention = botMentionMatcher.replaceAll("").trim();
        boolean includesHelp = HELP_PATTERN.matcher(messageContents).find();

        if (contentsWithoutBotMention.length() == 0 || includesHelp) {
            StringBuilder helpMessage = new StringBuilder(HELP_TEXT)
                    .append("\n")
                    .append("Right now, I can do conversions for:\n")
                    .append("> ")
                    .append(processor.getDimensions()
                            .map(Dimension::getName)
                            .collect(Collectors.joining("\n> "))
                    );
            them.getPrivateChannel().subscribe(privateChannel -> {
                privateChannel.createMessage(helpMessage.toString()).subscribe();
            });
        }
    }

    private void
    formatProcessingResults(List<ProcessingResult> results, StringBuilder output) {
        for (ProcessingResult result : results) {
            StringBuilder originalValueOutput = new StringBuilder("");
            StringBuilder standardOutput = new StringBuilder("");
            StringBuilder errorOutput = new StringBuilder("");

            formatOriginalValue(result.sourceValue, originalValueOutput);
            formatResults(result.values, standardOutput);
            formatErrors(result.errors, errorOutput);

            if (standardOutput.length() + errorOutput.length() > 0) {
                if (standardOutput.length() > 0) {
                    output.append(originalValueOutput).append(standardOutput);
                }
                output.append(errorOutput);
            }
        }
    }

    private void
    formatOriginalValue(UnitValue originalValue, StringBuilder output) {
        addUnitValueString(originalValue, output);
        output.append(" = \n");
    }

    private void
    formatResults(List<UnitValue> results, StringBuilder output) {
        for (UnitValue result : results) {
            output.append("> ");
            addUnitValueString(result, output);
            output.append("\n");
        }
    }

    private void
    formatErrors(List<Exception> errors, StringBuilder output) {
        for (Exception error : errors) {
            if (error instanceof UnitRangeException) {
                UnitRangeException rangeError = (UnitRangeException) error;

                addUnitValueString(rangeError.getOffendingValue(), output);
                output.append(" is ");
                output.append(rangeError.isMaximum() ? "greater than " : "less than ");
                addUnitValueString(rangeError.getRangeLimitingValue(), output);
                output.append(", the ");
                output.append(rangeError.isMaximum() ? "maximum." : "minimum.");
            }
        }
    }

    private void
    addUnitValueString(UnitValue value, StringBuilder buffer) {
        buffer.append("**")
                .append(format.format(value.getValue()))
                .append(" ")
                .append(value.getUnit().getShortName())
                .append("**");
    }


    private static final String HELP_TEXT =
            "Hi!\n"
            + "**To see this help text, tag me or DM me with \"help\" in the message "
            + "anywhere I can see it.**\n"
            + "If you send a message to me or in a channel I can see, I'll do my best to "
            + "convert any units I recognize in your message. If the unit you want to see "
            + "in the results doesn't appear, try mentioning me in the message (I convert to "
            + "more--usually uncommon--units if you tag me).";

}
