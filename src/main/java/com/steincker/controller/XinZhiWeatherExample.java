package com.steincker.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class XinZhiWeatherExample {

    public static void main(String[] args) {
        String apiKey = "SSWBcqYiMp5H_52gU"; // 请替换为你的心知天气 API Key
        String location = "ip"; // 请替换为你想要获取天气信息的城市
        String language = "zh-Hans";

        try {
            String weatherData = getWeatherData(apiKey, location, language);
            System.out.println("Weather Data for " + location + ":\n" + weatherData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getWeatherData(String apiKey, String location, String language) throws IOException {
        String apiUrl = "https://api.seniverse.com/v3/weather/now.json?key=" + apiKey + "&location=" + location + "&language=" + language +"&unit=c";

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // 设置请求方法为 GET
            connection.setRequestMethod("GET");

            // 获取响应代码
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    return response.toString();
                }
            } else {
                throw new IOException("Unexpected response code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }
}
