package tempbot;

import java.util.Set;
import tempbot.engine.Dimension;
import tempbot.engine.Unit;

public class ProcessorData {

	public static Set<Dimension>
	createAllDimensions() {
		return Set.of(
			createTemperatureDimension(),
			createSpeedDimension(),
			createBloodSugarDimension(),
			createWeightDimension(),
			createDistanceDimension()
		);
	}

	// TODO: add other dimensions
	// these may have to wait until I build a way to specify the target
	// of the conversion (i.e. "I want to convert a specific unit value to another
	// specific unit) just because of the massive number of units involved that people
	// might be interested in
	// * length
	//	 * do we maybe do a "large scale distance" versus "short scale distance?"
	//	   which we might call "distance" vs "length" maybe?
	// * liquid volume

	public static Dimension
	createTemperatureDimension() {
		final var celsius = Unit.builder()
			.fullName("degrees Celsius")
			.shortName("°C")
			.defaultConversionSource(true)
			.defaultConversionResult(true)
			.detectableName("C")
			.detectableName("c")
			.detectableName("Celsius")
			.detectableName("celsius")
			.convertTo(x -> x - 273.15d)
			.convertFrom(x -> x + 273.15d)
			.build();
		final var fahrenheit = Unit.builder()
			.fullName("degrees Fahrenheit")
			.shortName("°F")
			.defaultConversionSource(true)
			.defaultConversionResult(true)
			.detectableName("F")
			.detectableName("f")
			.detectableName("Fahrenheit")
			.detectableName("fahrenheit")
			.convertTo(x -> (x - 273.15d) * (9d/5d) + 32d)
			.convertFrom(x -> (x - 32d) * (5d/9d) + 273.15d)
			.build();
		final var kelvin = Unit.builder()
			.fullName("degrees Kelvin")
			.shortName("K")
			// don't recognize lowercase k to avoid thousands (like "100k") being interpreted as
			// temperatures
			.detectableName("Kelvin")
			.detectableName("kelvin")
			.convertTo(x -> x)
			.convertFrom(x -> x)
			.build();
		return Dimension.builder()
			.name("Temperature")
			.unit(celsius)
			.unit(fahrenheit)
			.unit(kelvin)
			.minValue(0d)
			.build();
	}

	public static Dimension
	createSpeedDimension() {
		final var milesPerHour = Unit.builder()
			.fullName("miles per hour")
			.shortName("mph")
			.defaultConversionResult(true)
			.detectableName("mi/hr")
			.convertTo(x -> (x / 0.44704d))
			.convertFrom(x -> (x * 0.44704d))
			.build();
		final var kilometersPerHour = Unit.builder()
			.fullName("kilometers per hour")
			.shortName("kph")
			.defaultConversionResult(true)
			.detectableName("km/hr")
			.convertTo(x -> (x * 3.6d))
			.convertFrom(x -> (x / 3.6d))
			.build();
		final var metersPerSecond = Unit.builder()
			.fullName("meters per second")
			.shortName("m/s")
			.defaultConversionResult(false)
			.convertTo(x -> x)
			.convertFrom(x -> x)
			.build();
		final var feetPerSecond = Unit.builder()
			.fullName("feet per second")
			.shortName("fps")
			.defaultConversionResult(false)
			.detectableName("ft/s")
			.convertTo(x -> (x / 0.3048d))
			.convertFrom(x -> (x * 0.3048d))
			.build();
		final var knots = Unit.builder()
			.fullName("nautical miles per hour")
			.shortName("knots")
			.convertTo(x -> x / 0.51444d)
			.convertFrom(x -> x * 0.51444d)
			.build();
		return Dimension.builder()
			.name("Speed")
			.unit(milesPerHour)
			.unit(kilometersPerHour)
			.unit(metersPerSecond)
			.unit(feetPerSecond)
			.unit(knots)
			.minValue(0d)
			.build();
	}

	public static Dimension
	createBloodSugarDimension() {
		final var usUnits = Unit.builder()
			.fullName("milligrams per deciliter")
			.shortName("mg/dL")
			.defaultConversionSource(true)
			.defaultConversionResult(true)
			.detectableName("mg/dl")
			.convertTo(x -> (x * 18d))
			.convertFrom(x -> (x / 18d))
			.build();
		final var globalUnits = Unit.builder()
			.fullName("millimols per liter")
			.shortName("mmol/L")
			.defaultConversionSource(true)
			.defaultConversionResult(true)
			.detectableName("mmol/l")
			.convertTo(x -> x)
			.convertFrom(x -> x)
			.build();
		return Dimension.builder()
			.name("Blood sugar")
			.unit(usUnits)
			.unit(globalUnits)
			.minValue(0d)
			.build();
	}

	public static Dimension
	createWeightDimension() {
		final var kilograms = Unit.builder()
			.fullName("kilograms")
			.shortName("kg")
			.defaultConversionResult(true)
			.convertTo(x -> x)
			.convertFrom(x -> x)
			.build();
		final var pounds = Unit.builder()
			.fullName("pounds")
			.shortName("lbs")
			.defaultConversionResult(true)
			.detectableName("lb")
			.convertTo(x -> (x * 2.2046d))
			.convertFrom(x -> (x / 2.2046d))
			.build();
		return Dimension.builder()
			.name("Weight")
			.unit(kilograms)
			.unit(pounds)
			.minValue(0d)
			.build();
	}

	public static Dimension
	createDistanceDimension() {
		final var meters = Unit.builder()
			.fullName("meters")
			.shortName("m")
			.detectableName("metres")
			.convertTo(x -> x)
			.convertFrom(x -> x)
			.build();
		final var kilometers = Unit.builder()
			.fullName("kilometers")
			.shortName("km")
			.detectableName("kilometres")
			.convertTo(x -> x / 1000d)
			.convertFrom(x -> x * 1000d)
			.build();
		final var miles = Unit.builder()
			.fullName("miles")
			.shortName("mi")
			.convertTo(x -> x / 1609.34d)
			.convertFrom(x -> x * 1609.34d)
			.build();
		final var nauticalMiles = Unit.builder()
			.fullName("nautical miles")
			.shortName("nmi")
			.convertTo(x -> x / 1852d)
			.convertFrom(x -> x * 1852d)
			.build();
		return Dimension.builder()
			.name("Distance")
			.unit(meters)
			.unit(kilometers)
			.unit(miles)
			.unit(nauticalMiles)
			.minValue(0d)
			.build();
	}

}
