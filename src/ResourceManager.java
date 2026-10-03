import java.util.concurrent.Semaphore;

/**
 * Coordinates access to printer and database resources with semaphores.
 *
 * <p>Workers call {@link #acquire(Resource)} before entering a resource
 * critical section and {@link #release(Resource)} afterward. This models
 * mutual exclusion and limited resource instances in an operating system.</p>
 */
public class ResourceManager {
	private final Semaphore printerSemaphore;
	private final Semaphore dbSemaphore;

	public ResourceManager(int printerPermits, int databasePermits) {
		// Fairness preserves FIFO order among blocked workers and prevents starvation.
		// It is critical here because otherwise a newly arriving job could repeatedly
		// overtake a worker that has already waited for the resource.
		printerSemaphore = new Semaphore(printerPermits, true);
		dbSemaphore = new Semaphore(databasePermits, true);
	}

	/** Acquires a resource and returns semaphore wait time in milliseconds. */
	public long acquire(Resource resource) throws InterruptedException {
		if (resource == null || resource == Resource.NONE) {
			return 0L;
		}

		Semaphore semaphore = semaphoreFor(resource);
		// Measuring around acquire() separates resource blocking from CPU work time.
		long waitStart = System.currentTimeMillis();
		semaphore.acquire();
		return Math.max(0L, System.currentTimeMillis() - waitStart);
	}

	/** Releases one permit previously acquired by a worker. */
	public void release(Resource resource) {
		if (resource == Resource.PRINTER) {
			printerSemaphore.release();
		} else if (resource == Resource.DATABASE) {
			dbSemaphore.release();
		}
	}

	public int getAvailablePrinterPermits() {
		// availablePermits() is a monitoring snapshot and may change immediately afterward.
		return printerSemaphore.availablePermits();
	}

	public int getAvailableDbPermits() {
		return dbSemaphore.availablePermits();
	}

	private Semaphore semaphoreFor(Resource resource) {
		if (resource == Resource.PRINTER) {
			return printerSemaphore;
		}
		if (resource == Resource.DATABASE) {
			return dbSemaphore;
		}
		throw new IllegalArgumentException("Unsupported resource: " + resource);
	}
}
