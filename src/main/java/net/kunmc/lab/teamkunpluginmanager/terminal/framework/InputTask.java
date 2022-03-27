package net.kunmc.lab.teamkunpluginmanager.terminal.framework;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface InputTask
{
    /**
     * 入力値を設定します。
     * ブロッキングされたスレッドは解放されます。
     *
     * @param value 入力値
     */
    void setValue(@NotNull String value);

    /**
     * ブロッキングして入力値を取得します。
     *
     * @return 入力値
     * @throws InterruptedException 入力値が取得できなかった場合/スレッドが殺された場合
     */
    @NotNull String waitAndGetValue() throws InterruptedException;

    /**
     * 回答を得られるまでブロッキングします。
     */
    void waitForAnswer() throws InterruptedException;

    /**
     * ブロッキングせずに入力値を取得します。
     *
     * @return 入力値
     */
    @Nullable String getRawValue();

    /**
     * 入力値が取得できるかどうかを返します。
     *
     * @return 入力値が取得できるかどうか
     */
    boolean isValueAvailable();

    /**
     * 入力をキャンセルしまします。
     */
    default void cancel()
    {
        this.getInput().cancelQuestion(this);
    }

    /**
     * 質問のUUIDを返します。
     *
     * @return 質問のUUID
     */
    @NotNull UUID getUuid();

    /**
     * 質問の体操者を返します。
     *
     * @return 質問の体操者
     */
    @Nullable UUID getTarget();

    /**
     * 質問で有効な入力値かどうかを返します。
     *
     * @return 質問で有効な入力値かどうか
     */
    boolean checkValidInput(String input);

    /**
     * このタスクを作成したInputを返します。
     *
     * @return このタスクを作成したInput
     */
    @NotNull Input getInput();

    /**
     * 質問を出力します。
     * 実装時は {@link Terminal#info(String)} を使用することが望ましいです。
     * また、{@link InputTask#getChoices()} が内部で呼び出されます。
     */
    void printQuestion();

    /**
     * 質問の選択肢を取得します。
     * Mapの鍵はクリック時に自動入力される値で、値は表示される値です。
     * デフォルトでは {@link org.bukkit.ChatColor#GREEN} 色で表示されます。
     */
    Map<String, String> getChoices();
}
