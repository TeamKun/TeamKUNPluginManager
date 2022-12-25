package net.kunmc.lab.kpm.installer.impls.install;

/**
 * インストールのエラーを表す列挙型です。
 */
public enum InstallErrorCause
{
    // Environment errors
    /**
     * 変更しようとしているプラグインが、無視リストに登録されており、
     * {@link net.kunmc.lab.kpm.installer.signals.assertion.IgnoredPluginSignal} でも強制インストールが選択されませんでした。
     */
    PLUGIN_IGNORED,
    /**
     * インストールしようとしているプラグインが、既にインストールされており、
     * {@link net.kunmc.lab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal} でも置換が選択されませんでした。
     */
    PLUGIN_ALREADY_INSTALLED,
    /**
     * プラグインの持つKPM情報ファイルが不正であり、
     * {@link net.kunmc.lab.kpm.installer.signals.InvalidKPMInfoFileSignal} でも無視が選択されませんでした。
     */
    INVALID_KPM_INFO_FILE,
}
