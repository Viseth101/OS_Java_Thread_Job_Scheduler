import java.util.List;
import java.util.concurrent.BlockingQueue;

/** Releases workload jobs into the scheduler according to their arrival times. */
public class JobGenerator extends Thread {
	private final List<Job> jobs;
	private final BlockingQueue<Job> arrivalQueue;

	public JobGenerator(List<Job> jobs, BlockingQueue<Job> arrivalQueue) {
		super("Job-Generator");
		this.jobs = List.copyOf(jobs);
		this.arrivalQueue = arrivalQueue;
	}

	@Override
	public void run() {
		long previousArrivalMs = 0L;

		for (Job job : jobs) {
			long waitTime = Math.max(0L, job.getArrivalMs() - previousArrivalMs);
			try {
				Thread.sleep(waitTime);
				job.setActualArrivalTime(System.currentTimeMillis());
				job.setState(JobState.ARRIVED);
				ProjectLogger.log("JobGenerator", job.getId(), "ARRIVE", "");
				arrivalQueue.put(job);
				previousArrivalMs = job.getArrivalMs();
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
}
