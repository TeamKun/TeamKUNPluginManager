package net.kunmc.lab.kpm.interfaces.hook;

import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.kpminfo.InvalidInformationFileException;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * KPMフックの受け取りを行うクラスを管理するクラスです。
 */
public interface HookRecipientList extends Collection<KPMHookRecipient>, List<KPMHookRecipient>, RandomAccess, Cloneable, Serializable
{
    /**
     * フックを実行します。
     *
     * @param hook フック
     */
    void runHook(KPMHook hook);

    void add(@NotNull String className);

    /**
     * 予約クラス名からフックを作成します。
     *
     * @param registry KPM レジストリのインスタンス
     * @throws InvalidInformationFileException 予約クラス名が無効な場合
     */
    void bakeHooks(@NotNull KPMRegistry registry) throws InvalidInformationFileException;

    KPMRegistry getRegistry();
}
