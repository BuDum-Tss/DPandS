package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

  public static void main(String[] args) {
    int port = 9090;

    try {
      ServerSocket serverSocket = new ServerSocket(port);
      System.out.println("Echo server is running on port " + port);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket);

        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println("Received from client: " + line);

          // Отправляем обратно клиенту тот же текст
          writer.println("Echo from server: " + line);

          if (line.equalsIgnoreCase("exit")) {
            break;
          }
        }

        reader.close();
        writer.close();
        clientSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
