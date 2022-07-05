package group.msg.jpowermonitor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class CsvMeasurementsReader {
    private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;
    private static final String CSV_DELIMITER = ",";
    private static final Set<String> columnsFromInterest = Set.of("Date", "Time", "\"CPU Package Power [W]\"", "\"Total System Power [W]\"");

    private Path measurementsCsvFile;
    private Map<String, Integer> columnMapping;

    public CsvMeasurementsReader(String measurementsCsvFilePath) {
        validateMeasurementsCsvFile(measurementsCsvFilePath);
    }

    public static void main(String[] args) throws InterruptedException {
        CsvMeasurementsReader cmr = new CsvMeasurementsReader("hwidefault.CSV"); // relative path
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Map<String, String> measurements = cmr.readMeasurementsFromFile();
                measurements.forEach((k, v) -> System.out.println(k + " - " + v));
            }
        };
        Timer timer = new Timer();
        timer.schedule(tt, 10000L, 10000L);
        TimeUnit.SECONDS.sleep(31);
        tt.cancel();
        timer.cancel();
        timer.purge();
        System.exit(0);
    }

    public Map<String, String> readMeasurementsFromFile() {
        Map<String, String> measurements = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(measurementsCsvFile, DEFAULT_ENCODING)) {
            System.out.println("Trying to read measurements from file '" + measurementsCsvFile.toAbsolutePath().normalize() + "' using encoding " + DEFAULT_ENCODING.displayName());
            if (columnMapping == null) {
                initializeColumnMapping(reader);
            }
            String lastLine = "", tmp;
            while ((tmp = reader.readLine()) != null) {
                lastLine = tmp;
            }
            String[] values = lastLine.split(CSV_DELIMITER);
            columnsFromInterest.forEach(c -> measurements.putIfAbsent(c, values[columnMapping.get(c)]));
        } catch (IOException ex) {
            System.err.println("Cannot read measurements from file '" + measurementsCsvFile.toAbsolutePath().normalize() + "'");
            ex.printStackTrace();
        }
        return measurements;
    }

    private void validateMeasurementsCsvFile(String measurementsCsvFilePath) {
        measurementsCsvFile = Paths.get(measurementsCsvFilePath);
        if (!Files.isRegularFile(measurementsCsvFile)) {
            System.err.println("No measurements csv file found at '" + measurementsCsvFile.toAbsolutePath().normalize() + "'");
        }
    }

    private void initializeColumnMapping(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        String[] headers = headerLine.split(CSV_DELIMITER);
        columnMapping = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            if (columnsFromInterest.contains(headers[i])) {
                columnMapping.putIfAbsent(headers[i], i);
            }
        }
    }
}
