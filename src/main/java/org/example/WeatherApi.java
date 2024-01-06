package org.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherAPI {

    private String apiKey;

    public WeatherAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<WeatherData> getWeatherData() throws IOException, JSONException, ParseException {

        URL url = new URL("https://api.openweathermap.org/data/2.5/weather?appid=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Помилка отримання даних: " + responseCode);
        }

        String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        JSONObject response = new JSONObject(responseBody);

        List<WeatherData> weatherData = new ArrayList<>();
        JSONArray weatherArray = response.getJSONArray("weather");
        for (int i = 0; i < weatherArray.length(); i++) {
            JSONObject weatherObject = weatherArray.getJSONObject(i);

            WeatherData weatherDataItem = new WeatherData(
                    weatherObject.getString("id"),
                    weatherObject.getString("main"),
                    weatherObject.getJSONObject("main").getDouble("temp"),
                    weatherObject.getJSONObject("main").getDouble("humidity"),
                    weatherObject.getJSONObject("rain").getDouble("3h"),
                    weatherObject.getJSONObject("wind").getDouble("speed"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(weatherObject.getJSONObject("dt").getString("value"))
            );

            weatherData.add(weatherDataItem);
        }

        return weatherData;
    }
}
