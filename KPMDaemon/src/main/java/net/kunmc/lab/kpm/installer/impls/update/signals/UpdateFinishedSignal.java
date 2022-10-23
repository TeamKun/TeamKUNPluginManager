package net.kunmc.lab.kpm.installer.impls.update.signals;

import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;

import java.util.HashMap;

/**
 * エイリアスのアップデートが完了したことを示すシグナルです。
 */
@Value
public class UpdateFinishedSignal implements Signal
{
    /**
     * エイリアスの数です。
     */
    long aliases;
    /**
     * エイリアスとソースのペアの数です。
     */
    HashMap<String, Long> aliasesBySources;
}
