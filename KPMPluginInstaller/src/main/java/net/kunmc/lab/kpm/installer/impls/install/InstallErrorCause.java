package net.kunmc.lab.kpm.installer.impls.install;

import net.kunmc.lab.kpm.interfaces.installer.signals.InvalidKPMInfoFileSignal;
import net.kunmc.lab.kpm.interfaces.installer.signals.assertion.IgnoredPluginSignal;

/**
 * インストールのエラーを表す列挙型です。
 */
public enum InstallErrorCause
{
    // Environment errors
    /**
     * 変更しようとしているプラグインが、無視リストに登録されており、
     * {@link IgnoredPluginSignal} でも強制インストールが選択されませんでした。
     */
    PLUGIN_IGNORED,
    /**
     * インストールしようとしているプラグインが、既にインストールされており、
     * {@link net.kunmc.lab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal} でも置換が選択されませんでした。
     */
    PLUGIN_ALREADY_INSTALLED,
    /**
     * プラグインの持つKPM情報ファイルが不正であり、
     * {@link InvalidKPMInfoFileSignal} でも無視が選択されませんでした。
     */
    INVALID_KPM_INFO_FILE,
}
