// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

public class CreateExtensionStmtManagerTest {
    
    private static ConfigLoader configLoader;

    private static CreateExtensionStmtManager manager;

    private static PostgreSQLParser.CreateextensionstmtContext ctx;

    private static PostgreSQLParser.NameContext nameCtx;
    
    private static PostgreSQLParser.IdentifierContext identifierCtx;

    private static PostgreSQLParser.ColidContext colidCtx;

    private static TerminalNode identifierNode;
    
    private String supportedExtension = "apgdbcc";

    private String unsupportedExtension = "non_existent_extension";


    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        manager = new CreateExtensionStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.CreateextensionstmtContext.class);
        nameCtx = mock(PostgreSQLParser.NameContext.class);
        identifierCtx = mock(PostgreSQLParser.IdentifierContext.class);
        colidCtx = mock(PostgreSQLParser.ColidContext.class);
        identifierNode = mock(TerminalNode.class);
    }

    private void setupMocksForCreateExtensionContext(String extensionName, boolean isSupported, Feature feature) {
        when(configLoader.isSupportedExtension(extensionName)).thenReturn(true);
        when(identifierNode.getText()).thenReturn(extensionName);
        when(identifierCtx.Identifier()).thenReturn(identifierNode);
        when(colidCtx.identifier()).thenReturn(identifierCtx);
        when(nameCtx.colid()).thenReturn(colidCtx);
        when(ctx.name()).thenReturn(nameCtx);
        when(configLoader.getFeatureConfig("create_extension")).thenReturn(feature);
    }

    @Test
    public void test_createExtension_withSupportedExtension() {
        Feature feature = new Feature("create_extension", true, null);

        setupMocksForCreateExtensionContext(supportedExtension, true, feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_createExtension_withUnsupportedExtension() {
        Feature feature = new Feature("", false, String.format("Extension %s is not supported", unsupportedExtension));
        
        setupMocksForCreateExtensionContext(unsupportedExtension, false, feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createExtension_withNullContext() {
        List<StatementResult> resultList = manager.analyzeStatement(null);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals("Could not process create extension statement", result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createExtension_withNullName() {
        when(ctx.name()).thenReturn(null);
        
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals("Could not process create extension statement", result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createExtension_withNullColid() {
        when(ctx.name()).thenReturn(nameCtx);
        when(nameCtx.colid()).thenReturn(null);
        
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals("Could not process create extension statement", result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createExtension_withNullIdentifier() {
        when(ctx.name()).thenReturn(nameCtx);
        when(nameCtx.colid()).thenReturn(colidCtx);
        when(colidCtx.identifier()).thenReturn(null);
        
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals("Could not process create extension statement", result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createExtension_withNullIdentifierToken() {
        when(ctx.name()).thenReturn(nameCtx);
        when(nameCtx.colid()).thenReturn(colidCtx);
        when(colidCtx.identifier()).thenReturn(identifierCtx);
        when(identifierCtx.Identifier()).thenReturn(null);
        
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals("Could not process create extension statement", result.getFeature().getErrorMessage());
    }
}
