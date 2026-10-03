import java.util.concurrent.BlockingQueue;

/**
 * Moves arrived jobs from the time-based queue into the policy-based ready queue.
 *
 * <p>The scheduler is the boundary between arrival timing and dispatch policy:
 * {@link JobGenerator} fills {@code arrivalQueue}, while workers consume the
 * selected {@link ReadyQueue}. Shutdown drains arrivals before publishing
 * poison pills, preventing a real job from being hidden behind a sentinel.</p>
 */
public class Scheduler extends Thread {
	private final BlockingQueue<Job> arrivalQueue;
	private final ReadyQueue readyQueue;
	private volatile boolean running = true;
	private volatile int shutdownWorkerCount;

	public Scheduler(BlockingQueue<Job> arrivalQueue, ReadyQueue readyQueue) {
		super("Scheduler");
		this.arrivalQueue = arrivalQueue;
		this.readyQueue = readyQueue;
	}

	@Override
	public void run() {
		while (true) {
			Job job;
			try {
				// Normal mode blocks for the next time-based arrival; drain mode polls during shutdown.
				if (running) {
					job = arrivalQueue.take();
				} else {
					job = arrivalQueue.poll();
					if (job == null) {
						return;
					}
				}
			} catch (InterruptedException exception) {
				if (running) {
					Thread.currentThread().interrupt();
					return;
				}
				continue;
			}

			if (!running) {
				// shutdown() interrupts take(); clear that flag before forwarding the already-taken job.
				Thread.interrupted();
			}
			job.setState(JobState.READY);
			ProjectLogger.log("Scheduler", job.getId(), "READY", "");
			readyQueue.addJob(job);
		}
	}

	/**
	 * Stops arrival processing and supplies one poison pill per worker.
	 *
	 * <p>The scheduler first enters drain mode and waits for its own thread to
	 * finish. Only then are sentinels inserted, so workers see every real job
	 * before graceful termination signals.</p>
	 */
	public synchronized void shutdown(int workerCount) {
		if (!running) {
			return;
		}

		running = false;
		shutdownWorkerCount = Math.max(0, workerCount);
		interrupt();

		try {
			// Joining establishes that no scheduler thread can enqueue a real job afterward.
			join();
			enqueueShutdownSignals();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}

	private void enqueueShutdownSignals() {
		// Each worker needs its own sentinel because one worker consumes one queue item.
		for (int index = 0; index < shutdownWorkerCount; index++) {
			readyQueue.addJob(Job.createPoisonPill());
		}
	}
}
