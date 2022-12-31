package net.kunmc.lab.kpm.task.tasks.dependencies.computer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.kpm.interfaces.task.TaskArgument;
import net.kunmc.lab.kpm.interfaces.task.tasks.dependencies.DependencyElement;

import java.util.List;

/**
 * 依存関係の読み込み順序を計算する際に使用される引数を表します。
 */
@AllArgsConstructor
public class DependsComputeOrderArgument implements TaskArgument
{
    /**
     * 計算対象の依存関係のリストです。
     */
    @Getter
    private final List<DependencyElement> collectedDependencies;
}
