package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install.InstallErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.DependsCollectErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description.DescriptionLoadErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.PluginsInstallErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.PluginResolveErrorCause;
import org.jetbrains.annotations.Nullable;

/**
 * インストールが完了したときのシグナルを処理するハンドラーです。
 */
public class InstallFinishedSignalHandler extends InstallFinishedSignalBase
{
    public InstallFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<?> result)
    {
        this.terminal.success("インストールが正常に完了しました。");
    }

    private boolean handleGeneralErrors(@Nullable InstallErrorCause cause)
    {
        if (cause == null)
            return false;

        switch (cause)
        {
            case PLUGIN_IGNORED:
                this.terminal.error("指定されたプラグインが無視リストに登録されています。");
                return true;
            case PLUGIN_ALREADY_INSTALLED:
                this.terminal.error("指定されたプラグインは既にインストールされています。");
                return true;
        }

        return false;
    }

    private void handlePluginResolveError(PluginResolveErrorCause cause)
    {
        switch (cause)
        {
            case CANCELLED:
                this.terminal.error("プラグイン解決がキャンセルされました。");
                break;
            case GOT_ERROR_RESULT:
                this.terminal.error("プラグイン解決中にエラーが発生しました。");
                break;
            case ILLEGAL_INTERNAL_STATE:
                this.terminal.error("プラグイン解決中に予期しない内部エラーが発生しました。");
                break;
        }
    }

    private void handleDownloadError(DownloadErrorCause cause)
    {
        switch (cause)
        {
            case ILLEGAL_HTTP_RESPONSE:
                this.terminal.error("ダウンロード中にサーバから無効なレスポンスを受け取りました。");
                break;
            case NO_BODY_IN_RESPONSE:
                this.terminal.error("Body が空でした。");
                break;
            case IO_EXCEPTION:
                this.terminal.error("ダウンロード中に I/O エラーが発生しました。");
                break;
            case UNKNOWN_ERROR:
                this.terminal.error("ダウンロード中に不明なエラーが発生しました。");
                break;
        }
    }

    private void handleDescriptionLoadError(DescriptionLoadErrorCause cause)
    {
        switch (cause)
        {
            case NOT_A_PLUGIN:
                this.terminal.error("指定されたファイルがプラグインではないか、 plugin.yml が存在しません。");
                break;
            case INVALID_DESCRIPTION:
                this.terminal.error("不正なプラグイン情報ファイルです。");
                break;
            case IO_EXCEPTION:
                this.terminal.error("プラグイン情報ファイルの読み込み中に I/O エラーが発生しました。");
                break;
        }
    }

    @SuppressWarnings("SwitchStatementWithoutDefaultBranch")
    private void handleDependsCollectError(DependsCollectErrorCause cause)
    {
        if (cause == DependsCollectErrorCause.SOME_DEPENDENCIES_COLLECT_FAILED)
        {
            this.terminal.error("いくつかの依存関係の解決に失敗しました。");
        }
    }

    private void handleInstallError(PluginsInstallErrorCause cause)
    {
        switch (cause)
        {
            case RELOCATE_FAILED:
                this.terminal.error("プラグインの再配置に失敗しました。");
                break;
            case INVALID_PLUGIN:
                this.terminal.error("不正なプラグインファイルをインストールしようとしました。");
                break;
            case INVALID_PLUGIN_DESCRIPTION:
                this.terminal.error("不正なプラグイン情報ファイルが含まれています。");
                break;
            case IO_EXCEPTION_OCCURRED:
                this.terminal.error("プラグインのインストール中に I/O エラーが発生しました。");
                break;
            case EXCEPTION_OCCURRED:
                this.terminal.error("プラグインのインストール中に予期しないエラーが発生しました。");
                break;
        }
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() instanceof InstallErrorCause &&
                this.handleGeneralErrors((InstallErrorCause) result.getReason()))
            return;

        Enum<?> errorCause = result.getReason();

        if (errorCause instanceof PluginResolveErrorCause)
            this.handlePluginResolveError((PluginResolveErrorCause) errorCause);
        else if (errorCause instanceof DownloadErrorCause)
            this.handleDownloadError((DownloadErrorCause) errorCause);
        else if (errorCause instanceof DescriptionLoadErrorCause)
            this.handleDescriptionLoadError((DescriptionLoadErrorCause) errorCause);
        else if (errorCause instanceof DependsCollectErrorCause)
            this.handleDependsCollectError((DependsCollectErrorCause) errorCause);
        else if (errorCause instanceof PluginsInstallErrorCause)
            this.handleInstallError((PluginsInstallErrorCause) errorCause);
        else
            this.terminal.error("不明なエラーが発生しました。");
    }
}
