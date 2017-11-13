package fyi.lorentz.tempBot.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Temperature {

    private static final String TEMPERATURE_FORMAT = "([+-]?(?:[0-9]*\\.[0-9]+|[0-9]+))[ ]?([fFcC])";
    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile(TEMPERATURE_FORMAT);

    private double celsius;
    private double fahrenheit;

    public
    Temperature(String rawTemp) {
        if(rawTemp.isEmpty()) {
            throw new IllegalArgumentException("Expected temperature string, got empty string");
        }

        double value;
        char unit;
        Matcher matcher = TEMPERATURE_PATTERN.matcher(rawTemp.trim());
        if (matcher.matches()) {
            value = Double.parseDouble(matcher.group(1));
            unit = matcher.group(2).charAt(0);
        }
        else {
            throw new IllegalArgumentException("Bad temperature format from \"" + rawTemp + "\"");
        }

        switch (Character.toUpperCase(unit)) {
            case 'C': {
                celsius = value;
                fahrenheit = convertCelsiusToFahrenheit(value);
                break;
            }
            case 'F': {
                fahrenheit = value;
                celsius = convertFahrenheitToCelsius(value);
                break;
            }
            default: {
                throw new IllegalStateException("Fell through all cases for unit type");
            }
        }
    }

    private double
    convertFahrenheitToCelsius(double value) {
        return (value - 32) * (5.0 / 9.0);
    }

    private double
    convertCelsiusToFahrenheit(double value) {
        return value * (9.0 / 5.0) + 32;
    }

    public double
    getCelsius() {
        return celsius;
    }

    public double
    getFahrenheit() {
        return fahrenheit;
    }

}
