package org.example;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    List<String> list1 = List.of("1: Hello!","1: How are you?", "1: Fine");
    List<String> list2 = List.of("2: Hello!","2: How are you?", "2: Fine");
    List<String> list3 = List.of("3: Hello!","3: How are you?", "3: Fine");
    List<String> list4 = List.of("4: Hello!","4: How are you?", "4: Fine");
    List<Thread> threads = List.of(
        new Thread(()->printAll(list1)),
        new Thread(()->printAll(list2)),
        new Thread(()->printAll(list3)),
        new Thread(()->printAll(list4)));
    threads.forEach(Thread::start);
  }
  public static void printAll(List<String> list){
    list.forEach(System.out::println);
    /*try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }*/
  }
}