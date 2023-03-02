package org.kunlab.kpm.installer.impls.upgrade.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.Plugin;
import org.kunlab.kpm.installer.impls.upgrade.UpgradeErrorCause;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.versioning.Version;

import javax.annotation.Nullable;

/**
 * プラグインのバージョンが不正であることを示すシグナルです。
 * 不正とされる理由は以下の通りです：
 * <ul>
 *     <li> {@link UpgradeErrorCause#PLUGIN_VERSION_NOT_DEFINED} - プラグインのバージョンが定義されていません。 </li>
 *     <li> {@link UpgradeErrorCause#PLUGIN_VERSION_FORMAT_MALFORMED} - プラグインのバージョンが定義してるバージョンの形式が不正です。 </li>
 *     <li> {@link UpgradeErrorCause#PLUGIN_IS_OLDER_OR_EQUAL} -  プラグインのバージョンが既存のプラグインのバージョンと変わらないか、古いです。 </li>
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvalidPluginVersionSignal extends Signal
{
    /**
     * アップグレード対象のプラグインです。
     */
    private final Plugin plugin;
    /**
     * プラグインのバージョンが不正と判断された理由です。
     */
    private final UpgradeErrorCause invalidReason;

    /**
     * サーバのプラグインのバージョンです。
     * 解析に失敗した場合は {@code null} です。
     */
    @Nullable
    private final Version serverPluginVersion;

    /**
     * アップグレードしようとしているプラグインのバージョンです。
     * 解析に失敗した場合は {@code null} です。
     */
    @Nullable
    private final Version pluginVersion;

    /**
     * アップグレードを続けるかどうかを示すフラグです。
     */
    private boolean continueUpgrade;

    /**
     * プラグインを除外するかどうかを示すフラグです。
     */
    private boolean excludePlugin;

    public InvalidPluginVersionSignal(Plugin plugin, UpgradeErrorCause invalidReason)
    {
        this(plugin, invalidReason, null, null, true, true);
    }

    public InvalidPluginVersionSignal(Plugin plugin, UpgradeErrorCause invalidReason, @Nullable Version serverPluginVersion, @Nullable Version pluginVersion)
    {
        this(plugin, invalidReason, serverPluginVersion, pluginVersion, true, true);
    }
}
