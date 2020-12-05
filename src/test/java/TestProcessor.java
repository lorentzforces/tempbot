package fyi.lorentz.tempbot.engine;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static fyi.lorentz.tempbot.Constants.COMPARISON_EPSILON;
import static org.junit.Assert.assertEquals;

public class TestProcessor {

    private Processor processor;

    @Before
    public void
    setup() {
        processor = buildBasicTemperatureProcessor();
    }

    @Test
    public void
    testBasicValuesAreParsed() {
        ProcessingResult zeroCelsius = processor.processMessage("0 C", false).get(0);
        assertDoublesEqual(0d, zeroCelsius.sourceValue.getValue());

        ProcessingResult tenCelsius = processor.processMessage("10 C", false).get(0);
        assertDoublesEqual(10d, tenCelsius.sourceValue.getValue());
    }

    @Test
    public void
    testSignsAreParsed() {
        ProcessingResult positive = processor.processMessage("40 C", false).get(0);
        assertDoublesEqual(40d, positive.sourceValue.getValue());

        ProcessingResult negative = processor.processMessage("-40 C", false).get(0);
        assertDoublesEqual(-40d, negative.sourceValue.getValue());
    }

    @Test
    public void
    testSpacesAreOptional() {
        ProcessingResult noSpace = processor.processMessage("32F", false).get(0);
        assertDoublesEqual(32d, noSpace.sourceValue.getValue());
    }

    @Test
    public void
    testOriginalUnitIsNotIncludedInResults() {
        ProcessingResult notOriginalUnit = processor.processMessage("32F", false).get(0);
        assertEquals(1, notOriginalUnit.values.size());
    }

    @Test
    public void
    testConvertAllUnitsReturnsAllUnits() {
        ProcessingResult allUnits = processor.processMessage("32F", true).get(0);
        assertEquals(2, allUnits.values.size());
    }

    @Test
    public void
    testDefaultSourcesAreAutomaticallyConverted() {
        List<ProcessingResult> defaultConversionsOnly =
                processor.processMessage("32F 500K", false);
        assertEquals(1, defaultConversionsOnly.size());
    }

    @Test
    public void
    testMaxMinValuesAreRespected() {
        ProcessingResult belowAbsoluteZero = processor.processMessage("-20 K", true).get(0);

        assertEquals(1, belowAbsoluteZero.errors.size());
        Exception error = belowAbsoluteZero.errors.get(0);
        assertEquals(UnitRangeException.class, error.getClass());
    }


    @Test
    public void
    testConversionIsCorrect() {
        ProcessingResult waterFreezing = processor.processMessage("32 F", false).get(0);

        Double celsius = getUnitValueFromResult(waterFreezing, "degrees Celsius");
        assertDoublesEqual(0d, celsius);

        ProcessingResult commonPoint = processor.processMessage("-40 C", false).get(0);

        Double fahrenheit = getUnitValueFromResult(commonPoint, "degrees Fahrenheit");
        assertDoublesEqual(-40d, fahrenheit);
    }

    @Test
    public void
    testConversionToBaseUnitIsCorrect() {
        ProcessingResult baseUnit = processor.processMessage("32F", true).get(0);

        Double kelvin = getUnitValueFromResult(baseUnit, "degrees Kelvin");
        assertDoublesEqual(273.15d, kelvin);
    }

    @Test
    public void
    testMultipleValuesAreConverted() {
        List<ProcessingResult> multipleResults =
                processor.processMessage("32 F and some text and 23 F", false);

        assertEquals(2, multipleResults.size());

        assertDoublesEqual(32d, multipleResults.get(0).sourceValue.getValue());
        assertDoublesEqual(23d, multipleResults.get(1).sourceValue.getValue());
    }

    @Test
    public void
    testDuplicateValuesAreConsolidated() {
        List<ProcessingResult> results = processor.processMessage("32F and 32F", false);

        assertEquals(1, results.size());
    }

    @Test
    public void
    testMaximumValuesProcessed() {
        // assumes that Constants.MAX_CONVERSIONS is 10
        List<ProcessingResult> results =
                processor.processMessage("1F 2F 3F 4F 5F 6F 7F 8F 9F 10F 11F 12F", false);

        assertEquals(10, results.size());
    }

    @Test
    public void
    testSpecificConversionSucceeds() {
        List<ProcessingResult> results = processor.processMessage("5C to Kelvin", false);

        assertEquals(1, results.size());
        ProcessingResult result = results.get(0);
        assertDoublesEqual(278.15d, getUnitValueFromResult(result, "degrees Kelvin"));
    }

    @Test
    public void
    testSpecificConversionWorksOnNonDefaultUnits() {
        List<ProcessingResult> specificResults = processor.processMessage("2K to C", false);
        assertEquals(1, specificResults.size());
    }

    @Test
    public void
    testSpecificConversionBetweenDimensionsFails() {
        List<ProcessingResult> results = processor.processMessage("5C to dummy", false);

        assertEquals(1, results.size());
        ProcessingResult result = results.get(0);
        assertEquals(1, result.errors.size());
        Exception error = result.errors.get(0);
        assertEquals(MismatchedDimensionsException.class, error.getClass());
    }

    @Test
    public void
    testSpecificAndGeneralConversionsBothWork() {
        List<ProcessingResult> results = processor.processMessage("0F 5C to Kelvin", false);

        assertEquals(2, results.size());

        ProcessingResult generalConversion = results.get(0);
        assertEquals(1, generalConversion.values.size());
        assertDoublesEqual(0d, generalConversion.sourceValue.getValue());

        ProcessingResult specificConversion = results.get(1);
        assertEquals(1, specificConversion.values.size());
        assertDoublesEqual(
                278.15d,
                getUnitValueFromResult(specificConversion, "degrees Kelvin")
        );
    }

    @Test
    public void
    testMaxMinValuesAreRespectedWithSpecificConversions() {
        List<ProcessingResult> results =
                processor.processMessage("-20 K to Celsius", true);

        assertEquals(1, results.size());
        ProcessingResult belowAbsoluteZero = results.get(0);
        assertEquals(1, belowAbsoluteZero.errors.size());
        Exception error = belowAbsoluteZero.errors.get(0);
        assertEquals(UnitRangeException.class, error.getClass());
    }

    /**
     * Temperature is used as a reasonable representative use case.
     */
    private Processor
    buildBasicTemperatureProcessor() {
        UnitBuilder celsius = new UnitBuilder()
                .setFullName("degrees Celsius")
                .setShortName("°C")
                .setIsDefaultConversionSource(true)
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
                .setIsDefaultConversionSource(true)
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
        UnitBuilder randomOtherUnit = new UnitBuilder()
                .setFullName("dummy")
                .setShortName("dumb")
                .setConversionTo(x -> x)
                .setConversionFrom(x -> x);
        DimensionBuilder randomDimension = new DimensionBuilder()
                .setName("Dummy Dimension")
                .addUnit(randomOtherUnit);
        ProcessorBuilder processorBuilder = new ProcessorBuilder();
        processorBuilder.addDimension(temperature);
        processorBuilder.addDimension(randomDimension);

        return processorBuilder.build();
    }

    private double
    getUnitValueFromResult(ProcessingResult result, String fullUnitName) {
        return result.values.stream()
                .filter(value -> value.getUnit().getFullName().equals(fullUnitName))
                .map(UnitValue::getValue)
                .findFirst().get();
    }

    private void
    assertDoublesEqual(double expected, double actual) {
        assertEquals(expected, actual, COMPARISON_EPSILON);
    }

}
