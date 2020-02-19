package fyi.lorentz.tempbot.engine;

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
        return new Processor(masterPattern, newDimensions, dimensionMap);
    }

    private Pattern
    buildPattern(Collection<Dimension> dimensions) {
        String lineBeginningOrWhitespace = "(?:^|\\s)";

        StringBuilder numberRegex = new StringBuilder("(?<")
            .append(Processor.PATTERN_NUMBER_GROUP)
            .append(">[-+]?[0-9]*\\.?[0-9]+)");

        String sometimesASpace = "[ ]?";

        String labelRegex = dimensions.stream()
            .flatMap(dimension -> dimension.getUnitNames().stream())
            .map(unitLabel -> Pattern.quote(unitLabel))
            .collect(Collectors.joining("|"));

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

    public ProcessorBuilder
    addDimension(DimensionBuilder dimension) {
        dimensions.add(dimension);
        return this;
    }

}
