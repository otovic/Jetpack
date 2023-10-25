package utility;

import java.time.LocalTime;

public class UtilityService {
    public static String parseTimeForLog(LocalTime time) {
        StringBuilder parsedTime = new StringBuilder();
        parsedTime.append(time.getHour());
        parsedTime.append(":");
        parsedTime.append(time.getMinute());
        parsedTime.append(":");
        parsedTime.append(time.getSecond());

        return parsedTime.toString();
    }
}
