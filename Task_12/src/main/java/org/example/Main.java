package org.example;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    List<String> list = new LinkedList<>();
    Thread sortingThread = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(5000); // Ждем 5 секунд
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        synchronized (list){
          Collections.sort(list);
        }
      }
    });
    sortingThread.setDaemon(true);
    sortingThread.start();
    Scanner scanner = new Scanner(System.in);
    String input = "";
    while (!input.equals("exit")) {
      System.out.print("Введите строку: ");
      input = scanner.nextLine();

      synchronized(list) {
        if (input.isEmpty()) {
          System.out.println("Текущее состояние списка: " + list);
        } else {
          while (input.length() > 80) {
            list.add(input.substring(0, 80));
            input = input.substring(80);
          }
          list.add(input);
        }
      }
    }
  }
}