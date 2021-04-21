package tempbot.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Dimension {

	private final String name;
	private final List<Unit> units;
	private final Map<String, Unit> unitMapping;
	private final Double minValue;
	private final Double maxValue;

	public Dimension(
			String name,
			List<Unit> units,
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
	 * @param returnAllUnits if false, only return default units, otherwise all
	 */
	public List<UnitValue>
	convertUnit(
			UnitValue initialValue,
			boolean returnAllUnits
	) throws UnitRangeException {
		checkUnitValue(initialValue);

		Double baseValue = initialValue.getUnit().convertFrom(initialValue.getValue());
		return units.stream()
				.filter( unit -> (returnAllUnits || unit.isDefaultConversionResult()) )
				.filter( unit -> unit != initialValue.getUnit())
				.map( unit -> new UnitValue(unit, unit.convertTo(baseValue)) )
				.collect(Collectors.toList());
	}

	public List<UnitValue>
	convertSpecificUnit(
			UnitValue initialValue,
			Unit destinationUnit
	) throws UnitRangeException {
		checkUnitValue(initialValue);

		Double baseValue = initialValue.getUnit().convertFrom(initialValue.getValue());

		return units.stream()
				// due to this filter there should only ever be one element in this list
				.filter( unit -> unit.equalsUnit(destinationUnit) )
				.map( unit -> new UnitValue(unit, unit.convertTo(baseValue)) )
				.collect(Collectors.toList());
	}

	private void
	checkUnitValue(UnitValue input) throws UnitRangeException{
		Double baseValue = input.getUnit().convertFrom(input.getValue());

		// check for min/max values
		if (minValue != null && baseValue < minValue) {
			double minValueInUnit = input.getUnit().convertTo(minValue);
			throw new UnitRangeException(
					new UnitValue(input.getUnit(), minValueInUnit),
					input
			);
		}
		if (maxValue != null && baseValue > maxValue) {
			double maxValueInUnit = input.getUnit().convertTo(maxValue);
			throw new UnitRangeException(
					new UnitValue(input.getUnit(), maxValueInUnit),
					input
			);
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
