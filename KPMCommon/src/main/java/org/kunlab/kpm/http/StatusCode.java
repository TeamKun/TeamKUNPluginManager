package org.kunlab.kpm.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HTTP ステータスコードの列挙です。
 */
@AllArgsConstructor
@Getter
public enum StatusCode
{
    // Internal
    UNKNOWN(-1, "Unknown"),

    // 1xx(Informational): リクエストを受け付けました。
    RANGE_INFORMATIONAL_BEGIN(100, "Informational"),

    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),
    EARLY_HINTS(103, "Early Hints"),

    RANGE_INFORMATIONAL_END(199, "Informational"),

    // 2xx(Success): リクエストは正常に処理されました。
    RANGE_SUCCESS_BEGIN(200, "Success"),

    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MULTI_STATUS(207, "Multi-Status"),
    ALREADY_REPORTED(208, "Already Reported"),
    // Missing 209 -> 225
    IM_USED(226, "IM Used"),

    RANGE_SUCCESS_END(299, "Success"),

    // 3xx(Redirection): リクエストを完了するには追加のアクションが必要です。
    RANGE_REDIRECTION_BEGIN(300, "Redirection"),

    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    // Missing 306
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    RANGE_REDIRECTION_END(399, "Redirection"),

    // 4xx(Client Error): リクエストに問題があります。
    RANGE_CLIENT_ERROR_BEGIN(400, "Client Error"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    URI_TOO_LONG(414, "URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    I_AM_A_TEAPOT(418, "I'm a teapot"),
    // Missing 419 -> 420
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    LOCKED(423, "Locked"),
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    TOO_EARLY(425, "Too Early"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    // Missing 427
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    // Missing 430
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    // Missing 432 -> 450
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    RANGE_CLIENT_ERROR_END(499, "Client Error"),

    // 5xx(Server Error): サーバに問題があります。
    RANGE_SERVER_ERROR_BEGIN(500, "Server Error"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    LOOP_DETECTED(508, "Loop Detected"),
    // Missing 509
    NOT_EXTENDED(510, "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required"),

    RANGE_SERVER_ERROR_END(599, "Server Error");

    private final int code;
    private final String message;

    private static boolean isInRange(int code, StatusCode min, StatusCode max)
    {
        return min.code <= code && code <= max.code;
    }

    /**
     * レスポンスコードに対応する {@link StatusCode} を返します。
     */
    public static StatusCode valueOf(int code)
    {
        return Arrays.stream(values()).parallel()
                .filter(v -> v.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * OK かどうかを判定します。
     */
    public boolean isOK()
    {
        return this == OK;
    }

    /**
     * 1xx(Informational) かどうかを判定します。
     */
    public boolean isInformational()
    {
        return isInRange(this.code, RANGE_INFORMATIONAL_BEGIN, RANGE_INFORMATIONAL_END);
    }

    /**
     * 成功レスポンスかどうかを判定します。
     */
    public boolean isSuccess()
    {
        return isInRange(this.code, RANGE_SUCCESS_BEGIN, RANGE_SUCCESS_END);
    }

    /**
     * リダイレクトレスポンスかどうかを判定します。
     */
    public boolean isRedirect()
    {
        return isInRange(this.code, RANGE_REDIRECTION_BEGIN, RANGE_REDIRECTION_END);
    }

    /**
     * クライアントエラーレスポンスかどうかを判定します。
     */
    public boolean isClientError()
    {
        return isInRange(this.code, RANGE_CLIENT_ERROR_BEGIN, RANGE_CLIENT_ERROR_END);
    }

    /**
     * サーバエラーレスポンスかどうかを判定します。
     */
    public boolean isServerError()
    {
        return isInRange(this.code, RANGE_SERVER_ERROR_BEGIN, RANGE_SERVER_ERROR_END);
    }

    /**
     * エラーレスポンスかどうかを判定します。
     * レスポンスが不明の場合もエラーレスポンスとみなします。
     */
    public boolean isError()
    {
        return this.isClientError() || this.isServerError() || this == UNKNOWN;
    }

    @Override
    public String toString()
    {
        return this.code + " " + this.message;
    }
}
