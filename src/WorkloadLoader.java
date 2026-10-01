import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Loads scheduler jobs from the project's simple CSV workload format. */
public final class WorkloadLoader {
	private static final int EXPECTED_COLUMN_COUNT = 6;

	private WorkloadLoader() {
		// Utility class.
	}

	/** Reads all jobs from a CSV file, excluding its header row. */
	public static List<Job> loadJobs(String filePath) throws IOException {
		List<Job> jobs = new ArrayList<>();

		try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
			reader.readLine();

			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.isBlank()) {
					continue;
				}

				String[] columns = line.split(",", -1);
				if (columns.length != EXPECTED_COLUMN_COUNT) {
					throw new IOException("Invalid workload row at line " + lineNumber
							+ ": expected " + EXPECTED_COLUMN_COUNT + " columns");
				}

				try {
					jobs.add(new Job(
							columns[0].trim(),
							Long.parseLong(columns[1].trim()),
							Integer.parseInt(columns[2].trim()),
							Long.parseLong(columns[3].trim()),
							Resource.fromString(columns[4]),
							Long.parseLong(columns[5].trim())));
				} catch (NumberFormatException exception) {
					throw new IOException("Invalid numeric value at line " + lineNumber, exception);
				}
			}
		}

		return jobs;
	}
}
