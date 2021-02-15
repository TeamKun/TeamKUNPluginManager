package net.kunmc.lab.teamkunpluginmanager.console.utils;

import com.g00fy2.versioncompare.Version;
import net.kunmc.lab.teamkunpluginmanager.common.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import net.kunmc.lab.teamkunpluginmanager.common.known.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.common.utils.GitHubURLBuilder;
import net.kunmc.lab.teamkunpluginmanager.common.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.common.utils.URLUtils;
import net.kunmc.lab.teamkunpluginmanager.console.PluginManagerConsole;
import net.kunmc.lab.teamkunpluginmanager.console.Progress;
import net.kunmc.lab.teamkunpluginmanager.spigot.plugin.InstallResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;

public class Installer
{

    public static void uninstall(String pluginName)
    {
        uninstall(pluginName, false);
    }

    public static void uninstall(String pluginName, boolean force)
    {

        System.out.println("依存関係ツリーを読み込み中...");

        DependencyTree.Info info = DependencyTree.getInfo(pluginName, false);
        if (info == null)
        {
            System.out.println("E: プラグインが見つかりませんでした。");
            return;
        }

        if (new File(pluginName).exists())
        {
            System.out.println("E: プラグインが見つかりませんでした。");
            return;
        }

    }

    /**
     * インストール
     *
     * @param url   名前またはurl
     * @param print 出力
     */

