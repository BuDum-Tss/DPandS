package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class Founder {

  private final List<Runnable> workers;
  private final CyclicBarrier barrier;

  public Founder(final Company company) {
    barrier = new CyclicBarrier(company.getDepartmentsCount(), company::showCollaborativeResult);
    List<Runnable> list = new ArrayList<>();
    for (int i = 0; i < company.getDepartmentsCount(); i++) {
      int finalI = i;
      list.add(() -> {
        company.getFreeDepartment(finalI).performCalculations();
        System.out.println("Department finished! Waiting others...");
        try {
          barrier.await();
        } catch (InterruptedException ex) {

        } catch (BrokenBarrierException ex) {
          throw new RuntimeException(ex);
        }
      });
    }
    this.workers = list;
  }

  public void start() {
    Thread thread = Thread.currentThread();
    for (final Runnable worker : workers) {
      new Thread(worker).start();
    }
  }
}