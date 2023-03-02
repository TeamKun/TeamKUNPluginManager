package org.kunlab.kpm.installer.impls.uninstall;

import org.kunlab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;

/**
 * アンインストールのエラーを表す列挙型です。
 */
public enum UnInstallErrorCause
{
    /**
     * 指定されたプラグインが見つかりませんでした。
     */
    PLUGIN_NOT_FOUND,
    /**
     * 指定されたプラグインが無視リストに登録されており、
     * {@link IgnoredPluginSignal} でも強制アンインストールが選択されませんでした。
     */
    PLUGIN_IGNORED,
    /**
     * 指定されたプラグインが他のプラグインの依存関係に含まれており、
     * {@link PluginIsDependencySignal} でも強制アンインストールが選択されませんでした。
     */
    PLUGIN_IS_DEPENDENCY,
    /**
     * アンインストールがユーザまたはシグナルによってキャンセルされました。
     */
    CANCELLED,
}
