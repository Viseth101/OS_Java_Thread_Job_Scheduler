import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Priority-based ready queue with deterministic sequence tie-breaking.
 *
 * <p>Lower numeric priority values run first. Equal priorities use the job's
 * sequence number so the comparator supplies a stable, FIFO-like tie-break.</p>
 */
public class PriorityQueueImpl implements ReadyQueue {
	// Comparator order models a non-preemptive priority scheduler's dispatch choice.
	private static final Comparator<Job> JOB_COMPARATOR = Comparator
			.comparingInt(Job::getPriority)
			.thenComparingInt(Job::getSequenceNumber);

	private final PriorityBlockingQueue<Job> queue = new PriorityBlockingQueue<>(11, JOB_COMPARATOR);

	@Override
	public void addJob(Job job) {
		// PriorityBlockingQueue safely supports concurrent scheduler producers and worker consumers.
		queue.add(job);
	}

	@Override
	public Job takeNextJob() throws InterruptedException {
		// A worker sleeps here until a job or poison pill is available.
		return queue.take();
	}

	@Override
	public int size() {
		// Monitoring observes this queue without removing or locking a job.
		return queue.size();
	}
}
