package fyi.lorentz.tempbot.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        Double baseValue = initialValue.getUnit().convertFrom(initialValue.getValue());

        // check for min/max values
        if (minValue != null && baseValue < minValue.doubleValue()) {
            double minValueInUnit = initialValue.getUnit().convertTo(minValue);
            throw new UnitRangeException(
                    new UnitValue(initialValue.getUnit(), minValueInUnit),
                    initialValue
            );
        }
        if (maxValue != null && baseValue > maxValue.doubleValue()) {
            double maxValueInUnit = initialValue.getUnit().convertTo(maxValue);
            throw new UnitRangeException(
                    new UnitValue(initialValue.getUnit(), maxValueInUnit),
                    initialValue
            );
        }

        return units.stream()
                .filter( unit -> (returnAllUnits || unit.isDefaultConversionResult()) )
                .filter( unit -> unit != initialValue.getUnit())
                .map( unit -> new UnitValue(unit, unit.convertTo(baseValue)) )
                .collect(Collectors.toList());
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
