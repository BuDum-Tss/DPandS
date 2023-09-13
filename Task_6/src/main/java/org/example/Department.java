package org.example;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Department {

  private final int identifier;
  private final int workingSeconds;
  private int calculationResult = 0;

  public Department(final int identifier) {
    this.identifier = identifier;
    this.workingSeconds = ThreadLocalRandom.current().nextInt(1, 6);
  }

  /**
   * Симуляция работы длительностью в workingSeconds секунд. В данном случае просто вычисляем
   * сумму.
   */
  public void performCalculations() {
    for (int i = 0; i < workingSeconds; i++) {
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
      } catch (final InterruptedException ignored) {
// Ignored case.
      }
      calculationResult += i;
    }
  }

  /**
   * @return Уникальный идентификатор для отдела, по которому мы можем отличить его от других.
   */
  public int getIdentifier() {
    return identifier;
  }

  /**
   * ВАЖНО! Далеко не самый правильный способ вычисления и получения данных, но для демонстрации
   * работы барьера пойдёт.
   *
   * @return Результат вычислений.
   */
  public int getCalculationResult() {
    return calculationResult;
  }
}