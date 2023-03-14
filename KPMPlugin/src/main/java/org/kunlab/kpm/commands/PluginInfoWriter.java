package org.kunlab.kpm.commands;

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
import org.kunlab.kpm.Utils;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.kunlab.kpm.kpminfo.interfaces.KPMInfoManager;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.meta.DependencyNode;
import org.kunlab.kpm.meta.InstallOperator;
import org.kunlab.kpm.meta.PluginMeta;
import org.kunlab.kpm.meta.interfaces.PluginMetaManager;
import org.kunlab.kpm.resolver.QueryContext;
import org.kunlab.kpm.utils.PluginUtil;
import org.kunlab.kpm.utils.TerminalWriter;

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
                return LangProvider.get("command.info.meta.install_operator.admin");
            case KPM_PLUGIN_UPDATER:
                return LangProvider.get("command.info.meta.install_operator.kpm_updater");
            case KPM_DEPENDENCY_RESOLVER:
                return LangProvider.get("command.info.meta.install_operator.kpm_dependency_resolver");
            case OTHER:
                return LangProvider.get("general.other");
            default:
            case UNKNOWN:
                return LangProvider.get("general.unknown");
        }
    }

    private static String loadOrderToString(PluginLoadOrder order)
    {
        switch (order)
        {
            case POSTWORLD:
                return LangProvider.get("command.info.base.load_timing.post_world");
            case STARTUP:
                return LangProvider.get("command.info.base.load_timing.start_up");
            default:
                return LangProvider.get("general.unknown");
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
        this.printBoolean("command.info.meta", isAdditionInfoPresent);
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
        this.printBoolean("general.kpm_info", hasKPMInfoFile);
        if (hasKPMInfoFile)
            this.writeKPMInfo();

        this.printSeparator();
    }

    public void writeBaseInfo()
    {
        PluginDescriptionFile desc = this.plugin.getDescription();

        this.printString("command.info.base.name", desc.getName());
        this.printString("command.info.base.version", desc.getVersion());
        this.printString("command.info.base.load_timing", loadOrderToString(desc.getLoad()));
        this.printStringFull("command.info.base.authors", StringUtils.join(desc.getAuthors(), ", "));
        this.printStringOrEmpty("command.info.base.authors", desc.getDescription());
        this.printStringOrEmpty("command.info.base.website", desc.getWebsite());
        this.printStringOrEmpty("command.info.base.log_prefix", desc.getPrefix());
        this.printBoolean("command.info.base.enabled", this.plugin.isEnabled());
    }

    public void writeKPMInfo()
    {
        if (this.infoFile == null)
            throw new IllegalStateException("No KPM information file found: " + this.plugin.getName());

        KPMInformationFile info = this.infoFile;

        this.printString("command.info.kpm_info.kpm_version", info.getKpmVersion() + "+");

        QueryContext resolveQuery = info.getUpdateQuery();
        if (resolveQuery != null)
            this.printString("command.info.meta.resolve_query", resolveQuery.toString(), ClickEvent.Action.SUGGEST_COMMAND,
                    resolveQuery.toString(), LangProvider.get("general.chat.click_to_completion")
            );

        String[] recipes = info.getRecipes();
        if (recipes != null)
        {
            this.printString("command.info.kpm_info.recipe_suggestions", StringUtils.join(recipes, ", "));
            this.printString("command.info.kpm_info.recipes", String.valueOf(recipes.length));
        }
    }

    public void writeFileInfo()
    {
        Path pluginPath = PluginUtil.getFile(this.plugin).toPath();

        this.printString("command.info.file.name", pluginPath.getFileName().toString());
        this.printString("command.info.file.size", Utils.roundSizeUnit(pluginPath.toFile().length()));
        this.printString("command.info.file.last_modified", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(pluginPath.toFile().lastModified())));

        this.printString("command.info.file.hash", "");
        this.printString("command.info.file.hash.sha1", PluginUtil.getHash(pluginPath, "SHA-1"));
        this.printStringFull("command.info.file.hash.sha256", PluginUtil.getHash(pluginPath, "SHA-256"));
        this.printString("command.info.file.hash.md5", PluginUtil.getHash(pluginPath, "MD5"));

    }

    public void writeMetaInfo()
    {
        PluginMeta meta = this.meta;
        if (meta == null)
            throw new IllegalStateException("Unable to find the plugin meta for " + this.plugin.getName());

        String resolveQuery = meta.getResolveQuery();

        this.printStringOrEmpty("command.info.meta.install_operator", installOperatorToString(meta.getInstalledBy()));
        this.printStringOrEmpty("command.info.meta.installed_at", timeStampToString(meta.getInstalledAt()));
        this.printBoolean("command.info.meta.dependency", meta.isDependency());

        boolean resolveQueryOverride = this.infoFile != null && this.infoFile.getUpdateQuery() != null;
        String overwrote = LangProvider.get("command.info.meta.resolve_query.overwrote");
        if (resolveQuery == null)
            this.printStringFull(
                    "command.info.meta.resolve_query",
                    LangProvider.get("general.empty") + (resolveQueryOverride ? overwrote: "")
            );
        else
            this.printString("command.info.meta.resolve_query", resolveQuery + (resolveQueryOverride ? ChatColor.GRAY + overwrote: ""),
                    ClickEvent.Action.SUGGEST_COMMAND, resolveQuery, LangProvider.get("general.chat.click_to_completion")
            );
    }

    public void writeDependencyInfo()
    {
        PluginMeta meta = this.meta;
        if (meta == null)
            throw new IllegalStateException("Unable to find the plugin meta for " + this.plugin.getName());

        this.terminal.write(
                Component.text(ChatColor.GREEN + LangProvider.get("command.info.meta.dependency") +
                                ChatColor.WHITE + "：")
                        .append(this.getDependencyComponents(meta.getDependedBy(), true))
        );

        this.terminal.write(
                Component.text(ChatColor.GREEN + LangProvider.get("command.info.meta.depended") +
                                ChatColor.WHITE + "：")
                        .append(this.getDependencyComponents(meta.getDependsOn(), false))
        );
    }

    private TextComponent getDependencyComponents(List<DependencyNode> dependencies, boolean direction)
    {
        ComponentBuilder<TextComponent, ?> builder = Component.text();

        if (dependencies.isEmpty())
            builder.append(Component.text(LangProvider.get("general.none")));

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
