package org.kunlab.kpm.upgrader.signals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;

public class UpgraderDeploySignal extends Signal
{
    @Getter
    @AllArgsConstructor
    public static class Pre extends UpgraderDeploySignal
    {
        private final Path path;
    }

    public static class Post extends UpgraderDeploySignal
    {
    }

    @Getter
    @AllArgsConstructor
    public static class Error extends UpgraderDeploySignal
    {
        ErrorCause cause;

        public enum ErrorCause
        {
            ALREADY_DEPLOYED,
            DEPLOYER_NOT_EXISTS,
            IO_EXCEPTION_OCCURRED,
        }
    }
}
