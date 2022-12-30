package net.kunmc.lab.kpm.upgrader.signals;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kunmc.lab.kpm.signal.Signal;
import net.kunmc.lab.kpm.versioning.Version;

@Getter
@AllArgsConstructor
public class LatestFetchSignal extends Signal
{
    protected final Version currentVersion;

    @EqualsAndHashCode(callSuper = true)
    public static class Pre extends LatestFetchSignal
    {
        public Pre(Version currentVersion)
        {
            super(currentVersion);
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class Post extends LatestFetchSignal
    {
        private final Version latestVersion;

        public Post(Version currentVersion, Version latestVersion)
        {
            super(currentVersion);
            this.latestVersion = latestVersion;
        }

        public boolean isUpgradable()
        {
            return this.getLatestVersion().isNewerThan(this.getCurrentVersion());
        }

        public boolean isFetchedOlderVersion()
        {
            return this.getLatestVersion().isOlderThan(this.getCurrentVersion());
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class Error extends LatestFetchSignal
    {
        private final String errorMessage;

        public Error(Version currentVersion, String errorMessage)
        {
            super(currentVersion);
            this.errorMessage = errorMessage;
        }
    }

}
