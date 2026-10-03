import java.util.Locale;

/**
 * Shared resources that a job may need while it is running.
 *
 * <p>{@link ResourceManager} maps these values to fair semaphores, while
 * {@link Job} stores the resource requirement loaded from the workload.</p>
 */
public enum Resource {
	/** A job does not need a semaphore-protected resource. */
	NONE,
	/** The simulated printer resource. */
	PRINTER,
	/** The simulated database resource. */
	DATABASE;

	/**
	 * Converts a user-provided resource name to a resource value.
	 *
	 * @param name resource name; may be null, blank, or unrecognized
	 * @return the matching resource, or {@link #NONE} when no match exists
	 */
	// Parsing input at the model boundary keeps CSV and command-line details out of the scheduler.
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