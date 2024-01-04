package org.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        List<String> usCities = Arrays.asList("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose");

        for (String city : usCities) {
            try {
                String weatherDataJson = WeatherApi.getWeatherData(city);
                List<WeatherData> weatherData = WeatherApi.parseWeatherData(weatherDataJson);

                printHottestAndColdestStations(city, weatherData);

                printStationsWithConsecutiveRain(city, weatherData);

                printStationsWithTemperatureIncrease(city, weatherData);

                printAverageStatsByMonth(city, weatherData);

                printWindiestMonth(city, weatherData);

                System.out.println("-----------------------------------------");

            } catch (IOException e) {
                System.err.println("Error while fetching data for " + city + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error for " + city + ": " + e.getMessage());
            }
        }
    }

    private static void printHottestAndColdestStations(String city, List<WeatherData> weatherData) {
        List<Integer> hottestStations = WeatherApi.findHottestStations(weatherData, 10);
        List<Integer> coldestStations = WeatherApi.findColdestStations(weatherData, 10);

        System.out.println("City: " + city);
        System.out.println("Top 10 Hottest Stations: " + hottestStations);
        System.out.println("Top 10 Coldest Stations: " + coldestStations);
    }

    private static void printStationsWithConsecutiveRain(String city, List<WeatherData> weatherData) {
        List<Integer> stationsWithRain = WeatherApi.findStationsWithConsecutiveRain(weatherData, 7);
        System.out.println("City: " + city);
        System.out.println("Stations with more than 7 consecutive days of rain: " + stationsWithRain);
    }

    private static void printStationsWithTemperatureIncrease(String city, List<WeatherData> weatherData) {
        List<Integer> stationsWithTemperatureIncrease = WeatherApi.findStationsWithTemperatureIncrease(weatherData, 5, 5);
        System.out.println("City: " + city);
        System.out.println("Stations with a temperature increase of at least 5°C over 5 consecutive days: "
                + stationsWithTemperatureIncrease);
    }

    private static void printAverageStatsByMonth(String city, List<WeatherData> weatherData) {
        Map<String, Double> averageTemperatureByMonth = WeatherApi.calculateAverageByMonth(weatherData, "temperature");
        Map<String, Double> averageHumidityByMonth = WeatherApi.calculateAverageByMonth(weatherData, "humidity");
        Map<String, Double> averagePrecipitationByMonth = WeatherApi.calculateAverageByMonth(weatherData, "precipitation");

        System.out.println("City: " + city);
        System.out.println("Average Temperature by Month: " + averageTemperatureByMonth);
        System.out.println("Average Humidity by Month: " + averageHumidityByMonth);
        System.out.println("Average Precipitation by Month: " + averagePrecipitationByMonth);
    }

    private static void printWindiestMonth(String city, List<WeatherData> weatherData) {
        String windiestMonth = WeatherApi.findWindiestMonth(weatherData);
        System.out.println("City: " + city);
        System.out.println("Windiest Month: " + windiestMonth);
    }
}