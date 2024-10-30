// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlterObjectDependsStmtManagerTest {
    private static ConfigLoader configLoader;


    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_alterTriggerDependsOn()
    {
        AlterObjectDependsStmtManager manager = new AlterObjectDependsStmtManager(configLoader);
        PostgreSQLParser.AlterobjectdependsstmtContext ctx = mock(
            PostgreSQLParser.AlterobjectdependsstmtContext.class
        );
        Feature feature = new Feature("alter_trigger_depends_on", false, "ALTER TRIGGER DEPENDS ON is not supported");
        when(ctx.TRIGGER()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_trigger_depends_on")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterMaterializedViewDependsOn()
    {
        AlterObjectDependsStmtManager manager = new AlterObjectDependsStmtManager(configLoader);
        PostgreSQLParser.AlterobjectdependsstmtContext ctx = mock(
            PostgreSQLParser.AlterobjectdependsstmtContext.class
        );
        Feature feature = new Feature("alter_materialized_view_depends_on", false, "ALTER MATERIALIZED VIEW OWNER TO is not supported");
        when(ctx.MATERIALIZED()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_materialized_view_depends_on")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterIndexDependsOn()
    {
        AlterObjectDependsStmtManager manager = new AlterObjectDependsStmtManager(configLoader);
        PostgreSQLParser.AlterobjectdependsstmtContext ctx = mock(
            PostgreSQLParser.AlterobjectdependsstmtContext.class
        );
        Feature feature = new Feature("alter_object_depend_alter_index", false, "ALTER INDEX is not supported");
        when(ctx.INDEX()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_object_depend_alter_index")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }
}
