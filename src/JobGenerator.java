import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Releases workload jobs into the time-based arrival pipeline.
 *
 * <p>This thread models an external source of process arrivals. Its
 * {@code arrivalQueue} is ordered by simulated time, not by FCFS or priority;
 * the separate {@link Scheduler} later applies the selected ready-queue policy.</p>
 */
public class JobGenerator extends Thread {
	private final List<Job> jobs;
	private final BlockingQueue<Job> arrivalQueue;

	public JobGenerator(List<Job> jobs, BlockingQueue<Job> arrivalQueue) {
		// Copy the list so another thread cannot mutate the generator's workload while it runs.
		super("Job-Generator");
		this.jobs = List.copyOf(jobs);
		this.arrivalQueue = arrivalQueue;
	}

	@Override
	public void run() {
		long previousArrivalMs = 0L;

		for (Job job : jobs) {
			// Relative sleeps reproduce the absolute arrival times from the CSV workload.
			long waitTime = Math.max(0L, job.getArrivalMs() - previousArrivalMs);
			try {
				Thread.sleep(waitTime);
				job.setActualArrivalTime(System.currentTimeMillis());
				job.setState(JobState.ARRIVED);
				ProjectLogger.log("JobGenerator", job.getId(), "ARRIVE", "");
				// arrivalQueue is time-based handoff; Scheduler will later choose the policy queue.
				arrivalQueue.put(job);
				previousArrivalMs = job.getArrivalMs();
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
}
