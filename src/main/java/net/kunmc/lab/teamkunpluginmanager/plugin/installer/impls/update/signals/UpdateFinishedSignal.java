package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.update.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;

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
