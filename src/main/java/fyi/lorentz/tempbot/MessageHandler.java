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
    // literally just a pattern to grab the first word
    private static final String PATTERN_WORD_GROUP = "word";
    private static final String PATTERN_DIMENSION_GROUP = "dimension";
    private static final Pattern WORD_PATTERN =
            Pattern.compile("\\b(?<" + PATTERN_WORD_GROUP + ">\\w+)\\b");

    private final Processor processor;
    private final User currentUser;
    private final DecimalFormat format;
    private final Pattern rawBotMentionPattern;
    private final Pattern helpPattern;

    public MessageHandler(Processor processor, User currentUser) {
        this.processor = processor;
        this.currentUser = currentUser;

        StringBuilder formatString = new StringBuilder("###,###,###,###.");
        for (int i = 0; i < PRECISION; i++) {
            formatString.append("#");
        }
        format = new DecimalFormat(formatString.toString());

        rawBotMentionPattern = Pattern.compile("<@!?" + currentUser.getId().asString() + ">");
        helpPattern = buildHelpMatchPattern(processor);
    }

    private static Pattern
    buildHelpMatchPattern(Processor processor) {
        String lineBeginningOrWhitespace = "(?:^|\\s)";
        String spacing = "\\s+";
        String dimensionRegex = processor.getDimensions()
                .map(dimension -> Pattern.quote(dimension.getName()))
                .collect(Collectors.joining("|"));

        StringBuilder helpPattern = new StringBuilder(lineBeginningOrWhitespace)
                .append("help")
                .append("(?:")
                .append(spacing)
                .append("(?<")
                .append(PATTERN_DIMENSION_GROUP)
                .append(">")
                .append(dimensionRegex)
                .append("))?") // closes capturing group as well as the non-capturing one
                .append("\\b");

        return Pattern.compile(helpPattern.toString(), Pattern.CASE_INSENSITIVE);
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
        Matcher helpMatcher = helpPattern.matcher(messageContents);

        StringBuilder helpMessage = new StringBuilder();
        if (helpMatcher.find()) {
            String dimensionString = helpMatcher.group(PATTERN_DIMENSION_GROUP);
            if (dimensionString == null) {
                createGeneralHelpMessage(helpMessage);
            }
            else {
                createDimensionListingMessage(
                        processor.getDimensionFromName(dimensionString),
                        helpMessage
                );
            }
        }
        else if (contentsWithoutBotMention.length() == 0) {
            createGeneralHelpMessage(helpMessage);
        }


        if (helpMessage.length() > 0) {
            them.getPrivateChannel().subscribe(privateChannel -> {
                privateChannel.createMessage(helpMessage.toString()).subscribe();
            });
        }
    }

    private void
    createDimensionListingMessage(Dimension dimension, StringBuilder message) {
        message.append(DIMENSION_HELP_TEXT)
                .append("\n> ")
                .append(dimension.getUnits()
                        .map(unit -> {
                            return new StringBuilder()
                                    .append("**")
                                    .append(unit.getFullName())
                                    .append("** ")
                                    .append("(")
                                    .append(unit.getDetectableNames().stream()
                                            .collect(Collectors.joining(", ")))
                                    .append(")");
                        })
                        .collect(Collectors.joining("\n> "))
                );
    }

    private void
    createGeneralHelpMessage(StringBuilder message) {
        message.append(HELP_TEXT)
                .append("\n")
                .append(DIMENSION_EXPLAIN_TEXT)
                .append("\n> ")
                .append(processor.getDimensions()
                        .map(Dimension::getName)
                        .collect(Collectors.joining("\n> "))
                );
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
            + "**To see this help text, tag or DM me with \"help\" in the message.**\n"
            + "If you send a message to me or in a channel I can see, I'll do my best to "
            + "convert any units I recognize in your message. If the unit you want to see "
            + "in the results doesn't appear, try mentioning me in the message (I convert to "
            + "more--usually uncommon--units if you tag me).\n"
            + "To convert to a specific unit, tag or DM me and write \"*<unit value>* to "
            + "*<result unit>*\"";

    private static final String DIMENSION_EXPLAIN_TEXT =
            "Right now, I can do conversions for these categories (tag or DM me and say "
            + "\"help *<category name>*\" to see the units I can convert in each category:";

    private static final String DIMENSION_HELP_TEXT =
            "These are the units of that type I can convert:";


}
