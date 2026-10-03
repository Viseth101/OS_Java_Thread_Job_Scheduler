/**
 * Lifecycle states used by the scheduler for a job.
 *
 * <p>The values model the familiar OS process path: arrival, readiness,
 * execution, resource blocking, and completion.</p>
 */
public enum JobState {
	/** The generator has released the job according to its arrival time. */
	ARRIVED,
	/** The scheduler has placed the job in a policy-controlled ready queue. */
	READY,
	/** A worker currently owns the CPU simulation for this job. */
	RUNNING,
	/** The job is blocked waiting for a printer or database permit. */
	WAITING_RESOURCE,
	/** The worker has recorded the job's completion metrics. */
	COMPLETED
}
