package net.kunmc.lab.teamkunpluginmanager.installer.impls.install;

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
}
