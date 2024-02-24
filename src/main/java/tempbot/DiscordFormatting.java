package tempbot;

import static tempbot.Constants.PRECISION;

import java.text.DecimalFormat;
import java.util.List;
import lombok.NonNull;
import tempbot.engine.ProcessingResult;
import tempbot.engine.ProcessingResult.ConvertedValues;
import tempbot.engine.ProcessingResult.ProcessingError;
import tempbot.engine.ProcessingResult.ProcessingError.DimensionMismatch;
import tempbot.engine.ProcessingResult.ProcessingError.SystemError;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange;
import tempbot.engine.ProcessingResult.ProcessingError.UnknownUnitType;
import tempbot.engine.ProcessingResult.ProcessingError.UnparseableNumber;
import tempbot.engine.ProcessingResult.ValueNotConverted;
import tempbot.engine.UnitValue;

public class DiscordFormatting {

	private static final ThreadLocal<DecimalFormat> numberFormat = ThreadLocal.withInitial(() ->
		new DecimalFormat("###,###,###,###." + "#".repeat(PRECISION)));

	private DiscordFormatting() {
		// private constructor
	}

	public static void
	formatProcessingResults(
		@NonNull List<ProcessingResult> results,
		@NonNull StringBuilder output
	) {
		for (final ProcessingResult result : results) {
			final var standardOutput = new StringBuilder();
			final var errorOutput = new StringBuilder();

			switch (result) {
				case ConvertedValues vals -> formatConvertedValues(vals, standardOutput);
				case ValueNotConverted c -> throw new RuntimeException();
				case ProcessingError err -> formatError(err, errorOutput);
			}

			output.append(standardOutput);
			output.append(errorOutput);
			// trailing newline is trimmed by Discord
			output.append("\n");
		}
	}

	private static void
	formatConvertedValues(@NonNull ConvertedValues result, @NonNull StringBuilder output) {
		if (result.values().size() > 0) {
			addUnitValueString(result.sourceValue(), output);
			output.append(" = ");

			if (result.values().size() == 1) {
				addUnitValueString(result.values().get(0), output);
			}
			else {
				output.append("\n");
				for (final UnitValue value : result.values()) {
					output.append("> ");
					addUnitValueString(value, output);
					output.append("\n");
				}
			}
		}
	}

	private static void
	formatError(final ProcessingError error, final StringBuilder output) {
		switch (error) {
			case UnitOutOfRange err -> {
				addUnitValueString(err.sourceValue(), output);
				output.append(" is ");
				output.append(switch (err.limitType()) {
					case MAXIMUM -> "greater than ";
					case MINIMUM -> "less than ";
				});
				addUnitValueString(err.rangeLimitingValue(), output);
				output.append(", the ");
				output.append(switch (err.limitType()) {
					case MAXIMUM -> "maximum";
					case MINIMUM -> "minimum";
				});
				output.append(".");
			}
			case DimensionMismatch err -> {
				output.append("Can't convert units from ")
					.append(err.sourceDimension().getName())
					.append(" to ")
					.append(err.destinationDimension().getName())
					.append(".");
			}
			case UnknownUnitType err -> {
				output.append("Unknown unit type: ")
					.append(err.badUnitString())
					.append(".");
			}
			case UnparseableNumber err -> {
				output.append("Could not get a numeric value from: ")
					.append(err.badNumberString())
					.append(".");
			}
			case SystemError sysError ->
				output.append("A system error occurred while processing a unit conversion.");
		}
	}

	private static void
	addUnitValueString(final UnitValue value, final StringBuilder buffer) {
		buffer.append("**")
			.append(numberFormat.get().format(value.value()))
			.append(" ")
			.append(value.unit().getShortName())
			.append("**");
	}

}
