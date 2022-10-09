package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean.signal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * 不要なデータの削除中であることを示すシグナルです。
 */
@Data
public class GarbageDeletingSignal implements Signal
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
