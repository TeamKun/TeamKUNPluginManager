package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install;

/**
 * インストールのエラーを表す列挙型です。
 */
public enum InstallErrorCause
{
    // Tasks
    /**
     * タスクの実行中に例外が発生しました。
     */
    TASK_EXCEPTION_OCCURRED,
    /**
     * タスクの実行に失敗しました。
     */
    TASK_FAILED,

    // Environment errors
    /**
     * 変更しようとしているプラグインが、無視リストに登録されており、
     * {@link net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal} でも強制インストールが選択されませんでした。
     */
    PLUGIN_IGNORED,
    /**
     * インストールしようとしているプラグインが、既にインストールされており、
     * {@link net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install.signals.AlreadyInstalledPluginSignal} でも置換が選択されませんでした。
     */
    PLUGIN_ALREADY_INSTALLED,

    // Exceptions
    /**
     * 例外が発生しました。
     */
    EXCEPTION_OCCURRED,
}
