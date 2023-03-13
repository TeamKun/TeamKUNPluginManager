package org.kunlab.kpm;

import lombok.AllArgsConstructor;

import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class BasicExceptionHandler implements ExceptionHandler
{
    private final Logger logger;

    @Override
    public void report(Throwable e)
    {
        this.logger.log(Level.WARNING, "An exception has occurred while operating KPMDaemon.", e);
    }
}
