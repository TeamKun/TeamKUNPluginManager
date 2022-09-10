package net.kunmc.lab.teamkunpluginmanager.plugin.signal;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * シグナルを受け取り、処理を行うクラスです。
 * シグナルは、{@link SignalHandler}アノテーションを付与したメソッドによって処理され、以下のシグニチャを持つ必要があります。
 * <pre>
 *     public void methodName({@link InstallProgress<>}, {@link InstallerSignal})
 * </pre>
 */
public class SignalHandleManager
{
    private final ArrayList<SignalHandlerList<? extends InstallerSignal>> handlerLists;

    public SignalHandleManager()
    {
        handlerLists = new ArrayList<>();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void invokeHandler(@NotNull InstallProgress<?, ?> installProgress, SignalHandlerList handler,
                                      InstallerSignal signal)
    {
        if (handler.isSignalType(signal.getClass()))
            handler.onSignal(installProgress, signal);
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends InstallerSignal>> enumerateHandlers(Object object)
    {
        return Arrays.stream(object.getClass().getMethods()).parallel()
                .filter(method -> method.isAnnotationPresent(SignalHandler.class))
                .filter(method -> method.getParameterCount() == 2)
                .map(method -> method.getParameterTypes()[1])
                .filter(InstallerSignal.class::isAssignableFrom)
                .map(clazz -> (Class<? extends InstallerSignal>) clazz)
                .collect(Collectors.toList());
    }

    /**
     * ハンドラを登録します。
     *
     * @param object ハンドラを持つオブジェクト
     */
    public void register(Object object)
    {
        List<Class<? extends InstallerSignal>> handleTargetClasses = enumerateHandlers(object);

        for (SignalHandlerList<? extends InstallerSignal> handlerList : handlerLists)
        {
            handlerList.bakeAll(object);
            handleTargetClasses.removeIf(handlerList::isSignalType);
        }

        for (Class<? extends InstallerSignal> handleTargetClass : handleTargetClasses)
        {
            SignalHandlerList<? extends InstallerSignal> handlerList = new SignalHandlerList<>(handleTargetClass);
            handlerLists.add(handlerList);
            handlerList.bakeAll(object);
        }
    }

    /**
     * シグナルを受け取り、処理を行います。
     *
     * @param installProgress インストールの進捗
     * @param signal          シグナル
     */
    public void handleSignal(@NotNull InstallProgress<?, ?> installProgress, InstallerSignal signal)
    {
        for (SignalHandlerList<? extends InstallerSignal> handlerList : handlerLists)
            invokeHandler(installProgress, handlerList, signal);
    }

    /**
     * このインスタンスをコピーします。
     *
     * @return コピーされたインスタンス
     */
    public SignalHandleManager copy()
    {
        SignalHandleManager manager = new SignalHandleManager();
        manager.handlerLists.addAll(handlerLists);
        return manager;
    }
}
