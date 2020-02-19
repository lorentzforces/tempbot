package fyi.lorentz.tempbot.engine;

public class UnitValue {

    private final Unit unit;
    private final Double value;

    public UnitValue(
            Unit unit,
            Double value
    ) {
        if (value == null) {
            throw new IllegalArgumentException("Unit value cannot be null");
        }
        this.unit = unit;
        this.value = value;
    }

    public Unit
    getUnit() {
        return unit;
    }

    public Double
    getValue() {
        return value;
    }

}
