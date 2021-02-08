package net.kunmc.lab.teamkunpluginmanager.console.commands.stracture;

public interface CommandBase
{
    /**
     * 名前を返す
     * @return 名前
     */
    String getName();

    /**
     * エイリアスを一覧取得
     * @return エイリアス
     */
    String[] getAliases();

    /**
     * 実行する
     * @param args 引数
     * @return 終了コード
     */
    int run(String... args);

    /**
     * 使い方を表示する
     */
    void printHelp();

}
