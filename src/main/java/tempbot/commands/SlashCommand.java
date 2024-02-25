package tempbot.commands;

import java.util.List;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand {

	String getName();
	String getDescription();
	List<OptionData> getOptions();
	void handleCommandEvent(@NonNull SlashCommandInteractionEvent event);

	default SlashCommandData
	getRegistrationObject() {
		return Commands.slash(getName(), getDescription()).addOptions(getOptions());
	}

}

