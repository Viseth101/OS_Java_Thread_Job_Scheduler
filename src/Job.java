
/** A schedulable unit of work and its execution metrics. */
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

    /**
     * Creates a normal schedulable job.
     */
    public Job(String id, long arrivalMs, int priority, long workMs,
            Resource resource, long resourceMs) {
        this(id, arrivalMs, priority, workMs, resource, resourceMs, false);
    }

    /**
     * Creates the scheduler sentinel used to request worker shutdown.
     */
    private Job() {
        this(POISON_PILL_ID, 0L, Integer.MAX_VALUE, 0L, Resource.NONE, 0L, true);
    }

    private Job(String id, long arrivalMs, int priority, long workMs,
            Resource resource, long resourceMs, boolean poisonPill) {
        this.id = id;
        this.arrivalMs = arrivalMs;
        this.priority = priority;
        this.workMs = workMs;
        this.resource = resource == null ? Resource.NONE : resource;
        this.resourceMs = resourceMs;
        this.poisonPill = poisonPill;
        this.state = JobState.ARRIVED;
    }

    /**
     * Returns a sentinel job that is not part of the workload.
     */
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

    public synchronized int getSequenceNumber() {
        return sequenceNumber;
    }

    public synchronized void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public synchronized long getActualArrivalTime() {
        return actualArrivalTime;
    }

    public synchronized void setActualArrivalTime(long actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public synchronized long getStartTime() {
        return startTime;
    }

    public synchronized void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public synchronized long getResourceWaitStartTime() {
        return resourceWaitStartTime;
    }

    public synchronized void setResourceWaitStartTime(long resourceWaitStartTime) {
        this.resourceWaitStartTime = resourceWaitStartTime;
    }

    public synchronized long getResourceWaitTime() {
        return resourceWaitTime;
    }

    public synchronized void setResourceWaitTime(long resourceWaitTime) {
        this.resourceWaitTime = resourceWaitTime;
    }

    public synchronized long getCompletionTime() {
        return completionTime;
    }

    public synchronized void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state == null ? JobState.ARRIVED : state;
    }

    public synchronized long getWaitingTime() {
        return startTime - actualArrivalTime;
    }

    public synchronized long getTurnaroundTime() {
        return completionTime - actualArrivalTime;
    }

    public boolean isValidTurnaroundTime() {
        long expected = getWaitingTime() + workMs + resourceWaitTime + resourceMs;
        long actual = getTurnaroundTime();
        // Allow up to 50ms of OS thread scheduling jitter
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
