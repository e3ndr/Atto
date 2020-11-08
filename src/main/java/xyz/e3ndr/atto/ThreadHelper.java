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

}
