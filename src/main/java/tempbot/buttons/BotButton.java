package tempbot.buttons;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public sealed interface BotButton {

	String getName();
	void handleButtonEvent(@NonNull ButtonInteractionEvent event);
	Button getRegistrationObject();

	public non-sealed interface SecondaryButton extends BotButton {
		String getLabel();

		@Override
		default Button
		getRegistrationObject() {
			return Button.secondary(getName(), getLabel());
		}
	}

}
