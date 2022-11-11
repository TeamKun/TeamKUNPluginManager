package net.kunmc.lab.kpm.installer.impls.clean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.installer.AbstractInstallerArgument;
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

    public CleanArgument(List<String> excludeDataNames)
    {
        this.excludeDataNames = new ArrayList<>(excludeDataNames);
    }

    public CleanArgument()
    {
        this.excludeDataNames = new ArrayList<>();
        this.excludeDataNames.add("bStats");
    }

    /**
     * 除外するデータを追加します。
     *
     * @param dataName 除外するデータの名前
     * @return このインスタンス
     */
    public CleanArgument addExcludeDataName(@NotNull String dataName)
    {
        this.excludeDataNames.add(dataName);
        return this;
    }

}
