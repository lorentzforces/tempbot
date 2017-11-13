package fyi.lorentz.tempBot.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemperatureProcessor {

    private static final String TEMPERATURE_REGEX =
            "(?:^|\\s)`*([+\\-]?(?:[0-9]*\\.[0-9]+|[0-9]+)[ ]?[fFcC])`*(?:\\s|$)";
    private static final Pattern TEMPERATURE_PATTERN =
            Pattern.compile(TEMPERATURE_REGEX);

    private String text;

    public TemperatureProcessor(String text) {
        this.text = text;
    }

    public String
    processMessage() {
        String[] temperatureStrings = getTemperatureStrings();
        Temperature[] temperatures = convertTemperatures(temperatureStrings);

        StringBuilder output = new StringBuilder(temperatureStrings.length * 30);
        if(temperatures.length > 0) {
            for (int i = 0; i < temperatures.length; i++) {
                formatTemperature(output, temperatures[i]);
                if (i < temperatures.length - 1) {
                    output.append("\n");
                }
            }
        }

        return output.toString();
    }

    private Temperature[]
    convertTemperatures(String[] temperatureStrings) {
        Temperature[] result = new Temperature[temperatureStrings.length];

        for(int i = 0; i < temperatureStrings.length; i++) {
            result[i] = new Temperature(temperatureStrings[i]);
        }

        return result;
    }

    private void
    formatTemperature(StringBuilder buffer, Temperature temp) {
        DecimalFormat format = new DecimalFormat("###,###,###,###.###");

        buffer.append("**")
                .append(format.format(temp.getCelsius()))
                .append("\u00b0C")
                .append("**")
                .append(" = ")
                .append("**")
                .append(format.format(temp.getFahrenheit()))
                .append("\u00b0F")
                .append("**");
    }

    public String[]
    getTemperatureStrings() {
        ArrayList<String> tempStrings = new ArrayList<>();
        Matcher matcher = TEMPERATURE_PATTERN.matcher(text);

        int currentIndex = 0;
        while(matcher.find(currentIndex)) {
            tempStrings.add(matcher.group().trim());

            currentIndex = matcher.end();
            // workaround to allow adjacent temperature strings to "share" whitespace
            if(Character.isWhitespace(text.charAt(matcher.end() - 1))) {
                currentIndex -= 1;
            }
        }

        String[] stringArray = new String[0];
        return tempStrings.toArray(stringArray);
    }

}
