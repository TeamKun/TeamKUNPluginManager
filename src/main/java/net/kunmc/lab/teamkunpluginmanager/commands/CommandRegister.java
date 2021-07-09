package net.kunmc.lab.teamkunpluginmanager.commands;

import com.destroystokyo.paper.Title;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.Say2Functional;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CommandRegister
{
    private final static String CLIENT_ID = "94c5d446dbc765895979";

    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.register"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (args.length < 1 && sender instanceof BlockCommandSender)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm register <Token>");
            return;
        }

        if (!TeamKunPluginManager.session.lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        if (args.length == 1)
        {
            TeamKunPluginManager.vault.vault(args[0]);
            sender.sendMessage(ChatColor.GREEN + "S: トークンを正常に保管しました！");
            TeamKunPluginManager.session.unlock();
            return;
        }


        sender.sendMessage(ChatColor.GREEN + "GitHubサーバを用いてトークンを生成しますか? y/N> ");
        TeamKunPluginManager.functional.add(sender instanceof Player ? ((Player) sender).getUniqueId(): null, new Say2Functional.FunctionalEntry(
                String::startsWith,
                s -> {
                    if (!s.equalsIgnoreCase("y"))
                        return;
                    performAction(sender);
                }
        ));
    }

    private static void performAction(CommandSender sender)
    {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "サーバと通信しています...");

        Pair<Integer, String> data = URLUtils.postAsString("https://github.com/login/device/code?client_id=" + CLIENT_ID + "&scope=repo%2Cpublic_repo",
                "", "application/json", "text/plain"
        );

        if (data.getKey() != 200)
        {
            sender.sendMessage(ChatColor.RED + "E: エラーが発生しました。しばらくしてからもう一度お試しください。");
            return;
        }

        JsonObject object = new Gson().fromJson(data.getValue(), JsonObject.class);

        final String device_code = object.get("device_code").getAsString();
        final String user_code = object.get("user_code").getAsString();
        final String verif_uri = object.get("verification_uri").getAsString();
        final int expires_sec = object.get("expires_in").getAsInt();
        final int get_interval = object.get("interval").getAsInt();

        sender.sendMessage(ChatColor.DARK_GREEN + "入力するコード：" + ChatColor.WHITE + user_code);
        sender.sendMessage(ChatColor.DARK_GREEN + "こちらからコードを有効化してください：" + ChatColor.BLUE + ChatColor.UNDERLINE + verif_uri);
        sender.sendMessage(ChatColor.DARK_GRAY + "I：コードは、" + (expires_sec / 60) + "分で失効します。");
        if (sender instanceof Player)
            showTitle((Player) sender, user_code, expires_sec);
        //ここからポーリング

        final boolean[] success = {false};
        BukkitRunnable polling = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Pair<Integer, String> data = URLUtils.postAsString(
                        "https://github.com/login/oauth/access_token?client_id=" + CLIENT_ID +
                                "&grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code&device_code=" + device_code,
                        "",
                        "application/json",
                        "text/plain"
                );
                if (data.getKey() != 200)
                {
                    sender.sendMessage(ChatColor.RED + "E: エラーが発生しました。: Server response with" + data.getKey());
                    this.cancel();
                    return;
                }

                JsonObject response = new Gson().fromJson(data.getValue(), JsonObject.class);

                if (response.has("error"))
                {
                    if (response.get("error").getAsString().equals("authorization_pending"))
                        return;
                    String error = response.get("error").getAsString();
                    sender.sendMessage(ChatColor.RED + "E: エラーが発生いたしました。：" + parseError(error));
                    if (sender instanceof Player)
                        ((Player) sender).resetTitle();
                    this.cancel();
                    return;
                }

                TeamKunPluginManager.vault.vault(response.get("access_token").getAsString());
                sender.sendMessage(ChatColor.GREEN + "S: トークンを正常に保管しました！");
                if (sender instanceof Player)
                    ((Player) sender).resetTitle();
                TeamKunPluginManager.session.unlock();
                success[0] = true;
                this.cancel();
            }
        };
        BukkitTask task = polling.runTaskTimer(TeamKunPluginManager.plugin, 100L, (get_interval * 20L) + 20L);

        BukkitRunnable expire = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!success[0])
                    return;
                task.cancel();
            }
        };
        expire.runTaskLater(TeamKunPluginManager.plugin, get_interval * 20L);
    }

    private static String parseError(String err)
    {
        String response = err;

        switch (err)
        {
            case "expired_token":
            case "incorrect_device_code":
                response = "有効期限が切れています。もう一度お試しください。";
                break;
            case "access_denied":
                response = "キャンセルされました。";
                break;
        }

        return response;
    }

    private static void showTitle(Player player, String code, int expires)
    {
        player.sendTitle(Title.builder()
                .title(code)
                .subtitle("上記のコードを入力してください。")
                .fadeIn(10)
                .fadeOut(10)
                .stay(expires * 20)
                .build()
        );
    }
}
