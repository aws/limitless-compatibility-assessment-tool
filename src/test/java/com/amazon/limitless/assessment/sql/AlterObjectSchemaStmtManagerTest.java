// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlterObjectSchemaStmtManagerTest {

    private static ConfigLoader configLoader;

    private static AlterObjectSchemaStmtManager manager;

    private static PostgreSQLParser.AlterobjectschemastmtContext ctx;

    private static PostgreSQLParser.NameContext nameCtx;
    
    private static PostgreSQLParser.IdentifierContext identifierCtx;

    private static PostgreSQLParser.ColidContext colidCtx;

    private static TerminalNode identifierNode;
    
    private String supportedExtension = "apgdbcc";

    private String unsupportedExtension = "non_existent_extension";

    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        manager = new AlterObjectSchemaStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.AlterobjectschemastmtContext.class);
        nameCtx = mock(PostgreSQLParser.NameContext.class);
        identifierCtx = mock(PostgreSQLParser.IdentifierContext.class);
        colidCtx = mock(PostgreSQLParser.ColidContext.class);
        identifierNode = mock(TerminalNode.class);
    }

    private void setupMocksForAlterObjectSchemaContext(String extensionName, boolean isSupported, Feature feature) {
        when(ctx.EXTENSION()).thenReturn(mock(TerminalNode.class));
        when(identifierNode.getText()).thenReturn(extensionName);
        when(identifierCtx.Identifier()).thenReturn(identifierNode);
        when(colidCtx.identifier()).thenReturn(identifierCtx);
        when(nameCtx.colid()).thenReturn(colidCtx);
        when(ctx.name()).thenReturn(Arrays.asList(nameCtx));
        when(configLoader.getFeatureConfig("alter_extension_set_schema")).thenReturn(feature);
        when(configLoader.isSupportedExtension(extensionName)).thenReturn(isSupported);
    }

    @Test
    public void test_alterObjectSchema_withSupportedExtension() {
        Feature feature = new Feature("alter_extension_set_schema", true, "");

        setupMocksForAlterObjectSchemaContext(supportedExtension, true, feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_alterObjectSchema_withUnsupportedExtension() {
        Feature feature = new Feature("", false, String.format("Extension %s is not supported", unsupportedExtension));

        setupMocksForAlterObjectSchemaContext(unsupportedExtension, false, feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterObjectSchema_withoutExtension() {
        Feature feature = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        when(ctx.EXTENSION()).thenReturn(null);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterObjectSchema_alterView() {
        Feature feature = new Feature("alter_view_change_schema", false, "ALTER VIEW CHANGE SCHEMA is not supported");
        when(ctx.VIEW()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_view_change_schema")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        assertFalse(resultList.get(0).getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), resultList.get(0).getFeature().getErrorMessage());
    }

    @Test
    public void test_alterObjectSchema_alterType() {
        Feature feature = new Feature("alter_type", true, "");
        when(ctx.TYPE_P()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("alter_type")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        assertTrue(resultList.get(0).getFeature().isSupported());
    }
    
    @Test
    public void test_alterTable_setSchema() {
        Feature feature = new Feature("alter_table_set_schema", true, "");

        when(ctx.ALTER()).thenReturn(mock(TerminalNode.class));
        when(ctx.TABLE()).thenReturn(mock(TerminalNode.class));
        when(ctx.SET()).thenReturn(mock(TerminalNode.class));
        when(ctx.SCHEMA()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("alter_table_set_schema")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }
}