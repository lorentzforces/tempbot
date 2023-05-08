package tempbot.engine;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

public class Unit {

	@Getter
	private final String fullName;

	@Getter
	private final String shortName;

	@Getter
	private final boolean defaultConversionSource;

	@Getter
	private final boolean defaultConversionResult;

	private final Set<String> detectableNames;

	private final Function<Double, Double> convertTo;
	private final Function<Double, Double> convertFrom;

	@Builder
	public Unit(
		final @NonNull String fullName,
		final @NonNull String shortName,
		final boolean defaultConversionSource,
		final boolean defaultConversionResult,
		final @NonNull @Singular Set<String> detectableNames,
		final @NonNull Function<Double, Double> convertTo,
		final @NonNull Function<Double, Double> convertFrom
	) {
		this.fullName = fullName;
		this.shortName = shortName;
		this.defaultConversionSource = defaultConversionSource;
		this.defaultConversionResult = defaultConversionResult;
		this.detectableNames =
			Stream.concat(
				Stream.of(fullName, shortName),
				detectableNames.stream()
			).collect(Collectors.toUnmodifiableSet());

		this.convertTo = convertTo;
		this.convertFrom = convertFrom;
	}

	public Set<String>
	getDetectableNames() {
		return Collections.unmodifiableSet(detectableNames);
	}

	public Double
	convertTo(Double input) {
		return convertTo.apply(input);
	}

	public Double
	convertFrom(Double input) {
		return convertFrom.apply(input);
	}

	public boolean
	equalsUnit(Unit that) {
		return (
			this.shortName.equals(that.shortName)
			&& this.fullName.equals(that.fullName)
		);
	}

}
