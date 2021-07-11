package tempbot;

import java.util.Map;
import org.junit.Test;
import tempbot.Constants.LogFormat;
import tempbot.Constants.LogLevel;
import tempbot.Constants.LogOutput;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static tempbot.Constants.LOG_FILE_NAME;
import static tempbot.LogConfigurer.CONSOLE_WRITER_NAME;
import static tempbot.LogConfigurer.FILE_WRITER_NAME;

public class LogConfigurerTest {

	@Test
	public void configurationSetsLogLevel() {
		Map<String, String> configuredProperties =
				LogConfigurer.configureLogging(
						LogLevel.DEBUG,
						LogOutput.CONSOLE_AND_FILE,
						LogFormat.DEV
				);

		assertThat(configuredProperties.get("level"), is(LogLevel.DEBUG.name()));
	}

	@Test
	public void configurationSetsBothLogLocations() {
		Map<String, String> configuredProperties =
				LogConfigurer.configureLogging(
						LogLevel.DEBUG,
						LogOutput.CONSOLE_AND_FILE,
						LogFormat.DEV
				);

		assertThat(configuredProperties.get(CONSOLE_WRITER_NAME), is("console"));
		assertThat(configuredProperties.get(FILE_WRITER_NAME), is("file"));
		assertThat(configuredProperties.get(FILE_WRITER_NAME + ".file"), is(LOG_FILE_NAME));
	}

	@Test
	public void configurationSetsOneLogLocation() {
		Map<String, String> consoleOnlyProperties =
				LogConfigurer.configureLogging(
						LogLevel.DEBUG,
						LogOutput.CONSOLE,
						LogFormat.DEV
				);
		assertThat(consoleOnlyProperties.get(CONSOLE_WRITER_NAME), is("console"));
		assertThat(consoleOnlyProperties.get(FILE_WRITER_NAME), is(nullValue()));

		Map<String, String> fileOnlyProperties =
				LogConfigurer.configureLogging(
						LogLevel.DEBUG,
						LogOutput.FILE,
						LogFormat.DEV
				);
		assertThat(fileOnlyProperties.get(FILE_WRITER_NAME), is("file"));
		assertThat(fileOnlyProperties.get(CONSOLE_WRITER_NAME), is(nullValue()));
		assertThat(fileOnlyProperties.get(FILE_WRITER_NAME + ".file"), is(LOG_FILE_NAME));
	}

	@Test
	public void formatsIncludeOrDontIncludeClassName() {
		Map<String, String> devFormatProperties =
				LogConfigurer.configureLogging(
						LogLevel.DEBUG,
						LogOutput.CONSOLE,
						LogFormat.DEV
				);
		assertThat(
				devFormatProperties.get(CONSOLE_WRITER_NAME + ".format"),
				containsString("{class-name}")
		);
		assertThat(
				devFormatProperties.get(CONSOLE_WRITER_NAME + ".format"),
				containsString("{line}")
		);

		Map<String, String> nonDevFormatProperties =
				LogConfigurer.configureLogging(
						LogLevel.DEBUG,
						LogOutput.CONSOLE,
						LogFormat.NON_DEV
				);
		assertThat(
				nonDevFormatProperties.get(CONSOLE_WRITER_NAME + ".format"),
				not(containsString("{class-name}"))
		);
		assertThat(
				nonDevFormatProperties.get(CONSOLE_WRITER_NAME + ".format"),
				not(containsString("{line}"))
		);
	}

}
