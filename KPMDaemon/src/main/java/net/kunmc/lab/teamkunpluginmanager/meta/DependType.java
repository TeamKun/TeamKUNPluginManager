package net.kunmc.lab.teamkunpluginmanager.meta;

/**
 * 依存の種類です。
 */
public enum DependType
{
    /**
     * ハードな依存です。
     * 依存先のプラグインが存在しない場合、依存元のプラグインはロードされません。
     */
    HARD_DEPEND,
    /**
     * ソフトな依存です。
     * 依存先のプラグインが存在しない場合でも、依存元のプラグインはロードされます。
     */
    SOFT_DEPEND,
    /**
     * 自プラグインのロード前にロードする必要がある依存です。
     */
    LOAD_BEFORE
}
