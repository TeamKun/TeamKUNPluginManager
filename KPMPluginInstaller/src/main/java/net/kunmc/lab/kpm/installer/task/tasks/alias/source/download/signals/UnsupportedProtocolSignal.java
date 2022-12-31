package net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals;

import lombok.Getter;

import java.net.URL;

/**
 * サポートされていないプロトコルが指定された場合に送信されるシグナルです。
 */
@Getter
public class UnsupportedProtocolSignal extends InvalidRemoteSignal
{
    /**
     * プロトコル名です。
     */
    private final String protocol;

    public UnsupportedProtocolSignal(String remoteName, URL remoteURL)
    {
        super(remoteName, remoteURL.toString(), remoteURL);
        this.protocol = remoteURL.getProtocol();
    }
}
