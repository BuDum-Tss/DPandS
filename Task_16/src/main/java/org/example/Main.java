package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
  private static final int PAGE_SIZE = 25;

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java SimpleHTTPClient <URL>");
      System.exit(1);
    }

    String urlString = args[0];

    try {
      URL url = new URL(urlString);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        int linesPrinted = 0;

        while ((line = reader.readLine()) != null) {
          System.out.println(line);
          linesPrinted++;

          if (linesPrinted % PAGE_SIZE == 0) {
            System.out.println("Press enter to scroll down");
            while ((System.in.read()) != '\n');
          }
        }
        reader.close();

      } else {
        System.out.println("HTTP GET request failed with error code: " + responseCode);
      }
      connection.disconnect();
    } catch (IOException e) {
      System.out.println("An error occurred: " + e.getMessage());
    }
  }
}