package org.kunlab.kpm.resolver.impl.github;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GitHub の URL をパースするユーティリティ・クラスです。
 */
@UtilityClass
public class GHURLParser
{
    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("^/(?<repository>(?<owner>[a-zA-Z\\d]" +
            "(?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38})/(?<repoName>\\w(?:\\w|-(?=\\w)){0,100}))" +
            "(?:/(?:tags|releases(?:/(?:tag/(?<tag>[^/]+)/?$|download/(?<downloadTag>[^/]+)/" +
            "(?<fileName>[^/]+)))?))?/?$");

    @NotNull
    /* non-public */ static GHURLParseResult parse(@NotNull String url)
    {
        URL urlObj;
        try
        {
            urlObj = new URL(url);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }

        Matcher matcher = GITHUB_REPO_PATTERN.matcher(urlObj.getPath());

        String repository = null;
        String owner = null;
        String tag = null;
        String repositoryName = null;
        String finalName = null;

        while (matcher.find())
        {
            String repositoryGroup = matcher.group("repository");
            String downloadTagGroup = matcher.group("downloadTag");
            String tagGroup = matcher.group("tag");
            String fileNameGroup = matcher.group("fileName");
            String ownerGroup = matcher.group("owner");
            String repoNameGroup = matcher.group("repoName");

            if (fileNameGroup != null && !fileNameGroup.isEmpty())
                finalName = fileNameGroup;

            if (!repositoryGroup.isEmpty())
                repository = repositoryGroup;

            if (downloadTagGroup != null && !downloadTagGroup.isEmpty())
                tag = downloadTagGroup;
            else if (tagGroup != null && !tagGroup.isEmpty())
                tag = tagGroup;

            if (ownerGroup != null && !ownerGroup.isEmpty())
                owner = ownerGroup;

            if (repoNameGroup != null && !repoNameGroup.isEmpty())
                repositoryName = repoNameGroup;
        }

        if (repository == null || owner == null || repositoryName == null)
            throw new IllegalArgumentException("Invalid GitHub URL: " + urlObj);

        return new GHURLParseResult(owner, repositoryName, repository, tag, finalName);
    }
}
