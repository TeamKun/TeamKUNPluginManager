package org.kunlab.kpm.kpminfo;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class MapUtil
{
    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6,
                                        String k7, Object v7, String k8, Object v8,
                                        String k9, Object v9)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
            this.put(k7, v7);
            this.put(k8, v8);
            this.put(k9, v9);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6,
                                        String k7, Object v7, String k8, Object v8)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
            this.put(k7, v7);
            this.put(k8, v8);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6,
                                        String k7, Object v7)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
            this.put(k7, v7);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1, String k2, Object v2)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
            this.put(k2, v2);
        }};
    }

    public static Map<String, Object> $(String k1, Object v1)
    {
        return new HashMap<String, Object>()
        {{
            this.put(k1, v1);
        }};
    }

    public static Map<String, Object> $()
    {
        return new HashMap<>();
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6,
                                        String k7, Object v7, String k8, Object v8,
                                        String k9, Object v9)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
            this.put(k7, v7);
            this.put(k8, v8);
            this.put(k9, v9);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6,
                                        String k7, Object v7, String k8, Object v8)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
            this.put(k7, v7);
            this.put(k8, v8);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6,
                                        String k7, Object v7)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
            this.put(k7, v7);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5, String k6, Object v6)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
            this.put(k6, v6);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4,
                                        String k5, Object v5)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
            this.put(k5, v5);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3, String k4, Object v4)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
            this.put(k4, v4);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2,
                                        String k3, Object v3)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
            this.put(k3, v3);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1, String k2, Object v2)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
            this.put(k2, v2);
        }};
    }

    public static Map<String, Object> $(Map<String, Object> base,
                                        String k1, Object v1)
    {
        return new HashMap<String, Object>(base)
        {{
            this.put(k1, v1);
        }};
    }


}
