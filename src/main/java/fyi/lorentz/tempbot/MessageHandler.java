package fyi.lorentz.tempbot;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import fyi.lorentz.tempbot.engine.ProcessingResult;
import fyi.lorentz.tempbot.engine.Processor;
import fyi.lorentz.tempbot.engine.UnitRangeException;
import fyi.lorentz.tempbot.engine.UnitValue;
import java.text.DecimalFormat;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler {

    private static final Logger logger = LogManager.getLogger(MessageHandler.class);

    private DecimalFormat format = new DecimalFormat("###,###,###,###.###");

    private final Processor processor;
    private final User currentUser;

    public MessageHandler(Processor processor, User currentUser) {
        this.processor = processor;
        this.currentUser = currentUser;
    }

    // TODO: add help documentation
    public void
    handle(Message message) {
        logger.debug("Message handling: " + message.getContent().orElse(""));
        boolean botWasMentioned = message.getUserMentionIds().contains(currentUser.getId());

        List<ProcessingResult> results =
                processor.processMessage(message.getContent().orElse(""), botWasMentioned);

        StringBuilder output = new StringBuilder("");

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

        if (output.length() > 0) {
            message.getChannel().subscribe(channel -> {
                channel.createMessage(output.toString()).subscribe();
            });
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

}
