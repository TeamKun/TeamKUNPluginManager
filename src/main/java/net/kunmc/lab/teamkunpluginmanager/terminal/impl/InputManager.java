package net.kunmc.lab.teamkunpluginmanager.terminal.impl;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.terminal.framework.InputTask;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 入力のマネージャです。
 */
public class InputManager implements Listener
{
    @Getter
    private static final InputManager instance;

    static
    {
        instance = new InputManager();
    }

    private final Map<UUID, ArrayList<InputTask>> inputTasks;

    private InputManager()
    {
        inputTasks = new HashMap<>();
    }

    /**
     * このマネージャをBukkitに登録します。
     *
     * @param plugin 登録するプラグイン
     */
    public static void register(Plugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(instance, plugin);
    }

    /**
     * 入力タスクを追加します。
     *
     * @param uuid      登録するプレイヤーのUUID。Nullの場合はコンソールからの入力を受け付け
     * @param inputTask 入力タスク
     */
    public void addInputTask(@Nullable UUID uuid, InputTask inputTask)
    {
        ArrayList<InputTask> inputTasks = this.inputTasks.get(uuid);
        if (inputTasks == null)
            inputTasks = new ArrayList<>();
        inputTasks.add(inputTask);

        if (inputTasks.size() == 1)
            inputTask.printQuestion();
        this.inputTasks.put(uuid, inputTasks);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSay(AsyncChatEvent event)
    {
        ArrayList<InputTask> inputTasks = this.inputTasks.get(event.getPlayer().getUniqueId());
        String message = ((TextComponent) event.originalMessage().asComponent()).content();

        if (inputTasks == null || inputTasks.isEmpty())
            return;

        event.setCancelled(true);

        InputTask inputTask = inputTasks.get(0);

        if (!inputTask.checkValidInput(message))
        {
            inputTask.getInput().getTerminal().error("入力が無効です：" + message);
            return;
        }

        inputTask.setValue(message);
        inputTasks.remove(0);
        if (!inputTasks.isEmpty())
            inputTasks.get(0).printQuestion();
    }

    @EventHandler
    public void onConsoleSay(ServerCommandEvent e)
    {
        ArrayList<InputTask> inputTasks = this.inputTasks.get(null);

        e.setCancelled(true);

        if (inputTasks == null || inputTasks.isEmpty())
            return;
        InputTask inputTask = inputTasks.get(0);
        String message = e.getCommand();

        if (!inputTask.checkValidInput(message))
        {
            inputTask.getInput().getTerminal().error("入力が無効です：" + message);
            return;
        }

        inputTask.setValue(message);
        inputTasks.remove(0);
        if (!inputTasks.isEmpty())
            inputTasks.get(0).printQuestion();
    }

    /**
     * 指定したプレイヤの入力タスクをすべて削除します。
     *
     * @param uuid 削除するプレイヤのUUID
     */
    public void cancelInputTask(UUID uuid)
    {
        ArrayList<InputTask> inputTasks = this.inputTasks.get(uuid);
        if (inputTasks == null)
            return;
        inputTasks.clear();
    }

    /**
     * 指定したプレイヤの入力タスクを削除します。
     *
     * @param uuid      削除するプレイヤのUUID
     * @param inputTask 削除する入力タスク
     */
    public void cancelInputTask(UUID uuid, InputTask inputTask)
    {
        ArrayList<InputTask> inputTasks = this.inputTasks.get(uuid);
        if (inputTasks == null)
            return;
        inputTasks.remove(inputTask);
    }
}
