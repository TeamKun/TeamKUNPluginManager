package net.kunmc.lab.teamkunpluginmanager.console;

import org.apache.commons.collections.iterators.LoopingIterator;

import java.util.Arrays;

public class Progress
{
    String[] spinner = {"|", "/", "-", "\\"};

    Thread thread;

    public Progress(String message)
    {
        Runnable runnable = () -> {
            for (LoopingIterator it = new LoopingIterator(Arrays.asList(spinner)); it.hasNext(); )
            {
                String s = (String) it.next();
                System.out.print("\r" + message + "..." + s);
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    System.out.println("\r" + message + "...完了");
                    return;
                }
            }
        };

        thread = new Thread(runnable);
    }

    public Progress start()
    {
        if (thread == null)
            return null;
        if (thread.isAlive())
            return this;

        thread.start();
        return this;
    }

    public void stop()
    {
        if (!thread.isAlive())
            return;
        thread.interrupt();
        thread = null;
    }
}
