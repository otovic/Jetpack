package logger;

import event_snapshot.Snapshot;
import exceptions.LoggerException;
import server.Request;
import utility.UtilityService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalTime;

public class Logger {
    private static boolean checkLogFileExists() throws LoggerException {
        try {
            File logFile = new File("server_logs.txt");
            return logFile.exists();
        } catch (Exception e) {
            throw new LoggerException("Error while checking if logger file exists!");
        }
    }

    private static boolean createLogsFile() throws LoggerException {
        try {
            File logFile = new File("server_logs.txt");
            return logFile.createNewFile();
        } catch (Exception e) {
            throw new LoggerException("Error while creating logs file!");
        }
    }

    public static void logMessage(LogType type, boolean printToConsole, Snapshot priorEvent) throws LoggerException {
        try (BufferedWriter logger = new BufferedWriter(new FileWriter("server_logs.txt", true))){
            LocalTime time = LocalTime.now();
            if(!checkLogFileExists()) {
                createLogsFile();
            }
            switch (type) {
                case SERVER_START -> logger.append(UtilityService.parseTimeForLog(time))
                        .append(": ")
                        .append("Type: SERVER_START | ")
                        .append("Details: " + priorEvent.snapshot + "\n");

                case ERROR -> logger.append(UtilityService.parseTimeForLog(time))
                        .append(": ")
                        .append("Type: ERROR | ")
                        .append("Details: " + priorEvent.snapshot + "\n");

                case CLIENT_CONNECTED -> logger.append(UtilityService.parseTimeForLog(time))
                        .append(" - ")
                        .append("Type: CLIENT_CONNECTED ")
                        .append("Details: " + priorEvent.snapshot + "\n");
            }
            if(printToConsole) System.out.println(priorEvent.snapshot);
        } catch (Exception e) {
            System.out.println("ERROR: Error while writing logs! MESSAGE: " + e.getMessage());
        }
    }

    public static void logRequest(Request req, boolean printToConsole, Snapshot priorEvent) throws LoggerException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("server_logs.txt", true))) {
            LocalTime time = LocalTime.now();
            writer.append(UtilityService.parseTimeForLog(time))
                    .append(": ")
                    .append("Request made");
            if(printToConsole) System.out.println("Request made");
        } catch (Exception e) {
            System.out.println("ERROR: Error while writing logs! MESSAGE: " + e.getMessage());
        }
    }
}
