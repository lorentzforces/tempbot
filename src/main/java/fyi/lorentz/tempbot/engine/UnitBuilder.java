package fyi.lorentz.tempbot.engine;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class UnitBuilder {

    private String fullName;
    private String shortName;
    private boolean isDefaultConversionResult;
    private Set<String> detectableNames;

    private Double minValue;
    private Double maxValue;
    private Function<Double, Double> convertTo;
    private Function<Double, Double> convertFrom;

    public UnitBuilder() {
        detectableNames = new HashSet<>();
        minValue = null;
        maxValue = null;
    }

    public Unit
    build() {
        return new Unit(
                fullName,
                shortName,
                isDefaultConversionResult,
                detectableNames,
                convertTo,
                convertFrom
        );
    }

    public UnitBuilder
    setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public UnitBuilder
    setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public UnitBuilder
    setIsDefaultConversionResult(boolean isDefaultConversionResult) {
        this.isDefaultConversionResult = isDefaultConversionResult;
        return this;
    }

    public UnitBuilder
    addDetectableName(String detectableName) {
        detectableNames.add(detectableName);
        return this;
    }

    public UnitBuilder
    setConversionTo(Function<Double, Double> conversionFunction) {
        this.convertTo = conversionFunction;
        return this;
    }

    public UnitBuilder
    setConversionFrom(Function<Double, Double> conversionFunction) {
        this.convertFrom = conversionFunction;
        return this;
    }

}
