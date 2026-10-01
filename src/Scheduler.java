import java.util.concurrent.BlockingQueue;

/** Moves arrived jobs into the selected scheduling policy's ready queue. */
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
				Thread.interrupted();
			}
			job.setState(JobState.READY);
			ProjectLogger.log("Scheduler", job.getId(), "READY", "");
			readyQueue.addJob(job);
		}
	}

	/** Stops arrival processing and supplies one shutdown sentinel per worker. */
	public synchronized void shutdown(int workerCount) {
		if (!running) {
			return;
		}

		running = false;
		shutdownWorkerCount = Math.max(0, workerCount);
		interrupt();

		try {
			join();
			enqueueShutdownSignals();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}

	private void enqueueShutdownSignals() {
		for (int index = 0; index < shutdownWorkerCount; index++) {
			readyQueue.addJob(Job.createPoisonPill());
		}
	}
}
