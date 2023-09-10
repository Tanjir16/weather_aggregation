import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONUtility {

    public static void main(String[] args) {
        // Test the methods
        Map<String, Object> testData = new HashMap<>();
        testData.put("key", "value");
        JSONObject json = convertDataToJSON(testData);
        System.out.println("Converted data to JSON: " + json);

        String jsonString = "{\"key\":\"value\"}";
        Object obj = parseJSONToObject(jsonString);
        System.out.println("Parsed JSON to Object: " + obj);
    }

    public static JSONObject convertDataToJSON(Map<String, Object> data) {
        // Convert and return as JSON
        return new JSONObject(data);
    }

    public static Object parseJSONToObject(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            return parser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
