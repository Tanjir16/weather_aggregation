import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class Modify {

    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("dd/MM:ha");

    public static void main(String[] args) {
        Modify modify = new Modify();
        String[] filePaths = {
                "src/weather_data.txt",
                "src/weather_data2.txt"
        };

        while (true) {
            modify.processFiles(filePaths);
            try {
                Thread.sleep(60000); // 30 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void processFiles(String[] filePaths) {
        for (String filePath : filePaths) {
            processFile(filePath);
        }
    }

    private void processFile(String filePath) {
        String content = readFile(filePath);
        if (content != null) {
            String newContent = modifyContent(content);
            writeFile(filePath, newContent);
            System.out.println(filePath + " modified successfully!");
        }
    }

    private String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace(); // Add this line to print the stack trace
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    private void writeFile(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
        } catch (IOException e) {
            e.printStackTrace(); // Add this line to print the stack trace
            System.out.println("Error: " + e.getMessage());
        }
    }

    private String modifyContent(String content) {
        content = modifyId(content);
        content = incrementAirTemp(content);
        content = randomizeWindSpeed(content);
        content = incrementDateTime(content);
        return content;
    }

    // ... (rest of the methods remain the same, no change in the below methods) ...

    private String incrementAirTemp(String content) {
        Pattern pattern = Pattern.compile("(air_temp:)([-]?\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            double updatedTemp = Double.parseDouble(matcher.group(2)) + 1;
            matcher.appendReplacement(sb, matcher.group(1) + updatedTemp);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String randomizeWindSpeed(String content) {
        Random rand = new Random();
        int newKmh = rand.nextInt(11) + 10;
        int newKt = newKmh / 2;

        content = content.replaceAll("wind_spd_kmh:\\d+", "wind_spd_kmh:" + newKmh);
        content = content.replaceAll("wind_spd_kt:\\d+", "wind_spd_kt:" + newKt);

        return content;
    }

    private String modifyId(String content) {
        Pattern pattern = Pattern.compile("(id:IDS609)(\\d)");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int lastDigit = Integer.parseInt(matcher.group(2));
            int newDigit = (lastDigit + 1) % 10;
            matcher.appendReplacement(sb, matcher.group(1) + newDigit);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String incrementDateTime(String content) {
        Pattern patternFull = Pattern.compile("(local_date_time_full:)(\\d{14})");
        Pattern patternShort = Pattern.compile("(local_date_time:)(\\d{2}/\\d{2}:\\d{2}[ap]m)");
        Matcher matcherFull = patternFull.matcher(content);
        Matcher matcherShort = patternShort.matcher(content);
        StringBuffer sb = new StringBuffer();

        Calendar cal = Calendar.getInstance();

        while (matcherFull.find()) {
            try {
                Date date = FULL_DATE_FORMAT.parse(matcherFull.group(2));
                cal.setTime(date);
                cal.add(Calendar.HOUR, 1);
                String updatedDateTimeFull = FULL_DATE_FORMAT.format(cal.getTime());
                matcherFull.appendReplacement(sb, matcherFull.group(1) + updatedDateTimeFull);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        matcherFull.appendTail(sb);
        content = sb.toString();

        sb = new StringBuffer();

        while (matcherShort.find()) {
            try {
                Date date = SHORT_DATE_FORMAT.parse(matcherShort.group(2));
                cal.setTime(date);
                cal.add(Calendar.HOUR, 1);
                String updatedDateTimeShort = SHORT_DATE_FORMAT.format(cal.getTime()).toLowerCase();
                matcherShort.appendReplacement(sb, matcherShort.group(1) + updatedDateTimeShort);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        matcherShort.appendTail(sb);
        return sb.toString();
    }
}
