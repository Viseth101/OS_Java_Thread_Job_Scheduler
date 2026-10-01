/** Thread-safe console logger for scheduler events. */
public final class ProjectLogger {
	private static long simulationStartTime;

	private ProjectLogger() {
		// Utility class.
	}

	/** Starts a new simulation clock. */
	public static synchronized void init() {
		simulationStartTime = System.currentTimeMillis();
	}

	/** Logs a structured scheduler event with elapsed milliseconds. */
	public static synchronized void log(String actor, String jobId,
			String action, String extra) {
		long elapsedMs = System.currentTimeMillis() - simulationStartTime;
		StringBuilder message = new StringBuilder()
				.append('[')
				.append(String.format("%04d", Math.max(0L, elapsedMs)))
				.append(" ms] [")
				.append(actor == null ? "" : actor)
				.append("] ")
				.append(jobId == null ? "" : jobId)
				.append(' ')
				.append(action == null ? "" : action);

		if (extra != null && !extra.isBlank()) {
			message.append(' ').append(extra);
		}

		System.out.println(message);
	}

	/** Writes an already-formatted message without adding scheduler metadata. */
	public static synchronized void logRaw(String message) {
		System.out.println(message == null ? "null" : message);
	}
}
