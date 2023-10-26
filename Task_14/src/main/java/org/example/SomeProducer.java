package org.example;

import java.util.concurrent.Semaphore;

public class SomeProducer implements Runnable{

  private final String name;
  private final Semaphore semaphore;
  private final Semaphore[] parts;
  private final int delay;
  public SomeProducer(String name, Semaphore semaphore, Semaphore[] parts, int delay) {
    this.name = name;
    this.semaphore = semaphore;
    this.parts = parts;
    this.delay = delay;
  }

  @Override
  public void run() {
    for(int i=1; true; i++){
      try {
        for (Semaphore part : parts) {
          part.acquire();
        }
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        break;
      }
      System.out.println(name + " №" + i + " created");
      semaphore.release();
    }
  }
}
