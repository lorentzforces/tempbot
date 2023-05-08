package tempbot.config;

import lombok.Builder;
import lombok.NonNull;
import tempbot.Constants.LogFormat;
import tempbot.Constants.LogLevel;
import tempbot.Constants.LogOutput;

@Builder
public record ClientConfig(
	@NonNull String secret,
	@NonNull LogLevel logLevel,
	@NonNull LogOutput logOutput,
	@NonNull LogFormat logFormat
){}
