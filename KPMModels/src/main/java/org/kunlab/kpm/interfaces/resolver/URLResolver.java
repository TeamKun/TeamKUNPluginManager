package org.kunlab.kpm.interfaces.resolver;

import org.kunlab.kpm.resolver.QueryContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL を解決するリゾルバのインターフェースです。
 */
public interface URLResolver extends BaseResolver
{

    /**
     * このリゾルバが対応してるURLのホスト名を返します。
     *
     * @return このリゾルバが対応してるURLのホスト名
     */
    String[] getHosts();

    /**
     * このリゾルバがクエリに対応しているかを返します。
     *
     * @param query クエリ (URL)
     * @return このリゾルバがクエリに対応しているか
     */
    @Override
    default boolean isValidResolver(QueryContext query)
    {
        try
        {
            URL url = new URL(query.getQuery());

            if (this.getHosts().length == 0)
                return true;

            for (String host : this.getHosts())
                if (url.getHost().equalsIgnoreCase(host))
                    return true;
        }
        catch (MalformedURLException e)
        {
            return false;
        }
        return false;
    }

    default Matcher urlMatcher(Pattern pattern, String urlString)
    {
        URL url;
        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            return null;
        }

        return pattern.matcher(url.getPath());
    }
}
