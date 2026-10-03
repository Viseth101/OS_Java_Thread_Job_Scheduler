# Mini Job Scheduler & Shared Resource Manager

## Compilation

Compile all Java source files into the `bin/` directory:

```bash
javac src/*.java -d bin/
```

## Execution

```bash
java -cp bin Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>
```

Arguments:

- `workload.csv`: workload file containing job arrival, priority, CPU-work, and resource data.
- `fcfs|priority`: ready-queue policy: first-come, first-served or priority scheduling.
- `workers`: number of worker threads executing jobs concurrently.
- `printerPermits`: number of simultaneous printer-resource permits.
- `databasePermits`: number of simultaneous database-resource permits.

Example:

```bash
java -cp bin Main Workloads/jobs_standard.csv priority 3 1 2
```

## Architecture Notes

- The scheduler uses non-preemptive execution: once a worker starts a job, it runs the simulated CPU burst until completion or resource blocking.
- `Thread.sleep()` simulates CPU bursts and resource-use durations without consuming real CPU time.
- Priority scheduling orders lower numeric priorities first; equal priorities are resolved deterministically with each job's sequence number.
- Shared printer and database access is controlled by fair semaphores.
- The generator sends jobs through a time-based arrival queue, while the scheduler transfers them to a policy-based ready queue.
- Workers terminate safely through a Poison Pill mechanism. The scheduler drains real jobs before inserting one sentinel per worker, avoiding busy-waiting and abrupt thread termination.
