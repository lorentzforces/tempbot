package tempbot.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tinylog.Logger;
import tempbot.engine.ProcessingResult.ParsedSourceValue;
import tempbot.engine.ProcessingResult.ProcessingError;
import tempbot.engine.ProcessingResult.ValueNotConverted;

import static tempbot.Constants.MAX_CONVERSIONS;

public class UserInputProcessor {
	private final Map<String, Dimension> dimensionsByNameLower;
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
		dimensionsByNameLower = dimensions.stream().collect(Collectors.toMap(
			dimension -> dimension.getName().toLowerCase(),
			Function.identity()
		));
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
				dimensionNames.stream().map(Pattern::quote).collect(Collectors.joining("|"))
			)
			+ "\\b"; // terminating word boundary

		return Pattern.compile(unitsRegex);
	}

	public ProcessingResult
	processSpecificConversionRequest(
		final String sourceValueString,
		final String destinationUnitString
	) {
		final var sourceResult = extractSourceValue(sourceValueString);

		if (sourceResult.error != null) {
			return sourceResult.error;
		}

		final var sourceValue = sourceResult.parsedValue;
		final var sourceDimension = dimensionsByUnitName.get(sourceValue.unit().getShortName());
		final var destDimension = dimensionsByUnitName.get(destinationUnitString);

		if (destDimension == null) {
			return new ProcessingError.UnknownUnitType(destinationUnitString);
		}
		if (!destDimension.getName().equals(sourceDimension.getName())) {
			return new ProcessingError.DimensionMismatch(
				sourceValue,
				sourceDimension,
				destDimension
			);
		}

		return destDimension.convertSpecificUnits(
			sourceValue,
			destDimension.getUnitByName(destinationUnitString)
		);
	}

	public Optional<Dimension>
	getDimensionByName(@NonNull String name) {
		return Optional.ofNullable(dimensionsByNameLower.get(name.toLowerCase()));
	}

	public List<ProcessingResult>
	processMessage(final String message) {
		final var unitLabelMatcher = unitsPattern.matcher(message);

		final var results = new ArrayList<ProcessingResult>();
		var previousMatchEnd = 0;
		while (unitLabelMatcher.find() && results.size() < MAX_CONVERSIONS) {
			final var sourceResult =
				extractSourceValue(message, previousMatchEnd, unitLabelMatcher.end());
			previousMatchEnd = unitLabelMatcher.end();

			// In "process everything in the message" mode, we don't care about most errors and
			// just silently ignore them. If we see an unknown unit error at this point, there
			// was a terrible error and we should log it.
			if (sourceResult.error instanceof ProcessingError.UnknownUnitType err) {
				logBadUnitLabel(err.badUnitString());
			}
			if (sourceResult.error != null) {
				continue;
			}

			final var sourceValue = sourceResult.parsedValue;

			final var valueAlreadyEncountered = results.stream()
				.filter(priorResult -> priorResult instanceof ParsedSourceValue)
				.map(typeCheckedValue ->
					((ParsedSourceValue) typeCheckedValue).sourceValue())
				.anyMatch(earlierValue -> earlierValue.equalsUnitValue(sourceValue));
			if (valueAlreadyEncountered) {
				continue;
			}

			if (sourceValue.unit().isDefaultConversionSource()) {
				final var dimension = dimensionsByUnitName.get(sourceValue.unit().getShortName());
				results.add(dimension.convertUnit(sourceValue));
			}
		}

		return results;
	}

	/**
	 * Represents the result of an attempt to extract a source UnitValue from a string which
	 * probably contains one. Consumers should check whether the error value is null before
	 * accessing the parsed value.
	 */
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static class SourceValueResult {
		public final UnitValue parsedValue;
		public final ProcessingError error;

		public static SourceValueResult goodValue(UnitValue parsedValue) {
			return new SourceValueResult(parsedValue, null);
		}

		public static SourceValueResult error(ProcessingError error) {
			return new SourceValueResult(null, error);
		}
	}

	private SourceValueResult
	extractSourceValue(String sourceString) {
		return extractSourceValue(sourceString, 0, sourceString.length());
	}

	private SourceValueResult
	extractSourceValue(String sourceString, int regionStart, int regionEnd) {
		final var unitLabelMatcher = unitsPattern.matcher(sourceString);
		unitLabelMatcher.region(regionStart, regionEnd);

		final var foundUnit = unitLabelMatcher.find();
		if (!foundUnit) {
			return SourceValueResult.error(new ProcessingError.UnknownUnitType(sourceString));
		}
		final var unitLabel = unitLabelMatcher.group(PATTERN_UNIT_LABEL_GROUP);

		final var numericMatcher = DOUBLE_NUMBER_PATTERN.matcher(sourceString);
		// we add one since the unit label regex includes the last digit
		final var numericalEnd = unitLabelMatcher.start() + 1;
		numericMatcher.region(0, numericalEnd);

		final var foundNumeric = numericMatcher.find();
		if (!foundNumeric) {
			return SourceValueResult.error(new ProcessingError.UnparseableNumber(
				sourceString.substring(0, numericalEnd).trim()
			));
		}

		final var valueString = numericMatcher.group();
		double doubleValue;
		try {
			doubleValue = Double.parseDouble(valueString);
		} catch (final NumberFormatException e) {
			logBadNumberFormat(valueString, e);
			return SourceValueResult.error(new ProcessingError.UnparseableNumber(valueString));
		}

		final var sourceUnit =
			dimensionsByUnitName
			.get(unitLabel)
			.getUnitByName(unitLabel);
		return SourceValueResult.goodValue(new UnitValue(sourceUnit, doubleValue));
	}

	private static void
	logBadUnitLabel(String unitLabel) {
		Logger.error(String.format("""
			Could not find a valid unit label when parsing a string that originally matched the \
			unit label matcher. This should never happen. String was: \"%s\"""",
			unitLabel
		));
	}

	private static void
	logBadNumberFormat(String valueString, NumberFormatException e) {
		Logger.error(e, String.format("""
			Encountered a NumberFormatException when parsing a regex-matched Double \
			value. This should never happen. Value string was: \"%s\"""",
			valueString
		));
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
