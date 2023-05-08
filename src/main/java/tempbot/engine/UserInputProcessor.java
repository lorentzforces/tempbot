package tempbot.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import org.tinylog.Logger;
import tempbot.engine.ProcessingResult.ParsedSourceValue;
import tempbot.engine.ProcessingResult.ProcessingError;
import tempbot.engine.ProcessingResult.ValueNotConverted;

import static tempbot.Constants.MAX_CONVERSIONS;

public class UserInputProcessor {
	private final Map<String, Dimension> dimensionsByUnitName = new HashMap<>();
	private final Pattern unitsPattern;

	@Getter
	private final Set<String> eagerDimensions;
	@Getter
	private final Set<String> nonEagerDimensions;

	private static final Pattern DOUBLE_NUMBER_PATTERN = RegexHelper.buildDoubleNumberPattern();
	private static final String PATTERN_UNIT_LABEL_GROUP = "unitName";

	public UserInputProcessor(
		@NonNull final Set<Dimension> dimensions
	) {
		final var eagerDimensionStaging = new HashSet<String>();
		final var nonEagerDimensionStaging = new HashSet<String>();
		for (final Dimension dimension : dimensions) {
			for (final String unitName : dimension.getUnitNames()) {
				dimensionsByUnitName.put(unitName, dimension);
			}


			if (dimension.isHasEagerConversions()) {
				eagerDimensionStaging.add(dimension.getName());
			} else {
				nonEagerDimensionStaging.add(dimension.getName());
			}
		}

		eagerDimensions = Collections.unmodifiableSet(eagerDimensionStaging);
		nonEagerDimensions = Collections.unmodifiableSet(nonEagerDimensionStaging);

		unitsPattern = buildUnitsPattern(dimensionsByUnitName.keySet());
	}

	private static Pattern
	buildUnitsPattern(final Collection<String> dimensionNames) {
		final String unitsRegex =
			"\\d" // one decimal digit
			+ RegexHelper.SOMETIMES_A_SPACE
			+ RegexHelper.buildNamedGroup(
				PATTERN_UNIT_LABEL_GROUP,
				dimensionNames .stream().map(Pattern::quote).collect(Collectors.joining("|"))
			)
			+ "\\b"; // terminating word boundary

		return Pattern.compile(unitsRegex);
	}

	public List<ProcessingResult>
	processMessage(final String message) {
		final var unitLabelMatcher = unitsPattern.matcher(message);
		final var numericMatcher = DOUBLE_NUMBER_PATTERN.matcher(message);

		final var results = new ArrayList<ProcessingResult>();
		final var previousMatchEnd = 0;
		while (unitLabelMatcher.find() && results.size() < MAX_CONVERSIONS) {
			final var unitLabel = unitLabelMatcher.group(PATTERN_UNIT_LABEL_GROUP);

			// we add one since the unit label regex includes the last digit
			final var numericalEnd = unitLabelMatcher.start() + 1;
			numericMatcher.region(previousMatchEnd, numericalEnd);

			// if there isn't a valid double value immediately preceding, just keep going through
			// the input message
			if (numericMatcher.find()) {
				final var valueString = numericMatcher.group();

				ProcessingResult result;
				try {
					final var doubleValue = Double.parseDouble(valueString);
					// this is where we'll break out if there's something horribly wrong

					final var sourceUnit =
						dimensionsByUnitName
						.get(unitLabel)
						.getUnitByName(unitLabel);
					final var sourceValue = new UnitValue(sourceUnit, doubleValue);

					final var valueAlreadyEncountered = results.stream()
						.filter(priorResult -> priorResult instanceof ParsedSourceValue)
						.map(typeCheckedValue ->
							((ParsedSourceValue) typeCheckedValue).sourceValue())
						.anyMatch(earlierValue -> earlierValue.equalsUnitValue(sourceValue));

					if (valueAlreadyEncountered) {
						continue;
					} else if (sourceUnit.isDefaultConversionSource()) {
						final var dimension = dimensionsByUnitName.get(unitLabel);
						result = dimension.convertUnit(sourceValue);
					} else {
						result = ValueNotConverted.builder().sourceValue(sourceValue).build();
					}
				} catch (final NumberFormatException e) {
					Logger.error(e, String.format("""
						Encountered a NumberFormatException when parsing a regex-matched Double \
						value. This should never happen.
						> Value String: %s""",
						valueString
					));
					result = new ProcessingError.SystemError();
				}

				results.add(result);
			}
		}

		return results;
	}

	private static class RegexHelper {
		public static final String SOMETIMES_A_SPACE = "[ ]?";

		public static final String NAMED_GROUP_START = "(?<";
		public static final String NAMED_GROUP_START_TERMINATOR = ">";
		public static final String NAMED_GROUP_TERMINATOR = ")";

		public static StringBuilder
		buildNamedGroup(final String groupLabel, final String groupRegex) {
			return new StringBuilder()
				.append(NAMED_GROUP_START)
				.append(groupLabel)
				.append(NAMED_GROUP_START_TERMINATOR)
				.append(groupRegex)
				.append(NAMED_GROUP_TERMINATOR);
		}

		/**
		 * Pattern matching a valid double value, original values taken from
		 * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Double.html
		 * <br><br>
		 * Our implementation adds a line-end anchor ("$") to the end of the pattern to match
		 * against the end of the region we specify. Our implementation ALSO does not consider
		 * a trailing decimal point to be valid (in other words, we accept "100" but not "100.").
		 */
		public static Pattern
		buildDoubleNumberPattern() {
			final var decimalDigits = "(\\d+)";
			final var leadingDigit =
				"("
				+ decimalDigits
				+ "(\\."
				+ decimalDigits
				+ ")?"
				+ ")";
			final var leadingDecimalPoint =
				"("
				+ "\\."
				+ decimalDigits
				+ ")";
			final var optionalSign = "[+-]?";
			final var doubleRegex =
				optionalSign
				+ "("
				+ leadingDigit
				+ "|"
				+ leadingDecimalPoint
				+ ")"
				+ "$";

			return Pattern.compile(doubleRegex);
		}
	}

}
