import java.rmi.UnexpectedException;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import javafx.concurrent.Task;

public class Main {
    private static final int THREAD_COUNT = 4;
    private static final int ITERATIONS_PER_THREAD = 10000000;

    public static void main(String[] args) throws UnexpectedException {
        AtomicReference<Double> pi = new AtomicReference<>(0.0);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);

        Thread[] threads = new Thread[THREAD_COUNT];

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received SIGINT. Stopping threads...");
            // Завершаем потоки, если они все еще работают
            for (Thread thread : threads) {
                thread.interrupt();
            }
            // Ждем завершения всех потоков
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new IllegalArgumentException("Something went wrong");
                }
            }
            double estimatedPi = pi.get() * 4;
            System.out.println("Estimated : " + estimatedPi);
        }));

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int finalI = i;
            threads[i] = new Thread(() -> {
                double localSum = 0.0;
                for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                    int n = (j * THREAD_COUNT) + finalI;
                    localSum += getLeibnizSeriesElement(n);
                    try {
                        // Синхронизация после каждой итерации
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e){
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                final double sum = localSum;
                pi.updateAndGet(currentValue -> currentValue + sum);
            });
            threads[i].start();
        }
    }

    private static double getLeibnizSeriesElement(int n) {
        return Math.pow(-1, n) / (2 * n + 1);
    }
}