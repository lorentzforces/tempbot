package tempbot.engine;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import tempbot.ProcessorData;
import tempbot.engine.ProcessingResult.ConvertedValues;
import tempbot.engine.ProcessingResult.ProcessingError.DimensionMismatch;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange;
import tempbot.engine.ProcessingResult.ProcessingError.UnknownUnitType;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange.LimitType;
import tempbot.engine.ProcessingResult.ValueNotConverted;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static tempbot.Constants.COMPARISON_EPSILON;

public class UserInputProcessorTest {

	private UserInputProcessor processor;

	@Before
	public void
	setup() {
		processor = buildBasicTemperatureProcessor();
	}

	@Test
	public void
	basicValuesAreParsed() {
		final var zeroCelsiusResults = processor.processMessage("0 C");
		assertThat(zeroCelsiusResults, hasSize(1));
		final var zeroCelsius = getConvertedValuesFromResult(zeroCelsiusResults.get(0));
		assertDoublesEqual(0d, zeroCelsius.sourceValue().value());

		final var tenCelsiusResults = processor.processMessage("10 C");
		assertThat(tenCelsiusResults, hasSize(1));
		final var tenCelsius = getConvertedValuesFromResult(tenCelsiusResults.get(0));
		assertDoublesEqual(10d, tenCelsius.sourceValue().value());
	}

	@Test
	public void
	signsAreParsed() {
		final var positiveResults = processor.processMessage("40 C");
		assertThat(positiveResults, hasSize(1));
		final var positive = getConvertedValuesFromResult(positiveResults.get(0));
		assertDoublesEqual(40d, positive.sourceValue().value());

		final var negativeResults = processor.processMessage("-40 C");
		assertThat(negativeResults, hasSize(1));
		final var negative = getConvertedValuesFromResult(negativeResults.get(0));
		assertDoublesEqual(-40d, negative.sourceValue().value());
	}

	@Test
	public void
	spacesAreOptional() {
		final var noSpaceResults = processor.processMessage("32F");
		assertThat(noSpaceResults, hasSize(1));
		final var noSpace  = getConvertedValuesFromResult(noSpaceResults.get(0));
		assertDoublesEqual(32d, noSpace.sourceValue().value());
	}

	@Test
	public void
	originalUnitIsNotIncludedInResults() {
		final var notOriginalUnitResults = processor.processMessage("32F");
		assertThat(notOriginalUnitResults, hasSize(1));

		final var notOriginalUnit = getConvertedValuesFromResult(notOriginalUnitResults.get(0));
		assertThat(notOriginalUnit.values(), hasSize(1));
	}

	@Test
	public void
	onlyDefaultSourcesAreConverted() {
		final var conversions = processor.processMessage("32F 500K");
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
		final var belowAbsoluteZeroResults = processor.processMessage("-300 C");
		assertThat(belowAbsoluteZeroResults, hasSize(1));
		final var belowAbsoluteZero = getOutOfRangeFromResult(belowAbsoluteZeroResults.get(0));
		assertThat(belowAbsoluteZero.limitType(), is(LimitType.MINIMUM));
	}

	@Test
	public void
	conversionIsCorrect() {
		final var waterFreezingResults = processor.processMessage("32 F");
		assertThat(waterFreezingResults, hasSize(1));
		final var celsius = getUnitValueFromResult(
			getConvertedValuesFromResult(waterFreezingResults.get(0)),
			"degrees Celsius"
		);
		assertDoublesEqual(0d, celsius);

		final var commonPointResults = processor.processMessage("-40 C");
		assertThat(commonPointResults, hasSize(1));
		final var fahrenheit = getUnitValueFromResult(
			getConvertedValuesFromResult(commonPointResults.get(0)),
			"degrees Fahrenheit"
		);
		assertDoublesEqual(-40d, fahrenheit);
	}

	@Test
	public void
	conversionsWillWorkWithLeadingPunctuation() {
		final var punctuationResults = processor.processMessage("(-32.8 F)");
		assertThat(punctuationResults, hasSize(1));
		final var convertedValue = getUnitValueFromResult(
			getConvertedValuesFromResult(punctuationResults.get(0)),
			"degrees Celsius"
		);
		assertDoublesEqual(-36d, convertedValue);
	}

	@Test
	public void
	multipleValuesAreConverted() {
		final var multipleResults = processor.processMessage("32 F and some text and 23 F");
		assertThat(multipleResults, hasSize(2));

		// these should be strictly ordered to match their original values' appearances in the
		// original message
		final var firstConversion = getConvertedValuesFromResult(multipleResults.get(0));
		assertDoublesEqual(32d, firstConversion.sourceValue().value());

		final var secondConversion = getConvertedValuesFromResult(multipleResults.get(1));
		assertDoublesEqual(23d, secondConversion.sourceValue().value());
	}

	@Test
	public void
	duplicateValuesAreConsolidated() {
		final var results = processor.processMessage("32F and 32F");
		assertThat(results, hasSize(1));
	}

	@Test
	public void
	maximumValuesProcessed() {
		// assumes that Constants.MAX_CONVERSIONS is 10
		final var results = processor.processMessage("1F 2F 3F 4F 5F 6F 7F 8F 9F 10F 11F 12F");
		assertThat(results, hasSize(10));
	}

	@Test
	public void
	specificConversionSucceeds() {
		final var result = processor.processSpecificConversionRequest("5C", "Kelvin");

		final var kelvin = getUnitValueFromResult(
			getConvertedValuesFromResult(result),
			"degrees Kelvin"
		);
		assertDoublesEqual(278.15d, kelvin);
	}

	@Test
	public void
	specificConversionBetweenDimensionsFails() {
		final var result = processor.processSpecificConversionRequest("5C", "dummy");

		assertThat(result, instanceOf(DimensionMismatch.class));
		final var err = (DimensionMismatch) result;
		assertThat(err.destinationDimension().getName(), is("DummyDimension"));
		assertThat(err.sourceDimension().getName(), is("Temperature"));
		assertThat(err.sourceValue().value(), is(5.0d));
		assertThat(err.sourceValue().unit().getShortName(), is("°C"));
	}

	@Test
	public void
	specificConversionFailsForGarbageDestinationUnit() {
		final var result = processor.processSpecificConversionRequest("5C", "asdf");

		assertThat(result, instanceOf(UnknownUnitType.class));
		final var err = (UnknownUnitType) result;
		assertThat(err.badUnitString(), is("asdf"));
	}

	@Test
	public void
	maxAndMinValuesAreRespectedWithSpecificConversions() {
		final var result = processor.processSpecificConversionRequest("-20 K", "Celsius");

		assertThat(result, instanceOf(UnitOutOfRange.class));
		final var err = (UnitOutOfRange) result;
		assertThat(err.limitType(), is(UnitOutOfRange.LimitType.MINIMUM));
		assertThat(err.rangeLimitingValue().value(), is(0d));
	}

	/**
	 * Temperature is used as a reasonable representative use case.
	 */
	private UserInputProcessor
	buildBasicTemperatureProcessor() {
		final var randomOtherUnit = Unit.builder()
			.fullName("dummy")
			.shortName("dumb")
			.convertTo(x -> x)
			.convertFrom(x -> x)
			.build();
		final var randomDimension = Dimension.builder()
			.name("DummyDimension")
			.unit(randomOtherUnit)
			.build();

		return new UserInputProcessor(Set.of(
			ProcessorData.createTemperatureDimension(),
			randomDimension
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
