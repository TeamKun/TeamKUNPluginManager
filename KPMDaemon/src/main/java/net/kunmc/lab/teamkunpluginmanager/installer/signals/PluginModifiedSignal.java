package net.kunmc.lab.teamkunpluginmanager.installer.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

/**
 * サーバ上のプラグインが変更されたことを示すシグナルです。
 * 新規にインストールされた場合、削除された場合、更新された場合にスローされます。
 */
@Value
public class PluginModifiedSignal implements Signal
{
    /**
     * 変更され他プラグインの情報です。
     *
     * @see PluginDescriptionFile
     */
    @NotNull
    PluginDescriptionFile pluginDescription;
    /**
     * 変更の種類です。
     */
    @NotNull
    ModifyType modifyType;

    /**
     * 変更の種類を表す列挙型です。
     */
    public enum ModifyType
    {
        /**
         * プラグインが新規にインストールされたことを示します。
         */
        ADD,
        /**
         * プラグインが削除されたことを示します。
         */
        REMOVE,
        /**
         * プラグインが更新されたことを示します。
         */
        UPGRADE
    }
}
