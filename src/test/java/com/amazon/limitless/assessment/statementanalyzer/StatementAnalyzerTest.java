// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.statementanalyzer;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.sql.AlterOwnerStmtManager;
import com.amazon.limitless.assessment.sql.CreateSchemaStmtManager;
import com.amazon.limitless.assessment.sql.CreateExtensionStmtManager;
import com.amazon.limitless.assessment.sql.AlterExtensionContentsStmtManager;
import com.amazon.limitless.assessment.sql.AlterExtensionStmtManager;
import com.amazon.limitless.assessment.sql.AlterObjectSchemaStmtManager;
import com.amazon.limitless.assessment.sql.CreateAmStmtManager;
import com.amazon.limitless.assessment.sql.DefineStmtManager;
import com.amazon.limitless.assessment.sql.CreateCastStmtManager;
import com.amazon.limitless.assessment.sql.CreateDomainStmtManager;
import com.amazon.limitless.assessment.sql.CreateForeignTableStmtManager;
import com.amazon.limitless.assessment.sql.CreateMatViewStmtManager;
import com.amazon.limitless.assessment.sql.GrantStmtManager;
import com.amazon.limitless.assessment.sql.RuleStmtManager;
import com.amazon.limitless.assessment.sql.CreateStatsStmtManager;
import com.amazon.limitless.assessment.sql.CreateSubscriptionStmtManager;
import com.amazon.limitless.assessment.sql.CreateTrigStmtManager;
import com.amazon.limitless.assessment.sql.DefaultUnsupportedStmtManager;
import com.amazon.limitless.assessment.sql.AlterObjectDependsStmtManager;
import com.amazon.limitless.assessment.sql.AlterSubscriptionStmtManager;
import com.amazon.limitless.assessment.sql.AlterTableStmtManager;
import com.amazon.limitless.assessment.sql.RenameStmtManager;
import com.amazon.limitless.assessment.sql.IndexStmtManager;
import com.amazon.limitless.assessment.sql.CreateTableStmtManager;
import com.amazon.limitless.assessment.sql.CreateSeqStmtManager;
import com.amazon.limitless.assessment.sql.AlterSeqStmtManager;
import com.amazon.limitless.assessment.sql.CreatePolicyStmtManager;
import com.amazon.limitless.assessment.sql.AlterPolicyStmtManager;
import com.amazon.limitless.assessment.sql.CreateOpClassStmtManager;
import com.amazon.limitless.assessment.sql.CreateOpFamilyStmtManager;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;

import java.util.Collections;
import java.util.List;

public class StatementAnalyzerTest {
    private static ConfigLoader configLoader;
    private StatementAnalyzer statementAnalyzer;

    @BeforeAll
    public static void setupClass() {
        configLoader = mock(ConfigLoader.class);
    }

    @BeforeEach
    public void setup() {
        statementAnalyzer = new StatementAnalyzer(configLoader);
    }

