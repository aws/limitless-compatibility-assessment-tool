// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;

import java.util.List;

public class IndexStmtManagerTest {

    private static ConfigLoader configLoader;
    private static IndexStmtManager manager;
    private static PostgreSQLParser.IndexstmtContext ctx;
    private static PostgreSQLParser.NameContext nameCtx;
    private static PostgreSQLParser.IdentifierContext identifierCtx;
    private static PostgreSQLParser.ColidContext colidCtx;
    private static TerminalNode identifierNode;
    private static PostgreSQLParser.Access_method_clauseContext access_method_clauseContext;
    private static PostgreSQLParser.Opt_uniqueContext opt_uniqueContext;
    private static PostgreSQLParser.Opt_concurrentlyContext opt_concurrentlyContext;

    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        manager = new IndexStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.IndexstmtContext.class);
        nameCtx = mock(PostgreSQLParser.NameContext.class);
        identifierCtx = mock(PostgreSQLParser.IdentifierContext.class);
        colidCtx = mock(PostgreSQLParser.ColidContext.class);
        identifierNode = mock(TerminalNode.class);
        access_method_clauseContext = mock(PostgreSQLParser.Access_method_clauseContext.class);
        opt_uniqueContext = mock(PostgreSQLParser.Opt_uniqueContext.class);
        opt_concurrentlyContext = mock(PostgreSQLParser.Opt_concurrentlyContext.class);
    }


    @Test
    public void test_indexCreate()
    {
        Feature feature = new Feature("index_create", true, null);
        when(ctx.access_method_clause()).thenReturn(access_method_clauseContext);
        when(ctx.opt_concurrently()).thenReturn(opt_concurrentlyContext);
        when(ctx.opt_unique()).thenReturn(opt_uniqueContext);
        when(access_method_clauseContext.USING()).thenReturn(null);
        when(opt_concurrentlyContext.CONCURRENTLY()).thenReturn(null);
        when(opt_uniqueContext.UNIQUE()).thenReturn(null);
        when(configLoader.getFeatureConfig("index_create")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
        assertEquals(feature.getContext(), result.getFeature().getContext());
    }

    @Test
    public void test_uniqueIndexCreate()
    {
        Feature feature = new Feature("index_create", true, null);
        Feature testFeature = new Feature("index_create_unique", true, null);
        when(ctx.access_method_clause()).thenReturn(access_method_clauseContext);
        when(ctx.opt_concurrently()).thenReturn(opt_concurrentlyContext);
        when(ctx.opt_unique()).thenReturn(opt_uniqueContext);
        when(access_method_clauseContext.USING()).thenReturn(null);
        when(opt_concurrentlyContext.CONCURRENTLY()).thenReturn(null);
        when(opt_uniqueContext.UNIQUE()).thenReturn(new TerminalNodeImpl(null));
        when(configLoader.getFeatureConfig("index_create")).thenReturn(feature);
        when(configLoader.getFeatureConfig("index_create_unique")).thenReturn(testFeature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
        assertEquals(testFeature.getContext(), result.getFeature().getContext());
    }

    @Test
    public void test_indexCreateInvalidType()
    {
        Feature feature = new Feature("index_create", true, null);
        Feature testFeature = new Feature("", false, "Index type dummyIndex is not supported");
        when(ctx.access_method_clause()).thenReturn(access_method_clauseContext);
        when(access_method_clauseContext.USING()).thenReturn(mock(TerminalNode.class));
        when(access_method_clauseContext.name()).thenReturn(nameCtx);
        when(nameCtx.colid()).thenReturn(colidCtx);
        when(colidCtx.identifier()).thenReturn(identifierCtx);
        when(identifierCtx.Identifier()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("dummyIndex");
        when(configLoader.getFeatureConfig("index_create")).thenReturn(feature);
        when(configLoader.isSupportedIndexType("dummyIndex")).thenReturn(false);
        when(ctx.opt_unique()).thenReturn(opt_uniqueContext);
        when(opt_uniqueContext.UNIQUE()).thenReturn(null);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(testFeature.getErrorMessage(), result.getFeature().getErrorMessage());
    }
}
