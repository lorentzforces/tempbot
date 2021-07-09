package tempbot.config;

import java.io.InputStream;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import tempbot.Constants.LoggingLevel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConfigPropertyFetcherTest {

	private static final String TEST_PROPERTY_FILE = "test-load-configuration.yml";

	ConfigPropertyFetcher configLoader;

	@Before
	public void
	setup() {
		InputStream testConfigFileStream =
				Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(TEST_PROPERTY_FILE);
		configLoader = new ConfigPropertyFetcher(testConfigFileStream, TEST_PROPERTY_FILE);
	}

	@Test
	public void
	testStringPropertyIsLoaded() throws Exception {
		Optional<String> stringPropertyValue =
				configLoader.fetchConfigProperty(String.class, "testStringProperty");

		assertThat(stringPropertyValue.isPresent(), is(true));
		assertThat(stringPropertyValue.orElseThrow(), is("test string property value"));
	}

	@Test
	public void
	testEnumPropertyIsLoaded() throws Exception {
		Optional<LoggingLevel> enumPropertyValue =
				configLoader.fetchConfigProperty(LoggingLevel.class, "testEnumProperty");

		assertThat(enumPropertyValue.isPresent(), is(true));
		assertThat(enumPropertyValue.orElseThrow(), is(LoggingLevel.DEBUG));
	}

	@Test(expected = ConfigLoadException.class)
	public void
	testEnumPropertyWithInvalidValueThrows() throws Exception {
		configLoader.fetchConfigProperty(LoggingLevel.class, "testInvalidEnumProperty");
	}

	@Test
	public void
	testMissingPropertyReturnsEmpty() throws Exception {
		Optional<String> emptyValue =
				configLoader.fetchConfigProperty(String.class, "nonExistentProperty");

		assertThat(emptyValue.isEmpty(), is(true));
	}

	@Test(expected = ConfigLoadException.class)
	public void
	testMissingRequiredPropertyThrows() throws Exception {
		configLoader.requireConfigProperty(LoggingLevel.class, "testInvalidEnumProperty");
	}

}
