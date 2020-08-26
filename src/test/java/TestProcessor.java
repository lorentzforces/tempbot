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
    testMaxMinValuesAreRespected() {
        ProcessingResult belowAbsoluteZero = processor.processMessage("-20 K", false).get(0);

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

    /**
     * Temperature is used as a reasonable representative use case.
     */
    private Processor
    buildBasicTemperatureProcessor() {
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
        ProcessorBuilder processorBuilder = new ProcessorBuilder();
        processorBuilder.addDimension(temperature);

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
