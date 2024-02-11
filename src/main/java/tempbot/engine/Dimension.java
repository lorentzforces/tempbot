package tempbot.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.tinylog.Logger;
import tempbot.engine.ProcessingResult.ConvertedValues;
import tempbot.engine.ProcessingResult.ProcessingError;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange;

public class Dimension {

	@Getter
	private final String name;

	@Getter
	private final List<Unit> units;

	private final Double minValue;
	private final Double maxValue;

	private final Map<String, Unit> unitMapping;

	@Getter
	private final Set<String> unitNames;

	@Getter
	private final boolean hasEagerConversions;

	@Builder
	public Dimension(
		@NonNull final String name,
		@NonNull @Singular final List<Unit> units,
		final Double minValue,
		final Double maxValue
	) {
		this.name = name;
		this.units = Collections.unmodifiableList(units);

		unitMapping = new HashMap<>();
		var eagerUnitSeen = false;
		for (final Unit unit : units) {
			for (final String unitName : unit.getDetectableNames()) {
				unitMapping.put(unitName, unit);
			}
			eagerUnitSeen = eagerUnitSeen || unit.isDefaultConversionSource();
		}

		this.unitNames = Collections.unmodifiableSet(unitMapping.keySet());
		hasEagerConversions = eagerUnitSeen;

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Convert an input value with the given unit to all applicable units.
	 */
	public ProcessingResult
	convertUnit(final UnitValue initialValue) {
		final var maybeRangeError = checkUnitValue(initialValue);
		if (maybeRangeError.isPresent()) {
			return maybeRangeError.orElseThrow();
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

	/**
	 * Convert an input value with a given unit to a specific output unit. Callers should check
	 * whether the dimension match for provided unites, because we can't access the other
	 * dimension from here; callers can provide a better error message when the dimensions don't
	 * match.
	 */
	public ProcessingResult
	convertSpecificUnits(final UnitValue initialValue, final Unit destinationUnit) {
		final var maybeRangeError = checkUnitValue(initialValue);
		if (maybeRangeError.isPresent()) {
			return maybeRangeError.orElseThrow();
		}

		// This is a sanity check to bail out if this dimension doesn't have both units in the
		// conversion.
		final var unitsAreDifferentDimensions = !(
			unitMapping.containsKey(initialValue.unit().getFullName())
			&& unitMapping.containsKey(destinationUnit.getFullName())
		);
		if (unitsAreDifferentDimensions) {
			Logger.error(String.format("""
				Dimension %s did not contain %s and/or %s units when attempting to perform a \
				specific conversion.""",
				name,
				initialValue.unit().getFullName(),
				destinationUnit.getFullName()
			));
			// Return a system error because we shouldn't get to this point, and the caller can
			// construct a DimensionMismatch result that provides better information.
			return new ProcessingError.SystemError();
		}

		final var baseValue = initialValue.unit().convertFrom(initialValue.value());
		return ConvertedValues.builder()
			.sourceValue(initialValue)
			.values(List.of(new UnitValue(destinationUnit, destinationUnit.convertTo(baseValue))))
			.build();
	}

	public List<UnitValue>
	convertSpecificUnit(
		final UnitValue initialValue,
		final Unit destinationUnit
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
	checkUnitValue(final UnitValue input) {
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

	/**
	 * Returns null if given string does not map to a unit
	 */
	public Unit
	getUnitByName(final String name) {
		return unitMapping.get(name);
	}

}
