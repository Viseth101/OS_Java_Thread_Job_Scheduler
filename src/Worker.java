/**
 * Represents a CPU worker thread that executes jobs from the ready queue.
 *
 * <p>A worker performs non-preemptive simulated work, may block on a shared
 * resource semaphore, records completion statistics, and stops when it reads
 * its poison pill. It interacts with {@link Scheduler}, {@link ReadyQueue},
 * {@link ResourceManager}, and {@link Statistics}.</p>
 */
public class Worker extends Thread {
	private final ReadyQueue readyQueue;
	private final ResourceManager resourceManager;
	private final Statistics statistics;

	public Worker(int id, ReadyQueue readyQueue,
			ResourceManager resourceManager, Statistics statistics) {
		super("Worker-" + id);
		this.readyQueue = readyQueue;
		this.resourceManager = resourceManager;
		this.statistics = statistics;
	}

	@Override
	public void run() {
		while (true) {
			final Job job;
			try {
				// Blocking here models a worker sleeping until the dispatcher has a ready process.
				job = readyQueue.takeNextJob();
			} catch (InterruptedException exception) {
				// An interrupt is an external shutdown request; restore the flag before exiting.
				Thread.currentThread().interrupt();
				break;
			}

			if (job.isPoisonPill()) {
				// The scheduler places one poison pill per worker for graceful termination.
				break;
			}

			try {
				// Interrupted work is abandoned cleanly rather than counted as completed.
				executeJob(job);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	private void executeJob(Job job) throws InterruptedException {
		// This section models non-preemptive CPU execution: the worker owns the job until it blocks or finishes.
		job.setStartTime(System.currentTimeMillis());
		job.setState(JobState.RUNNING);
		ProjectLogger.log(getName(), job.getId(), "START", "");

		Thread.sleep(job.getWorkMs());

		Resource resource = job.getResource();
		if (resource != Resource.NONE) {
			// Resource waiting is a blocked state, distinct from waiting in the ready queue.
			job.setState(JobState.WAITING_RESOURCE);
			ProjectLogger.log(getName(), job.getId(), "WAIT", resource.name());
			job.setResourceWaitStartTime(System.currentTimeMillis());

			long waitDuration = resourceManager.acquire(resource);
			ProjectLogger.log(getName(), job.getId(), "ACQUIRE", resource.name());
			job.setResourceWaitTime(waitDuration);
			job.setState(JobState.RUNNING);

			try {
				// A worker owns the permit during this critical section.
				Thread.sleep(job.getResourceMs());
			} finally {
				// Always release, even when sleep is interrupted, or another job could deadlock forever.
				resourceManager.release(resource);
				ProjectLogger.log(getName(), job.getId(), "RELEASE", resource.name());
			}
		}

		// Completion is recorded only after CPU work and any required critical section finish.
		job.setCompletionTime(System.currentTimeMillis());
		job.setState(JobState.COMPLETED);
		ProjectLogger.log(getName(), job.getId(), "COMPLETE", "");
		statistics.recordJobCompletion(job);
	}
}
