/** Lifecycle states used by the scheduler for a job. */
public enum JobState {
	ARRIVED,
	READY,
	RUNNING,
	WAITING_RESOURCE,
	COMPLETED
}
