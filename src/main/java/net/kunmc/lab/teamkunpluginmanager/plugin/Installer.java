package net.kunmc.lab.teamkunpluginmanager.plugin;

import com.g00fy2.versioncompare.Version;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.attributes.AttributeChoice;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.loader.PluginLoader;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.HashLib;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;
import net.kunmc.lab.teamkunpluginmanager.utils.http.RequestMethod;
import net.kunmc.lab.teamkunpluginmanager.utils.http.Requests;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Installer
{

    /**
     * アンインストールをする
     *
     * @param terminal 発行者(Nullable)
     * @param name     対象プラグ
     * @param force    強制削除かどうか
     */
    public static void unInstall(@NotNull Terminal terminal, @NotNull String name, boolean force)
    {

        terminal.writeLine(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");
        DependencyTree.Info info = DependencyTree.getInfo(name, false);

        //プラグインが見つからない場合はreturn
        if (info == null)
        {
            terminal.error("プラグインが見つかりませんでした。");
            return;
        }

        //プラグインを取得。
        Plugin plugin = Bukkit.getPluginManager().getPlugin(info.name);

        //アンインストール対象が存在するかチェック
        if (!PluginUtil.isPluginLoaded(info.name))
        {
            terminal.error("プラグインが見つかりませんでした。");
            return;
        }

        //保護されているプラグインの場合は削除せずreturn
        if (TeamKunPluginManager.getPlugin().getPluginConfig().
                getStringList("ignore").stream().anyMatch(s -> s.equalsIgnoreCase(info.name)))
        {
            terminal.warn("このプラグインは保護されています。\n" +
                    ChatColor.YELLOW + "   保護されているプラグインを削除すると、サーバの動作に支障を来す可能性がございます。");
            terminal.error("システムが保護されました。");
            return;
        }

        //他のプラグインの依存関係になっている場合はreturn
        // 強制アンインストールである場合は無視
        if (!info.rdepends.isEmpty() && !force)
        {
            terminal.warn("このプラグインは以下のプラグインの依存関係です。");
            terminal.writeLine(ChatColor.BLUE + info.rdepends.stream().parallel().map(depend -> depend.depend).collect(Collectors.joining(" ")));
            terminal.writeLine(ChatColor.YELLOW + "    依存関係になっているプラグインを削除すると、サーバの動作に支障を来す可能性がございます。");
            terminal.error("システムが保護されました。");
            return;
        }

        terminal.writeLine(ChatColor.LIGHT_PURPLE + "プラグインを削除中...");

        //プラグインをアンロード
        PluginLoader.getInstance().unloadPlugin(plugin);

        //非同期実行
        Runner.runLaterAsync(() -> {
            //プラグインのファイルを取得
            File file = PluginUtil.getFile(plugin);
            //ファイルが有った場合は削除
            if (file != null)
                file.delete();

            //依存関係ツリーをワイプする
            DependencyTree.wipePlugin(plugin);
            terminal.writeLine(ChatColor.RED + "- " + plugin.getName() + ":" + plugin.getDescription().getVersion());

            // エラーが有っる場合は表示
            String statusError = Messages.getErrorMessage();
            if (!statusError.isEmpty())
                terminal.writeLine(statusError);

            // 削除できるプラグイン(使われない依存関係等)があれば通知
            String autoRemovable = Messages.getUnInstallableMessage();

            if (!autoRemovable.isEmpty())
                terminal.writeLine(autoRemovable);
            terminal.writeLine(Messages.getStatusMessage(0, 1, 0));
            terminal.success(plugin.getName() + ":" + plugin.getDescription().getVersion() + " を正常にアンインストールしました。");

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
     * @param terminal              結果を表示する対象の人
     * @param url                   URL!!!
     * @param ignoreInstall         インストールを除外するかどうか
     * @param withoutResolveDepends 依存関係解決をしない
     * @param withoutRemove         削除しない
     * @param withoutDownload       ダウンロードしない
     * @return ファイル名, プラグイン名
     */
    public static InstallResult install(@NotNull Terminal terminal, String url, boolean ignoreInstall, boolean withoutResolveDepends, boolean withoutRemove, boolean withoutDownload)
    {
        //jarのURL
        String jarURL = url;
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

                terminal.error(errorResult.getCause().getMessage() + " リゾルバ：" + errorResult.getSource().getName());

                terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
                return new InstallResult(add, remove, modify, false);
            }
            else if (resolveResult instanceof MultiResult)
            {
                MultiResult multiResult = (MultiResult) resolveResult;

                try
                {
                    if (terminal.isQuiet())
                    {
                        resolveResult = multiResult.getResolver().autoPickOnePlugin(multiResult);
                        if (resolveResult instanceof ErrorResult)
                        {
                            ErrorResult errorResult = (ErrorResult) resolveResult;

                            terminal.error(errorResult.getCause().getMessage() +
                                    " リゾルバ：" + errorResult.getSource().getName());

                            terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
                            return new InstallResult(add, remove, modify, false);
                        }
                        else if (resolveResult instanceof SuccessResult)
                            jarURL = ((SuccessResult) resolveResult).getDownloadUrl();
                        else
                            throw new IllegalStateException("resolveResultが不正です：プラグイン作成者に報告してください。");
                    }
                    else
                    {
                        SuccessResult result = dependAskToTerminal(terminal, multiResult);

                        if (result == null)
                        {
                            terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
                            return new InstallResult(add, remove, modify, false);
                        }

                        jarURL = result.getDownloadUrl();
                    }
                }
                catch (IllegalArgumentException ignored)
                {
                }
            }
            else if (resolveResult instanceof SuccessResult)
                jarURL = ((SuccessResult) resolveResult).getDownloadUrl();
        }

        long downloadResult;
        Path downloadPath;

        //ダウンロード
        if (!withoutDownload)
        {
            terminal.writeLine(ChatColor.GOLD + "ファイルのダウンロード中...");

            //ダウンロード開始時間を控えておく
            long startTime = System.currentTimeMillis();

            //ファイルをダウンロード
            try
            {
                downloadPath = Paths.get("plugins", jarURL.substring(url.lastIndexOf("/") + 1));
                downloadResult = Requests.downloadFile(
                        RequestMethod.GET,
                        jarURL,
                        downloadPath
                );
            }
            catch (IOException e)
            {
                terminal.error("ファイルのダウンロードに失敗しました：" + e.getMessage());
                terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
                return new InstallResult(add, remove, modify, false);
            }


            terminal.writeLine(Messages.getModifyMessage(Messages.ModifyType.ADD, downloadPath.getFileName().toString()));
            add++;

            terminal.writeLine(ChatColor.DARK_GREEN.toString() + new BigDecimal(String.valueOf(System.currentTimeMillis()))
                    .subtract(new BigDecimal(String.valueOf(startTime)))
                    .divide(new BigDecimal("1000"), 2, RoundingMode.DOWN) + "秒で取得しました。");
        }
        else //ダウンロードしない
        {
            downloadResult = -1;
            downloadPath = Paths.get(url);
        }

        terminal.writeLine(ChatColor.LIGHT_PURPLE + "情報を読み込み中...");

        //plugin.yml
        PluginDescriptionFile description;
        try
        {
            //plugin.ymlを読み取り
            description = PluginUtil.loadDescription(downloadPath.toFile());
        }
        catch (FileNotFoundException e) //ファイルが見つからない場合はreturn
        {
            terminal.error("ファイルが見つかりませんでした。");
            if (!withoutRemove)
                delete(terminal, downloadPath.toFile());

            terminal.writeLine(Messages.getStatusMessage(add, remove, modify));

            return new InstallResult(add, remove, modify, false);
        }
        catch (IOException | InvalidDescriptionException e) //plugin.ymlがおかしい場合はreturn
        {
            terminal.error("情報を読み込めませんでした。");
            if (!withoutRemove)
                delete(terminal, downloadPath.toFile());

            terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
            return new InstallResult(add, remove, modify, false);
        }

        //保護されているプラグインの場合はインスコ・変換せずreturn
        if (TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream().anyMatch(s -> s.equalsIgnoreCase(description.getName())))
        {
            terminal.error("このプラグインは保護されています。");
            add--;
            if (!withoutRemove)
                delete(terminal, downloadPath.toFile());
            terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
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
                        downloadPath.toFile(),
                        new File("plugins/" + fileName)
                );
                downloadPath = Paths.get("plugins/" + fileName);
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
            terminal.writeLine(Messages.getModifyMessage(
                    Messages.ModifyType.MODIFY,
                    plugin.getName() + ":" + plugin.getDescription().getVersion() +
                            " => " + description.getName() + ":" + description.getVersion()
            ));

            //バージョンの低いプラグインをアンインストール。
            PluginLoader.getInstance().unloadPlugin(plugin);

            Runner.runLater(() -> {
                //削除する場合は削除
                if (!withoutRemove)
                    delete(terminal, PluginUtil.getFile(plugin));
            }, 10L);
        }
        else if (PluginUtil.isPluginLoaded(description.getName())) //バージョンが変わらない(もしくは低い)。
        {
            add--;
            terminal.warn("既に同じプラグインが存在します。");

            //それでもインストールするかどうかを尋
            if (terminal.isPlayer())  // TODO: Remove this if statement
            {
                //ファイルの比較を行う
                terminal.writeLine(getDiffMessage(PluginUtil.getFile(plugin), false));
                terminal.writeLine(getDiffMessage(downloadPath.toFile(), true));
                terminal.writeLine("\n");
                //ファイナルにコピーする。
                String fileName = downloadPath.getFileName().toString();

                try
                {
                    QuestionResult questionResult = terminal.getInput()
                            .showQuestion("プラグインを置換しますか？", QuestionAttribute.YES, QuestionAttribute.CANCELLABLE)
                            .waitAndGetResult();

                    if (questionResult.test(QuestionAttribute.CANCELLABLE))
                    {
                        if (!withoutRemove && new File("plugins/" + fileName).exists())
                            delete(terminal, new File("plugins/" + fileName));

                        terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
                        terminal.success(description.getFullName() + " を正常にインストールしました。");
                    }
                    else
                    {
                        unInstall(terminal.quiet(), description.getName(), true);
                        InstallResult ir = install(terminal, fileName, false, false, false, true);
                        terminal.writeLine(Messages.getStatusMessage(ir.getAdd(), ir.getRemove(), ir.getModify() + 1));
                    }
                    return new InstallResult(downloadPath.getFileName().toString(), description.getName(),
                            add, remove, modify, true
                    );

                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            //削除
            if (!withoutRemove && downloadPath.toFile().exists())
                delete(terminal, downloadPath.toFile());

            terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
            terminal.success(description.getFullName() + " を正常にインストールしました。");
            return new InstallResult(downloadPath.getFileName().toString(), description.getName(),
                    add, remove, modify, true
            );

        }

        added.add(new InstallResult(downloadPath.getFileName().toString(), description.getName(), add, remove, modify, true));

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
                terminal.writeLine(ChatColor.GOLD + "依存関係をダウンロード中...");

                dependFirst = false;
            }

            //クエリを直リンに変換
            ResolveResult resolveResult = TeamKunPluginManager.getPlugin().getResolver().resolve(dependency);
            String dependUrl;

            if (resolveResult instanceof ErrorResult)
            {
                ErrorResult errorResult = (ErrorResult) resolveResult;

                terminal.error(errorResult.getCause().getMessage() +
                        " リゾルバ：" + errorResult.getSource().getName());
                terminal.warn(dependency + ": " + dependency.substring(5));
                failedResolve.add(dependency);
                continue;
            }
            else if (resolveResult instanceof MultiResult)
            {
                MultiResult multiResult = (MultiResult) resolveResult;

                resolveResult = multiResult.getResolver().autoPickOnePlugin(multiResult);
                if (resolveResult instanceof ErrorResult)
                {
                    terminal.warn(dependency + ": " + dependency.substring(5));
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
            InstallResult dependResolve = Installer.install(terminal.quiet(), dependUrl, true, false, true, false);
            //ファイルの名前がない場合は失敗としてマーク
            if (dependResolve.getFileName().isEmpty())
            {
                failedResolve.add(dependency);
                continue;
            }

            if (Bukkit.getPluginManager().getPlugin(dependUrl.substring(dependUrl.lastIndexOf("/"))) == null)
                terminal.writeLine(ChatColor.GREEN + "+ " + dependUrl.substring(dependUrl.lastIndexOf("/") + 1));
            else
            {
                Plugin dependPlugin = Bukkit.getPluginManager().getPlugin(dependUrl.substring(dependUrl.lastIndexOf("/")));
                terminal.writeLine(ChatColor.GREEN + "+ " + dependPlugin.getName() + ":" + dependPlugin.getDescription().getVersion());
            }
            added.add(dependResolve);
            add++;

        }

        //依存関係が1つでも読まれた場合はかかった時間を表示
        if (!dependFirst)
            terminal.writeLine(ChatColor.DARK_GREEN.toString() + new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(startTime))).divide(new BigDecimal("1000")).setScale(2, RoundingMode.DOWN) + "秒で取得しました。");

        //結果を表示しないモードで依存関係エラーが発生した場合はreturn
        if (!terminal.isPlayer() && !failedResolve.isEmpty())
            return new InstallResult(add, remove, modify, true);

        //依存関係エラーが発生した場合は表示
        if (!failedResolve.isEmpty())
        {
            terminal.writeLine(Messages.getStatusMessage(add, remove, modify));
            terminal.warn(description.getFullName() + " を正常にインストールしましたが、以下の依存関係の処理に失敗しました。");
            terminal.writeLine(ChatColor.RED + String.join(", ", failedResolve));
            return new InstallResult(downloadPath.getFileName().toString(), description.getName(),
                    add, remove, modify, true
            );
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
                        terminal.error("Bukkitのインジェクションに失敗しました。");

                        //削除する場合は削除
                        if (!withoutRemove)
                            delete(terminal, new File("plugins/" + f.getFileName()));

                        //プラグインをアンロード
                        PluginLoader.getInstance().unloadPlugin(plugin);

                        Runner.runLaterAsync(() -> {
                            File file = PluginUtil.getFile(plugin);
                            if (!withoutRemove && file != null)
                                file.delete();
                        }, 20L);
                    }

                    PluginLoader.getInstance().loadPlugin(new File("plugins", f.getFileName()).toPath());
                }
                catch (Exception e) //例外が発生した場合
                {
                    //削除する場合は削除
                    if (!withoutRemove)
                        delete(terminal, new File("plugins/" + f.getFileName()));
                    e.printStackTrace();
                    //失敗フラグを建てる
                    success.set(false);
                }
            }
        }

        //失敗フラグが立っていた場合は表示
        if (!success.get())
            terminal.error("プラグインの読み込みに失敗しました。");

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
            terminal.writeLine(statusError);

        // 削除できるプラグイン(使われない依存関係等)があれば通知
        String autoRemovable = Messages.getUnInstallableMessage();
        if (!autoRemovable.isEmpty())
            terminal.writeLine(autoRemovable);

        terminal.writeLine(Messages.getStatusMessage(finalAdd1, remove, finalModify1));
        terminal.success(description.getFullName() + " を正常にインストールしました。");
            /*}
        }.runTaskLater(TeamKunPluginManager.plugin, 10L);*/
        return new InstallResult(downloadPath.getFileName().toString(), description.getName(),
                add, remove, modify, true
        );
    }

    private static String getDiffMessage(File f, boolean isNew)
    {
        String header = ChatColor.BLUE + (isNew ? "---新規インストール---": "---既存プラグイン---") + "\n";
        if (f == null)
            return header +
                    "    " + ChatColor.RED + "ファイルなし";

        return header +
                "    " + pi("ファイル名", f.getName()) +
                "    " + pi("ファイルサイズ", f.exists() ? Utils.roundSizeUnit(f.length()): ChatColor.RED + "N/A") +
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

    public static void delete(Terminal terminal, File f)
    {
        try
        {
            f.delete();
        }
        catch (Exception e)
        {
            terminal.error("ファイルの削除に失敗しました: " + f.getName());
        }

    }

    private static HashMap<String, SuccessResult> buildChoicesRecursive(int count, MultiResult result)
    {
        HashMap<String, SuccessResult> choices = new HashMap<>();

        for (ResolveResult resolveResult : result.getResults())
        {
            if (resolveResult instanceof SuccessResult)
            {
                choices.put(count + "." + resolveResult.hashCode(), (SuccessResult) resolveResult);
                continue;
            }

            if (resolveResult instanceof MultiResult)
                choices.putAll(buildChoicesRecursive(count + 1, (MultiResult) resolveResult));
        }

        return choices;
    }

    private static SuccessResult dependAskToTerminal(Terminal terminal, MultiResult result)
    {
        terminal.warn("複数のリソースが見つかりました。");

        Map<String, SuccessResult> hashResultMap = buildChoicesRecursive(1, result);
        Map<String, String> hashTextMap = hashResultMap.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getFileName()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        QuestionResult questionResult;

        try
        {
            questionResult =
                    terminal.getInput().showQuestion("インストールするリリースを選択してください。",
                                    new AttributeChoice(hashTextMap), QuestionAttribute.CANCELLABLE
                            )
                            .waitAndGetResult();
        }
        catch (InterruptedException e)
        {
            terminal.error("処理が中断されました。");
            return null;
        }

        if (questionResult.test(QuestionAttribute.CANCELLABLE))
        {
            terminal.warn("インストールがキャンセルされました。");
            return null;
        }

        return hashResultMap.get(questionResult.getRawAnswer());
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
}
