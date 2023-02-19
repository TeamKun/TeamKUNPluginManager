package org.kunlab.kpm.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.install.InstallErrorCause;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;
import org.kunlab.kpm.task.tasks.dependencies.collector.DependsCollectErrorCause;
import org.kunlab.kpm.task.tasks.description.DescriptionLoadErrorCause;
import org.kunlab.kpm.task.tasks.download.DownloadErrorCause;
import org.kunlab.kpm.task.tasks.install.PluginsInstallErrorCause;
import org.kunlab.kpm.task.tasks.resolve.PluginResolveErrorCause;

/**
 * インストールが完了したときのシグナルを処理するハンドラーです。
 */
public class InstallFinishedSignalHandler extends InstallFinishedSignalBase
{
    private static final MsgArgs INSTALLER_NAME = MsgArgs.of("name", "%%installer.install%%");

    public InstallFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        this.terminal.success("インストールが正常に完了しました。");
    }

    private boolean handleGeneralErrors(@Nullable InstallErrorCause cause)
    {
        if (cause == null)
            return false;

        String key;
        switch (cause)
        {
            case PLUGIN_IGNORED:
                key = "installer.install.errors.excluded";
                break;
            case PLUGIN_ALREADY_INSTALLED:
                key = "installer.install.errors.duplicate";
                break;
            case PLUGIN_NOT_MANUALLY_INSTALLABLE:
                key = "installer.install.errors.notManuallyInstallable";
                break;
            case INVALID_KPM_INFO_FILE:
                key = "installer.install.errors.invalidKpmInfo";
                break;
            default:
                return false;
        }

        this.terminal.error(LangProvider.get(
                key,
                MsgArgs.of("name", "%%general.plugin.specifiedPlugin%%")
        ));

        return true;
    }

    private void handlePluginResolveError(PluginResolveErrorCause cause)
    {
        String key;
        switch (cause)
        {
            /* This cause is handled in PluginResolveSignalHandler
            case CANCELLED:
                this.terminal.error("プラグイン解決がキャンセルされました。");
                break;*/
            case GOT_ERROR_RESULT:
                key = "tasks.resolve.errors.gotErrorResult";
                break;
            case ILLEGAL_INTERNAL_STATE:
                key = "tasks.resolve.errors.illegalInternalState";
                break;
            default:
                return;
        }

        this.terminal.error(LangProvider.get(key));
    }

    private void handleDownloadError(DownloadErrorCause cause)
    {
        String key;
        boolean named = false;
        switch (cause)
        {
            case ILLEGAL_HTTP_RESPONSE:
                key = "tasks.download.errors.illegalHttpResponse";
                break;
            case NO_BODY_IN_RESPONSE:
                key = "tasks.download.errors.noBodyInResponse";
                break;
            case IO_EXCEPTION:
                key = "tasks.download.errors.ioException";
                named = true;
                break;
            case UNKNOWN_ERROR:
                key = "tasks.download.errors.unknown";
                named = true;
                break;
            default:
                return;
        }

        if (named)
            this.terminal.error(LangProvider.get(
                    key,
                    MsgArgs.of("name", "%%tasks.download%%")
            ));
        else
            this.terminal.error(LangProvider.get(key));
    }

    private void handleDescriptionLoadError(DescriptionLoadErrorCause cause)
    {
        String key;
        boolean named = false;
        switch (cause)
        {
            case NOT_A_PLUGIN:
                key = "tasks.description.errors.notPlugin";
                break;
            case INVALID_DESCRIPTION:
                key = "tasks.description.errors.invalidDescription";
                break;
            case IO_EXCEPTION:
                key = "tasks.description.errors.ioException";
                break;
            default:
                return;
        }

        //noinspection ConstantValue
        if (named)
            this.terminal.error(LangProvider.get(
                    key,
                    MsgArgs.of("name", "%%tasks.description%%")
            ));
        else
            this.terminal.error(LangProvider.get(key));
    }

    @SuppressWarnings("SwitchStatementWithoutDefaultBranch")
    private void handleDependsCollectError(DependsCollectErrorCause cause)
    {
        if (cause == DependsCollectErrorCause.SOME_DEPENDENCIES_COLLECT_FAILED)
            this.terminal.error(LangProvider.get("tasks.deps.collect.errors.someCollectFailed"));
    }

    private void handleInstallError(PluginsInstallErrorCause cause)
    {
        String key;
        boolean named = false;
        switch (cause)
        {
            case RELOCATE_FAILED:
                key = "tasks.install.errors.relocateFailed";
                break;
            case INVALID_PLUGIN:
                key = "tasks.install.errors.invalidPlugin";
                break;
            case INVALID_PLUGIN_DESCRIPTION:
                key = "tasks.install.errors.invalidDescription";
                break;
            case INCOMPATIBLE_WITH_BUKKIT_VERSION:
                key = "tasks.install.errors.incompatible.bukkit";
                this.terminal.error(LangProvider.get(key));
                key = "tasks.install.errors.incompatible.bukkit.suggest";
                this.terminal.hint(LangProvider.get(key));
                return;
            case INCOMPATIBLE_WITH_KPM_VERSION:
                key = "tasks.install.errors.incompatible.kpm";
                break;
            case IO_EXCEPTION_OCCURRED:
                key = "tasks.install.errors.ioException";
                named = true;
                break;
            case EXCEPTION_OCCURRED:
                key = "tasks.install.errors.unknown";
                named = true;
                break;
            default:
                return;
        }

        if (named)
            this.terminal.error(LangProvider.get(
                    key,
                    MsgArgs.of("name", "%%tasks.install%%")
            ));
        else
            this.terminal.error(LangProvider.get(key));
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

        this.handleOtherError(result, INSTALLER_NAME);
    }
}
