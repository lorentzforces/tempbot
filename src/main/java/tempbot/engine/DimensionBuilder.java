package tempbot.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DimensionBuilder {

	private String name;
	private List<UnitBuilder> units;
	private Double minValue;
	private Double maxValue;

	public DimensionBuilder() {
		units = new ArrayList<>();
		minValue = null;
		maxValue = null;
	}

	public Dimension
	build() {
		return new Dimension(
			name,
			units.stream().map(builder -> builder.build()).collect(Collectors.toList()),
			minValue,
			maxValue
		);
	}

	public DimensionBuilder
	setName(String name) {
		this.name = name;
		return this;
	}

	public DimensionBuilder
	addUnit(UnitBuilder newUnit) {
		units.add(newUnit);
		return this;
	}

	/**
	 * The minimum value is given as a scalar in the base unit of the dimension.
	 * (i.e. it is the minimum value of the result of calling convertFrom on any unit
	 * in the dimension)
	 */
	public DimensionBuilder
	setMinValue(Double minValue) {
		this.minValue = minValue;
		return this;
	}

	/**
	 * The maximum value is given as a scalar in the base unit of the dimension.
	 * (i.e. it is the maximum value of the result of calling convertFrom on any unit
	 * in the dimension)
	 */
	public DimensionBuilder
	setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
		return this;
	}

}
