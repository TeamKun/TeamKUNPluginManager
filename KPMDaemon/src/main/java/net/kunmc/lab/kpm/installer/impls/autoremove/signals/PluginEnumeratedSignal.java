package net.kunmc.lab.kpm.installer.impls.autoremove.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
    private final ArrayList<String> targetPlugins;

    /**
     * 自動削除をキャンセルするかどうかを示すフラグです。
     * このフラグがtrueの場合、自動削除はキャンセルされます。
     */
    private boolean cancel;

    public PluginEnumeratedSignal(@NotNull ArrayList<String> targetPlugins)
    {
        this.targetPlugins = targetPlugins;
        this.cancel = false;
    }
}
