import java.util.concurrent.Semaphore;

/** Coordinates access to the scheduler's shared resources. */
public class ResourceManager {
	private final Semaphore printerSemaphore;
	private final Semaphore dbSemaphore;

	public ResourceManager(int printerPermits, int databasePermits) {
		printerSemaphore = new Semaphore(printerPermits, true);
		dbSemaphore = new Semaphore(databasePermits, true);
	}

	/** Acquires a resource and returns the time spent waiting in milliseconds. */
	public long acquire(Resource resource) throws InterruptedException {
		if (resource == null || resource == Resource.NONE) {
			return 0L;
		}

		Semaphore semaphore = semaphoreFor(resource);
		long waitStart = System.currentTimeMillis();
		semaphore.acquire();
		return Math.max(0L, System.currentTimeMillis() - waitStart);
	}

	/** Releases one permit for a resource previously acquired by a job. */
	public void release(Resource resource) {
		if (resource == Resource.PRINTER) {
			printerSemaphore.release();
		} else if (resource == Resource.DATABASE) {
			dbSemaphore.release();
		}
	}

	public int getAvailablePrinterPermits() {
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
