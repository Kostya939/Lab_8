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


public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        WeatherAPI weatherAPI = new WeatherAPI("79d4b9ae6442c2c09bfc1a07c3df24c1");

        List<WeatherData> weatherData = weatherAPI.getWeatherData();

        analyzeData(weatherData);

        displayResults(weatherData);
    }

    private static void displayResults(List<WeatherData> weatherData) {
    }

    private static void analyzeData(List<WeatherData> weatherData) {

        List<WeatherData> hottestStations = getTop10StationsByTemperature(weatherData, true);
        List<WeatherData> coldestStations = getTop10StationsByTemperature(weatherData, false);

        List<WeatherData> wettestStations = getTop10StationsByPrecipitation(weatherData);

        List<WeatherData> stationsWithLongRainyPeriods = getStationsWithLongRainyPeriods(weatherData);

        List<WeatherData> stationsWithTemperatureIncreases = getStationsWithTemperatureIncreases(weatherData);

        Map<Integer, WeatherData> monthlyData = getAverageMonthlyData(weatherData);

        int monthWithHighestWindSpeed = getMonthWithHighestWindSpeed(monthlyData);
    }

    private static List<WeatherData> getTop10StationsByTemperature(List<WeatherData> weatherData, boolean isHottest) {

        Collections.sort(weatherData, (a, b) -> {
            if (isHottest) {
                return b.getAverageTemperature() - a.getAverageTemperature();
            } else {
                return a.getAverageTemperature() - b.getAverageTemperature();
            }
        });

        return weatherData.subList(0, 10);
    }

    private static List<WeatherData> getTop10StationsByPrecipitation(List<WeatherData> weatherData) {

        Collections.sort(weatherData, (a, b) -> {
            return b.getAveragePrecipitation() - a.getAveragePrecipitation();
        });

        return weatherData.subList(0, 10);
    }

    private static List<WeatherData> getStationsWithLongRainyPeriods(List<WeatherData> weatherData) {

        List<WeatherData> stationsWithLongRainyPeriods = new ArrayList<>();

        for (WeatherData weatherDataItem : weatherData) {
            int rainyDaysCount = 0;
            for (int i = 0; i < weatherData.size() - 1; i++) {
                if (weatherDataItem.getPrecipitation() > 0 && weatherData.get(i + 1).getPrecipitation() > 0) {
                    rainyDaysCount++;
                } else {
                    rainyDaysCount = 0;
                }

                if (rainyDaysCount >= 7) {
                    stationsWithLongRainyPeriods.add(weatherDataItem);
                    break;
                }
            }
        }

        return stationsWithLongRainyPeriods;
    }

