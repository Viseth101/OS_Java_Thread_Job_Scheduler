/** Executes ready jobs and coordinates their shared-resource usage. */
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
				job = readyQueue.takeNextJob();
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				break;
			}

			if (job.isPoisonPill()) {
				break;
			}

			try {
				executeJob(job);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	private void executeJob(Job job) throws InterruptedException {
		job.setStartTime(System.currentTimeMillis());
		job.setState(JobState.RUNNING);
		ProjectLogger.log(getName(), job.getId(), "START", "");

		Thread.sleep(job.getWorkMs());

		Resource resource = job.getResource();
		if (resource != Resource.NONE) {
			job.setState(JobState.WAITING_RESOURCE);
			ProjectLogger.log(getName(), job.getId(), "WAIT", resource.name());
			job.setResourceWaitStartTime(System.currentTimeMillis());

			long waitDuration = resourceManager.acquire(resource);
			ProjectLogger.log(getName(), job.getId(), "ACQUIRE", resource.name());
			job.setResourceWaitTime(waitDuration);
			job.setState(JobState.RUNNING);

			try {
				Thread.sleep(job.getResourceMs());
			} finally {
				resourceManager.release(resource);
				ProjectLogger.log(getName(), job.getId(), "RELEASE", resource.name());
			}
		}

		job.setCompletionTime(System.currentTimeMillis());
		job.setState(JobState.COMPLETED);
		ProjectLogger.log(getName(), job.getId(), "COMPLETE", "");
		statistics.recordJobCompletion(job);
	}
}
