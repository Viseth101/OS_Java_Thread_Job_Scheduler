import java.util.concurrent.LinkedBlockingQueue;

/**
 * First-come, first-served ready queue.
 *
 * <p>{@link LinkedBlockingQueue} provides thread-safe producer/consumer
 * operations. In this policy, insertion order is the scheduling order.</p>
 */
public class FCFSQueue implements ReadyQueue {
	private final LinkedBlockingQueue<Job> queue = new LinkedBlockingQueue<>();

	@Override
	public void addJob(Job job) {
		// put() is the blocking-queue producer operation; this queue is unbounded in this simulation.
		try {
			queue.put(job);
		} catch (InterruptedException exception) {
			// Preserve the job during shutdown, then restore the caller's interrupt status.
			queue.add(job);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public Job takeNextJob() throws InterruptedException {
		// take() blocks a worker instead of wasting CPU in a polling loop.
		return queue.take();
	}

	@Override
	public int size() {
		// The value is a momentary monitoring snapshot, not a scheduling decision.
		return queue.size();
	}
}
