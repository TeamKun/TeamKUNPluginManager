package org.kunlab.kpm.kpminfo;

import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kunlab.kpm.hook.HookExecutorImpl;
import org.kunlab.kpm.interfaces.KPMRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class KPMInfoParserTest
{
    private static KPMRegistry getMockedRegistry()
    {
        KPMRegistry registry = mock(KPMRegistry.class);
        when(registry.getHookExecutor()).thenReturn(new HookExecutorImpl(registry));

        return registry;
    }

    private static Map<String, Object> required()
    {
        return MapUtil.$(
                "kpm", "3.0.0"
        );
    }

    @Nested
    public class KPMVersionTest
    {
        @Test
        void kpmがない場合は失敗するか()
        {
            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), MapUtil.$())
            );
        }

        @Test
        void 正しいkpmが成功するか()
        {
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), MapUtil.$("kpm", "3.0.0"))
            );
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), MapUtil.$("kpm", "3.0.0-pre1"))
            );

        }

        @Test
        void kpmがSemverでない場合は失敗するか()
        {
            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), MapUtil.$("kpm", "aaaa"))
            );

            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), MapUtil.$("kpm", ""))
            );
        }
    }

    @Nested
    public class UpdateQueryTest
    {
        @Test
        void updateQueryがない場合は無視されるか()
        {
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), required())
            );
        }

        @Test
        void 正しいupdateQueryが成功するか()
        {
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "update", "github>test/test")
                    )
            );
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$("kpm", "3.0.0", "update", "https://example.com/example.jar")
                    )
            );
        }

        @Test
        void updateQueryが空の場合は失敗するか()
        {
            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$("kpm", "3.0.0", "update", "")
                    )
            );
        }
    }

    @Nested
    public class HookRecipientTest
    {
        @Test
        void hooksがない場合は無視されるか()
        {
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), required())
            );
        }

        @Test
        void 正しいhooksが成功するか()
        {
            List<String> hooks = new ArrayList<>();
            hooks.add("org.kunlab.kpm.example.hooks.Examplehook");
            hooks.add("org.kunlab.kpm.example.hooks.Examplehook2");

            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "hooks", hooks)
                    )
            );
        }

        @Test
        void 不正な型のhooksが失敗するか()
        {
            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "hooks", new KPMInfoParserTest())
                            // List以外の型であればなんでもよい
                    )
            );
        }

        @Test
        void 不正な中身の型のhooksが失敗するか()
        {
            List<? super KPMInfoParserTest> hooks = new ArrayList<>();
            hooks.add(new KPMInfoParserTest());
            hooks.add(new KPMInfoParserTest());

            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "hooks", hooks)
                    )
            );
        }
    }

    @Nested
    public class RecipesTest
    {
        @Test
        void recipesがない場合は無視されるか()
        {
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), required())
            );
        }

        @Test
        void 正しいrecipesが成功するか()
        {
            List<String> recipes = new ArrayList<>();
            recipes.add("minecraft:iron_ingot");
            recipes.add("minecraft:gold_ingot");
            recipes.add("minecraft:diamond");

            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "recipes", recipes)
                    )
            );
        }

        @Test
        void 不正な型のrecipesが失敗するか()
        {
            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "recipes", new KPMInfoParserTest())
                            // List以外の型であればなんでもよい
                    )
            );
        }

        @Test
        void 不正な中身の型のrecipesが失敗するか()
        {
            List<? super KPMInfoParserTest> recipes = new ArrayList<>();
            recipes.add(new KPMInfoParserTest());
            recipes.add(new KPMInfoParserTest());

            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "recipes", recipes)
                    )
            );
        }
    }

    @Nested
    public class DependenciesTest
    {
        @Test
        void dependenciesがない場合は無視されるか()
        {
            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(getMockedRegistry(), required())
            );
        }

        @Test
        void 正しいdependenciesが成功するか()
        {
            Map<String, String> dependencies = new HashMap<>();
            dependencies.put("test", "github>test/test");
            dependencies.put("test2", "https://example.com/example.jar");
            dependencies.put("test3", "aliasTest==v3.0.1");

            assertDoesNotThrow(
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "dependencies", dependencies
                            )
                    ));
        }

        @Test
        void 不正な型のdependenciesが失敗するか()
        {
            assertThrows(
                    InvalidInformationFileException.class,
                    () -> KPMInfoParser.loadFromMap(
                            getMockedRegistry(),
                            MapUtil.$(required(), "dependencies", new KPMInfoParserTest())
                            // Map以外の型であればなんでもよい
                    )
            );
        }
    }
}
