package xyz.e3ndr.atto;

import lombok.NonNull;

public class ThreadHelper {

    public static void executeLater(@NonNull Runnable task, long millis) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(millis);
                    task.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void repeat(@NonNull Runnable task, long millis, @NonNull String name) {
        Thread t = (new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(millis);
                        task.run();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.setName(name);
        t.start();
    }

}
