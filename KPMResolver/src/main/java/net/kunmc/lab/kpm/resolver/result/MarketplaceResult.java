package net.kunmc.lab.kpm.resolver.result;

import org.jetbrains.annotations.NotNull;

/**
 * プラグインの説明等が掲載されいる場合に返されるクラスです。
 */
public interface MarketplaceResult
{
    /**
     * 掲載されているタイトル/名前を返します。
     *
     * @return 掲載されているタイトル/名前
     */
    @NotNull
    String getTitle();

    /**
     * 掲載先のURLを返します．
     *
     * @return 掲載先のURL
     */
    @NotNull
    String getUrl();

    /**
     * 掲載されている紹介文を返します。
     *
     * @return 掲載されている紹介文
     */
    @NotNull
    String getDescription();
}
