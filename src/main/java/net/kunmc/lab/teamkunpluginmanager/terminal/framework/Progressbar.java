package net.kunmc.lab.teamkunpluginmanager.terminal.framework;

import org.jetbrains.annotations.Nullable;

public interface Progressbar
{

    /**
     * プログレスバーの最大値を設定します。
     *
     * @param max 最大値
     */
    void setProgressMax(int max);

    /**
     * プログレスバーの現在の値を設定します。
     *
     * @param progress 現在の値
     */
    void setProgress(int progress);

    /**
     * プログレスバーの接頭辞を変更します。
     *
     * @param prefix 接頭辞
     */
    void setPrefix(@Nullable String prefix);

    /**
     * プログレスバーの接尾辞を変更します。
     *
     * @param suffix 接尾辞
     */
    void setSuffix(@Nullable String suffix);

    /**
     * プログレスバーの大きさを変更します。
     *
     * @param size 大きさ
     */
    void setSize(int size);

    /**
     * プログレスバーを表示します。
     * 更新の場合は {@link #update()} を呼び出してください。
     */
    void show();

    /**
     * プログレスバーを非表示にします。
     */
    void hide();

    /**
     * プログレスバーを更新します。
     */
    void update();
}
