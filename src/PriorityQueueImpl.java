import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/** Priority-based ready queue with deterministic sequence tie-breaking. */
public class PriorityQueueImpl implements ReadyQueue {
	private static final Comparator<Job> JOB_COMPARATOR = Comparator
			.comparingInt(Job::getPriority)
			.thenComparingInt(Job::getSequenceNumber);

	private final PriorityBlockingQueue<Job> queue = new PriorityBlockingQueue<>(11, JOB_COMPARATOR);

	@Override
	public void addJob(Job job) {
		queue.add(job);
	}

	@Override
	public Job takeNextJob() throws InterruptedException {
		return queue.take();
	}

	@Override
	public int size() {
		return queue.size();
	}
}
