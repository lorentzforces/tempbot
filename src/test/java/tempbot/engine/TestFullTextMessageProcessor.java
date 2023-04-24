package tempbot.engine;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import tempbot.ProcessorData;
import tempbot.engine.ProcessingResult.ConvertedValues;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange.LimitType;
import tempbot.engine.ProcessingResult.ValueNotConverted;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static tempbot.Constants.COMPARISON_EPSILON;

public class TestFullTextMessageProcessor {

	private FullTextMessageProcessor processor;

	@Before
	public void
	setup() {
		processor = buildBasicTemperatureProcessor();
	}

	@Test
	public void
	basicValuesAreParsed() {
		var zeroCelsiusResults = processor.processMessage("0 C");
		assertThat(zeroCelsiusResults, hasSize(1));
		var zeroCelsius = getConvertedValuesFromResult(zeroCelsiusResults.get(0));
		assertDoublesEqual(0d, zeroCelsius.sourceValue().value());

		var tenCelsiusResults = processor.processMessage("10 C");
		assertThat(tenCelsiusResults, hasSize(1));
		var tenCelsius = getConvertedValuesFromResult(tenCelsiusResults.get(0));
		assertDoublesEqual(10d, tenCelsius.sourceValue().value());
	}

	@Test
	public void
	signsAreParsed() {
		var positiveResults = processor.processMessage("40 C");
		assertThat(positiveResults, hasSize(1));
		var positive = getConvertedValuesFromResult(positiveResults.get(0));
		assertDoublesEqual(40d, positive.sourceValue().value());

		var negativeResults = processor.processMessage("-40 C");
		assertThat(negativeResults, hasSize(1));
		var negative = getConvertedValuesFromResult(negativeResults.get(0));
		assertDoublesEqual(-40d, negative.sourceValue().value());
	}

	@Test
	public void
	spacesAreOptional() {
		var noSpaceResults = processor.processMessage("32F");
		assertThat(noSpaceResults, hasSize(1));
		var noSpace  = getConvertedValuesFromResult(noSpaceResults.get(0));
		assertDoublesEqual(32d, noSpace.sourceValue().value());
	}

	@Test
	public void
	originalUnitIsNotIncludedInResults() {
		var notOriginalUnitResults = processor.processMessage("32F");
		assertThat(notOriginalUnitResults, hasSize(1));

		var notOriginalUnit = getConvertedValuesFromResult(notOriginalUnitResults.get(0));
		assertThat(notOriginalUnit.values(), hasSize(1));
	}

	@Test
	public void
	onlyDefaultSourcesAreConverted() {
		var conversions = processor.processMessage("32F 500K");
		assertThat(conversions, hasSize(2));

		assertThat(
			conversions.stream().filter(result -> result instanceof ConvertedValues).count(),
			is(1L)
		);
		assertThat(
			conversions.stream().filter(result -> result instanceof ValueNotConverted).count(),
			is(1L)
		);
	}

	@Test
	public void
	maxMinValuesAreRespected() {
		var belowAbsoluteZeroResults = processor.processMessage("-300 C");
		assertThat(belowAbsoluteZeroResults, hasSize(1));
		var belowAbsoluteZero = getOutOfRangeFromResult(belowAbsoluteZeroResults.get(0));
		assertThat(belowAbsoluteZero.limitType(), is(LimitType.MINIMUM));
	}

	@Test
	public void
	conversionIsCorrect() {
		var waterFreezingResults = processor.processMessage("32 F");
		assertThat(waterFreezingResults, hasSize(1));
		var celsius = getUnitValueFromResult(
			getConvertedValuesFromResult(waterFreezingResults.get(0)),
			"degrees Celsius"
		);
		assertDoublesEqual(0d, celsius);

		var commonPointResults = processor.processMessage("-40 C");
		assertThat(commonPointResults, hasSize(1));
		var fahrenheit = getUnitValueFromResult(
			getConvertedValuesFromResult(commonPointResults.get(0)),
			"degrees Fahrenheit"
		);
		assertDoublesEqual(-40d, fahrenheit);
	}

//	@Test
//	public void
//	conversionToBaseUnitIsCorrect() {
//		var baseUnitResults = processor.processMessage("32F");
//		assertThat(baseUnitResults, hasSize(1));
//		var baseUnit = getConvertedValuesFromResult(baseUnitResults.get(0));
//		var kelvin = getUnitValueFromResult(baseUnit, "degrees Kelvin");
//		assertDoublesEqual(273.15d, kelvin);
//	}

