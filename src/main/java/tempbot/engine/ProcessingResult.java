package tempbot.engine;

import java.util.List;
import lombok.Builder;

public sealed interface ProcessingResult
permits
	ProcessingResult.ConvertedValues,
	ProcessingResult.ValueNotConverted,
	ProcessingResult.ProcessingError
{
	@Builder
	record ConvertedValues(
		List<UnitValue> values,
		UnitValue sourceValue
	) implements ProcessingResult, ParsedSourceValue {}

	@Builder
	record ValueNotConverted(UnitValue sourceValue) implements ProcessingResult, ParsedSourceValue {}

	sealed interface ProcessingError extends ProcessingResult
	permits
		ProcessingError.UnitOutOfRange,
		ProcessingError.DimensionMismatch,
		ProcessingError.SystemError
	{
		@Builder
		record UnitOutOfRange(
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
		record DimensionMismatch(
			UnitValue sourceValue,
			Dimension sourceDimension,
			Dimension destinationDimension
		) implements ProcessingError, ParsedSourceValue {}

		@Builder
		record SystemError() implements ProcessingError {}
	}

	interface ParsedSourceValue {
		UnitValue sourceValue();
	}

}
