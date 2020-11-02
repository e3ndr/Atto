package xyz.e3ndr.atto;

public class ThreadHelper {

    public static void executeLater(Runnable task, long millis) {
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
