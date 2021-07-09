package tempbot.config;

/**
 * Exception thrown for unrecoverable errors encountered while loading a configuration file.
 */
public class ConfigLoadException extends Exception {

	public ConfigLoadException() {
		super();
	}

	public ConfigLoadException(String message) {
		super(message);
	}

	public ConfigLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigLoadException(Throwable cause) {
		super(cause);
	}

}
