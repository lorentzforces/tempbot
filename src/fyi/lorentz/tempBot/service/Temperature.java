package fyi.lorentz.tempBot.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Temperature {

    private static final String TEMPERATURE_FORMAT = "([+-]?(?:[0-9]*\\.[0-9]+|[0-9]+))[ ]?([fFcC])";
    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile(TEMPERATURE_FORMAT);

    private Map<String, Double> unitValues;

    private String initialUnit;
    private String convertedUnit;

    public
    Temperature(String rawTemp) {
        if(rawTemp.isEmpty()) {
            throw new IllegalArgumentException("Expected temperature string, got empty string");
        }

        double value;
        Matcher matcher = TEMPERATURE_PATTERN.matcher(rawTemp.trim());
        if (matcher.matches()) {
            value = Double.parseDouble(matcher.group(1));
            initialUnit = matcher.group(2).toUpperCase();
            convertedUnit = "C".equals(initialUnit) ? "F" : "C";
        }
        else {
            throw new IllegalArgumentException("Bad temperature format from \"" + rawTemp + "\"");
        }

        unitValues = new HashMap<>();
        unitValues.put(initialUnit, value);
        unitValues.put(
                convertedUnit,
                convertToUnit(convertedUnit, unitValues.get(initialUnit)) );
    }

    private double
    convertToUnit(String unit, double value) {
        double result = Double.NaN;

        switch(unit) {
            case "C": {
                result = (value - 32) * (5.0 / 9.0);
                break;
            }
            case "F": {
                result =  value * (9.0 / 5.0) + 32;
                break;
            }
        }

        return result;
    }

    public double
    getUnitValue(String unit) {
        return unitValues.get(unit);
    }

    public String
    getInitialUnit() {
        return initialUnit;
    }

    public String
    getConvertedUnit() {
        return convertedUnit;
    }

}
