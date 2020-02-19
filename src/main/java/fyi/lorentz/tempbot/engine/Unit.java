package fyi.lorentz.tempbot.engine;

import java.util.Set;
import java.util.function.Function;

public class Unit {

    private final String fullName;
    private final String shortName;
    private final boolean isDefaultConversionResult;
    private final Set<String> detectableNames;

    private final Function<Double, Double> convertTo;
    private final Function<Double, Double> convertFrom;

    public Unit(
            String fullName,
            String shortName,
            boolean isDefaultConversionResult,
            Set<String> detectableNames,
            Function<Double, Double> convertTo,
            Function<Double, Double> convertFrom
    ) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.isDefaultConversionResult = isDefaultConversionResult;
        this.detectableNames = detectableNames;
        detectableNames.add(fullName);
        detectableNames.add(shortName);

        this.convertTo = convertTo;
        this.convertFrom = convertFrom;
    }

    public String
    getFullName() {
        return fullName;
    }

    public String
    getShortName() {
        return shortName;
    }

    public boolean
    isDefaultConversionResult() {
        return isDefaultConversionResult;
    }

    public Set<String>
    getDetectableNames() {
        return detectableNames;
    }

    public Double convertTo(Double input) {
        return convertTo.apply(input);
    }

    public Double convertFrom(Double input) {
        return convertFrom.apply(input);
    }

}
