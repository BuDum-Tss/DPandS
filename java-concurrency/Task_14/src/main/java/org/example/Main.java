package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Main {

  public static void main(String[] args) {
    Semaphore widget = new Semaphore(0);
    Semaphore module = new Semaphore(0);
    Semaphore partA = new Semaphore(0);
    Semaphore partB = new Semaphore(0);
    Semaphore partC = new Semaphore(0);

    List<Thread> producers = new ArrayList<>();
    producers.add(new Thread(new SomeProducer("Widget", widget, new Semaphore[]{partC, module}, 0)));
    producers.add(new Thread(new SomeProducer("Module", module, new Semaphore[]{partA, partB}, 0)));
    producers.add(new Thread(new SomeProducer("Part A", partA, new Semaphore[]{}, 1000)));
    producers.add(new Thread(new SomeProducer("Part B", partB, new Semaphore[]{}, 2000)));
    producers.add(new Thread(new SomeProducer("Part C", partC, new Semaphore[]{}, 3000)));

    producers.forEach(Thread::start);
  }
}