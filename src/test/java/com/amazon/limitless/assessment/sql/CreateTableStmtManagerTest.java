// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.common.DependencyObject;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateTableStmtManagerTest {
    private static ConfigLoader configLoader;
    private static DependencyObject dependencyObject;
    private static CreateTableStmtManager manager;
    private static PostgreSQLParser.CreatestmtContext ctx;
    private static PostgreSQLParser.OncommitoptionContext onCommitOptionCtx;
    private static PostgreSQLParser.OptwithContext optWithCtx;
    private static PostgreSQLParser.PartitionspecContext partitionSpecCtx;
    private static PostgreSQLParser.TablelikeclauseContext tableLikeClauseCtx;
    private static PostgreSQLParser.Qualified_nameContext qualified_nameCtx;
    private static PostgreSQLParser.IdentifierContext identifierCtx;
    private static PostgreSQLParser.ColidContext colidCtx;
    private static TerminalNode identifierNode;
    private static PostgreSQLParser.IndirectionContext indirectionCtx;
    private static PostgreSQLParser.Indirection_elContext indirection_elCtx;
    private static PostgreSQLParser.Attr_nameContext attr_nameCtx;
    private static PostgreSQLParser.CollabelContext collabelCtx;

    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        dependencyObject = mock(DependencyObject.class);
        manager = new CreateTableStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.CreatestmtContext.class);
        onCommitOptionCtx = mock(PostgreSQLParser.OncommitoptionContext.class);
        optWithCtx = mock(PostgreSQLParser.OptwithContext.class);
        partitionSpecCtx = mock(PostgreSQLParser.PartitionspecContext.class);
        tableLikeClauseCtx = mock(PostgreSQLParser.TablelikeclauseContext.class);
        identifierNode = mock(TerminalNode.class);
        qualified_nameCtx = mock(PostgreSQLParser.Qualified_nameContext.class);
        identifierCtx = mock(PostgreSQLParser.IdentifierContext.class);
        colidCtx = mock(PostgreSQLParser.ColidContext.class);
        indirectionCtx = mock(PostgreSQLParser.IndirectionContext.class);
        indirection_elCtx = mock(PostgreSQLParser.Indirection_elContext.class);
        attr_nameCtx = mock(PostgreSQLParser.Attr_nameContext.class);
        collabelCtx = mock(PostgreSQLParser.CollabelContext.class);
    }

    private void setupMocksForCreateTableContext() {
        Feature defaultFeature = new Feature("create_table_standard", true, null);
        when(configLoader.getFeatureConfig("create_table_standard")).thenReturn(defaultFeature);
    }

    @Test
    public void test_createStmt_withTableType() {
        Feature feature = new Feature("create_table_of_type", false, "CREATE TABLE OF is not supported");

        setupMocksForCreateTableContext();
        when(ctx.OF()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("create_table_of_type")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withOnCommitDropOption() {
        Feature feature = new Feature("parameter_on_commit_drop", false, "DROP is not supported");

        setupMocksForCreateTableContext();
        when(ctx.oncommitoption()).thenReturn(onCommitOptionCtx);
        when(onCommitOptionCtx.DROP()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_on_commit_drop")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withOnCommitDeleteOption() {
        Feature feature = new Feature("parameter_on_commit_delete_rows", false, "DELETE ROWS is not supported");

        setupMocksForCreateTableContext();
        when(ctx.oncommitoption()).thenReturn(onCommitOptionCtx);
        when(onCommitOptionCtx.DELETE_P()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_on_commit_delete_rows")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withOnCommitPreserveOption() {
        Feature feature = new Feature("parameter_on_commit_preserve_rows", false, "PRESERVE ROWS is not supported");

        setupMocksForCreateTableContext();
        when(ctx.oncommitoption()).thenReturn(onCommitOptionCtx);
        when(onCommitOptionCtx.PRESERVE()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_on_commit_preserve_rows")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withIfNotExistsClause() {
        Feature feature = new Feature("parameter_if_not_exists_create_table", true, null);

        setupMocksForCreateTableContext();
        when(ctx.IF_P()).thenReturn(mock(TerminalNode.class));
        when(ctx.NOT()).thenReturn(mock(TerminalNode.class));
        when(ctx.EXISTS()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_if_not_exists_create_table")).thenReturn(feature);
        when(ctx.qualified_name(0)).thenReturn(qualified_nameCtx);
        when(qualified_nameCtx.colid()).thenReturn(colidCtx);
        when(colidCtx.identifier()).thenReturn(identifierCtx);
        when(identifierCtx.Identifier()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("public");

        when(qualified_nameCtx.indirection()).thenReturn(indirectionCtx);
        when(indirectionCtx.indirection_el(0)).thenReturn(indirection_elCtx);
        when(indirection_elCtx.attr_name()).thenReturn(attr_nameCtx);
        when(attr_nameCtx.collabel()).thenReturn(collabelCtx);
        when(collabelCtx.identifier()).thenReturn(identifierCtx);
        when(identifierCtx.getText()).thenReturn("table1");

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        assertEquals(1, dependencyObject.dependencyObjectMap.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_createStmt_withStorageParameter() {
        Feature feature = new Feature("storage_parameter", true, null);

        setupMocksForCreateTableContext();
        when(ctx.optwith()).thenReturn(optWithCtx);
        when(optWithCtx.WITH()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("storage_parameter")).thenReturn(feature);
        when(ctx.qualified_name(0)).thenReturn(qualified_nameCtx);
        when(qualified_nameCtx.colid()).thenReturn(colidCtx);
        when(colidCtx.identifier()).thenReturn(identifierCtx);
        when(identifierCtx.Identifier()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("public");

        when(qualified_nameCtx.indirection()).thenReturn(indirectionCtx);
        when(indirectionCtx.indirection_el(0)).thenReturn(indirection_elCtx);
        when(indirection_elCtx.attr_name()).thenReturn(attr_nameCtx);
        when(attr_nameCtx.collabel()).thenReturn(collabelCtx);
        when(collabelCtx.identifier()).thenReturn(identifierCtx);
        when(identifierCtx.getText()).thenReturn("table1");

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        assertEquals(1, dependencyObject.dependencyObjectMap.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_createStmt_withPartitionSpec() {
        Feature feature = new Feature("parameter_partition_by", false, "PARTITION BY is not supported");

        setupMocksForCreateTableContext();
        when(ctx.optpartitionspec()).thenReturn(mock(PostgreSQLParser.OptpartitionspecContext.class));
        when(ctx.optpartitionspec().partitionspec()).thenReturn(mock(PostgreSQLParser.PartitionspecContext.class));
        when(ctx.optpartitionspec().partitionspec().PARTITION()).thenReturn(mock(TerminalNode.class));
        when(ctx.optpartitionspec().partitionspec().BY()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_partition_by")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withTablespace() {
        Feature feature = new Feature("parameter_tablespace", false, "TABLESPACE is not supported");

        setupMocksForCreateTableContext();
        when(ctx.opttablespace()).thenReturn(mock(PostgreSQLParser.OpttablespaceContext.class));
        when(ctx.opttablespace().TABLESPACE()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_tablespace")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withInherits() {
        Feature feature = new Feature("parameter_inherits", false, "INHERITS is not supported");

        setupMocksForCreateTableContext();
        when(ctx.optinherit()).thenReturn(mock(PostgreSQLParser.OptinheritContext.class));
        when(ctx.optinherit().INHERITS()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_inherits")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withTableAccessMethod() {
        Feature feature = new Feature("parameter_using_method", false, "USING METHOD is not supported");

        setupMocksForCreateTableContext();
        when(ctx.table_access_method_clause()).thenReturn(mock(PostgreSQLParser.Table_access_method_clauseContext.class));
        when(ctx.table_access_method_clause().USING()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("parameter_using_method")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withTempClause() {
        Feature feature = new Feature("create_temporary_table", false, "CREATE TEMPORARY TABLE is not supported");

        setupMocksForCreateTableContext();
        when(ctx.opttemp()).thenReturn(mock(PostgreSQLParser.OpttempContext.class));
        when(ctx.opttemp().TEMPORARY()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("create_temporary_table")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createStmt_withTableElement_likeClause() {
        Feature feature = new Feature("create_table_like", false, "CREATE TABLE (... LIKE ...) is not supported");

        setupMocksForCreateTableContext();
        when(ctx.opttableelementlist()).thenReturn(mock(PostgreSQLParser.OpttableelementlistContext.class));
        when(ctx.opttableelementlist().tableelementlist()).thenReturn(mock(PostgreSQLParser.TableelementlistContext.class));

        PostgreSQLParser.TableelementContext tableElementCtx = mock(PostgreSQLParser.TableelementContext.class);
        List<PostgreSQLParser.TableelementContext> tableElementList = Collections.singletonList(tableElementCtx);
        when(ctx.opttableelementlist().tableelementlist().tableelement()).thenReturn(tableElementList);
        when(tableElementCtx.tablelikeclause()).thenReturn(tableLikeClauseCtx);
        when(tableLikeClauseCtx.LIKE()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig("create_table_like")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }
}
