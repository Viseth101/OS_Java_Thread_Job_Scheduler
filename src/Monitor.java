/** Periodically reports scheduler and shared-resource state. */
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
		running = false;
		interrupt();
	}
}
