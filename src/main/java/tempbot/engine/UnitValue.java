package tempbot.engine;

import static tempbot.Util.doublesAreEqual;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record UnitValue(@NonNull Unit unit, double value) {

	// NOTE: this is not necessarily transitive because of fuzzy double equality
	public boolean
	equalsUnitValue(UnitValue that) {
		return (
			this.unit.equalsUnit(that.unit)
			&& doublesAreEqual(this.value, that.value)
		);
	}

}
