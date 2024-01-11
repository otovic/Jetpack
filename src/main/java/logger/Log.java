package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.Buffer;
import java.time.LocalTime;

import exceptions.LoggerException;
import utility.UtilityService;

/**
 * Log klasa pomocu koje se loguju poruke u fajl..
 */
public class Log {

    public Log() throws LoggerException {

    }

    public static void log(LogType type, boolean printToConsole, String message) throws LoggerException {
        try (BufferedWriter logger = new BufferedWriter(new FileWriter("server_logs.txt", true))) {
            if(!checkLogFileExists()) createLogsFile();
            switch (type) {
                case ERROR:
                    logger.append(UtilityService.parseTimeForLog(LocalTime.now()))
                        .append(": ")
                        .append("Type: ERROR | ")
                        .append("Details: " + message + "\n");
                    if (printToConsole) System.out.println(message);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new LoggerException("Error while creating logs file!");
        }
    }

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
}
