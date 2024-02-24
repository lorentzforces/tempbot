package tempbot.commands;

import java.util.List;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface SlashCommand {

	String getName();
	String getDescription();
	List<OptionData> getOptions();
	void handleCommandEvent(@NonNull SlashCommandInteractionEvent event);

}

