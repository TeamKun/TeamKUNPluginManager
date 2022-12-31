package net.kunmc.lab.kpm.commands;

import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.kpm.Utils;
import net.kunmc.lab.kpm.interfaces.kpminfo.KPMInfoManager;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import net.kunmc.lab.kpm.meta.DependencyNode;
import net.kunmc.lab.kpm.meta.InstallOperator;
import net.kunmc.lab.kpm.meta.PluginMeta;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.kpm.utils.TerminalWriter;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PluginInfoWriter extends TerminalWriter
{
    private final Plugin plugin;

    @Nullable
    private final PluginMeta meta;
    @Nullable
    private final KPMInformationFile infoFile;

    public PluginInfoWriter(@NotNull KPMRegistry registry, @NotNull Terminal terminal, @NotNull Plugin plugin)
    {
        super(terminal);

        this.plugin = plugin;

        PluginMetaManager metaManager = registry.getPluginMetaManager();
        if (metaManager.hasPluginMeta(plugin))
            this.meta = metaManager.getProvider().getPluginMeta(plugin.getName());
        else
            this.meta = null;

        KPMInfoManager infoManager = registry.getKpmInfoManager();
        if (infoManager.hasInfo(plugin))
            this.infoFile = infoManager.getInfo(plugin);
        else
            this.infoFile = null;
    }

    private static String installOperatorToString(InstallOperator operator)
    {
        switch (operator)
        {
            case SERVER_ADMIN:
                return "サーバ管理者";
            case KPM_PLUGIN_UPDATER:
                return "KPMプラグインアップデータ";
            case KPM_DEPENDENCY_RESOLVER:
                return "KPM依存関係リゾルバ";
            case OTHER:
                return ChatColor.GRAY + "その他";
            default:
            case UNKNOWN:
                return ChatColor.GRAY + "不明";
        }
    }

    private static String loadOrderToString(PluginLoadOrder order)
    {
        switch (order)
        {
            case POSTWORLD:
                return "ワールド読込後";
            case STARTUP:
                return "サーバ起動時";
            default:
                return ChatColor.GRAY + "不明";
        }
    }

    private static String timeStampToString(long timeStamp)
    {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timeStamp));
    }

    @Override
    public void write()
    {
        this.printSeparator();
        this.writeBaseInfo();
        this.printSeparatorShort();

        boolean isAdditionInfoPresent = this.meta != null;
        this.printBoolean("追加メタ情報", isAdditionInfoPresent);
        if (isAdditionInfoPresent)
        {
            this.writeMetaInfo();
            this.printSeparatorShort();
            this.writeDependencyInfo();
        }

        this.printSeparatorShort();
        this.writeFileInfo();
        this.printSeparatorShort();

        boolean hasKPMInfoFile = this.infoFile != null;
        this.printBoolean("KPM 情報ファイル", hasKPMInfoFile);
        if (hasKPMInfoFile)
            this.writeKPMInfo();

        this.printSeparator();
    }

    public void writeBaseInfo()
    {
        PluginDescriptionFile desc = this.plugin.getDescription();

        this.printString("名前", desc.getName());
        this.printString("バージョン", desc.getVersion());
        this.printString("読込", loadOrderToString(desc.getLoad()));
        this.printStringFull("作者", StringUtils.join(desc.getAuthors(), ", "));
        this.printStringOrEmpty("説明", desc.getDescription());
        this.printStringOrEmpty("ウェブサイト", desc.getWebsite());
        this.printStringOrEmpty("ログ接頭辞", desc.getPrefix());
        this.printBoolean("有効", this.plugin.isEnabled());
    }

    public void writeKPMInfo()
    {
        if (this.infoFile == null)
            throw new IllegalStateException("No KPM information file found: " + this.plugin.getName());

        KPMInformationFile info = this.infoFile;

        this.printString("対応KPMバージョン", info.getKpmVersion() + "+");

        QueryContext resolveQuery = info.getUpdateQuery();
        if (resolveQuery != null)
            this.printString("解決クエリ", resolveQuery.toString(), ClickEvent.Action.SUGGEST_COMMAND,
                    resolveQuery.toString(), "クリックしてクエリをチャットに補完！"
            );

        String[] recipes = info.getRecipes();
        if (recipes != null)
        {
            this.printString("レシピ候補", StringUtils.join(recipes, ", "));
            this.printString("レシピ数", String.valueOf(recipes.length));
        }
    }

    public void writeFileInfo()
    {
        Path pluginPath = PluginUtil.getFile(this.plugin).toPath();

        this.printString("ファイル名", pluginPath.getFileName().toString());
        this.printString("ファイルサイズ", Utils.roundSizeUnit(pluginPath.toFile().length()));
        this.printString("最終更新日", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(pluginPath.toFile().lastModified())));

        this.printString("ハッシュ", "");
        this.printString("  SHA-1", Utils.getHash(pluginPath, "SHA-1"));
        this.printStringFull("  SHA-256", Utils.getHash(pluginPath, "SHA-256"));
        this.printString("  MD5", Utils.getHash(pluginPath, "MD5"));

    }

    public void writeMetaInfo()
    {
        PluginMeta meta = this.meta;
        if (meta == null)
            throw new IllegalStateException("Unable to find the plugin meta for " + this.plugin.getName());

        String resolveQuery = meta.getResolveQuery();

        this.printStringOrEmpty("インストール者", installOperatorToString(meta.getInstalledBy()));
        this.printStringOrEmpty("インストール日時", timeStampToString(meta.getInstalledAt()));
        this.printBoolean("依存関係?", meta.isDependency());

        boolean resolveQueryOverride = this.infoFile != null && this.infoFile.getUpdateQuery() != null;
        if (resolveQuery == null)
            this.printStringFull("解決クエリ", ChatColor.GRAY + "未設定" + (resolveQueryOverride ? " (上書きされています)": ""));
        else
            this.printString("解決クエリ", resolveQuery + (resolveQueryOverride ? ChatColor.GRAY + " (上書きされています)": ""),
                    ClickEvent.Action.SUGGEST_COMMAND, resolveQuery, "クリックしてクエリをチャットに補完！"
            );
    }

    public void writeDependencyInfo()
    {
        PluginMeta meta = this.meta;
        if (meta == null)
            throw new IllegalStateException("Unable to find the plugin meta for " + this.plugin.getName());

        this.terminal.write(
                Component.text(ChatColor.GREEN + "依存関係" + ChatColor.WHITE + "：")
                        .append(this.getDependencyComponents(meta.getDependedBy(), true))
        );

        this.terminal.write(
                Component.text(ChatColor.GREEN + "被依存関係" + ChatColor.WHITE + "：")
                        .append(this.getDependencyComponents(meta.getDependsOn(), false))
        );
    }

    private TextComponent getDependencyComponents(List<DependencyNode> dependencies, boolean direction)
    {
        ComponentBuilder<TextComponent, ?> builder = Component.text();

        if (dependencies.isEmpty())
            builder.append(Component.text(ChatColor.GRAY + "なし"));

        for (DependencyNode dependency : dependencies)
            builder.append(this.getDependencyComponent(dependency, direction)).append(Component.text("  "));

        return builder.build();
    }

    private TextComponent getDependencyComponent(DependencyNode dependency, boolean direction)
    {
        ComponentBuilder<TextComponent, ?> builder = Component.text();

        ChatColor color;
        switch (dependency.getDependType())
        {
            case HARD_DEPEND:
                color = ChatColor.DARK_GREEN;
                break;
            case SOFT_DEPEND:
                color = ChatColor.GREEN;
                break;
            case LOAD_BEFORE:
                color = ChatColor.DARK_BLUE;
                break;
            default:
                color = ChatColor.GRAY;
        }

        builder.append(
                Component.text(color + (direction ? dependency.getPlugin(): dependency.getDependsOn()))
                        .hoverEvent(HoverEvent.showText(Component.text("クリックして詳細を表示")))
                        .clickEvent(ClickEvent.runCommand("/kpm info " +
                                (direction ? dependency.getPlugin(): dependency.getDependsOn()))
                        )
        );

        return builder.build();
    }
}
