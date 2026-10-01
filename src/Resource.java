import java.util.Locale;

/** Resources that a job may need while it is running. */
public enum Resource {
	NONE,
	PRINTER,
	DATABASE;

	/**
	 * Converts a user-provided resource name to a resource value.
	 *
	 * @param name resource name; may be null, blank, or unrecognized
	 * @return the matching resource, or {@link #NONE} when no match exists
	 */
    
	public static Resource fromString(String name) {
		if (name == null || name.isBlank()) {
			return NONE;
		}

		try {
			return valueOf(name.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			return NONE;
		}
	}
}