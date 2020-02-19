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

            handleOriginalValue(result.sourceValue, originalValueOutput);
            handleResults(result.values, standardOutput);
            handleErrors(result.errors, errorOutput);

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
    handleOriginalValue(UnitValue originalValue, StringBuilder output) {
        output.append(format.format(originalValue.getValue()))
                .append(" ")
                .append(originalValue.getUnit().getFullName())
                .append(" = ")
                .append("\n");
    }

    private void
    handleResults(List<UnitValue> results, StringBuilder output) {
        for (UnitValue result : results) {
            output.append("> ")
                    .append(format.format(result.getValue()))
                    .append(" ")
                    .append(result.getUnit().getFullName())
                    .append("\n");
        }
    }

    private void
    handleErrors(List<Exception> errors, StringBuilder output) {
        for (Exception error : errors) {
            if (error instanceof UnitRangeException) {
                UnitRangeException rangeError = (UnitRangeException) error;
                output.append("**ERROR:** Provided value ")
                        .append(format.format(rangeError.getOffendingValue().getValue()))
                        .append(" is ")
                        .append(
                                rangeError.isMaximum()
                                        ? "greater than the maximum value "
                                        : "less than the minimum value "
                        )
                        .append(format.format(rangeError.getRangeLimitingValue().getValue()))
                        .append(" for ")
                        .append(rangeError.getRangeLimitingValue().getUnit().getFullName());
            }
        }
    }

}
