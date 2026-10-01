import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe aggregate metrics for completed jobs. */
public class Statistics {
	private final AtomicInteger completedJobsCount = new AtomicInteger();
	private final AtomicLong totalWaitingTime = new AtomicLong();
	private final AtomicLong totalTurnaroundTime = new AtomicLong();
	private final AtomicLong totalResourceWaitTime = new AtomicLong();
	private final AtomicInteger resourceJobCount = new AtomicInteger();
	private final ConcurrentHashMap<String, Job> completedJobs = new ConcurrentHashMap<>();

	/** Records one completed job and updates all applicable aggregates. */
	public void recordJobCompletion(Job job) {
		if (job == null) {
			throw new IllegalArgumentException("job must not be null");
		}

		totalWaitingTime.addAndGet(job.getWaitingTime());
		totalTurnaroundTime.addAndGet(job.getTurnaroundTime());

		if (job.getResource() != Resource.NONE) {
			totalResourceWaitTime.addAndGet(job.getResourceWaitTime());
			resourceJobCount.incrementAndGet();
		}

		completedJobs.put(job.getId(), job);
		completedJobsCount.incrementAndGet();

		if (!job.isValidTurnaroundTime()) {
			ProjectLogger.logRaw("WARNING: invalid turnaround equation for job " + job.getId());
		}
	}

	public long getAvgWaitingTime() {
		return average(totalWaitingTime.get(), completedJobsCount.get());
	}

	public int getCompletedJobsCount() {
		return completedJobsCount.get();
	}

	public long getAvgTurnaroundTime() {
		return average(totalTurnaroundTime.get(), completedJobsCount.get());
	}

	public long getAvgResourceWaitTime() {
		return average(totalResourceWaitTime.get(), resourceJobCount.get());
	}

	public double getThroughput(long simulationTimeMs) {
		if (simulationTimeMs <= 0L) {
			return 0.0;
		}
		return completedJobsCount.get() / (simulationTimeMs / 1000.0);
	}

	/** Prints the final aggregate metrics as one readable table. */
	public void printSummary(long totalSimulationDurationMs) {
		ProjectLogger.logRaw("=== Simulation Summary ===");
		ProjectLogger.logRaw("| Metric                    | Value       |");
		ProjectLogger.logRaw(String.format("| Completed jobs            | %11d |", completedJobsCount.get()));
		ProjectLogger.logRaw(String.format("| Average waiting time (ms) | %11d |", getAvgWaitingTime()));
		ProjectLogger.logRaw(String.format("| Average turnaround (ms)   | %11d |", getAvgTurnaroundTime()));
		ProjectLogger.logRaw(String.format("| Average resource wait (ms)| %11d |", getAvgResourceWaitTime()));
		ProjectLogger.logRaw(String.format("| Throughput (jobs/sec)     | %11.3f |", getThroughput(totalSimulationDurationMs)));
		ProjectLogger.logRaw("===========================");
	}

	private long average(long total, int count) {
		return count == 0 ? 0L : total / count;
	}
}
