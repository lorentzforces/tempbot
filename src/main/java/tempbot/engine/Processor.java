package tempbot.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static tempbot.Constants.MAX_CONVERSIONS;

public class Processor {
//
//	public static final String PATTERN_NUMBER_GROUP = "number";
//	public static final String PATTERN_LABEL_GROUP = "label";
//	public static final String PATTERN_DESTINATION_UNIT_GROUP = "unit";
//
//	private final Pattern masterPattern;
//	private final Map<String, Dimension> dimensionMap;
//	private final List<Dimension> dimensions;
//
//	public Processor(
//		Pattern masterPattern,
//		Pattern specificConversionPattern,
//		List<Dimension> dimensions,
//		Map<String, Dimension> dimensionMap
//	) {
//		this.masterPattern = masterPattern;
//		this.dimensions = dimensions;
//		this.dimensionMap = dimensionMap;
//	}
//
// 	public List<ProcessingResult>
//
// 	public List<ProcessingResult>
// 	processMessage(String message, boolean doMoreConversions) {
// 		Matcher mainMatcher = masterPattern.matcher(message);
//
// 		List<ProcessingResult> results = new ArrayList<>();
//
// 		// checking against the max this way means that we consider
// 		// the number of results --NOT including duplicates--
// 		while (mainMatcher.find() && results.size() < MAX_CONVERSIONS) {
// 			ProcessingResult result = new ProcessingResult();
//
// 			result.valueString = mainMatcher.group(PATTERN_NUMBER_GROUP);
// 			result.labelString = mainMatcher.group(PATTERN_LABEL_GROUP);
// 			result.dimension = dimensionMap.get(result.labelString);
// 			result.unit = result.dimension.getUnitByName(result.labelString);
//
// 			try {
// 				Double value = Double.valueOf(result.valueString);
// 				result.sourceValue = new UnitValue(result.unit, value);
// 			}
// 			catch (NumberFormatException e) {
// 				result.errors.add(e);
// 			}
//
// 		return results;
// 	}
//
//	private void
//	doSpecificConversion(
//		Matcher mainMatcher,
//		Matcher specificMatcher,
//		ProcessingResult result
//	) {
//		specificMatcher.region(mainMatcher.end(), specificMatcher.regionEnd());
//		if (specificMatcher.find()) {
//			result.isSpecificConversion = true;
//			String unitString = specificMatcher.group(PATTERN_DESTINATION_UNIT_GROUP);
//			Dimension destinationDimension = dimensionMap.get(unitString);
//			if (!result.dimension.getName().equals(destinationDimension.getName())) {
//				result.errors.add(
//					new MismatchedDimensionsException(
//						result.dimension,
//						destinationDimension
//					)
//				);
//			}
//			else {
//				try {
//					result.values =
//						result.dimension.convertSpecificUnit(
//							result.sourceValue,
//							result.dimension.getUnitByName(unitString)
//						);
//				}
//				catch (UnitRangeException e) {
//					result.errors.add(e);
//				}
//			}
//		}
//	}
//
//	public Stream<Dimension>
//	getDimensions() {
//		return dimensions.stream();
//	}
//
//	public Dimension
//	getDimensionFromName(String name) {
//		return dimensions.stream()
//			.filter(dimension -> dimension.getName().equalsIgnoreCase(name))
//			.findFirst()
//			.orElse(null);
//	}
//
}
