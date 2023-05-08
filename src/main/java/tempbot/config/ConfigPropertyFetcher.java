package tempbot.config;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

class ConfigPropertyFetcher {

	private final String readableFileIdentifier;
	private final Map<String, String> rawConfigProperties;

	/**
	 * Create a configuration loader to load a yaml configuration file from an {@link InputStream}.
	 * @param clientConfigFile an input stream representing a yaml configuration file
	 * @param readableFileIdentifier
	 *		the identifier to use to refer to the configuration file in log messages
	 */
	@SuppressWarnings("unchecked")
	protected
	ConfigPropertyFetcher(final InputStream clientConfigFile, final String readableFileIdentifier) {
		final var yamlLoader = new Load(
			LoadSettings.builder().setLabel("tempbot configuration file").build()
		);

		this.readableFileIdentifier = readableFileIdentifier;
		rawConfigProperties =
			(Map<String, String>) yamlLoader.loadFromInputStream(clientConfigFile);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T>
	fetchConfigProperty(
		final Class<T> type,
		final String name
	) throws ConfigLoadException {
		final var rawProperty = fetchRawProperty(name);

		if (rawProperty.isEmpty()) {
			return (Optional<T>) rawProperty;
		}
		else {
			return type.isEnum()
				? parseRawEnumProperty(rawProperty.orElseThrow(), type)
				: parseRawNonEnumProperty(rawProperty.orElseThrow(), type);
		}
	}

	public <T> T
	requireConfigProperty(
		final Class<T> type,
		final String name
	) throws ConfigLoadException {
		return fetchConfigProperty(type, name).orElseThrow(() ->
			new ConfigLoadException(String.format(
				"Required property not found: expected property \"%s\" in configuration %s",
				name,
				readableFileIdentifier
			)));
	}

	// TODO: figure out what to do if we're ever not getting Strings out of the yaml loader
	private Optional<String>
	fetchRawProperty(
		final String name
	) {
		final var rawProperty = rawConfigProperties.get(name);
		return rawProperty == null ? Optional.empty() : Optional.of(rawProperty);
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<T>
	parseRawEnumProperty(
		final Object rawProperty,
		final Class<T> type
	) throws ConfigLoadException {
		// this is an enum type, we expect a string value in the config file
		String stringValue = null;
		try {
			stringValue = (String) rawProperty;
		}
		catch (final ClassCastException e) {
			throw new ConfigLoadException(String.format(
				"Property type mismatch: found [%s], expected a String representing a [%s] "
					+ "in configuration %s",
				rawProperty.getClass().getCanonicalName(),
				type.getCanonicalName(),
				readableFileIdentifier
			));
		}

		T result = null;
		try {
			// this casting is nasty AF, but it's the price we pay for convenient parsing methods
			result = (T) Enum.valueOf( (Class<? extends Enum>) type, stringValue);
		}
		catch (final IllegalArgumentException e) {
			throw new ConfigLoadException(String.format(
				"Invalid property enum value: \"%s\" is not a valid value for property \"%s\" "
					+ "in configuration %s",
				stringValue,
				type.getCanonicalName(),
				readableFileIdentifier
			));
		}

		// we throw exceptions any time this could be null, so we know by this point it isn't
		return Optional.of(result);
	}

	private <T> Optional<T>
	parseRawNonEnumProperty(
		final Object rawProperty,
		final Class<T> type
	) throws ConfigLoadException {
		T result = null;
		try {
			result = type.cast(rawProperty);
		}
		catch (final ClassCastException e) {
			throw new ConfigLoadException(String.format(
				"Property type mismatch: found [%s], expected a String representing a [%s] "
					+ "in configuration %s",
				rawProperty.getClass().getCanonicalName(),
				type.getCanonicalName(),
				readableFileIdentifier
			));
		}

		// we throw exceptions any time this could be null, so we know by this point it isn't
		return Optional.of(result);
	}

}
