// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;

import java.util.List;

public class RenameStmtManagerTest {

    private static ConfigLoader configLoader;

    private static RenameStmtManager manager;

    private static PostgreSQLParser.RenamestmtContext ctx;

    private static PostgreSQLParser.Opt_columnContext optColumnCtx;

    private static PostgreSQLParser.Relation_exprContext relationExprCtx;

    private static TerminalNode terminalNode;

    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        manager = new RenameStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.RenamestmtContext.class);
        terminalNode = mock(TerminalNode.class);
        optColumnCtx = mock(PostgreSQLParser.Opt_columnContext.class);
        relationExprCtx = mock(PostgreSQLParser.Relation_exprContext.class);
    }

    private void setupMocksForAlterTableRenameContext() {
        when(ctx.ALTER()).thenReturn(terminalNode);
        when(ctx.TABLE()).thenReturn(terminalNode);
        when(ctx.RENAME()).thenReturn(terminalNode);
    }

    @Test
    public void test_renameAlterIndex() {
        Feature feature = new Feature("rename_alter_index", false, "ALTER INDEX is not supported");
        when(ctx.INDEX()).thenReturn(terminalNode);
        when(configLoader.getFeatureConfig("rename_alter_index")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_renameAlterView()
    {
        RenameStmtManager manager = new RenameStmtManager(configLoader);
        PostgreSQLParser.RenamestmtContext ctx = mock(
            PostgreSQLParser.RenamestmtContext.class
        );
        Feature feature = new Feature("alter_view_rename", false, "RENAME VIEW is not supported");
        when(ctx.VIEW()).thenReturn(terminalNode);
        when(configLoader.getFeatureConfig("alter_view_rename")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_renameAlterType()
    {
        RenameStmtManager manager = new RenameStmtManager(configLoader);
        PostgreSQLParser.RenamestmtContext ctx = mock(
            PostgreSQLParser.RenamestmtContext.class
        );
        Feature feature = new Feature("alter_type", true, "");
        when(ctx.TYPE_P()).thenReturn(terminalNode);
        when(configLoader.getFeatureConfig("alter_type")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_alterTable_withRenameColumnOnly() {
        Feature feature = new Feature("alter_table_only", false, "ALTER TABLE ONLY is not supported");
        setupMocksForAlterTableRenameContext();

        when(ctx.opt_column()).thenReturn(optColumnCtx);
        when(optColumnCtx.COLUMN()).thenReturn(terminalNode);
        when(ctx.relation_expr()).thenReturn(relationExprCtx);
        when(relationExprCtx.ONLY()).thenReturn(terminalNode);
        when(configLoader.getFeatureConfig("alter_table_only")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterTable_withRenameColumn() {
        Feature feature = new Feature("alter_table_rename_column", true, "");
        setupMocksForAlterTableRenameContext();

        when(ctx.opt_column()).thenReturn(optColumnCtx);
        when(optColumnCtx.COLUMN()).thenReturn(terminalNode);
        when(configLoader.getFeatureConfig("alter_table_rename_column")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_alterTable_withRenameColumn_constraint() {
        Feature feature = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        setupMocksForAlterTableRenameContext();

        when(ctx.CONSTRAINT()).thenReturn(terminalNode);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterTable_withRename() {
        Feature feature = new Feature("alter_table_rename", true, "");
        setupMocksForAlterTableRenameContext();

        when(configLoader.getFeatureConfig("alter_table_rename")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }
}
