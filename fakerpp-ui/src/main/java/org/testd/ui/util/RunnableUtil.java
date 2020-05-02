package org.testd.ui.util;

public class RunnableUtil {

    public static Runnable andThen(Runnable task, Runnable then) {
        return () -> {
          task.run();
          then.run();
        };
    }


}
