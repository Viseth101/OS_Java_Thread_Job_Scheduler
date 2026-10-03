/**
 * Periodically reports a snapshot of scheduler state.
 *
 * <p>The monitor is an observer thread: it reads the policy queue, atomic
 * statistics, and semaphore permit counts without removing jobs or changing
 * scheduling decisions.</p>
 */
public class Monitor extends Thread {
	private final ReadyQueue readyQueue;
	private final Statistics statistics;
	private final ResourceManager resourceManager;
	private volatile boolean running = true;

	public Monitor(ReadyQueue readyQueue, Statistics statistics,
			ResourceManager resourceManager) {
		super("Monitor");
		this.readyQueue = readyQueue;
		this.statistics = statistics;
		this.resourceManager = resourceManager;
	}

	@Override
	public void run() {
		while (running) {
			try {
				// Sleeping avoids busy-waiting and gives the simulation a one-second observation interval.
				Thread.sleep(1000L);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				break;
			}

			if (running) {
				ProjectLogger.logRaw(String.format(
						"[MONITOR] Ready: %d | Completed: %d | Printer Permits: %d | DB Permits: %d",
						readyQueue.size(),
						statistics.getCompletedJobsCount(),
						resourceManager.getAvailablePrinterPermits(),
						resourceManager.getAvailableDbPermits()));
			}
		}
	}

	public void shutdown() {
		// Set the flag first, then interrupt sleep so shutdown does not wait a full second.
		running = false;
		interrupt();
	}
}
