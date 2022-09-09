package net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.utils.http.HTTPResponse;
import net.kunmc.lab.teamkunpluginmanager.utils.http.RequestContext;
import net.kunmc.lab.teamkunpluginmanager.utils.http.Requests;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurseBukkitResolver implements URLResolver
{
    private static final String basePatterns = "(?<slug>\\w+)(/files(/(?<version>\\d+))?(/download)?)?/?$";
    private static final Pattern BUKKIT_PATTERN = Pattern.compile("^/projects/" + basePatterns);
    private static final Pattern CURSE_PATTERN = Pattern.compile("^/minecraft/bukkit-plugins/" + basePatterns);
    private static final String BUKKIT_API_VERSION;

    static
    {
        String baseVersion = Bukkit.getMinecraftVersion();
        // remove patch version
        if (StringUtils.countMatches(baseVersion, ".") >= 2)
            baseVersion = StringUtils.split(baseVersion, ".")[0] + "." + StringUtils.split(baseVersion, ".")[1];
        BUKKIT_API_VERSION = baseVersion;
    }

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

        if (matcher == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.INVALID_QUERY, errorSource);

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
            return new ErrorResult(this, ErrorResult.ErrorCause.INVALID_QUERY, errorSource);

        HTTPResponse response = Requests.request(RequestContext.builder()
                .url("https://servermods.forgesvc.net/servermods/projects?search=" + slug)
                .build());

        if (response.getStatus() != HTTPResponse.RequestStatus.OK)
            return processErrorResponse(response, errorSource);

        JsonElement json = response.getAsJson();
        if (!json.isJsonArray())
            return new ErrorResult(this, ErrorResult.ErrorCause.SERVER_RESPONSE_MALFORMED, errorSource);

        JsonArray projectSearchResult = (JsonArray) json;

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
            return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, errorSource);

        return processFiles(slug, name, projectId, version, errorSource);
    }

    private ResolveResult processFiles(String slug, String name, long projectId, String version, ResolveResult.Source source)
    {
        HTTPResponse response = Requests.request(RequestContext.builder()
                .url("https://servermods.forgesvc.net/servermods/files?projectIds=" + projectId)
                .build());

        ErrorResult mayError = processErrorResponse(response, source);
        if (mayError != null)
            return mayError;

        JsonElement json = response.getAsJson();
        if (!json.isJsonArray())
            return new ErrorResult(this, ErrorResult.ErrorCause.SERVER_RESPONSE_MALFORMED, source);

        JsonArray projectFilesResult = (JsonArray) json;
        if (projectFilesResult.size() == 0)
            return new ErrorResult(this, ErrorResult.ErrorCause.ASSET_NOT_FOUND, source);

        JsonObject pickedPlugin = pickUpValidVersion(projectFilesResult, version);
        if (pickedPlugin == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.MATCH_PLUGIN_NOT_FOUND, source);

        String downloadUrl = pickedPlugin.get("downloadUrl").getAsString();
        String fileName = pickedPlugin.get("fileName").getAsString();
        String versionName = pickedPlugin.get("name").getAsString();

        return new CurseBukkitSuccessResult(this, downloadUrl, projectId, fileName, versionName, source, slug, name);
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
