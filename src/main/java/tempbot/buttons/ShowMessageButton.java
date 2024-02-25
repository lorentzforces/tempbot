package tempbot.buttons;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import tempbot.buttons.BotButton.SecondaryButton;

import static tempbot.DiscordFormatting.appendMentionVia;

/**
 * A button to take an ephemeral bot message and convert it into a message visible to anyone in
 * the source channel.
 *
 * Assumes the message triggering the button event is already ephemeral.
 */
public class ShowMessageButton implements SecondaryButton {

	@Override
	public String
	getName() {
		return "show-message";
	}

	@Override
	public String
	getLabel() {
		return "Show to others";
	}

	@Override
	public void
	handleButtonEvent(@NonNull ButtonInteractionEvent event) {
		// sanity check in case the button finds itself on a message that's already visible
		if (!event.getMessage().isEphemeral()) {
			return;
		}

		final var messageContent =
			appendMentionVia(event.getMessage().getContentRaw(), event.getMember());
		// remove buttons from the source message
		event.deferEdit().setComponents()
			.and(event.getGuildChannel().sendMessage(messageContent))
			.queue();
	}

}
