package net.kunmc.lab.kpm.installer.task.tasks.garbage.clean.signal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * 不要なデータの削除中であることを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GarbageDeletingSignal extends Signal
{
    /**
     * 不要なデータのパスです。
     */
    @NotNull
    private final Path garbageData;

    /**
     * 削除前に呼ばれるシグナルです。
     */
    public static class Pre extends GarbageDeletingSignal
    {
        /**
         * 削除をスキップするかどうかです。
         */
        @Getter
        @Setter
        private boolean skip;

        public Pre(Path garbageData)
        {
            super(garbageData);
            this.skip = false;
        }
    }

    /**
     * 削除後に呼ばれるシグナルです。
     */
    public static class Post extends GarbageDeletingSignal
    {
        public Post(Path garbageData)
        {
            super(garbageData);
        }
    }
}
