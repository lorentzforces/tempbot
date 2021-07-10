package tempbot;

import java.util.Map;
import org.junit.Test;
import tempbot.Constants.LogOutput;
import tempbot.Constants.LoggingLevel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static tempbot.Constants.LOG_FILE_NAME;
import static tempbot.LogConfigurer.CONSOLE_WRITER_NAME;
import static tempbot.LogConfigurer.FILE_WRITER_NAME;

public class LogConfigurerTest {

	@Test
	public void configurationSetsLogLevel() {
		Map<String, String> configuredProperties =
				LogConfigurer.configureLogging(LoggingLevel.DEBUG, LogOutput.CONSOLE_AND_FILE);

		assertThat(configuredProperties.get("level"), is(LoggingLevel.DEBUG.name()));
	}

	@Test
	public void configurationSetsLogLocation() {
		Map<String, String> configuredProperties =
				LogConfigurer.configureLogging(LoggingLevel.DEBUG, LogOutput.CONSOLE_AND_FILE);

		assertThat(configuredProperties.get(CONSOLE_WRITER_NAME), is("console"));
		assertThat(configuredProperties.get(FILE_WRITER_NAME), is("file"));
		assertThat(configuredProperties.get(FILE_WRITER_NAME + ".file"), is(LOG_FILE_NAME));
	}

}
