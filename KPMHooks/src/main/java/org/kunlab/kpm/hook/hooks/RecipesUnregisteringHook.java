package org.kunlab.kpm.hook.hooks;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.hook.interfaces.KPMHook;

import java.util.ArrayList;

/**
 * プラグインのレシピが削除されるときに呼び出されるフックです。
 */
public class RecipesUnregisteringHook implements KPMHook
{
    /**
     * 削除対象のレシピを検索するときに呼び出されます。
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Searching extends RecipesUnregisteringHook
    {
        /**
         * 削除対象の名前空間です。
         */
        @NotNull
        ArrayList<String> targetNamespaces;

        /**
         * 名前空間を追加します。
         *
         * @param namespace 名前空間
         */
        public void addTarget(@NotNull String namespace)
        {
            this.targetNamespaces.add(namespace);
        }
    }

    /**
     * レシピが削除される前に呼び出されます。
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Pre extends RecipesUnregisteringHook
    {
        /**
         * 削除対象のレシピです。
         */
        private final Recipe recipe;

        /**
         * 削除をキャンセルするかどうかを表します。
         */
        private boolean cancelled;
    }

    /**
     * レシピが削除された後に呼び出されます。
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Post extends RecipesUnregisteringHook
    {
        /**
         * 削除対象のレシピです。
         */
        @NotNull
        Recipe recipe;
    }
}
