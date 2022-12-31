package net.kunmc.lab.kpm.task.tasks.alias.source.download.signals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * リモートが不正だった場合に送信されるシグナルです。
 */
@AllArgsConstructor
@Getter
public abstract class InvalidRemoteSignal extends Signal
{
    /**
     * リモートの名前です。
     */
    @NotNull
    private final String remoteName;
    /**
     * リモートのURLです。
     */
    @NotNull
    private final String remoteURL;

    /**
     * URLのオブジェクトです。
     */
    @Nullable
    private final URL urlObject;

    public InvalidRemoteSignal(@NotNull String remoteName, @NotNull String remoteURL)
    {
        this(remoteName, remoteURL, null);
    }
}
