
/**
 * Represents one schedulable process-like unit in the simulation.
 *
 * <p>The workload loader creates jobs, the generator timestamps their arrival,
 * the scheduler places them in a policy queue, and workers update their
 * execution state and timing metrics. A poison-pill job is a sentinel used by
 * the scheduler to stop workers without abruptly killing their threads.</p>
 */
public class Job {

    private static final String POISON_PILL_ID = "POISON_PILL";

    private final String id;
    private final long arrivalMs;
    private final int priority;
    private final long workMs;
    private final Resource resource;
    private final long resourceMs;
    private final boolean poisonPill;

    private int sequenceNumber;
    private long actualArrivalTime;
    private long startTime;
    private long resourceWaitStartTime;
    private long resourceWaitTime;
    private long completionTime;
    private volatile JobState state;

    /** Creates a normal schedulable job from one workload row. */
    public Job(String id, long arrivalMs, int priority, long workMs,
            Resource resource, long resourceMs) {
        this(id, arrivalMs, priority, workMs, resource, resourceMs, false);
    }

    /** Creates the private sentinel used to request graceful worker shutdown. */
    private Job() {
        this(POISON_PILL_ID, 0L, Integer.MAX_VALUE, 0L, Resource.NONE, 0L, true);
    }

    private Job(String id, long arrivalMs, int priority, long workMs,
            Resource resource, long resourceMs, boolean poisonPill) {
        // Final fields describe the workload; mutable fields are filled in by pipeline threads.
        this.id = id;
        this.arrivalMs = arrivalMs;
        this.priority = priority;
        this.workMs = workMs;
        this.resource = resource == null ? Resource.NONE : resource;
        this.resourceMs = resourceMs;
        this.poisonPill = poisonPill;
        this.state = JobState.ARRIVED;
    }

    /** Returns a poison-pill sentinel that is not part of the workload. */
    public static Job createPoisonPill() {
        return new Job();
    }

    public String getId() {
        return id;
    }

    public long getArrivalMs() {
        return arrivalMs;
    }

    public int getPriority() {
        return priority;
    }

    public long getWorkMs() {
        return workMs;
    }

    public Resource getResource() {
        return resource;
    }

    public long getResourceMs() {
        return resourceMs;
    }

    public boolean isPoisonPill() {
        return poisonPill;
    }

    /** Reads the sequence used to break equal-priority ties. */
    public synchronized int getSequenceNumber() {
        return sequenceNumber;
    }

    /** Assigns a deterministic FIFO tie-break value. */
    public synchronized void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /** Reads the wall-clock timestamp at which the generator released this job. */
    public synchronized long getActualArrivalTime() {
        return actualArrivalTime;
    }

    /** Stores the actual release timestamp used by waiting-time metrics. */
    public synchronized void setActualArrivalTime(long actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    /** Reads the timestamp at which a worker began non-preemptive execution. */
    public synchronized long getStartTime() {
        return startTime;
    }

    /** Stores the worker start timestamp. */
    public synchronized void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /** Reads when this job began waiting for its semaphore-protected resource. */
    public synchronized long getResourceWaitStartTime() {
        return resourceWaitStartTime;
    }

    /** Stores the beginning of the resource wait interval. */
    public synchronized void setResourceWaitStartTime(long resourceWaitStartTime) {
        this.resourceWaitStartTime = resourceWaitStartTime;
    }

    /** Reads the measured time spent waiting for a shared resource. */
    public synchronized long getResourceWaitTime() {
        return resourceWaitTime;
    }

    /** Stores the measured semaphore wait duration. */
    public synchronized void setResourceWaitTime(long resourceWaitTime) {
        this.resourceWaitTime = resourceWaitTime;
    }

    /** Reads the completion timestamp recorded by the worker. */
    public synchronized long getCompletionTime() {
        return completionTime;
    }

    /** Stores the completion timestamp used by turnaround metrics. */
    public synchronized void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    /** Reads the current lifecycle state; volatile makes cross-thread updates visible. */
    public JobState getState() {
        return state;
    }

    /** Publishes a new lifecycle state to the other pipeline threads. */
    public void setState(JobState state) {
        this.state = state == null ? JobState.ARRIVED : state;
    }

    /** Calculates time spent ready but not yet running. */
    public synchronized long getWaitingTime() {
        return startTime - actualArrivalTime;
    }

    /** Calculates total time from actual arrival to completion. */
    public synchronized long getTurnaroundTime() {
        return completionTime - actualArrivalTime;
    }

    /** Checks the scheduling accounting identity, allowing OS timing jitter. */
    public boolean isValidTurnaroundTime() {
        long expected = getWaitingTime() + workMs + resourceWaitTime + resourceMs;
        long actual = getTurnaroundTime();
        // Real threads can be delayed by the OS, so exact millisecond equality is unrealistic.
        return Math.abs(actual - expected) <= 50;
    }

    @Override
    public synchronized String toString() {
        return "Job{"
                + "id='" + id + '\''
                + ", priority=" + priority
                + ", state=" + state
                + ", resource=" + resource
                + ", resourceMs=" + resourceMs
                + '}';
    }
}