    @Test
    public void test_statementAnalyzer_createSchema() throws NoSuchFieldException, IllegalAccessException {
        CreateSchemaStmtManager manager = mock(CreateSchemaStmtManager.class);
        PostgreSQLParser.CreateschemastmtContext ctx = mock(
            PostgreSQLParser.CreateschemastmtContext.class
        );
        Feature feature = new Feature("create_schema", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createSchemaStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }


    @Test
    public void test_statementAnalyzer_alterOwner() throws IllegalAccessException, NoSuchFieldException {
        AlterOwnerStmtManager manager = mock(AlterOwnerStmtManager.class);
        PostgreSQLParser.AlterownerstmtContext ctx = mock(
            PostgreSQLParser.AlterownerstmtContext.class
        );
        Feature feature = new Feature("alter_schema_owner_to", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterOwnerStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.SCHEMA, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createExtension() throws NoSuchFieldException, IllegalAccessException {
        CreateExtensionStmtManager manager = mock(CreateExtensionStmtManager.class);
        PostgreSQLParser.CreateextensionstmtContext ctx = mock(
            PostgreSQLParser.CreateextensionstmtContext.class
        );
        Feature feature = new Feature("create_extension", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createExtensionStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterExtension() throws NoSuchFieldException, IllegalAccessException {
        AlterExtensionStmtManager manager = mock(AlterExtensionStmtManager.class);
        PostgreSQLParser.AlterextensionstmtContext ctx = mock(
            PostgreSQLParser.AlterextensionstmtContext.class
        );
        Feature feature = new Feature("alter_extension_update_version", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterExtensionStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterExtensionContents() throws NoSuchFieldException, IllegalAccessException {
        AlterExtensionContentsStmtManager manager = mock(AlterExtensionContentsStmtManager.class);
        PostgreSQLParser.AlterextensioncontentsstmtContext ctx = mock(
            PostgreSQLParser.AlterextensioncontentsstmtContext.class
        );
        Feature feature = new Feature("alter_extension_add_object", false, "ALTER EXTENSION ADD OBJECT is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterExtensionContentsStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterExtensionSchema() throws NoSuchFieldException, IllegalAccessException {
        AlterObjectSchemaStmtManager manager = mock(AlterObjectSchemaStmtManager.class);
        PostgreSQLParser.AlterobjectschemastmtContext ctx = mock(
            PostgreSQLParser.AlterobjectschemastmtContext.class
        );
        Feature feature = new Feature("alter_extension_set_schema", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterObjectSchemaStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);
        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_statementAnalyzer_createTable() throws NoSuchFieldException, IllegalAccessException {
        CreateTableStmtManager manager = mock(CreateTableStmtManager.class);
        PostgreSQLParser.CreatestmtContext ctx = mock(
            PostgreSQLParser.CreatestmtContext.class
        );
        Feature feature = new Feature("create_table_standard", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createTableStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);
        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
    }
    
    @Test
    public void test_statementAnalyzer_createAm() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateAmStmtManager manager = mock(CreateAmStmtManager.class);
        PostgreSQLParser.CreateamstmtContext ctx = mock(
            PostgreSQLParser.CreateamstmtContext.class
        );
        Feature feature = new Feature("create_access_method", false, "CREATE ACCESS METHOD is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createAmStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_define() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        DefineStmtManager manager = mock(DefineStmtManager.class);
        PostgreSQLParser.DefinestmtContext ctx = mock(
            PostgreSQLParser.DefinestmtContext.class
        );
        Feature feature = new Feature("create_aggregate", false, "CREATE AGGREGATE is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("defineStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createCast() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateCastStmtManager manager = mock(CreateCastStmtManager.class);
        PostgreSQLParser.CreatecaststmtContext ctx = mock(
            PostgreSQLParser.CreatecaststmtContext.class
        );
        Feature feature = new Feature("create_cast", false, "CREATE CAST is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createCastStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createDomain() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateDomainStmtManager manager = mock(CreateDomainStmtManager.class);
        PostgreSQLParser.CreatedomainstmtContext ctx = mock(
            PostgreSQLParser.CreatedomainstmtContext.class
        );
        Feature feature = new Feature("create_domain", false, "CREATE DOMAIN is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createDomainStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createForeignTable() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateForeignTableStmtManager manager = mock(CreateForeignTableStmtManager.class);
        PostgreSQLParser.CreateforeigntablestmtContext ctx = mock(
            PostgreSQLParser.CreateforeigntablestmtContext.class
        );
        Feature feature = new Feature("create_foreign_table", false, "CREATE FOREIGN TABLE is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createForeignTableStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createMatView() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateMatViewStmtManager manager = mock(CreateMatViewStmtManager.class);
        PostgreSQLParser.CreatematviewstmtContext ctx = mock(
            PostgreSQLParser.CreatematviewstmtContext.class
        );
        Feature feature = new Feature("create_materialized_view", false, "CREATE MATERIALIZED VIEW is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createMatViewStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_rule() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        RuleStmtManager manager = mock(RuleStmtManager.class);
        PostgreSQLParser.RulestmtContext ctx = mock(
            PostgreSQLParser.RulestmtContext.class
        );
        Feature feature = new Feature("create_rule", false, "CREATE RULE is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("ruleStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createStats() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateStatsStmtManager manager = mock(CreateStatsStmtManager.class);
        PostgreSQLParser.CreatestatsstmtContext ctx = mock(
            PostgreSQLParser.CreatestatsstmtContext.class
        );
        Feature feature = new Feature("create_statistics", false, "CREATE STATISTICS is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createStatsStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createSubscription() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateSubscriptionStmtManager manager = mock(CreateSubscriptionStmtManager.class);
        PostgreSQLParser.CreatesubscriptionstmtContext ctx = mock(
            PostgreSQLParser.CreatesubscriptionstmtContext.class
        );
        Feature feature = new Feature("create_subscription", false, "CREATE SUBSCRIPTION is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createSubscriptionStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createTrig() throws NoSuchFieldException, IllegalAccessException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateTrigStmtManager manager = mock(CreateTrigStmtManager.class);
        PostgreSQLParser.CreatetrigstmtContext ctx = mock(
            PostgreSQLParser.CreatetrigstmtContext.class
        );
        Feature feature = new Feature("create_trigger", false, "CREATE TRIGGER is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createTrigStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterSubscription() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        AlterSubscriptionStmtManager manager = mock(AlterSubscriptionStmtManager.class);
        PostgreSQLParser.AltersubscriptionstmtContext ctx = mock(
            PostgreSQLParser.AltersubscriptionstmtContext.class
        );
        Feature feature = new Feature("alter_subscription", false, "ALTER SUBSCRIPTION is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterSubscriptionStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterObjectDepends() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        AlterObjectDependsStmtManager manager = mock(AlterObjectDependsStmtManager.class);
        PostgreSQLParser.AlterobjectdependsstmtContext ctx = mock(
            PostgreSQLParser.AlterobjectdependsstmtContext.class
        );
        Feature feature = new Feature("alter_trigger_depends_on", false, "ALTER TRIGGER DEPENDS ON is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterObjectDependsStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.TRIGGER, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterTables() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        AlterTableStmtManager manager = mock(AlterTableStmtManager.class);
        PostgreSQLParser.AltertablestmtContext ctx = mock(
            PostgreSQLParser.AltertablestmtContext.class
        );
        Feature feature = new Feature("alter_materialized_view", false, "ALTER MATERIALIZED VIEW is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterTableStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.TRIGGER, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_rename() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        RenameStmtManager manager = mock(RenameStmtManager.class);
        PostgreSQLParser.RenamestmtContext ctx = mock(
            PostgreSQLParser.RenamestmtContext.class
        );
        Feature feature = new Feature("rename_alter_index", false, "ALTER INDEX is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("renameStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.TRIGGER, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_index() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        IndexStmtManager manager = mock(IndexStmtManager.class);
        PostgreSQLParser.IndexstmtContext ctx = mock(
            PostgreSQLParser.IndexstmtContext.class
        );
        Feature feature = new Feature("index_create_concurrent", false, "Concurrent CREATE INDEX is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("indexStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.TRIGGER, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createSequence() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateSeqStmtManager manager = mock(CreateSeqStmtManager.class);
        PostgreSQLParser.CreateseqstmtContext ctx = mock(
            PostgreSQLParser.CreateseqstmtContext.class
        );
        Feature feature = new Feature("create_temp_sequnece", false, "CREATE TEMP SEQUENCE is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createSeqStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.SEQUENCE, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterSequence() throws NoSuchFieldException, IllegalAccessException {
        AlterSeqStmtManager manager = mock(AlterSeqStmtManager.class);
        PostgreSQLParser.AlterseqstmtContext ctx = mock(
            PostgreSQLParser.AlterseqstmtContext.class
        );
        Feature feature = new Feature("alter_sequence_owned_by", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterSeqStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.SEQUENCE, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createPolicy() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreatePolicyStmtManager manager = mock(CreatePolicyStmtManager.class);
        PostgreSQLParser.CreatepolicystmtContext ctx = mock(
            PostgreSQLParser.CreatepolicystmtContext.class
        );
        Feature feature = new Feature("create_policy", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createPolicyStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.POLICY, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_alterPolicy() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        AlterPolicyStmtManager manager = mock(AlterPolicyStmtManager.class);
        PostgreSQLParser.AlterpolicystmtContext ctx = mock(
            PostgreSQLParser.AlterpolicystmtContext.class
        );
        Feature feature = new Feature("alter_policy", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("alterPolicyStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.POLICY, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_createOperatorClass() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateOpClassStmtManager manager = mock(CreateOpClassStmtManager.class);
        PostgreSQLParser.CreateopclassstmtContext ctx = mock(
            PostgreSQLParser.CreateopclassstmtContext.class
        );
        Feature feature = new Feature("create_operator_class", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createOpClassStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.OPERATOR, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test    
    public void test_statementAnalyzer_createOperatorFamily() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        CreateOpFamilyStmtManager manager = mock(CreateOpFamilyStmtManager.class);
        PostgreSQLParser.CreateopfamilystmtContext ctx = mock(
            PostgreSQLParser.CreateopfamilystmtContext.class
        );
        Feature feature = new Feature("create_operator_family", true, null);
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("createOpFamilyStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.OPERATOR, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertTrue(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
    }

    @Test
    public void test_statementAnalyzer_defaultUnsupported() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        DefaultUnsupportedStmtManager manager = mock(DefaultUnsupportedStmtManager.class);
        PostgreSQLParser.CreatefdwstmtContext ctx = mock(PostgreSQLParser.CreatefdwstmtContext.class);

        Feature feature = new Feature("unsupported", false, "This statement is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("defaultUnsupportedStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(manager.analyzeStatement()).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement();
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE.getContext());
        assertEquals(result.getFeature().getErrorMessage(), ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE.getErrorMessage());
    }

    @Test
    public void test_statementAnalyzer_grantStmt() throws IllegalAccessException, NoSuchFieldException {
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer(configLoader);
        GrantStmtManager manager = mock(GrantStmtManager.class);
        PostgreSQLParser.GrantstmtContext ctx = mock(
            PostgreSQLParser.GrantstmtContext.class
        );
        Feature feature = new Feature("grant_privilege_on_type", false,
            "GRANT PRIVILEGE ON TYPE is not supported");
        StatementResult expectedResult = new StatementResult();
        expectedResult.setFeature(feature);
        List<StatementResult> expectedResultList = Collections.singletonList(expectedResult);
        Field privateField = StatementAnalyzer.class.getDeclaredField("grantStmtManager");
        privateField.setAccessible(true);
        privateField.set(statementAnalyzer, manager);
        when(ctx.getToken(PostgreSQLParser.GRANT, 0)).thenReturn(any());
        when(manager.analyzeStatement(ctx)).thenReturn(expectedResultList);

        List<StatementResult> resultList = statementAnalyzer.parse(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        verify(manager, times(1)).analyzeStatement(ctx);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), expectedResult.getFeature().getContext());
        assertEquals(result.getFeature().getErrorMessage(), expectedResult.getFeature().getErrorMessage());

    }

}
