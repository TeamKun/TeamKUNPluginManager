package net.kunmc.lab.teamkunpluginmanager.plugin;

import com.g00fy2.versioncompare.Version;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.HashLib;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.Say2Functional;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class Installer
{

    /**
     * アンインストールをする
     *
     * @param sender 発行者(Nullable)
     * @param name   対象プラグ
     * @param force  強制削除かどうか
     */
    public static void unInstall(CommandSender sender, String name, boolean force)
    {
        //senderがnullだった場合はダミーと差し替え
        if (sender == null)
            sender = dummySender(); //ぬるぽ抑制

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");
        DependencyTree.Info info = DependencyTree.getInfo(name, false);

        //プラグインが見つからない場合はreturn
        if (info == null)
        {
            sender.sendMessage(ChatColor.RED + "E: プラグインが見つかりませんでした。");
            return;
        }

        //プラグインを取得。
        Plugin plugin = Bukkit.getPluginManager().getPlugin(info.name);

        //アンインストール対象が存在するかチェック
        if (!PluginUtil.isPluginLoaded(info.name))
        {
            sender.sendMessage(ChatColor.RED + "E: プラグインが見つかりませんでした。");
            return;
        }

        //保護されているプラグインの場合は削除せずreturn
        if (TeamKunPluginManager.getPlugin().getPluginConfig().
                getStringList("ignore").stream().anyMatch(s -> s.equalsIgnoreCase(info.name)))
        {
            sender.sendMessage(ChatColor.YELLOW + "W: このプラグインは保護されています。\n" +
                    ChatColor.YELLOW + "   保護されているプラグインを削除すると、サーバの動作に支障を来す可能性がございます。");
            sender.sendMessage(ChatColor.RED + "E: システムが保護されました。");
            return;
        }

        //他のプラグインの依存関係になっている場合はreturn
        // 強制アンインストールである場合は無視
        if (!info.rdepends.isEmpty() && !force)
        {
            sender.sendMessage(ChatColor.YELLOW + "W: このプラグインは以下のプラグインの依存関係です。");
            sender.sendMessage(ChatColor.BLUE + info.rdepends.stream().parallel().map(depend -> depend.depend).collect(Collectors.joining(" ")));
            sender.sendMessage(ChatColor.YELLOW + "    依存関係になっているプラグインを削除すると、サーバの動作に支障を来す可能性がございます。");
            sender.sendMessage(ChatColor.RED + "E: システムが保護されました。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "プラグインを削除中...");

        //プラグインをアンロード
        PluginUtil.unload(plugin);

        CommandSender finalSender = sender;
        //非同期実行
        Runner.runLaterAsync(() -> {
            //プラグインのファイルを取得
            File file = PluginUtil.getFile(plugin);
            //ファイルが有った場合は削除
            if (file != null)
                file.delete();

            //依存関係ツリーをワイプする
            DependencyTree.wipePlugin(plugin);
            finalSender.sendMessage(ChatColor.RED + "- " + plugin.getName() + ":" + plugin.getDescription().getVersion());

            // エラーが有っる場合は表示
            String statusError = Messages.getErrorMessage();
            if (!statusError.isEmpty())
                finalSender.sendMessage(statusError);

            // 削除できるプラグイン(使われない依存関係等)があれば通知
            String autoRemovable = Messages.getUnInstallableMessage();

            if (!autoRemovable.isEmpty())
                finalSender.sendMessage(autoRemovable);
            finalSender.sendMessage(Messages.getStatusMessage(0, 1, 0));
            finalSender.sendMessage(ChatColor.GREEN + "S: " + plugin.getName() + ":" + plugin.getDescription().getVersion() + " を正常にアンインストールしました。");

        }, 20L);
    }

    /**
     * APIからのエラーjsonを取得
     *
     * @param json {@code message}項を含んだjson
     * @return メッセージ
     */
    private static String error(String json)
    {
        try
        {
            // gsonを用いて変換
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            if (!jsonObject.has("message"))
                return "";
            return jsonObject.get("message").getAsString();
        }
        catch (Exception ignored)
        {
            return "";
        }
    }

    /**
     * URlからぶちこむ！
     *
     * @param sender                結果を表示する対象の人
     * @param url                   URL!!!
     * @param ignoreInstall         インストールを除外するかどうか
     * @param withoutResolveDepends 依存関係解決をしない
     * @param withoutRemove         削除しない
     * @param withoutDownload       ダウンロードしない
     * @return ファイル名, プラグイン名
     */
    public static InstallResult install(CommandSender sender, String url, boolean ignoreInstall, boolean withoutResolveDepends, boolean withoutRemove, boolean withoutDownload)
    {
        //senderがnullだった場合はダミーと差し替え
        if (sender == null)
            sender = dummySender(); //ぬるぽ抑制

        //jarのURL
        String jarURL = url;
        //finalしないと非同期できない。
        CommandSender finalSender = sender;
        //追加されたプラグインとステータス。
        ArrayList<InstallResult> added = new ArrayList<>();

        //追加数
        int add = 0;
        //削除数
        int remove = 0;
        //変更数(アップデート等)
        int modify = 0;

        //ダウンロードする場合はURL・クエリを直リンに変換
        if (!withoutDownload)
        {
            ResolveResult resolveResult = TeamKunPluginManager.getPlugin().getResolver().resolve(url);

            if (resolveResult instanceof ErrorResult)
            {
                ErrorResult errorResult = (ErrorResult) resolveResult;

                sender.sendMessage(ChatColor.RED + "E: " + errorResult.getCause().getMessage() +
                        " リゾルバ：" + errorResult.getSource().getName());

                finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
                return new InstallResult(add, remove, modify, false);
            }
            else if (resolveResult instanceof MultiResult)
            {
                MultiResult multiResult = (MultiResult) resolveResult;

                try
                {
                    if (sender.getName().equals("DUMMY1145141919810931"))  // TODO: なんやねんこのクソコード
                    {
                        resolveResult = multiResult.getResolver().autoPickOnePlugin(multiResult);
                        if (resolveResult instanceof ErrorResult)
                        {
                            ErrorResult errorResult = (ErrorResult) resolveResult;

                            sender.sendMessage(ChatColor.RED + "E: " + errorResult.getCause().getMessage() +
                                    " リゾルバ：" + errorResult.getSource().getName());

                            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
                            return new InstallResult(add, remove, modify, false);
                        }
                        else if (resolveResult instanceof SuccessResult)
                            jarURL = ((SuccessResult) resolveResult).getDownloadUrl();
                        else
                            throw new IllegalStateException("resolveResultが不正です：プラグイン作成者に報告してください。");
                    }
                    else
                    {
                        List<Pair<String, String>> multi = Arrays.stream(multiResult.getResults())
                                .filter(result -> result instanceof SuccessResult)
                                .map(result -> {
                                    SuccessResult successResult = (SuccessResult) result;
                                    return new Pair<>(successResult.getVersion(), successResult.getDownloadUrl());
                                }).collect(Collectors.toList());

                        depend_askToCommandSender(sender, multi, ignoreInstall, withoutResolveDepends, withoutRemove);
                        return new InstallResult(add, remove, modify, false);
                    }
                }
                catch (IllegalArgumentException ignored)
                {
                }
            }
            else if (resolveResult instanceof SuccessResult)
                jarURL = ((SuccessResult) resolveResult).getDownloadUrl();
        }

        Pair<Boolean, String> downloadResult;

        //ダウンロード
        if (!withoutDownload)
        {
            finalSender.sendMessage(ChatColor.GOLD + "ファイルのダウンロード中...");

            //ダウンロード開始時間を控えておく
            long startTime = System.currentTimeMillis();

            //ファイルをダウンロード
            downloadResult = URLUtils.downloadFile(jarURL);
            if (downloadResult.getLeft() == null && downloadResult.getRight().startsWith("ERROR "))
            {
                finalSender.sendMessage(ChatColor.YELLOW + "W: " + downloadResult.getRight().substring(6));
                finalSender.sendMessage(ChatColor.RED + "E: ファイルのダウンロードに失敗しました。");
                finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
                return new InstallResult(add, remove, modify, false);
            }

            finalSender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.ADD, downloadResult.getRight()));
            add++;

            finalSender.sendMessage(ChatColor.DARK_GREEN.toString() + new BigDecimal(String.valueOf(System.currentTimeMillis()))
                    .subtract(new BigDecimal(String.valueOf(startTime)))
                    .divide(new BigDecimal("1000"), 2, RoundingMode.DOWN) + "秒で取得しました。");
        }
        else //ダウンロードしない
            downloadResult = new Pair<>(true, url);

        finalSender.sendMessage(ChatColor.LIGHT_PURPLE + "情報を読み込み中...");

        //plugin.yml
        PluginDescriptionFile description;
        try
        {
            //plugin.ymlを読み取り
            description = PluginUtil.loadDescription(new File("plugins/" + downloadResult.getRight()));
        }
        catch (FileNotFoundException e) //ファイルが見つからない場合はreturn
        {
            finalSender.sendMessage(ChatColor.RED + "E: ファイルが見つかりませんでした。");
            if (!withoutRemove)
                delete(finalSender, new File("plugins/" + downloadResult.getRight()));

            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));

            return new InstallResult(add, remove, modify, false);
        }
        catch (IOException | InvalidDescriptionException e) //plugin.ymlがおかしい場合はreturn
        {
            finalSender.sendMessage(ChatColor.RED + "E: 情報を読み込めませんでした。");
            if (!withoutRemove)
                delete(finalSender, new File("plugins/" + downloadResult.getRight()));

            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new InstallResult(add, remove, modify, false);
        }

        //保護されているプラグインの場合はインスコ・変換せずreturn
        if (TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream().anyMatch(s -> s.equalsIgnoreCase(description.getName())))
        {
            sender.sendMessage(ChatColor.RED + "E: このプラグインは保護されています。");
            add--;
            if (!withoutRemove)
                delete(finalSender, new File("plugins/" + downloadResult.getRight()));
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new InstallResult(add, remove, modify, false);
        }

        //spigotのapiから始まる場合はファイル名が番号のため
        //プラグイン名-バージョン.jar に戻す。
        if (jarURL.startsWith("https://apple.api.spiget.org"))
        {
            try
            {
                String fileName = description.getName().replace(" ", "") +
                        "-" +
                        description.getVersion() +
                        ".jar";
                //ファイルを移動
                //114514.jar => YJSNPIPlugin-1.0.jar
                FileUtils.moveFile(
                        new File("plugins/" + downloadResult.getRight()),
                        new File("plugins/" + fileName)
                );
                downloadResult = new Pair<>(false, fileName);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        //依存関係ツリーを取得
        DependencyTree.Info info = DependencyTree.getInfo(description.getName(), false);

        Plugin plugin = Bukkit.getPluginManager().getPlugin(description.getName());
        //同じ名前のプラグインがあるかどうか。
        //あった場合は、バージョンを比較し高い場合はインストール
        if (PluginUtil.isPluginLoaded(description.getName()) && new Version(plugin.getDescription().getVersion()).isLowerThan(description.getVersion()))
        {
            modify++;
            add--;
            finalSender.sendMessage(Messages.getModifyMessage(
                    Messages.ModifyType.MODIFY,
                    plugin.getName() + ":" + plugin.getDescription().getVersion() +
                            " => " + description.getName() + ":" + description.getVersion()
            ));

            //バージョンの低いプラグインをアンインストール。
            PluginUtil.unload(plugin);

            Runner.runLater(() -> {
                //削除する場合は削除
                if (!withoutRemove)
                    delete(finalSender, PluginUtil.getFile(plugin));
            }, 10L);
        }
        else if (PluginUtil.isPluginLoaded(description.getName())) //バージョンが変わらない(もしくは低い)。
        {
            add--;
            finalSender.sendMessage(ChatColor.YELLOW + "W: 既に同じプラグインが存在します。");

            //それでもインストールするかどうかを尋
            if (!dummySender().equals(sender))
            {
                //ファイルの比較を行う
                finalSender.sendMessage(getDiffMessage(PluginUtil.getFile(plugin), false));
                finalSender.sendMessage(getDiffMessage(new File("plugins/" + downloadResult.getRight()), true));
                finalSender.sendMessage("\n");
                //ファイナルにコピーする。
                String fileName = downloadResult.getRight();

                try
                {
                    QuestionResult questionResult = Terminals.of(sender).getInput()
                            .showQuestion("プラグインを置換しますか？", QuestionAttribute.YES, QuestionAttribute.CANCELLABLE)
                            .waitAndGetResult();

                    if (questionResult.test(QuestionAttribute.CANCELLABLE))
                    {
                        if (!withoutRemove && new File("plugins/" + fileName).exists())
                            delete(finalSender, new File("plugins/" + fileName));

                        finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
                        finalSender.sendMessage(ChatColor.GREEN + "S: " + description.getFullName() + " を正常にインストールしました。");
                    }
                    else
                    {
                        unInstall(null, description.getName(), true);
                        InstallResult ir = install(finalSender, fileName, false, false, false, true);
                        finalSender.sendMessage(Messages.getStatusMessage(ir.getAdd(), ir.getRemove(), ir.getModify() + 1));
                    }
                    return new InstallResult(downloadResult.getRight(), description.getName(), add, remove, modify, true);

                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            //削除
            if (!withoutRemove && new File("plugins/" + downloadResult.getRight()).exists())
                delete(finalSender, new File("plugins/" + downloadResult.getRight()));

            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            finalSender.sendMessage(ChatColor.GREEN + "S: " + description.getFullName() + " を正常にインストールしました。");
            return new InstallResult(downloadResult.getRight(), description.getName(), add, remove, modify, true);

        }

        added.add(new InstallResult(downloadResult.getRight(), description.getName(), add, remove, modify, true));

        //==================依存関係解決処理 ここから==================

        //forが回ったかのフラグ
        boolean dependFirst = true;
        //依存関係の処理に失敗したプラグイン
        ArrayList<String> failedResolve = new ArrayList<>();

        //ダウンロード開始時間を控えておく
        long startTime = System.currentTimeMillis();

        for (String dependency : description.getDepend())
        {
            //依存関係を処理しない場合はbreak
            if (withoutResolveDepends)
                break;

            //プラグインが既に存在する場合はcontinue
            if (Bukkit.getPluginManager().isPluginEnabled(dependency))
                continue;

            //最初の依存関係解決の場合
            if (dependFirst)
            {
                finalSender.sendMessage(ChatColor.GOLD + "依存関係をダウンロード中...");

                dependFirst = false;
            }

            //クエリを直リンに変換
            ResolveResult resolveResult = TeamKunPluginManager.getPlugin().getResolver().resolve(dependency);
            String dependUrl;

            if (resolveResult instanceof ErrorResult)
            {
                ErrorResult errorResult = (ErrorResult) resolveResult;

                sender.sendMessage(ChatColor.RED + "E: " + errorResult.getCause().getMessage() +
                        " リゾルバ：" + errorResult.getSource().getName());
                finalSender.sendMessage(ChatColor.YELLOW + "W: " + dependency + ": " + dependency.substring(5));
                failedResolve.add(dependency);
                continue;
            }
            else if (resolveResult instanceof MultiResult)
            {
                MultiResult multiResult = (MultiResult) resolveResult;

                resolveResult = multiResult.getResolver().autoPickOnePlugin(multiResult);
                if (resolveResult instanceof ErrorResult)
                {
                    finalSender.sendMessage(ChatColor.YELLOW + "W: " + dependency + ": " + dependency.substring(5));
                    failedResolve.add(dependency);
                    continue;
                }
                else if (resolveResult instanceof SuccessResult)
                    dependUrl = ((SuccessResult) resolveResult).getDownloadUrl();
                else
                    throw new IllegalStateException("resolveResultが不正です：プラグイン作成者に報告してください。");
            }
            else if (resolveResult instanceof SuccessResult)
                dependUrl = ((SuccessResult) resolveResult).getDownloadUrl();
            else
                throw new IllegalStateException("resolveResultが不正です：プラグイン作成者に報告してください。");

            //依存関係のインストール
            InstallResult dependResolve = Installer.install(null, dependUrl, true, false, true, false);
            //ファイルの名前がない場合は失敗としてマーク
            if (dependResolve.getFileName().isEmpty())
            {
                failedResolve.add(dependency);
                continue;
            }

            if (Bukkit.getPluginManager().getPlugin(dependUrl.substring(dependUrl.lastIndexOf("/"))) == null)
                finalSender.sendMessage(ChatColor.GREEN + "+ " + dependUrl.substring(dependUrl.lastIndexOf("/") + 1));
            else
            {
                Plugin dependPlugin = Bukkit.getPluginManager().getPlugin(dependUrl.substring(dependUrl.lastIndexOf("/")));
                finalSender.sendMessage(ChatColor.GREEN + "+ " + dependPlugin.getName() + ":" + dependPlugin.getDescription().getVersion());
            }
            added.add(dependResolve);
            add++;

        }

        //依存関係が1つでも読まれた場合はかかった時間を表示
        if (!dependFirst)
            finalSender.sendMessage(ChatColor.DARK_GREEN.toString() + new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(startTime))).divide(new BigDecimal("1000")).setScale(2, BigDecimal.ROUND_DOWN) + "秒で取得しました。");

        //結果を表示しないモードで依存関係エラーが発生した場合はreturn
        if (sender.equals(dummySender()) && !failedResolve.isEmpty())
            return new InstallResult(add, remove, modify, true);

        //依存関係エラーが発生した場合は表示
        if (!failedResolve.isEmpty())
        {
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            finalSender.sendMessage(ChatColor.YELLOW + "W: " + description.getFullName() + " を正常にインストールしましたが、以下の依存関係の処理に失敗しました。");
            finalSender.sendMessage(ChatColor.RED + String.join(", ", failedResolve));
            return new InstallResult(downloadResult.getRight(), description.getName(), add, remove, modify, true);
        }

        AtomicBoolean success = new AtomicBoolean(true);
        //インストールを行う場合
        if (!ignoreInstall)
        {
            //依存関係<=>非依存関係 を考慮し、読み込む順番を計算
            ArrayList<InstallResult> loadOrder = PluginUtil.mathLoadOrder(added);

            //読み込み順番に沿って読み込む
            for (InstallResult f : loadOrder)
            {
                try
                {
                    //プラグインが既に読まれてい場合はreturn
                    if (PluginUtil.isPluginLoaded(description.getName()))
                    {
                        finalSender.sendMessage(ChatColor.RED + "E: Bukkitのインジェクションに失敗しました。");

                        //削除する場合は削除
                        if (!withoutRemove)
                            delete(finalSender, new File("plugins/" + f.getFileName()));

                        //プラグインをアンロード
                        PluginUtil.unload(plugin);

                        Runner.runLaterAsync(() -> {
                            File file = PluginUtil.getFile(plugin);
                            if (!withoutRemove && file != null)
                                file.delete();
                        }, 20L);
                    }

                    //依存関係をロード
                    PluginUtil.load(f.getFileName().substring(0, f.getFileName().length() - 4));
                }
                catch (Exception e) //例外が発生した場合
                {
                    //削除する場合は削除
                    if (!withoutRemove)
                        delete(finalSender, new File("plugins/" + f.getFileName()));
                    e.printStackTrace();
                    //失敗フラグを建てる
                    success.set(false);
                }
            }
        }

        //失敗フラグが立っていた場合は表示
        if (!success.get())
            finalSender.sendMessage(ChatColor.RED + "E: プラグインの読み込みに失敗しました。");

        int finalAdd1 = add;
        int finalModify1 = modify;
        /*new BukkitRunnable()
        {

            @Override
            public void run()
            {*/
        //エラーが発生した場合
        String statusError = Messages.getErrorMessage();
        if (!statusError.isEmpty())
            finalSender.sendMessage(statusError);

        // 削除できるプラグイン(使われない依存関係等)があれば通知
        String autoRemovable = Messages.getUnInstallableMessage();
        if (!autoRemovable.isEmpty())
            finalSender.sendMessage(autoRemovable);

        finalSender.sendMessage(Messages.getStatusMessage(finalAdd1, remove, finalModify1));
        finalSender.sendMessage(ChatColor.GREEN + "S: " + description.getFullName() + " を正常にインストールしました。");
            /*}
        }.runTaskLater(TeamKunPluginManager.plugin, 10L);*/
        return new InstallResult(downloadResult.getRight(), description.getName(), add, remove, modify, true);
    }

    private static String getDiffMessage(File f, boolean isNew)
    {
        String header = ChatColor.BLUE + (isNew ? "---新規インストール---": "---既存プラグイン---") + "\n";
        if (f == null)
            return header +
                    "    " + ChatColor.RED + "ファイルなし";

        return header +
                "    " + pi("ファイル名", f.getName()) +
                "    " + pi("ファイルサイズ", f.exists() ? PluginUtil.getFileSizeString(f.length()): ChatColor.RED + "N/A") +
                "    " + pi("SHA1", f.exists() ? HashLib.genSha1(f): ChatColor.RED + "N/A");
    }

    private static String pi(String property, String value)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + value + "\n";
    }

    /**
     * 削除可能なデータフォルダを取得
     *
     * @return データフォルダ
     */
    public static String[] getRemovableDataDirs()
    {
        try
        {
            //ignoreされているものを全て取得
            List<String> bb = TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore");

            return Arrays.stream(Objects.requireNonNull(new File("plugins/").listFiles(File::isDirectory))) //plugins/の中のフォルダを全取得
                    .map(File::getName)                               //Stream<File> => Stream<String> ファイルの名前
                    .filter(file -> !PluginUtil.isPluginLoaded(file)) //プラグインフォルダが使用されていたら除外
                    .filter(file -> !bb.contains(file))               //除外リスト似合った場合はreturn
                    .toArray(String[]::new);                          //結果を全てreturn

        }
        catch (Exception e) //例外が発生した場合は空return
        {
            return new String[]{};
        }
    }

    public static void delete(CommandSender sender, File f)
    {
        try
        {
            f.delete();
        }
        catch (Exception e)
        {
            sender.sendMessage(ChatColor.RED + "E: ファイルの削除に失敗しました: " + f.getName());
        }

    }

    private static void depend_askToCommandSender(CommandSender sender, List<Pair<String, String>> jar, boolean ignoreInstall, boolean withoutResolveDepends, boolean withoutRemove)
    {
        UUID uuid = null;
        if (sender instanceof Player)
            uuid = ((Player) sender).getUniqueId();

        sender.sendMessage(ChatColor.YELLOW + "W: リソースが複数見つかりました。インストールするリソースを選択するか、キャンセルを行ってください。");
        AtomicInteger integer = new AtomicInteger(0);
        jar.forEach(pair -> {
            int index = integer.incrementAndGet();
            sender.sendMessage(new ComponentBuilder(ChatColor.LIGHT_PURPLE + "- [" + index + "] " + ChatColor.GREEN + pair.getLeft())
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kpm i " + pair.getRight()))
                    .event(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(ChatColor.GREEN + "クリックしてこのリリースをインストール").create()
                    ))
                    .create()
            );
        });
        sender.sendMessage(new ComponentBuilder(ChatColor.LIGHT_PURPLE + "- [c] " + ChatColor.RED + "キャンセル")
                .event(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.RED + "クリックしてこのリリースをキャンセル").create()
                ))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kpm i $-CF$"))
                .create());

        TeamKunPluginManager.getPlugin().getFunctional().add(uuid, new Say2Functional.FunctionalEntry(StringUtils::equalsIgnoreCase, s -> {
                    if (s.equalsIgnoreCase("c"))
                    {
                        sender.sendMessage(ChatColor.RED + "E: キャンセルされました。");
                        return;
                    }

                    int i = Integer.parseInt(s);
                    if (i < 1 || i > jar.size() + 1)
                    {
                        sender.sendMessage(ChatColor.RED + "リソースは 1 以上、" + (jar.size() + 1) + " 以下である必要があります。");
                        sender.sendMessage(ChatColor.RED + "E: キャンセルされました。");
                        return;
                    }

                    Pair<String, String> resource = jar.get(--i);
                    install(sender, resource.getRight(), ignoreInstall, withoutResolveDepends, withoutRemove, false);

                }, (String[]) ArrayUtils.add(IntStream.range(1, jar.size() + 1).parallel()
                        .mapToObj(String::valueOf)
                        .toArray(String[]::new), "c"))
        );
    }

    private static String pickPluginJar(List<Pair<String, String>> jar)
    {
        if (jar.isEmpty())
            return null;

        String result = "";
        String tmp = "";
        for (Pair<String, String> pair : jar)
        {
            String name = pair.getLeft();
            if (!name.endsWith(".jar") && name.endsWith(".zip"))
                continue;

            if (StringUtils.containsIgnoreCase(name, "plugin-") ||
                    StringUtils.containsIgnoreCase(name, "plugin."))
                result = pair.getRight();
            if (StringUtils.containsIgnoreCase(name, "plugin"))
                tmp = pair.getRight();
        }

        if (result.isEmpty() && !tmp.isEmpty())
            result = tmp;
        if (result.isEmpty())
            result = jar.get(0).getRight();

        return result;
    }

    private static List<Pair<String, String>> parseMultiResult(String mlt)
    {
        List<Pair<String, String>> result = new ArrayList<>();

        StringBuilder name = new StringBuilder();
        StringBuilder url = new StringBuilder();
        boolean escape = false;
        boolean flag = true;

        for (int i = 0; i < mlt.length(); i++)
        {
            char c = mlt.charAt(i);


            if (!escape && c == '|')
            {
                if (!name.toString().isEmpty() && !url.toString().isEmpty())
                {
                    result.add(new Pair<>(name.toString(), url.toString()));
                    name = new StringBuilder();
                    url = new StringBuilder();

                }
                flag = !flag;
                continue;
            }

            if (c == '\\')
            {
                escape = true;
                continue;
            }
            else
                escape = false;

            if (flag)
                name.append(c);
            else
                url.append(c);
        }

        return result;
    }

    /**
     * プラグインデータフォルダを削除
     *
     * @param name 対象
     * @return 合否
     */
    public static boolean clean(String name)
    {
        //依存関係エラーが有った場合は安全を優先しreturn
        if (DependencyTree.isErrors())
            return false;

        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);

        if (PluginUtil.isPluginLoaded(name))
            return false;  //プラグインがイネーブルの時、プロセスロックが掛かる

        if (TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream()
                .anyMatch(s -> s.equalsIgnoreCase(name))) // 保護されていたら除外
            return false;

        if (plugin != null)
        {
            try
            {
                FileUtils.forceDelete(plugin.getDataFolder());
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }

        }

        try
        {
            Arrays.stream(Objects.requireNonNull(new File("plugins/").listFiles(File::isDirectory)))  //plugins/の中のフォルダを全取得
                    .filter(file -> file.getName().equalsIgnoreCase(name)) //一致するフォルダを取得
                    .forEach(file -> {
                        try
                        {
                            //強制削除
                            FileUtils.forceDelete(file);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    });
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static CommandSender dummySender()
    {
        return new CommandSender()
        {
            @Override
            public void sendMessage(@NotNull String message)
            {

            }

            @Override
            public void sendMessage(@Nonnull @NotNull String[] messages)
            {

            }

            @Override
            public void sendMessage(@Nullable UUID sender, @NotNull String message)
            {

            }

            @Override
            public void sendMessage(@Nullable UUID sender, @Nonnull @NotNull String[] messages)
            {

            }

            @Override
            public @NotNull Server getServer()
            {
                return Bukkit.getServer();
            }

            @Override
            public @NotNull String getName()
            {
                return "DUMMY1145141919810931";
            }

            @Override
            public @NotNull Spigot spigot()
            {
                return null;
            }

            @Override
            public boolean isPermissionSet(@NotNull String name)
            {
                return false;
            }

            @Override
            public boolean isPermissionSet(@NotNull Permission perm)
            {
                return false;
            }

            @Override
            public boolean hasPermission(@NotNull String name)
            {
                return false;
            }

            @Override
            public boolean hasPermission(@NotNull Permission perm)
            {
                return false;
            }

            @Override
            public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value)
            {
                return null;
            }

            @Override
            public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin)
            {
                return null;
            }

            @Override
            public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks)
            {
                return null;
            }

            @Override
            public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks)
            {
                return null;
            }

            @Override
            public void removeAttachment(@NotNull PermissionAttachment attachment)
            {

            }

            @Override
            public void recalculatePermissions()
            {

            }

            @Override
            public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions()
            {
                return null;
            }

            @Override
            public boolean isOp()
            {
                return false;
            }

            @Override
            public void setOp(boolean value)
            {

            }
        };
    }

}
