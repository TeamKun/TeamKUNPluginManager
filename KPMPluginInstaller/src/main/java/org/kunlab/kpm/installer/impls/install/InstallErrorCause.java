package org.kunlab.kpm.installer.impls.install;

import org.kunlab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import org.kunlab.kpm.interfaces.installer.signals.InvalidKPMInfoFileSignal;
import org.kunlab.kpm.interfaces.installer.signals.assertion.IgnoredPluginSignal;

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
     * {@link AlreadyInstalledPluginSignal} でも置換が選択されませんでした。
     */
    PLUGIN_ALREADY_INSTALLED,
    /**
     * プラグインの持つKPM情報ファイルが不正であり、
     * {@link InvalidKPMInfoFileSignal} でも無視が選択されませんでした。
     */
    INVALID_KPM_INFO_FILE,
}
