package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static jdk.javadoc.doclet.DocletEnvironment.ModuleMode.API;


public class WeatherApi {

    private List<WeatherData> weatherData;


    private static final String API_KEY = String.valueOf(API);
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static String getWeatherData(String cityName) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL + "?q=" + cityName + "&appid=" + API_KEY)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            assert response.body() != null;
            return response.body().string();
        } else {
            throw new IOException("HTTP error: " + response.code());
        }
    }


    public static List<WeatherData> parseWeatherData(String response) {
        List<WeatherData> weatherData = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response);

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("stations") && jsonObject.get("stations").isJsonObject()) {
                JsonObject stations = jsonObject.getAsJsonObject("stations");

                for (Map.Entry<String, JsonElement> entry : stations.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        JsonObject stationData = entry.getValue().getAsJsonObject();

                        WeatherData data = new WeatherData();
                        data.setId(Integer.parseInt(entry.getKey()));

                        if (stationData.has("date") && stationData.get("date").isJsonPrimitive()) {
                            data.setDate(stationData.get("date").getAsString());
                        }

                        if (stationData.has("temperature") && stationData.get("temperature").isJsonPrimitive()) {
                            data.setTemperature(stationData.get("temperature").getAsDouble());
                        }

                        if (stationData.has("humidity") && stationData.get("humidity").isJsonPrimitive()) {
                            data.setHumidity(stationData.get("humidity").getAsInt());
                        }

                        if (stationData.has("precipitation") && stationData.get("precipitation").isJsonPrimitive()) {
                            data.setPrecipitation(stationData.get("precipitation").getAsDouble());
                        }

                        if (stationData.has("wind_speed") && stationData.get("wind_speed").isJsonPrimitive()) {
                            data.setWindSpeed(stationData.get("wind_speed").getAsDouble());
                        }

                        weatherData.add(data);
                    }
                }
            }
        }

        return weatherData;
    }


    // Find The Hottest Stations
    public static List<Integer> findHottestStations(List<WeatherData> weatherData, int limit) {
        return weatherData.stream()
                .filter(data -> data.getTemperature() > 0)
                .collect(Collectors.groupingBy(WeatherData::getId,
                        Collectors.averagingDouble(WeatherData::getTemperature)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Find The Coldest Stations
    public static List<Integer> findColdestStations(List<WeatherData> weatherData, int limit) {
        return weatherData.stream()
                .filter(data -> data.getTemperature() > 0)
                .collect(Collectors.groupingBy(WeatherData::getId,
                        Collectors.averagingDouble(WeatherData::getTemperature)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    // Find The Wettest Stations
    public List<Integer> findWettestStations() {
        return weatherData.stream()
                .filter(data -> data.getPrecipitation() > 0)
                .collect(Collectors.groupingBy(WeatherData::getId,
                        Collectors.averagingDouble(WeatherData::getPrecipitation)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    public static List<Integer> findStationsWithConsecutiveRain(List<WeatherData> data, int consecutiveDays) {
        List<Integer> resultStations = new ArrayList<>();

        for (WeatherData stationData : data) {
            int consecutiveRainDays = 0;
            for (WeatherData dayData : data) {
                if (stationData.getId() == dayData.getId() && dayData.getPrecipitation() > 0) {
                    consecutiveRainDays++;
                    if (consecutiveRainDays >= consecutiveDays) {
                        resultStations.add(stationData.getId());
                        break;
                    }
                } else {
                    consecutiveRainDays = 0;
                }
            }
        }
        return resultStations.stream().distinct().collect(Collectors.toList());
    }


    public static List<Integer> findStationsWithTemperatureIncrease(List<WeatherData> data, double deltaTemperature, int consecutiveDays) {
        List<Integer> resultStations = new ArrayList<>();

        for (WeatherData stationData : data) {
            int consecutiveTemperatureIncreaseDays = 0;
            double previousTemperature = 0;

            for (WeatherData dayData : data) {
                if (stationData.getId() == dayData.getId()) {
                    if (dayData.getTemperature() - previousTemperature >= deltaTemperature) {
                        consecutiveTemperatureIncreaseDays++;
                        if (consecutiveTemperatureIncreaseDays >= consecutiveDays) {
                            resultStations.add(stationData.getId());
                            break;
                        }
                    } else {
                        consecutiveTemperatureIncreaseDays = 0;
                    }
                    previousTemperature = dayData.getTemperature();
                }
            }
        }
        return resultStations.stream().distinct().collect(Collectors.toList());
    }


    public static Map<String, Double> calculateAverageByMonth(List<WeatherData> data, String property) {
        Map<String, List<Double>> monthlyData = new HashMap<>();

        for (WeatherData dayData : data) {
            String month = dayData.getDate().substring(0, 7); // Assuming date is in "YYYY-MM-DD" format
            monthlyData.computeIfAbsent(month, k -> new ArrayList<>());

            switch (property) {
                case "temperature" -> monthlyData.get(month).add(dayData.getTemperature());
                case "humidity" -> monthlyData.get(month).add((double) dayData.getHumidity());
                case "precipitation" -> monthlyData.get(month).add(dayData.getPrecipitation());
            }
        }

        return monthlyData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0)
                ));
    }


    public static String findWindiestMonth(List<WeatherData> data) {
        Map<String, Double> averageWindSpeedByMonth = calculateAverageByMonth(data, "windSpeed");

        // Find the month with the highest average wind speed
        Map.Entry<String, Double> maxEntry = Collections.max(averageWindSpeedByMonth.entrySet(), Map.Entry.comparingByValue());
        return maxEntry.getKey();
    }


    public void displayData(List<WeatherData> data) {
        System.out.println("Weather Data Analysis:");

        // Display each weather data entry
        for (WeatherData dayData : data) {
            System.out.println(dayData);
        }
    }



    public WeatherApi(List<WeatherData> initialData) {
        setWeatherData(initialData);
    }

    public void setWeatherData(List<WeatherData> data) {
        weatherData = data;
    }

    public List<WeatherData> getWeatherData() {
        return weatherData;
    }

}
