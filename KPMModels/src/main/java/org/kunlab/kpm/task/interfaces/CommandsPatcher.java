package org.kunlab.kpm.task.interfaces;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface CommandsPatcher {
    /**
     * CommandMapを取得します。
     *
     * @return CommandMap
     */
    CommandMap getCommandMap();

    /**
     * 知られているコマンドを取得します。
     *
     * @return 知られているコマンド
     */
    Map<String, Command> getKnownCommands();

    /**
     * コマンドをラップします。
     *
     * @param command ラップするコマンド
     * @param alias   コマンドのエイリアス
     */
    void wrapCommand(Command command, String alias);

    /**
     * CraftServerのsyncCommandsを呼び出す。
     */
    void syncCommandsCraftBukkit();

    /**
     * コマンドをアンラップします。
     *
     * @param command アンラップするコマンド
     */
    void unWrapCommand(String command);

    /**
     * コマンドをパッチします。
     *
     * @param plugin       プラグイン
     * @param updatePlayer プレイヤーにコマンドの変更を通知するか({@link Player#updateCommands()})
     */
    void patchCommand(@NotNull Plugin plugin, boolean updatePlayer);

    /**
     * コマンドをパッチします。
     *
     * @param plugin パッチするプラグイン
     */
    void patchCommand(@NotNull Plugin plugin);

    /**
     * コマンドをアンパッチします。
     *
     * @param plugin       アンパッチするプラグイン
     * @param updatePlayer プレイヤーにコマンドの変更を通知するか({@link Player#updateCommands()})
     */
    void unPatchCommand(@NotNull Plugin plugin, boolean updatePlayer);

    /**
     * コマンドをアンパッチします。
     *
     * @param plugin プラグイン
     */
    void unPatchCommand(@NotNull Plugin plugin);

    /**
     * コマンドをすべて登録します。
     *
     * @param fallbackPrefix フォールバックのプレフィックス
     * @param commands       コマンド
     */
    void registerAll(String fallbackPrefix, List<Command> commands);
}
