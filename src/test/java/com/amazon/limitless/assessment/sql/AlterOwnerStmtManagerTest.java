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

public class AlterOwnerStmtManagerTest {
    private static ConfigLoader configLoader;


    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_alterOwnerSchema()
    {
        AlterOwnerStmtManager manager = new AlterOwnerStmtManager(configLoader);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_schema_owner_to", true, null);
        when(ctx.SCHEMA()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_schema_owner_to")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_alterOwnerAggregate()
    {
        AlterOwnerStmtManager manager = new AlterOwnerStmtManager(configLoader);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_aggregate_owner_to", false, "ALTER AGGREGATE OWNER TO is not supported");
        when(ctx.AGGREGATE()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_aggregate_owner_to")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterOwnerStatistics()
    {
        AlterOwnerStmtManager manager = new AlterOwnerStmtManager(configLoader);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_statistics_owner_to", false, "ALTER STATISTICS OWNER TO is not supported" );
        when(ctx.STATISTICS()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_statistics_owner_to")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterSubscription()
    {
        AlterOwnerStmtManager manager = new AlterOwnerStmtManager(configLoader);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_subscription_owner_to", false, "ALTER SUBSCRIPTION OWNER TO is not supported" );
        when(ctx.SUBSCRIPTION()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_subscription_owner_to")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterOwnerCollation()
    {
        AlterOwnerStmtManager manager = new AlterOwnerStmtManager(configLoader);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_collation_owner_to", true, null);
        when(ctx.SCHEMA()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_collation_owner_to")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_alterOwnerFunction()
    {
        AlterOwnerStmtManager manager = new AlterOwnerStmtManager(configLoader);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_function", true, null);
        when(ctx.SCHEMA()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_function")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_alterOwnerType()
    {
        AlterOwnerStmtManager manager = new AlterOwnerStmtManager(configLoader);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_type", true, null);
        when(ctx.TYPE_P()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_type")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }
}
