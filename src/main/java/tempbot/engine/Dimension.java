package tempbot.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NonNull;
import tempbot.engine.ProcessingResult.ConvertedValues;
import tempbot.engine.ProcessingResult.ProcessingError;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange;

public class Dimension {

	private final String name;
	private final List<Unit> units;
	private final Map<String, Unit> unitMapping;
	private final Double minValue;
	private final Double maxValue;

	public Dimension(
		@NonNull String name,
		@NonNull List<Unit> units,
		Double minValue,
		Double maxValue
	) {
		this.name = name;
		this.units = units;

		this.unitMapping = new HashMap<>();
		for (Unit unit : units) {
			for (String unitName : unit.getDetectableNames()) {
				this.unitMapping.put(unitName, unit);
			}
		}

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Convert an input value with the given unit to all applicable units.
	 */
	public ProcessingResult
	convertUnit(UnitValue initialValue) {
		final var maybeError = checkUnitValue(initialValue);
		if (maybeError.isPresent()) {
			return maybeError.orElseThrow();
		}

		final var baseValue = initialValue.unit().convertFrom(initialValue.value());

		return ConvertedValues.builder()
			.sourceValue(initialValue)
			.values(
				units.stream()
					.filter(Unit::isDefaultConversionResult)
					.filter(unit -> unit != initialValue.unit())
					.map(unit -> new UnitValue(unit, unit.convertTo(baseValue)))
					.toList()
			).build();
	}

	public List<UnitValue>
	convertSpecificUnit(
		UnitValue initialValue,
		Unit destinationUnit
	) {
		checkUnitValue(initialValue);
		final var baseValue = initialValue.unit().convertFrom(initialValue.value());

		return units.stream()
			// due to this filter there should only ever be one element in this list
			.filter(unit -> unit.equalsUnit(destinationUnit))
			.map(unit -> new UnitValue(unit, unit.convertTo(baseValue)))
			.toList();
	}

	private Optional<ProcessingError.UnitOutOfRange>
	checkUnitValue(UnitValue input) {
		final var baseValue = input.unit().convertFrom(input.value());

		if (minValue != null && baseValue < minValue) {
			final var minValueInUnit = input.unit().convertTo(minValue);
			return Optional.of(
				UnitOutOfRange.builder()
					.rangeLimitingValue(new UnitValue(input.unit(), minValueInUnit))
					.sourceValue(input)
					.build()
			);
		} else if (maxValue != null && baseValue > maxValue) {
			final var maxValueInUnit = input.unit().convertTo(maxValue);
			return Optional.of(
				UnitOutOfRange.builder()
					.rangeLimitingValue(new UnitValue(input.unit(), maxValueInUnit))
					.sourceValue(input)
					.build()
			);
		} else {
			return Optional.empty();
		}

	}

	public Stream<Unit>
	getUnits() {
		return units.stream();
	}

	public String
	getName() {
		return name;
	}

	public Set<String>
	getUnitNames() {
		return unitMapping.keySet();
	}

	/**
	 * Returns null if given string does not map to a unit
	 */
	public Unit
	getUnitByName(String name) {
		return unitMapping.get(name);
	}

}
