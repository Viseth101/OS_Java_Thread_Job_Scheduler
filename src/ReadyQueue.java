/**
 * Thread-safe abstraction for jobs waiting to be scheduled.
 *
 * <p>The scheduler inserts jobs here after arrival; workers remove them here.
 * Implementations choose the dispatch policy, such as FCFS or priority.</p>
 */
public interface ReadyQueue {
	/** Inserts a job into the policy queue. */
	void addJob(Job job);

	/** Blocks until the next policy-selected job is available. */
	Job takeNextJob() throws InterruptedException;

	/** Returns a concurrent snapshot of the number of waiting jobs. */
	int size();
}