	@Test
	public void
	multipleValuesAreConverted() {
		var multipleResults = processor.processMessage("32 F and some text and 23 F");
		assertThat(multipleResults, hasSize(2));

		// these should be strictly ordered to match their original values' appearances in the
		// original message
		var firstConversion = getConvertedValuesFromResult(multipleResults.get(0));
		assertDoublesEqual(32d, firstConversion.sourceValue().value());

		var secondConversion = getConvertedValuesFromResult(multipleResults.get(1));
		assertDoublesEqual(23d, secondConversion.sourceValue().value());
	}

	@Test
	public void
	duplicateValuesAreConsolidated() {
		var results = processor.processMessage("32F and 32F");
		assertThat(results, hasSize(1));
	}

	@Test
	public void
	maximumValuesProcessed() {
		// assumes that Constants.MAX_CONVERSIONS is 10
		var results = processor.processMessage("1F 2F 3F 4F 5F 6F 7F 8F 9F 10F 11F 12F");
		assertThat(results, hasSize(10));
	}

	@Test
	public void
	specificConversionSucceeds() {
		var results = processor.processMessage("5C to Kelvin");
		assertThat(results, hasSize(1));
		var kelvin = getUnitValueFromResult(
			getConvertedValuesFromResult(results.get(0)),
			"degrees Kelvin"
		);
		assertDoublesEqual(278.15d, kelvin);
	}

	@Test
	public void
	specificConversionWorksOnNonDefaultUnits() {
		var specificResults = processor.processMessage("2K to C");
		assertThat(specificResults, hasSize(1));
	}

//	@Test
//	public void
//	testSpecificConversionBetweenDimensionsFails() {
//		List<ProcessingResult> results = processor.processMessage("5C to dummy", false);
//
//		assertEquals(1, results.size());
//		ProcessingResult result = results.get(0);
//		assertEquals(1, result.errors.size());
//		Exception error = result.errors.get(0);
//		assertEquals(MismatchedDimensionsException.class, error.getClass());
//	}
//
//	@Test
//	public void
//	testSpecificAndGeneralConversionsBothWork() {
//		List<ProcessingResult> results = processor.processMessage("0F 5C to Kelvin", false);
//
//		assertEquals(2, results.size());
//
//		ProcessingResult generalConversion = results.get(0);
//		assertEquals(1, generalConversion.values.size());
//		assertDoublesEqual(0d, generalConversion.sourceValue.getValue());
//
//		ProcessingResult specificConversion = results.get(1);
//		assertEquals(1, specificConversion.values.size());
//		assertDoublesEqual(
//				278.15d,
//				getUnitValueFromResult(specificConversion, "degrees Kelvin")
//		);
//	}

//	@Test
//	public void
//	testMaxMinValuesAreRespectedWithSpecificConversions() {
//		List<ProcessingResult> results =
//				processor.processMessage("-20 K to Celsius", true);
//
//		assertEquals(1, results.size());
//		ProcessingResult belowAbsoluteZero = results.get(0);
//		assertEquals(1, belowAbsoluteZero.errors.size());
//		Exception error = belowAbsoluteZero.errors.get(0);
//		assertEquals(UnitRangeException.class, error.getClass());
//	}

	/**
	 * Temperature is used as a reasonable representative use case.
	 */
	private FullTextMessageProcessor
	buildBasicTemperatureProcessor() {
		UnitBuilder randomOtherUnit = new UnitBuilder()
				.setFullName("dummy")
				.setShortName("dumb")
				.setConversionTo(x -> x)
				.setConversionFrom(x -> x);
		DimensionBuilder randomDimension = new DimensionBuilder()
				.setName("Dummy Dimension")
				.addUnit(randomOtherUnit);

		return new FullTextMessageProcessor(Set.of(
			ProcessorData.createTemperatureDimension(),
			randomDimension.build()
		));
	}

	private double
	getUnitValueFromResult(ConvertedValues result, String fullUnitName) {
		return result.values().stream()
			.filter(value -> value.unit().getFullName().equals(fullUnitName))
			.map(UnitValue::value)
			.findFirst()
			.orElseThrow();
	}

	private void
	assertDoublesEqual(double expected, double actual) {
		assertThat(actual, closeTo(expected, COMPARISON_EPSILON));
	}

	private ConvertedValues
	getConvertedValuesFromResult(ProcessingResult result) {
		if (result instanceof ConvertedValues values) {
			return values;
		} else {
			throw new AssertionError(
				"Expected a ConvertedValues object, but found a different ProcessingResult: "
					+ result.toString()
			);
		}
	}

	private UnitOutOfRange
	getOutOfRangeFromResult(ProcessingResult result) {
		if (result instanceof UnitOutOfRange error) {
			return error;
		} else {
			throw new AssertionError(
				"Expected an UnitOutOfRange object, but found a different ProcessingResult: "
					+ result.toString()
			);
		}
	}

}
