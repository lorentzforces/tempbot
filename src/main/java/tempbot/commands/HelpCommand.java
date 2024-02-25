package tempbot.commands;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tempbot.engine.Dimension;
import tempbot.engine.UserInputProcessor;

public class HelpCommand implements SlashCommand {

	private static final List<OptionData> OPTIONS = List.of(
		new OptionData(
			OptionType.STRING,
			"unit-type",
			"the type of units to see more information about, for example \"temperature\""
		)
	);

	private final UserInputProcessor userInputProcessor;
	private final String generalHelpText;

	public HelpCommand(@NonNull UserInputProcessor userInputProcessor) {
		this.userInputProcessor = userInputProcessor;
		this.generalHelpText = buildHelpText(userInputProcessor);
	}

	@Override
	public String
	getName() {
		return "help";
	}

	@Override
	public String
	getDescription() {
		return "read help about TempBot or any of the things it can convert between";
	}

	@Override
	public List<OptionData>
	getOptions() {
		return OPTIONS;
	}

	@Override
	public void
	handleCommandEvent(@NonNull SlashCommandInteractionEvent event) {
		event.reply("test");
		event.reply(
			switch (event.getOption("unit-type")) {
				case null -> generalHelpText;
				case OptionMapping param -> getDimensionHelpMessage(param.getAsString());
			}
		).setEphemeral(true).queue();
	}

	private String
	getDimensionHelpMessage(@NonNull String dimensionName) {
		return userInputProcessor.getDimensionByName(dimensionName)
			.map(HelpCommand::buildDimensionHelpText)
			.orElse(generalHelpText);
	}

	private static String
	buildHelpText(@NonNull UserInputProcessor processor) {
		final var helpText = new StringBuilder("""
			TempBot is a unit-conversion bot. It can convert a variety of units to make your \
			conversations easier with friends who might use different temperature units from you, \
			or just satisfy your curiosity.

			TempBot will automatically convert units of the following types if it sees them in a \
			channel:
			""");

		processor.getEagerDimensions().stream().forEach(name -> {
			helpText.append("- ")
				.append(name)
				.append("\n");
		});

		helpText.append("\n")
			.append("TempBot can also convert units of the following types when asked:\n");

		processor.getNonEagerDimensions().stream().forEach(name -> {
			helpText.append("- ")
				.append(name)
				.append("\n");
		});

		helpText.append("\n")
			.append("""
				To see what units TempBot knows about for each type, invoke `/help` and include \
				the name of one of these unit types.""");

		return helpText.toString();
	}

	private static String
	buildDimensionHelpText(@NonNull Dimension dimension) {
		return new StringBuilder()
			.append("These are the ")
			.append(dimension.getName())
			.append(" units I can convert:")
			.append("\n> ")
			.append(dimension.getUnits().stream()
				.map(unit -> {
					return new StringBuilder()
						.append("**")
						.append(unit.getFullName())
						.append("** ")
						.append("(")
						.append(unit.getDetectableNames().stream()
							.collect(Collectors.joining(", "))
						).append(")");
				})
				.collect(Collectors.joining("\n> "))
			).toString();
	}

}
