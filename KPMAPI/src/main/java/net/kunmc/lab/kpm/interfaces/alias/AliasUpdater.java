package net.kunmc.lab.kpm.interfaces.alias;

/**
 * エイリアスを更新するトランザクションを補助するクラスです。
 */
public interface AliasUpdater
{
    /**
     * エイリアスのアップデートを行います。
     *
     * @param alias エイリアス
     * @param query クエリ
     */
    void update(String alias, String query);

    /**
     * すべてのアップデートを終了し、データベースの更新を行います。
     */
    void done();

    /**
     * アップデートをキャンセルし、データベースをロールバックします。
     */
    void cancel();

    long getAliasesCount();
}
