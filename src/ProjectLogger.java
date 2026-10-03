/**
 * Thread-safe console logger for scheduler events and summaries.
 *
 * <p>Generator, scheduler, workers, monitor, and statistics all write through
 * this class. Synchronizing each output method prevents messages from multiple
 * OS threads interleaving on the console.</p>
 */
public final class ProjectLogger {
	private static long simulationStartTime;

	private ProjectLogger() {
		// Utility class: all callers share one simulation clock and output stream.
	}

	/** Starts a new simulation clock used for relative event timestamps. */
	public static synchronized void init() {
		simulationStartTime = System.currentTimeMillis();
	}

	/** Logs one structured event with elapsed milliseconds and actor/job context. */
	public static synchronized void log(String actor, String jobId, String action, String extra) {
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

		// The synchronized method makes this entire line one console operation.
		System.out.println(message);
	}

	/** Writes an already-formatted monitor or summary message without extra metadata. */
	public static synchronized void logRaw(String message) {
		System.out.println(message == null ? "null" : message);
	}
}
