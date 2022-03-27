package net.kunmc.lab.teamkunpluginmanager.terminal.framework;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * データの入力を行うインターフェース
 */
public interface Input
{
    /**
     * ターミナルのインスタンスを取得します。
     */
    @NotNull
    Terminal getTerminal();

    /**
     * Y/N(yes/no)で回答できる質問を表示します。
     *
     * @param question 質問内容
     * @return 回答
     */
    @NotNull
    InputTask showYNQuestion(@NotNull String question);

    /**
     * キャンセル可能な、Y/N(yes/no)で回答できる質問を表示します。
     * また、キャンセルされた場合は値に{@code null}が収納されます。
     *
     * @param question 質問内容
     * @return 回答
     */
    @NotNull
    InputTask showYNQuestionCancellable(@NotNull String question);

    /**
     * 自由入力で回答できる質問を表示します。
     *
     * @param question 質問内容
     * @return 回答
     */
    @NotNull
    InputTask showInputQuestion(@NotNull String question);

    /**
     * 選択式の質問を表示します。
     *
     * @param question 質問
     * @param choices  選択肢
     * @return 回答
     */
    @NotNull
    InputTask showChoiceQuestion(@NotNull String question, String... choices);

    /**
     * 選択式の質問を表示します。
     *
     * @param question 質問
     * @param choices  選択肢
     * @return 回答
     */
    @NotNull
    InputTask showChoiceQuestion(@NotNull String question, @NotNull HashMap<String, String> choices);

    /**
     * 質問募集をキャンセルします。
     */
    void cancelQuestion(InputTask task);
}
