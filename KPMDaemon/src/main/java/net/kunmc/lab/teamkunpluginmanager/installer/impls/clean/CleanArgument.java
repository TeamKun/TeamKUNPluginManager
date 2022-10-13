package net.kunmc.lab.teamkunpluginmanager.installer.impls.clean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstallerArgument;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 不要データ削除の引数を格納するクラスです。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CleanArgument extends AbstractInstallerArgument
{
    /**
     * 自動削除から除外するデータ名のリストです。
     */
    @NotNull
    private final List<String> excludeDataNames;

    private CleanArgument(@NotNull List<String> excludeDataNames)
    {
        this.excludeDataNames = excludeDataNames;
    }

    public CleanArgument()
    {
        this(new ArrayList<>());
    }

    /**
     * 除外するデータを追加します。
     *
     * @param dataName 除外するデータの名前
     * @return このインスタンス
     */
    public CleanArgument addExcludeDataName(@NotNull String dataName)
    {
        excludeDataNames.add(dataName);
        return this;
    }

}
