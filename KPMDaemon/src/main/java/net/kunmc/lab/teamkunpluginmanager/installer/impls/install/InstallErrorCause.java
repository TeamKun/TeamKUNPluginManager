package net.kunmc.lab.teamkunpluginmanager.installer.impls.install;

import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install.signals.PluginIncompatibleWithKPMSignal;

/**
 * インストールのエラーを表す列挙型です。
 */
public enum InstallErrorCause
{
    // Environment errors
    /**
     * 変更しようとしているプラグインが、無視リストに登録されており、
     * {@link net.kunmc.lab.teamkunpluginmanager.installer.signals.assertion.IgnoredPluginSignal} でも強制インストールが選択されませんでした。
     */
    PLUGIN_IGNORED,
    /**
     * インストールしようとしているプラグインが、既にインストールされており、
     * {@link net.kunmc.lab.teamkunpluginmanager.installer.impls.install.signals.AlreadyInstalledPluginSignal} でも置換が選択されませんでした。
     */
    PLUGIN_ALREADY_INSTALLED,
    /**
     * プラグインの<code>api-version</code>が、このサーバーバージョンと互換性がありません。
     */
    INCOMPATIBLE_API_VERSION,
    /**
     * プラグインのKPM指定バージョンと、このKPMのバージョンが互換性がなく、
     * {@link PluginIncompatibleWithKPMSignal} でも強制インストールが選択されませんでした。
     */
    INCOMPATIBLE_KPM_VERSION,
}
