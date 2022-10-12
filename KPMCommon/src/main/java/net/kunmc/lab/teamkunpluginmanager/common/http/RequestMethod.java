package net.kunmc.lab.teamkunpluginmanager.common.http;

/**
 * HTTPリクエストのメソッドを表す列挙型です。
 */
public enum RequestMethod
{
    /**
     * GETメソッドです。
     */
    GET,
    /**
     * POSTメソッドです。
     */
    POST,
    /**
     * PUTメソッドです。
     */
    PUT,
    /**
     * DELETEメソッドです。
     */
    DELETE,
    /**
     * HEADメソッドです。
     */
    HEAD,
    /**
     * OPTIONSメソッドです。
     */
    OPTIONS,
    /**
     * TRACEメソッドです。
     */
    TRACE
}
