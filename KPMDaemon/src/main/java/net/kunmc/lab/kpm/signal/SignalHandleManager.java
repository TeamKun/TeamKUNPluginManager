package net.kunmc.lab.kpm.signal;

import net.kunmc.lab.kpm.installer.InstallProgress;
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
        this.handlerLists = new ArrayList<>();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void invokeHandler(@NotNull SignalHandlerList handler,
                                      @NotNull Signal signal)
    {
        if (handler.isSignalType(signal.getClass()))
            handler.onSignal(signal);
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

        for (SignalHandlerList<? extends Signal> handlerList : this.handlerLists)
        {
            handlerList.bakeAll(object);
            handleTargetClasses.removeIf(handlerList::isSignalType);
        }

        for (Class<? extends Signal> handleTargetClass : handleTargetClasses)
        {
            SignalHandlerList<? extends Signal> handlerList = new SignalHandlerList<>(handleTargetClass);
            this.handlerLists.add(handlerList);
            handlerList.bakeAll(object);
        }
    }

    /**
     * シグナルを受け取り、処理を行います。
     *
     * @param signal シグナル
     */
    public void handleSignal(@NotNull Signal signal)
    {
        for (SignalHandlerList<? extends Signal> handlerList : this.handlerLists)
        {
            invokeHandler(handlerList, signal);
            if (signal.isHandled())
                break;
        }
    }

    /**
     * このインスタンスをコピーします。
     *
     * @return コピーされたインスタンス
     */
    public SignalHandleManager copy()
    {
        SignalHandleManager manager = new SignalHandleManager();
        manager.handlerLists.addAll(this.handlerLists);
        return manager;
    }
}
