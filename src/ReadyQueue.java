/** Thread-safe abstraction for jobs waiting to be scheduled. */
public interface ReadyQueue {
	void addJob(Job job);

	Job takeNextJob() throws InterruptedException;
}
