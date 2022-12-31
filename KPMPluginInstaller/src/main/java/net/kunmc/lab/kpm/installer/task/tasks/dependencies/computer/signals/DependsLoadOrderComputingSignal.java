package net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * 依存関係の読み込み順序を計算する際に発生するシグナルを表します。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DependsLoadOrderComputingSignal extends Signal
{
    /**
     * 指定された依存関係のリストです。
     */
    @NotNull
    private final List<DependencyElement> dependencies;

    /**
     * 順序の計算前に発生するシグナルです。
     * 注意： {@link #getDependencies()} から取得される依存関係リストは、{@link Collections#unmodifiableList(List)} でラップされています。
     */
    public static class Pre extends DependsLoadOrderComputingSignal
    {
        public Pre(final List<DependencyElement> dependencies)
        {
            super(Collections.unmodifiableList(dependencies));
        }
    }

    /**
     * 順序の計算に成功したことを示すシグナルです。
     * 計算結果を書き換えることで、読み込み順序を変更できます。
     */
    public static class Post extends DependsLoadOrderComputingSignal
    {
        public Post(final List<DependencyElement> dependencies)
        {
            super(dependencies);
        }
    }
}
