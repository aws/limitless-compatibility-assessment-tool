// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateSchemaStmtManagerTest {
    private static ConfigLoader configLoader;


    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_createSchema() {
        CreateSchemaStmtManager manager = new CreateSchemaStmtManager(configLoader);
        PostgreSQLParser.CreateschemastmtContext ctx = mock(
            PostgreSQLParser.CreateschemastmtContext.class
        );
        Feature feature = new Feature("create_schema", true, null);
        PostgreSQLParser.OptschemaeltlistContext optschemaeltlistContext = mock(PostgreSQLParser.OptschemaeltlistContext.class);
        List<PostgreSQLParser.Schema_stmtContext> list = new ArrayList<>();
        when(optschemaeltlistContext.schema_stmt()).thenReturn(list);
        when(ctx.optschemaeltlist()).thenReturn(optschemaeltlistContext);
        when(configLoader.getFeatureConfig("create_schema")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_createSchema_withElements() {
        CreateSchemaStmtManager manager = new CreateSchemaStmtManager(configLoader);
        PostgreSQLParser.CreateschemastmtContext ctx = mock(
            PostgreSQLParser.CreateschemastmtContext.class
        );
        Feature feature = new Feature("create_schema_with_elements", false,
            "CREATE SCHEMA WITH ELEMENTS not supported");
        PostgreSQLParser.OptschemaeltlistContext optschemaeltlistContext = mock(PostgreSQLParser.OptschemaeltlistContext.class);
        List<PostgreSQLParser.Schema_stmtContext> list = new ArrayList<>();
        list.add(new PostgreSQLParser.Schema_stmtContext(new ParserRuleContext(), 0));
        when(optschemaeltlistContext.schema_stmt()).thenReturn(list);
        when(ctx.optschemaeltlist()).thenReturn(optschemaeltlistContext);
        when(configLoader.getFeatureConfig("create_schema_with_elements")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
        
    }
}
