package net.kunmc.lab.teamkunpluginmanager.resolver.result;

/**
 * 紹介文付きのリソースの解決結果を格納。
 */
public interface MarketplaceResult
{
    /**
     * 掲載されているタイトル/名前
     * @return 掲載されているタイトル/名前
     */
    String getTitle();

    /**
     * 掲載されているURL
     * @return 掲載されているURL
     */
    String getUrl();

    /**
     * 掲載されている紹介文
     * @return 掲載されている紹介文
     */
    String getDescription();
}
