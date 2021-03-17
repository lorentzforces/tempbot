package fyi.lorentz.tempbot.engine;

import static fyi.lorentz.tempbot.Util.doublesAreEqual;

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

	public boolean
	equalsUnitValue(UnitValue that) {
		return (
				this.unit.equalsUnit(that.unit)
				&& doublesAreEqual(this.value, that.value)
		);
	}

}
