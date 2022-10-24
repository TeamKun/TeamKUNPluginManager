package net.kunmc.lab.kpm.installer.impls.uninstall;

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
     * {@link net.kunmc.lab.kpm.installer.signals.assertion.IgnoredPluginSignal} でも強制アンインストールが選択されませんでした。
     */
    PLUGIN_IGNORED,
    /**
     * 指定されたプラグインが他のプラグインの依存関係に含まれており、
     * {@link net.kunmc.lab.kpm.installer.impls.uninstall.signals.PluginIsDependencySignal} でも強制アンインストールが選択されませんでした。
     */
    PLUGIN_IS_DEPENDENCY,
    /**
     * アンインストールがユーザまたはシグナルによってキャンセルされました。
     */
    CANCELLED,
}
