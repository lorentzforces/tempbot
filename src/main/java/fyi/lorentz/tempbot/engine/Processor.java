package fyi.lorentz.tempbot.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static fyi.lorentz.tempbot.Constants.MAX_CONVERSIONS;

public class Processor {

    public static final String PATTERN_NUMBER_GROUP = "number";
    public static final String PATTERN_LABEL_GROUP = "label";

    private Pattern masterPattern;
    private Map<String, Dimension> dimensionMap;
    private List<Dimension> dimensions;

    public Processor(
        Pattern masterPattern,
        List<Dimension> dimensions,
        Map<String, Dimension> dimensionMap
    ) {
        this.masterPattern = masterPattern;
        this.dimensions = dimensions;
        this.dimensionMap = dimensionMap;
    }

    public List<ProcessingResult>
    processMessage(String message, boolean returnAllUnits) {
        Matcher matcher = masterPattern.matcher(message);

        List<ProcessingResult> results = new ArrayList<>();

        // checking against the max this way means that we consider
        // the number of results --NOT including duplicates--
        while (matcher.find() && results.size() < MAX_CONVERSIONS) {
            ProcessingResult result = new ProcessingResult();

            result.valueString = matcher.group(PATTERN_NUMBER_GROUP);
            result.labelString = matcher.group(PATTERN_LABEL_GROUP);
            result.dimension = dimensionMap.get(result.labelString);
            result.unit = result.dimension.getUnitByName(result.labelString);

            try {
                Double value = Double.valueOf(result.valueString);
                result.sourceValue = new UnitValue(result.unit, value);
            }
            catch (NumberFormatException e) {
                result.errors.add(e);
            }

            boolean valueExists =
                    results.stream().anyMatch(existingResult ->
                            result.sourceValue.equalsUnitValue(existingResult.sourceValue)
                    );

            if (!valueExists && result.sourceValue != null) {
                try {
                    result.values =
                            result.dimension.convertUnit(
                                    result.sourceValue,
                                    returnAllUnits
                            );
                }
                catch (UnitRangeException e) {
                    result.errors.add(e);
                }

                results.add(result);
            }
        }

        return results;
    }

    public Stream<Dimension>
    getDimensions() {
        return dimensions.stream();
    }

}
