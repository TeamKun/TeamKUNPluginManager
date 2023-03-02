package org.kunlab.kpm.task.tasks.alias.source.download.signals;

/**
 * 不正なURLが指定された場合に送信されるシグナルです。
 */
public class MalformedURLSignal extends InvalidRemoteSignal
{
    public MalformedURLSignal(String remoteName, String remoteURL)
    {
        super(remoteName, remoteURL);
    }
}
