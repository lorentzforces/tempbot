package tempbot.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProcessorBuilder {

	private List<DimensionBuilder> dimensions;

	public ProcessorBuilder() {
		dimensions = new ArrayList<>();
	}

	public Processor build() {
		List<Dimension> newDimensions = new ArrayList<>();
		Map<String, Dimension> dimensionMap = new HashMap<>();
		for (DimensionBuilder builder : dimensions) {
			Dimension newDimension = builder.build();
			newDimensions.add(newDimension);
			for (String unitString : newDimension.getUnitNames()) {
				dimensionMap.put(unitString, newDimension);
			}
		}

		Pattern masterPattern = buildPattern(newDimensions);
		Pattern specificConversionPattern = buildSpecificConversionPattern(newDimensions);
		return new Processor(
				masterPattern,
				specificConversionPattern,
				newDimensions,
				dimensionMap
		);
	}

	private Pattern
	buildPattern(Collection<Dimension> dimensions) {
		String lineBeginningOrWhitespace = "(?:^|\\s)";

		StringBuilder numberRegex = new StringBuilder("(?<")
				.append(Processor.PATTERN_NUMBER_GROUP)
				.append(">[-+]?[0-9]*\\.?[0-9]+)");

		String sometimesASpace = "[ ]?";

		String labelRegex = buildLabelRegex(dimensions);

		StringBuilder finalRegex = new StringBuilder(lineBeginningOrWhitespace)
				.append(numberRegex)
				.append(sometimesASpace)
				.append("(?<")
				.append(Processor.PATTERN_LABEL_GROUP)
				.append(">")
				.append(labelRegex)
				.append(")\\b");

		return Pattern.compile(finalRegex.toString());
	}

	private Pattern
	buildSpecificConversionPattern(Collection<Dimension> dimensions) {
		String whitespaceAndTo = "\\s+[tT][oO]\\s+";
		String labelRegex = buildLabelRegex(dimensions);

		StringBuilder regex = new StringBuilder(whitespaceAndTo)
				.append("(?<")
				.append(Processor.PATTERN_DESTINATION_UNIT_GROUP)
				.append(">")
				.append(labelRegex)
				.append(")\\b");

		return Pattern.compile(regex.toString());
	}
	private String
	buildLabelRegex(Collection<Dimension> dimensions) {
		return dimensions.stream()
				.flatMap(dimension -> dimension.getUnitNames().stream())
				.map(unitLabel -> Pattern.quote(unitLabel))
				.collect(Collectors.joining("|"));
	}

	public ProcessorBuilder
	addDimension(DimensionBuilder dimension) {
		dimensions.add(dimension);
		return this;
	}

}
