package org.kunlab.kpm.signal;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.signal.handlers.autoremove.AutoRemoveFinishedSignalHandler;
import org.kunlab.kpm.signal.handlers.autoremove.AutoRemoveReadySignalHandler;
import org.kunlab.kpm.signal.handlers.clean.GarbageCleanFinishedSignalHandler;
import org.kunlab.kpm.signal.handlers.clean.GarbageCleanSignalHandler;
import org.kunlab.kpm.signal.handlers.common.CheckEnvSignalHandler;
import org.kunlab.kpm.signal.handlers.common.DownloadingSignalHandler;
import org.kunlab.kpm.signal.handlers.common.ModifySignalHandler;
import org.kunlab.kpm.signal.handlers.intall.DependenciesSignalHandler;
import org.kunlab.kpm.signal.handlers.intall.InstallFinishedSignalHandler;
import org.kunlab.kpm.signal.handlers.intall.InstallerSignalHandler;
import org.kunlab.kpm.signal.handlers.intall.ResolverSignalHandler;
import org.kunlab.kpm.signal.handlers.kpmupgrade.KPMUpgradeSignalHandler;
import org.kunlab.kpm.signal.handlers.register.TokenGenerateSignalHandler;
import org.kunlab.kpm.signal.handlers.register.TokenRegisterSignalHandler;
import org.kunlab.kpm.signal.handlers.uninstall.PluginIsDependencySignalHandler;
import org.kunlab.kpm.signal.handlers.uninstall.UninstallFinishedSignalHandler;
import org.kunlab.kpm.signal.handlers.uninstall.UninstallReadySignalHandler;
import org.kunlab.kpm.signal.handlers.uninstall.UninstallerSignalHandler;
import org.kunlab.kpm.signal.handlers.update.UpdateAliasesSignalHandler;
import org.kunlab.kpm.signal.handlers.upgrade.UpgradeFinishedSignalHandler;
import org.kunlab.kpm.signal.handlers.upgrade.UpgradeSignalHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HeadSignalHandlers
{
    private static List<Object> createHandlersList(List<Object> base, Object... handlers)
    {
        List<Object> list = new ArrayList<>(base);
        list.removeIf(Objects::isNull);
        list.addAll(Arrays.asList(handlers));
        return list;
    }

    @SafeVarargs
    private static List<Object> createHandlersList(List<Object>... lists)
    {
        return Arrays.stream(lists)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<Object> createHandlersList(Object... handlers)
    {
        return Arrays.asList(handlers);
    }

    /**
     * 共通ハンドラを返します.
     *
     * @param terminal ターミナル
     * @return 共通ハンドラ
     */
    public static List<Object> getCommonHandlers(@NotNull Terminal terminal)
    {
        return createHandlersList(
                new CheckEnvSignalHandler(terminal),
                new DownloadingSignalHandler(terminal),
                new ModifySignalHandler(terminal)
        );
    }

    public static List<Object> getInstallHandlers(@NotNull KPMRegistry registry, @NotNull Terminal terminal, boolean handleFinish)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                new ResolverSignalHandler(registry, terminal),
                new DownloadingSignalHandler(terminal),
                new CheckEnvSignalHandler(terminal),
                new DependenciesSignalHandler(terminal),
                new InstallerSignalHandler(terminal),
                handleFinish ? new InstallFinishedSignalHandler(terminal): null
        );
    }

    public static List<Object> getInstallHandlers(@NotNull KPMRegistry registry, @NotNull Terminal terminal)
    {
        return getInstallHandlers(registry, terminal, true);
    }

    public static List<Object> getUninstallHandlers(@NotNull KPMRegistry registry,
                                                    @NotNull Terminal terminal,
                                                    boolean handleFinish)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                new UninstallerSignalHandler(terminal),
                new PluginIsDependencySignalHandler(registry, terminal),
                new UninstallReadySignalHandler(terminal),
                handleFinish ? new UninstallFinishedSignalHandler(terminal): null
        );
    }

    public static List<Object> getUninstallHandlers(@NotNull KPMRegistry registry, @NotNull Terminal terminal)
    {
        return getUninstallHandlers(registry, terminal, true);
    }

    public static List<Object> getUpdateHandlers(@NotNull Terminal terminal)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                new UpdateAliasesSignalHandler(terminal)
        );
    }

    public static List<Object> getAutoRemoveHandlers(@NotNull Terminal terminal)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                new UninstallerSignalHandler(terminal),
                new AutoRemoveFinishedSignalHandler(terminal),
                new UninstallFinishedSignalHandler(terminal),
                new AutoRemoveReadySignalHandler(terminal)
        );
    }

    public static List<Object> getGarbageCleanHandlers(Terminal terminal)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                new GarbageCleanFinishedSignalHandler(terminal),
                new GarbageCleanSignalHandler(terminal)
        );
    }

    public static List<Object> getTokenRegistererHandlers(Terminal terminal)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                new TokenRegisterSignalHandler(terminal),
                new TokenGenerateSignalHandler(terminal)
        );
    }

    public static List<Object> getUpgraderHandlers(@NotNull KPMRegistry registry, @NotNull Terminal terminal, boolean isAuto)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                Arrays.asList(
                        new UpgradeSignalHandler(terminal, isAuto),
                        new UpgradeFinishedSignalHandler(terminal),
                        new UninstallerSignalHandler(terminal)
                ),
                getInstallHandlers(registry, terminal, false)
        );
    }

    public static List<Object> getKPMUpgraderHandlers(KPMRegistry registry, Terminal terminal)
    {
        return createHandlersList(
                new ResolverSignalHandler(registry, terminal),
                new KPMUpgradeSignalHandler(terminal)
        );
    }
}
