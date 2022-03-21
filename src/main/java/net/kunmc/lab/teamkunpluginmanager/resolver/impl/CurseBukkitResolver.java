package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurseBukkitResolver implements URLResolver
{
    private static final String basePatterns = "(?<slug>\\w+)(/files(/(?<version>\\d+))?(/download)?)?/?$";
    private static final Pattern BUKKIT_PATTERN = Pattern.compile("^/projects/" + basePatterns);
    private static final Pattern CURSE_PATTERN = Pattern.compile("^/minecraft/bukkit-plugins/" + basePatterns);
    private static final String BUKKIT_API_VERSION = StringUtils.split(Bukkit.getVersion(), "-")[0];

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        boolean bukkitFlag = StringUtils.containsIgnoreCase(query.getQuery(), "bukkit.org");

        ResolveResult.Source errorSource = bukkitFlag ? ResolveResult.Source.DEV_BUKKIT: ResolveResult.Source.CURSE_FORGE;

        Matcher matcher;
        if (bukkitFlag)
            matcher = urlMatcher(BUKKIT_PATTERN, query.getQuery());
        else
            matcher = urlMatcher(CURSE_PATTERN, query.getQuery());

        String slug = null;
        String version = null;

        while (matcher.find())
        {
            String slugGroup = matcher.group("slug");
            String versionGroup = matcher.group("version");

            if (slugGroup != null && !slugGroup.isEmpty())
                slug = slugGroup;
            if (versionGroup != null && !versionGroup.isEmpty())
                version = versionGroup;
        }

        if (slug == null)
            return new ErrorResult(ErrorResult.ErrorCause.INVALID_QUERY, errorSource);

        Pair<Integer, String> projectSearchResponse =  URLUtils.getAsString("https://servermods.forgesvc.net/servermods/projects?search=" + slug);

        if (projectSearchResponse.getLeft() != 200)
            return processErrorResponse(projectSearchResponse.getLeft(), errorSource);

        JsonArray projectSearchResult = new Gson().fromJson(projectSearchResponse.getRight(), JsonArray.class);

        String name = null;
        long projectId = -1;
        for (JsonElement elm : projectSearchResult)
        {
            JsonObject obj = (JsonObject) elm;

            if (obj.get("slug").getAsString().equalsIgnoreCase(slug))
            {
                projectId = obj.get("id").getAsLong();
                name = obj.get("name").getAsString();
                break;
            }
        }

        if (projectId == -1)
            return new ErrorResult(ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, errorSource);

        return processFiles(slug, name, projectId, version, errorSource);
    }

    private ResolveResult processFiles(String slug, String name, long projectId, String version, ResolveResult.Source source)
    {
        Pair<Integer, String> projectFilesResponse =  URLUtils.getAsString("https://servermods.forgesvc.net/servermods/files?projectIds=" + projectId);

        ErrorResult mayError = processErrorResponse(projectFilesResponse.getLeft(), source);
        if (mayError != null)
            return mayError;

        JsonArray projectFilesResult = new Gson().fromJson(projectFilesResponse.getRight(), JsonArray.class);
        if (projectFilesResult.size() == 0)
            return new ErrorResult(ErrorResult.ErrorCause.ASSET_NOT_FOUND, source);

        JsonObject pickedPlugin = pickUpValidVersion(projectFilesResult, version);
        if (pickedPlugin == null)
            return new ErrorResult(ErrorResult.ErrorCause.MATCH_PLUGIN_NOT_FOUND, source);

        String downloadUrl = pickedPlugin.get("downloadUrl").getAsString();
        String fileName = pickedPlugin.get("fileName").getAsString();
        String versionName = pickedPlugin.get("name").getAsString();

        return new CurseBukkitSuccessResult(downloadUrl, projectId, fileName, versionName, source, slug, name);
    }

    private static JsonObject pickUpValidVersion(JsonArray versions, @Nullable String version)
    {
        JsonObject file = null;

        for (JsonElement projectFile : versions)
        {
            JsonObject projectFileObject = projectFile.getAsJsonObject();

            if (version != null && projectFileObject.get("fileUrl").getAsString().endsWith(version))
                return projectFileObject;

            if (projectFileObject.get("gameVersion").getAsString().contains(BUKKIT_API_VERSION))
                file = projectFileObject;
        }

        return file;
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        return null;
    }

    @Override
    public String[] getHosts()
    {
        return new String[] {
                "dev.bukkit.org",
                "www.curseforge.com",
                "curseforge.com",
        };
    }
}
