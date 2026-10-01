import java.util.concurrent.BlockingQueue;

/** Moves arrived jobs into the selected scheduling policy's ready queue. */
public class Scheduler extends Thread {
	private final BlockingQueue<Job> arrivalQueue;
	private final ReadyQueue readyQueue;
	private volatile boolean running = true;

	public Scheduler(BlockingQueue<Job> arrivalQueue, ReadyQueue readyQueue) {
		super("Scheduler");
		this.arrivalQueue = arrivalQueue;
		this.readyQueue = readyQueue;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Job job = arrivalQueue.take();
				job.setState(JobState.READY);
				ProjectLogger.log("Scheduler", job.getId(), "READY", "");
				readyQueue.addJob(job);
			} catch (InterruptedException exception) {
				if (running) {
					Thread.currentThread().interrupt();
				}
				return;
			}
		}
	}

	/** Stops arrival processing and supplies one shutdown sentinel per worker. */
	public synchronized void shutdown(int workerCount) {
		if (!running) {
			return;
		}

		running = false;
		for (int index = 0; index < Math.max(0, workerCount); index++) {
			readyQueue.addJob(Job.createPoisonPill());
		}
		interrupt();
	}
}
