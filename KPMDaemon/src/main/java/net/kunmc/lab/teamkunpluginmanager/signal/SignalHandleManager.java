package net.kunmc.lab.teamkunpluginmanager.signal;

import net.kunmc.lab.teamkunpluginmanager.installer.InstallProgress;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * シグナルを受け取り、処理を行うクラスです。
 * シグナルは、{@link SignalHandler}アノテーションを付与したメソッドによって処理され、以下のシグニチャを持つ必要があります。
 * <pre>
 *     public void methodName({@link InstallProgress<>}, {@link Signal})
 * </pre>
 */
public class SignalHandleManager
{
    private final ArrayList<SignalHandlerList<? extends Signal>> handlerLists;

    public SignalHandleManager()
    {
        handlerLists = new ArrayList<>();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void invokeHandler(@NotNull InstallProgress<?, ?> installProgress, SignalHandlerList handler,
                                      Signal signal)
    {
        if (handler.isSignalType(signal.getClass()))
            handler.onSignal(installProgress, signal);
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Signal>> enumerateHandlers(Object object)
    {
        return Arrays.stream(object.getClass().getMethods()).parallel()
                .filter(method -> method.isAnnotationPresent(SignalHandler.class))
                .filter(method -> method.getParameterCount() == 1)
                .map(method -> method.getParameterTypes()[0])
                .filter(Signal.class::isAssignableFrom)
                .map(clazz -> (Class<? extends Signal>) clazz)
                .collect(Collectors.toList());
    }

    /**
     * ハンドラを登録します。
     *
     * @param object ハンドラを持つオブジェクト
     */
    public void register(Object object)
    {
        List<Class<? extends Signal>> handleTargetClasses = enumerateHandlers(object);

        for (SignalHandlerList<? extends Signal> handlerList : handlerLists)
        {
            handlerList.bakeAll(object);
            handleTargetClasses.removeIf(handlerList::isSignalType);
        }

        for (Class<? extends Signal> handleTargetClass : handleTargetClasses)
        {
            SignalHandlerList<? extends Signal> handlerList = new SignalHandlerList<>(handleTargetClass);
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
    public void handleSignal(@NotNull InstallProgress<?, ?> installProgress, Signal signal)
    {
        for (SignalHandlerList<? extends Signal> handlerList : handlerLists)
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