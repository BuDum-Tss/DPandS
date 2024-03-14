package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
  public static void main(String[] args) {
    int port = 8080;
    String remoteHost = "localhost";
    int remotePort = 9090;

    try {
      ServerSocket serverSocket = new ServerSocket(port);
      System.out.println("Server listening on port " + port);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket);

        try {
          Socket remoteSocket = new Socket(remoteHost, remotePort);
          System.out.println("Connected to remote server: " + remoteSocket);

          Thread inputThread = transmitData(clientSocket, remoteSocket, "Client");
          Thread outputThread = transmitData(remoteSocket, clientSocket, "Server");

          inputThread.start();
          outputThread.start();

          inputThread.join();
          outputThread.join();

          remoteSocket.close();
        } catch (IOException | InterruptedException e) {
          System.err.println("Error establishing connection to remote server: " + e.getMessage());
        }
        clientSocket.close();
      }
    } catch (IOException e) {
      System.err.println("Error starting the server: " + e.getMessage());
    }
  }

  private static Thread transmitData(Socket inputSocket, Socket outputSocket, String direction) {
    return new Thread(() -> {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputSocket.getOutputStream()), true);

        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(direction + " received: " + line);
          writer.println(line);
        }

        reader.close();
        writer.close();
      } catch (IOException e) {
        System.err.println("Error transmitting data from " + direction + ": " + e.getMessage());
      }
    });
  }
}
