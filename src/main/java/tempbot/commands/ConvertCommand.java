package tempbot.commands;

import java.util.List;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tempbot.DiscordFormatting;
import tempbot.engine.UserInputProcessor;

public class ConvertCommand implements SlashCommand {

	private static final List<OptionData> OPTIONS = List.of(
			new OptionData(
				OptionType.STRING,
				"source",
				"the source value to convert from, for example \"32 F\""
			).setRequired(true),
			new OptionData(
				OptionType.STRING,
				"result-unit",
				"the unit to convert to, for example \"degrees celsius\""
			).setRequired(true)
		);

	private final UserInputProcessor userInputProcessor;

	public ConvertCommand(@NonNull UserInputProcessor userInputProcessor) {
		this.userInputProcessor = userInputProcessor;
	}

	@Override
	public String getName() {
		return "convert";
	}

	@Override
	public String getDescription() {
		return "convert measurements from one unit to another";
	}

	@Override
	public List<OptionData> getOptions() {
		return OPTIONS;
	}

	@Override
	public void handleCommandEvent(@NonNull SlashCommandInteractionEvent event) {
		final var sourceString = event.getOption("source").getAsString();
		final var destUnitLabel = event.getOption("result-unit").getAsString();

		final var result =
			userInputProcessor.processSpecificConversionRequest(sourceString, destUnitLabel);
		final var resultMessage = new StringBuilder();
		DiscordFormatting.formatProcessingResults(List.of(result), resultMessage);

		event.reply(resultMessage.toString()).setEphemeral(true).queue();
	}

}
