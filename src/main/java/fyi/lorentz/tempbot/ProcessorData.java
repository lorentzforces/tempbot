package fyi.lorentz.tempbot;

import fyi.lorentz.tempbot.engine.DimensionBuilder;
import fyi.lorentz.tempbot.engine.Processor;
import fyi.lorentz.tempbot.engine.ProcessorBuilder;
import fyi.lorentz.tempbot.engine.UnitBuilder;

public class ProcessorData {

    public static Processor
    createProcesser() {
        return new ProcessorBuilder()
                .addDimension(createTemperatureDimension())
                .addDimension(createSpeedDimension())
                .addDimension(createBloodSugarDimension())
                .addDimension(createWeightDimension())
                .build();
        // TODO: add other dimensions
        // these may have to wait until I build a way to specify the target
        // of the conversion (i.e. "I want to convert a specific unit value to another
        // specific unit) just because of the massive number of units involved that people
        // might be interested in
        // * length
        //   * do we maybe do a "large scale distance" versus "short scale distance?"
        //     which we might call "distance" vs "length" maybe?
        // * liquid volume
    }

    public static DimensionBuilder
    createTemperatureDimension() {
        UnitBuilder celsius = new UnitBuilder()
                .setFullName("degrees Celsius")
                .setShortName("°C")
                .setIsDefaultConversionResult(true)
                .addDetectableName("C")
                .addDetectableName("c")
                .addDetectableName("Celsius")
                .addDetectableName("celsius")
                .setConversionTo(x -> x - 273.15d)
                .setConversionFrom(x -> x + 273.15d);
        UnitBuilder fahrenheit = new UnitBuilder()
                .setFullName("degrees Fahrenheit")
                .setShortName("°F")
                .setIsDefaultConversionResult(true)
                .addDetectableName("F")
                .addDetectableName("f")
                .addDetectableName("Fahrenheit")
                .addDetectableName("fahrenheit")
                .setConversionTo(x -> (x - 273.15d) * (9d/5d) + 32d)
                .setConversionFrom(x -> (x - 32d) * (5d/9d) + 273.15d);
        UnitBuilder kelvin = new UnitBuilder()
                .setFullName("degrees Kelvin")
                .setShortName("K")
                // don't recognize lowercase k to avoid thousands
                // being interpreted as temperatures
                .addDetectableName("Kelvin")
                .addDetectableName("kelvin")
                .setConversionTo(x -> x)
                .setConversionFrom(x -> x);
        DimensionBuilder temperature = new DimensionBuilder()
                .setName("Temperature")
                .addUnit(celsius)
                .addUnit(fahrenheit)
                .addUnit(kelvin)
                .setMinValue(0d);

        return temperature;
    }

    public static DimensionBuilder

    createSpeedDimension() {
        UnitBuilder milesPerHour = new UnitBuilder()
                .setFullName("miles per hour")
                .setShortName("mph")
                .setIsDefaultConversionResult(true)
                .addDetectableName("mi/hr")
                .setConversionTo(x -> (x / 0.44704d))
                .setConversionFrom(x -> (x * 0.44704d));
        UnitBuilder kilometersPerHour = new UnitBuilder()
                .setFullName("kilometers per hour")
                .setShortName("kph")
                .setIsDefaultConversionResult(true)
                .addDetectableName("km/hr")
                .setConversionTo(x -> (x * 3.6d))
                .setConversionFrom(x -> (x / 3.6d));
        UnitBuilder metersPerSecond = new UnitBuilder()
                .setFullName("meters per second")
                .setShortName("m/s")
                .setIsDefaultConversionResult(false)
                .setConversionTo(x -> x)
                .setConversionFrom(x -> x);
        UnitBuilder feetPerSecond = new UnitBuilder()
                .setFullName("feet per second")
                .setShortName("fps")
                .setIsDefaultConversionResult(false)
                .addDetectableName("ft/s")
                .setConversionTo(x -> (x / 0.3048d))
                .setConversionFrom(x -> (x * 0.3048d));
        DimensionBuilder speed = new DimensionBuilder()
                .setName("Speed")
                .addUnit(milesPerHour)
                .addUnit(kilometersPerHour)
                .addUnit(metersPerSecond)
                .addUnit(feetPerSecond)
                .setMinValue(0d);

        return speed;
    }

    public static DimensionBuilder
    createBloodSugarDimension() {
        UnitBuilder usUnits = new UnitBuilder()
                .setFullName("milligrams per deciliter")
                .setShortName("mg/dL")
                .setIsDefaultConversionResult(true)
                .addDetectableName("mg/dl")
                .setConversionTo(x -> (x * 18d))
                .setConversionFrom(x -> (x / 18d));
        UnitBuilder globalUnits = new UnitBuilder()
                .setFullName("millimols per liter")
                .setShortName("mmol/L")
                .setIsDefaultConversionResult(true)
                .addDetectableName("mmol/l")
                .setConversionTo(x -> x)
                .setConversionFrom(x -> x);
        DimensionBuilder bloodSugar = new DimensionBuilder()
                .setName("Blood sugar")
                .addUnit(usUnits)
                .addUnit(globalUnits)
                .setMinValue(0d);

        return bloodSugar;
    }

    public static DimensionBuilder
    createWeightDimension() {
        UnitBuilder kilograms = new UnitBuilder()
                .setFullName("kilograms")
                .setShortName("kg")
                .setIsDefaultConversionResult(true)
                .setConversionTo(x -> x)
                .setConversionFrom(x -> x);
        UnitBuilder pounds = new UnitBuilder()
                .setFullName("pounds")
                .setShortName("lbs")
                .setIsDefaultConversionResult(true)
                .addDetectableName("lb")
                .setConversionTo(x -> (x * 2.2046))
                .setConversionFrom(x -> (x / 2.2046));
        DimensionBuilder weight = new DimensionBuilder()
                .setName("Weight")
                .addUnit(kilograms)
                .addUnit(pounds)
                .setMinValue(0d);

        return weight;
    }

}
