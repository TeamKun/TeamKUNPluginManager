package org.kunlab.kpm.installer.impls.autoremove.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

import java.util.List;

/**
 * 自動削除するプラグインが列挙された場合に送信されるシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginEnumeratedSignal extends Signal
{
    /**
     * 自動削除するプラグインのリストです。
     * このリストを変更すると、自動削除の対象を変更できます。
     */
    @NotNull
    private final List<String> targetPlugins;

    /**
     * 自動削除をキャンセルするかどうかを示すフラグです。
     * このフラグがtrueの場合、自動削除はキャンセルされます。
     */
    private boolean cancel;

    public PluginEnumeratedSignal(@NotNull List<String> targetPlugins)
    {
        this.targetPlugins = targetPlugins;
        this.cancel = false;
    }
}
