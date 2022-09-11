package net.kunmc.lab.teamkunpluginmanager.commands.signal;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common.CheckEnvSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common.ModifySignalHandler;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall.DependenciesSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall.DownloadingSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall.ResolverSignalHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * シグナルを受け取り, 処理を行うクラス をまとめるためのユーティリティクラスです.
 */
public class HeadSignalHandlers
{
    private static List<Object> createHandlersList(List<Object> base, Object... handlers)
    {
        List<Object> list = new ArrayList<>(base);
        list.addAll(Arrays.asList(handlers));
        return list;
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
                new ModifySignalHandler(terminal)
        );
    }

    /**
     * インストールに使用するハンドラを返します.
     *
     * @param terminal ターミナル
     * @return インストールに使用するハンドラ
     */
    public static List<Object> getInstallHandlers(@NotNull Terminal terminal)
    {
        return createHandlersList(
                getCommonHandlers(terminal),
                new ResolverSignalHandler(terminal),
                new DownloadingSignalHandler(terminal),
                new CheckEnvSignalHandler(terminal),
                new DependenciesSignalHandler(terminal),
                new InstallerSignalHandler(terminal)
        );
    }
}
