import java.util.concurrent.LinkedBlockingQueue;

/** First-come, first-served ready queue. */
public class FCFSQueue implements ReadyQueue {
	private final LinkedBlockingQueue<Job> queue = new LinkedBlockingQueue<>();

	@Override
	public void addJob(Job job) {
		try {
			queue.put(job);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public Job takeNextJob() throws InterruptedException {
		return queue.take();
	}
}
