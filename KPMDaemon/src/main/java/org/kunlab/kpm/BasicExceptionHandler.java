package org.kunlab.kpm;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicExceptionHandler implements ExceptionHandler
{
    private final Logger logger;

    public BasicExceptionHandler(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void report(Throwable e)
    {
        this.logger.log(Level.WARNING, "An exception has occurred while operating KPMDaemon.", e);
    }
}
