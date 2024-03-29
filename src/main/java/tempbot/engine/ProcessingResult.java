package tempbot.engine;

import java.util.List;
import lombok.Builder;

public sealed interface ProcessingResult {
	@Builder
	record ConvertedValues(
		List<UnitValue> values,
		UnitValue sourceValue
	) implements ProcessingResult, ParsedSourceValue {}

	@Builder
	record ValueNotConverted(UnitValue sourceValue) implements ProcessingResult, ParsedSourceValue {}

	sealed interface ProcessingError extends ProcessingResult {
		@Builder
		public record UnitOutOfRange(
			UnitValue rangeLimitingValue,
			UnitValue sourceValue
		) implements ProcessingError, ParsedSourceValue {

			public enum LimitType {
				MAXIMUM,
				MINIMUM
			}

			public LimitType limitType() {
				return sourceValue().value() > rangeLimitingValue.value()
					? LimitType.MAXIMUM
					: LimitType.MINIMUM;
			}
		}

		@Builder
		public record DimensionMismatch(
			UnitValue sourceValue,
			Dimension sourceDimension,
			Dimension destinationDimension
		) implements ProcessingError, ParsedSourceValue {}

		@Builder
		public record UnknownUnitType(
			String badUnitString
		) implements ProcessingError {}

		@Builder
		public record UnparseableNumber(
			String badNumberString
		) implements ProcessingError {}

		@Builder
		public record SystemError() implements ProcessingError {}
	}

	interface ParsedSourceValue {
		UnitValue sourceValue();
	}

}
