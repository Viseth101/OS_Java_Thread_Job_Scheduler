import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Application entry point for the mini scheduler simulation. */
public class Main {
	private static final String USAGE =
			"Usage: java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>";

	public static void main(String[] args) {
		if (args.length != 5) {
			System.out.println(USAGE);
			System.exit(1);
		}

		try {
			String workloadFile = args[0];
			String policy = args[1].toLowerCase();
			int workerCount = Integer.parseInt(args[2]);
			int printerPermits = Integer.parseInt(args[3]);
			int dbPermits = Integer.parseInt(args[4]);

			if (workerCount <= 0 || printerPermits < 0 || dbPermits < 0) {
				throw new IllegalArgumentException("worker count must be positive and permit counts cannot be negative");
			}

			ProjectLogger.init();
			List<Job> jobs = WorkloadLoader.loadJobs(workloadFile);
			ResourceManager resourceManager = new ResourceManager(printerPermits, dbPermits);
			Statistics statistics = new Statistics();
			ReadyQueue readyQueue = policy.equals("priority")
					? new PriorityQueueImpl()
					: new FCFSQueue();
			BlockingQueue<Job> arrivalQueue = new LinkedBlockingQueue<>();

			Scheduler scheduler = new Scheduler(arrivalQueue, readyQueue);
			JobGenerator generator = new JobGenerator(jobs, arrivalQueue);
			Monitor monitor = new Monitor(readyQueue, statistics, resourceManager);
			List<Worker> workers = new ArrayList<>();
			for (int id = 1; id <= workerCount; id++) {
				workers.add(new Worker(id, readyQueue, resourceManager, statistics));
			}

			long simulationStartTime = System.currentTimeMillis();
			monitor.start();
			scheduler.start();
			for (Worker worker : workers) {
				worker.start();
			}
			generator.start();

			generator.join();
			scheduler.shutdown(workerCount);
			scheduler.join();
			for (Worker worker : workers) {
				worker.join();
			}
			monitor.shutdown();
			monitor.join();

			long totalSimulationTime = System.currentTimeMillis() - simulationStartTime;
			statistics.printSummary(totalSimulationTime);
		} catch (NumberFormatException exception) {
			System.err.println("Error: worker and permit counts must be integers.");
			System.exit(1);
		} catch (IOException exception) {
			System.err.println("Error loading workload: " + exception.getMessage());
			System.exit(1);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			System.err.println("Simulation interrupted.");
			System.exit(1);
		} catch (IllegalArgumentException exception) {
			System.err.println("Error: " + exception.getMessage());
			System.exit(1);
		}
	}
}
