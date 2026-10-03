# Mini Job Scheduler & Shared Resource Manager

**Name:** Lay Udtarakviseth  
**Course:** 517-312 Operating Systems

## 1. Results Table

| # | Workload / Policy | Workers | Printer | DB | avg WT | avg TAT | Throughput | avg RW |
|---:|---|---:|---:|---:|---:|---:|---:|---:|
| 1 | jobs_standard.csv / FCFS | 3 | 1 | 2 | 2939 ms | 5431 ms | 1.147 jobs/s | 70 ms |
| 2 | jobs_standard.csv / Priority | 3 | 1 | 2 | 2309 ms | 4932 ms | 1.109 jobs/s | 257 ms |
| 3 | jobs_standard.csv / Priority | 1 | 1 | 2 | 8434 ms | 10877 ms | 0.409 jobs/s | 0 ms |
| 4 | jobs_standard.csv / Priority | 5 | 1 | 2 | 1119 ms | 3961 ms | 1.296 jobs/s | 571 ms |
| 5 | jobs_printer.csv / Priority | 3 | 1 | 2 | 1126 ms | 4203 ms | 0.804 jobs/s | 1600 ms |
| 6 | jobs_printer.csv / Priority | 3 | 2 | 2 | 750 ms | 2518 ms | 1.442 jobs/s | 291 ms |
| 7 | jobs_standard.csv / Priority (repeat) | 3 | 1 | 2 | 2310 ms | 4940 ms | 1.107 jobs/s | 257 ms |

## 2. Analysis Questions

### 1. Increasing Workers from 3 to 5

Throughput does not increase proportionally: it rises from 1.109 jobs/s with 3 workers to 1.296 jobs/s with 5 workers, rather than approaching a 5/3 scaling factor. With printer permits fixed at one, additional CPU workers create more simultaneous contenders for the same semaphore, so average resource wait rises from 257 ms to 571 ms and the printer becomes a bottleneck that produces diminishing returns.

### 2. FCFS vs Priority

Under priority scheduling, high-priority jobs such as J07, J08, and J09 gain earlier access to workers, while lower-priority jobs such as J04, J05, and J06 wait longer behind the priority ordering. Priority improves average waiting time from 2939 ms to 2309 ms, but throughput is slightly lower because resource-heavy jobs can be grouped closer together, increasing semaphore contention and reducing effective worker utilization.

### 3. Increasing Printer Permits from 1 to 2

For `jobs_printer.csv`, increasing printer permits from one to two reduces average resource wait from 1600 ms to 291 ms and increases throughput from 0.804 jobs/s to 1.442 jobs/s. The bottleneck moves away from the printer semaphore toward CPU worker availability and the workload's physical arrival rate, so workers can make progress concurrently instead of queuing for the single printer permit.

### 4. Comparing Row 2 and Row 7

Rows 2 and 7 use the same workload, policy, worker count, and semaphore limits, yet their measurements differ slightly: average waiting time changes from 2309 ms to 2310 ms and throughput from 1.109 to 1.107 jobs/s. This is expected nondeterminism from OS-level thread scheduling, context switches, semaphore wake-up order, and small differences in when worker threads resume after `Thread.sleep()`.

## 3. AI Usage Declaration

AI assistance was used for architectural structuring and for explaining operating-systems concepts used in the project, including scheduling, semaphores, thread coordination, and graceful shutdown. The student manually verified thread synchronization, metric equations, experimental results, and final debugging of the implementation.