    public static InstallResult install(String url, boolean print)
    {
        int install = 0;
        int uninstall = 0;
        int change = 0;

        Progress progress = new Progress("データベースを構築中");
        if (print)
            progress.start();
        KnownPlugins.initialize(
                PluginManagerConsole.dataFolder.toFile(),
                PluginManagerConsole.config.getString("resolvePath")
        );

        if (print)
            progress.stop();

        if (!UrlValidator.getInstance().isValid(url))
        {
            // User/Repository 形式であれば先頭に GitHub のドメインを追加する。
            if (StringUtils.split(url, "/").length == 2)
            {
                url = "https://github.com/" + url;
                url = GitHubURLBuilder.urlValidate(url);

                // URL の指定が間違っていると終了。
                if (url.startsWith("ERROR "))
                {
                    if (Variables.OAuthToken.equals(""))
                        print("W: GitHub 用のOAuth Tokenを正しく設定していない可能性がございます。", print);

                    print("E: " + url.substring(6), print);
                    print(getResultMessage(install, change, uninstall), print);
                    return new InstallResult(false);
                }
            }
            else if (KnownPlugins.isKnown(url))
            {
                url = KnownPlugins.getKnown(url).url;
            }
            else
            {
                print("E: " + url + "が見つかりません。", print);
                print(getResultMessage(install, change, uninstall), print);
                return new InstallResult(false);
            }
        }

        // ダウンロード開始時間（ミリ秒）
        long downloadStartTime = System.currentTimeMillis();
        print("ファイルのダウンロード中...", print);

        Pair<Boolean, String> result = URLUtils.downloadFile(url);
        if (result.getValue().equals(""))
        {
            print("E: ファイルのダウンロードに失敗しました。", print);
            print(getResultMessage(install, change, uninstall), print);
            return new InstallResult(false);
        }

        print(getChangeMessage(ChangeType.INSTALL, result.getValue()), print);
        install++;

        print((System.currentTimeMillis() - downloadStartTime) / 1000L + "秒で取得しました。", print);
        Progress infoProg = new Progress("情報を読み込み中");
        infoProg.start();
        PluginYamlParser description;

        try
        {
            description = PluginYamlParser.fromJar(new File(result.getValue()));
        }
        catch (NoSuchFileException e)
        {
            infoProg.stop();
            print("E: plugin.yml が見つかりませんでした。", print);
            print(getResultMessage(install, change, uninstall), print);
            return new InstallResult(false);
        }
        catch (IOException e)
        {
            infoProg.stop();
            print("E: 情報を読み込めませんでした。", print);
            print(getResultMessage(install, change, uninstall), print);
            return new InstallResult(false);
        }

        infoProg.stop();

        // プラグイン保護するとこ飛ばします。

        DependencyTree.Info info = DependencyTree.getInfo(description.name, false);

        if (info != null && new Version(info.version).isHigherThan(description.version))
        {
            install--;
            print("W: 既に同じプラグインが存在します。", print);
            if (new File(result.getValue()).exists())
            {
                try
                {
                    File f = new File(result.getValue());
                    FileUtils.forceDelete(f);
                }
                catch (Exception e)
                { //getName => PeyaPeyaPlugin getFullName => PeyaPeyaPlugin:1.0
                    print("E: ファイルの削除に失敗しました: " + result.getValue(), print);
                    print(getResultMessage(install, change, uninstall), print);
                    return new InstallResult(false);
                }
            }
            print("S: " + description.name + ":" + description.version + "を正常にインストールされました", print);
            print(getResultMessage(install, change, uninstall), print);
            return new InstallResult(result.getValue(), description.name, install, uninstall, change, true);
        }

        boolean depend = false;   //依存関係の解決の必要性
        ArrayList<String> failedResolved = new ArrayList<>();   //依存関係の解決失敗

        for (String pl : description.depend)
        {
            if (!depend)
            {
                print("依存関係をダウンロード中...", print);
                downloadStartTime = System.currentTimeMillis();
                depend = true;
            }

            String temp1 = resolveDepend(pl);

            if (temp1.equals("ERROR")) //依存関係の解決に失敗
            {
                failedResolved.add(temp1);
                continue;
            }

            InstallResult dependResult = install(temp1, false);
            if (!dependResult.success)
            {
                failedResolved.add(temp1);
                continue;
            }

            print(getChangeMessage(ChangeType.INSTALL, dependResult.pluginName), print);
            install += dependResult.add;
            change += dependResult.modify;
            uninstall += dependResult.remove;
        }

        if (depend)
            print((System.currentTimeMillis() - downloadStartTime) / 1000L + "秒で取得しました。", print);

        if (result.getKey())
        {
            if (info != null && new Version(info.version).isHigherThan(description.version))
            {
                install--;
                change++;
                print(getChangeMessage(
                        ChangeType.CHANGE,
                        info.name + ":" + info.version + " => " + description.name + ":" + description.version
                ), print);
            }
            else
            {
                install--;
                print("W: 既に同じプラグインが存在します。", print);
                if (new File(result.getValue()).exists())
                {
                    try
                    {
                        File f = new File(result.getValue());
                        FileUtils.forceDelete(f);
                    }
                    catch (Exception e)
                    {
                        print("E: ファイルの削除に失敗しました: " + result.getValue(), print);
                    }
                }
                print(getResultMessage(install, uninstall, change), print);
                print("S: " + description.name + ":" + description.version + " を正常にインストールしました。", print);
                return new InstallResult(result.getValue(), description.name, install, uninstall, change, true);
            }
        }

        if (failedResolved.size() > 0)
        {
            print(getResultMessage(install, uninstall, change), print);
            print("W: " + description.name + " を正常にインストールしましたが、以下の依存関係の処理に失敗しました。", print);
            print(String.join(", ", failedResolved), print);
            return new InstallResult(result.getValue(), description.name, install, uninstall, change, true);
        }


        if (DependencyTree.unusedPlugins().size() != 0)
        {
            print("以下のプラグインがインストールされていますが、もう必要とされていません:", print);
            print("  " + String.join(" ", DependencyTree.unusedPlugins()), print);
            print("これを削除するには、'/kpm autoremove' を利用してください。", print);
        }

        if (print)
        {
            Progress progressDepend = new Progress("依存関係ツリーを構築中");
            progressDepend.start();
            DependencyTree.Info dInfo = new DependencyTree.Info();
            dInfo.name = description.name;
            dInfo.version = description.version;
            dInfo.depends = new ArrayList<>();
            Arrays.stream(description.depend).parallel()
                    .forEach(s -> {
                        DependencyTree.Info.Depend dep = new DependencyTree.Info.Depend();
                        dep.name = s;
                        dInfo.depends.add(dep);
                    });
            DependencyTree.crawlPlugin(dInfo);
            progressDepend.stop();
        }

        print(getResultMessage(install, uninstall, change), print);
        print("S: " + description.name + ":" + description.version + " を正常にインストールしました。", print);
        return new InstallResult(result.getValue(), description.name, install, uninstall, change, true);
    }

    public static <T> void print(T obj, boolean nPrint)
    {
        if (!nPrint)
            return; //無出力
        System.out.println(obj);
    }

    public static String resolveDepend(String name)
    {
        if (KnownPlugins.isKnown(name))
            return KnownPlugins.getKnown(name).url;

        String[] orgName = Variables.gitHubName;

        for (String on : orgName)
        {
            String gitHubRepo = on + "/" + name;

            String repository = GitHubURLBuilder.urlValidate("https://github.com/" + gitHubRepo);

            if (!repository.equals("ERROR"))
                return gitHubRepo;
        }

        return "ERROR";
    }

    private static String getChangeMessage(ChangeType type, String name)
    {
        switch (type)
        {
            case INSTALL:
                return "+ " + name;
            case CHANGE:
                return "~ " + name;
            case uninstall:
                return "- " + name;
            default:
                return "? " + name;
        }
    }

    private static String getResultMessage(int install, int change, int uninstall)
    {
        return install + "追加 " + change + "変更 " + uninstall + "削除";
    }

    private enum ChangeType
    {
        INSTALL,
        CHANGE,
        uninstall
    }

}
