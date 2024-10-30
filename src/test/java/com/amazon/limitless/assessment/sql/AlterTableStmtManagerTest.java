// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.testutils.TestPostgresSqlParserHelper;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlterTableStmtManagerTest {
    private static ConfigLoader configLoader;

    private static AlterTableStmtManager manager;

    private static PostgreSQLParser.AltertablestmtContext ctx;

    private static PostgreSQLParser.Alter_table_cmdsContext cmdsCtx;

    private static PostgreSQLParser.Alter_table_cmdContext cmdCtx;

    private static List<PostgreSQLParser.Alter_table_cmdContext> cmdActionList;

    private static PostgreSQLParser.Partition_cmdContext paritionCmdCtx;

    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        manager = new AlterTableStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.AltertablestmtContext.class);
        cmdCtx = mock(PostgreSQLParser.Alter_table_cmdContext.class);
        cmdsCtx = mock(PostgreSQLParser.Alter_table_cmdsContext.class);
        cmdActionList = List.of(cmdCtx);
        paritionCmdCtx = mock(PostgreSQLParser.Partition_cmdContext.class);
    }

    private void assertFeature(String featureName, boolean isSupported, String errorMessage, List<StatementResult> resultList) {
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertEquals(isSupported, result.getFeature().isSupported());
        assertEquals(errorMessage, result.getFeature().getErrorMessage());
    }

    private void mockFeatureConfig(String featureName, boolean isSupported, String errorMessage) {
        Feature feature = new Feature(featureName, isSupported, errorMessage);
        when(ctx.ALTER()).thenReturn(mock(TerminalNode.class));
        when(ctx.TABLE()).thenReturn(mock(TerminalNode.class));
        when(configLoader.getFeatureConfig(featureName)).thenReturn(feature);
    }

    private void mockAlterTableActionContext() {
        when(ctx.alter_table_cmds()).thenReturn(cmdsCtx);
        when(cmdsCtx.alter_table_cmd()).thenReturn(cmdActionList);
    }

    @Test
    public void testAlterMaterializedView() {
        when(ctx.MATERIALIZED()).thenReturn(mock(TerminalNode.class));
        mockFeatureConfig("alter_materialized_view", false, "ALTER MATERIALIZED VIEW is not supported");

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertFeature("alter_materialized_view", false, "ALTER MATERIALIZED VIEW is not supported", resultList);
    }

    @Test
    public void testAlterTableAlterIndex() {
        when(ctx.INDEX()).thenReturn(mock(TerminalNode.class));
        mockFeatureConfig("alter_table_alter_index", false, "ALTER INDEX is not supported");

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertFeature("alter_table_alter_index", false, "ALTER INDEX is not supported", resultList);
    }

    @Test
    public void test_alterTable() {
        AlterTableStmtManager manager = new AlterTableStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();
    
        // Feature variable definitions
        Feature alterTableStandard = new Feature("alter_table_standard", true, null);
        Feature ifNotExistsAlterTable = new Feature("parameter_if_not_exists_alter_table", true, null);
        Feature addColumn = new Feature("alter_table_add_column", true, null);
        Feature dropColumn = new Feature("alter_table_drop_column", true, null);
        Feature columnType = new Feature("alter_table_column_type", true, null);
        Feature columnTypeUsing = new Feature("alter_table_column_type_using", false, "COLUMN TYPE USING expression is not supported");
        Feature columnDefault = new Feature("alter_table_column_default", true, null);
        Feature setNotNull = new Feature("alter_table_column_set_not_null", true, null);
        Feature dropNotNull = new Feature("alter_table_column_drop_not_null", true, null);
        Feature dropExpression = new Feature("alter_table_column_drop_expression", false, "COLUMN DROP EXPRESSION is not supported on standard tables");
        Feature addIdentity = new Feature("alter_table_column_add_identity", false, "COLUMN ADD IDENTITY is not supported on standard tables");
        Feature dropIdentity = new Feature("alter_table_column_drop_identity", false, "COLUMN DROP IDENTITY is not supported on standard tables");
        Feature setIdentity = new Feature("alter_table_column_set_identity", false, "COLUMN SET IDENTITY is not supported on standard table");
        Feature setStatistics = new Feature("alter_table_column_set_statistics", false, "COLUMN SET STATISTICS is not supported on standard tables");
        Feature setAttributeOption = new Feature("alter_table_column_set_attribute_option", false, "COLUMN SET ATTRIBUTE OPTION is not supported on standard tables");
        Feature resetAttributeOption = new Feature("alter_table_column_reset_attribute_option", false, "COLUMN RESET ATTRIBUTE OPTION is not supported on standard tables");
        Feature setStorage = new Feature("alter_table_column_set_storage", false, "COLUMN SET STORAGE is not supported on standard tables");
        Feature setCompression = new Feature("alter_table_column_set_compression", false, "COLUMN SET COMPRESSION is not supported on standard tables");
        Feature alterConstraint = new Feature("alter_table_alter_constraint", false, "ALTER CONSTRAINT is not supported on standard tables");
        Feature validateConstraint = new Feature("alter_table_validate_constraint", true, null);
        Feature dropConstraint = new Feature("alter_table_drop_constraint", true, null);
        Feature disableTrigger = new Feature("alter_table_disable_trigger", false, "DISABLE TRIGGER is not supported on standard tables");
        Feature enableTrigger = new Feature("alter_table_enable_trigger", false, "ENABLE TRIGGER is not supported on standard tables");
        Feature enableReplicaTrigger = new Feature("alter_table_enable_replica_trigger", false, "ENABLE REPLICA TRIGGER is not supported on standard tables");
        Feature enableAlwaysTrigger = new Feature("alter_table_enable_always_trigger", false, "ENABLE ALWAYS TRIGGER is not supported on standard tables");
        Feature disableRule = new Feature("alter_table_disable_rule", false, "DISABLE RULE is not supported on standard tables");
        Feature enableRule = new Feature("alter_table_enable_rule", false, "ENABLE RULE is not supported on standard tables");
        Feature enableReplicaRule = new Feature("alter_table_enable_replica_rule", false, "ENABLE REPLICA RULE is not supported on standard tables");
        Feature enableAlwaysRule = new Feature("alter_table_enable_always_rule", false, "ENABLE ALWAYS RULE is not supported on standard tables");
        Feature rowLevelSecurity = new Feature("alter_table_row_level_security", true, null);
        Feature clusterOnIndex = new Feature("alter_table_cluster_on_index", false, "CLUSTER ON INDEX is not supported on standard tables");
        Feature setWithoutCluster = new Feature("alter_table_set_without_cluster", false, "SET WITHOUT CLUSTER is not supported on standard tables");
        Feature setWithoutOids = new Feature("alter_table_set_without_oids", false, "SET WITHOUT OIDS is not supported on standard tables");
        Feature setTablespace = new Feature("alter_table_set_tablespace", false, "SET TABLESPACE is not supported on standard tables");
        Feature setLogged = new Feature("alter_table_set_logged", false, "SET LOGGED is not supported on standard tables");
        Feature setUnlogged = new Feature("alter_table_set_unlogged", false, "SET UNLOGGED is not supported on standard tables");
        Feature setOptions = new Feature("alter_table_set_options", true, null);
        Feature resetOptions = new Feature("alter_table_reset_options", true, null);
        Feature inherit = new Feature("alter_table_inherit", false, "INHERIT is not supported on standard tables");
        Feature noInherit = new Feature("alter_table_no_inherit", false, "NO INHERIT is not supported on standard tables");
        Feature of = new Feature("alter_table_of", false, "OF is not supported on standard tables");
        Feature notOf = new Feature("alter_table_not_of", false, "NOT OF is not supported on standard tables");
        Feature ownerTo = new Feature("alter_table_owner_to", true, null);
        Feature replicaIdentity = new Feature("alter_table_replica_identity", false, "REPLICA IDENTITY is not supported on standard tables");
        Feature attachPartition = new Feature("alter_table_attach_partition", false, "ATTACH PARTITION is not supported on standard tables");
        Feature detachPartition = new Feature("alter_table_detach_partition", false, "DETACH PARTITION is not supported on standard tables");
        Feature alterTableOnly = new Feature("alter_table_only", false, "ALTER TABLE ONLY is not supported");
        Feature renameTable = new Feature("alter_table_rename", true, null);
        Feature setSchema = new Feature("alter_table_set_schema", true, null);
        Feature renameColumn = new Feature("alter_table_rename_column", true, null);
    
        // Mocking statements
        when(configLoader.getFeatureConfig("alter_table_standard")).thenReturn(alterTableStandard);
        when(configLoader.getFeatureConfig("parameter_if_not_exists_alter_table")).thenReturn(ifNotExistsAlterTable);
        when(configLoader.getFeatureConfig("alter_table_add_column")).thenReturn(addColumn);
        when(configLoader.getFeatureConfig("alter_table_drop_column")).thenReturn(dropColumn);
        when(configLoader.getFeatureConfig("alter_table_column_type")).thenReturn(columnType);
        when(configLoader.getFeatureConfig("alter_table_column_type_using")).thenReturn(columnTypeUsing);
        when(configLoader.getFeatureConfig("alter_table_column_default")).thenReturn(columnDefault);
        when(configLoader.getFeatureConfig("alter_table_column_set_not_null")).thenReturn(setNotNull);
        when(configLoader.getFeatureConfig("alter_table_column_drop_not_null")).thenReturn(dropNotNull);
        when(configLoader.getFeatureConfig("alter_table_column_drop_expression")).thenReturn(dropExpression);
        when(configLoader.getFeatureConfig("alter_table_column_add_identity")).thenReturn(addIdentity);
        when(configLoader.getFeatureConfig("alter_table_column_drop_identity")).thenReturn(dropIdentity);
        when(configLoader.getFeatureConfig("alter_table_column_set_statistics")).thenReturn(setStatistics);
        when(configLoader.getFeatureConfig("alter_table_column_set_attribute_option")).thenReturn(setAttributeOption);
        when(configLoader.getFeatureConfig("alter_table_column_reset_attribute_option")).thenReturn(resetAttributeOption);
        when(configLoader.getFeatureConfig("alter_table_column_set_storage")).thenReturn(setStorage);
        when(configLoader.getFeatureConfig("alter_table_column_set_compression")).thenReturn(setCompression);
        when(configLoader.getFeatureConfig("alter_table_alter_constraint")).thenReturn(alterConstraint);
        when(configLoader.getFeatureConfig("alter_table_validate_constraint")).thenReturn(validateConstraint);
        when(configLoader.getFeatureConfig("alter_table_drop_constraint")).thenReturn(dropConstraint);
        when(configLoader.getFeatureConfig("alter_table_disable_trigger")).thenReturn(disableTrigger);
        when(configLoader.getFeatureConfig("alter_table_enable_trigger")).thenReturn(enableTrigger);
        when(configLoader.getFeatureConfig("alter_table_enable_replica_trigger")).thenReturn(enableReplicaTrigger);
        when(configLoader.getFeatureConfig("alter_table_enable_always_trigger")).thenReturn(enableAlwaysTrigger);
        when(configLoader.getFeatureConfig("alter_table_disable_rule")).thenReturn(disableRule);
        when(configLoader.getFeatureConfig("alter_table_enable_rule")).thenReturn(enableRule);
        when(configLoader.getFeatureConfig("alter_table_enable_replica_rule")).thenReturn(enableReplicaRule);
        when(configLoader.getFeatureConfig("alter_table_enable_always_rule")).thenReturn(enableAlwaysRule);
        when(configLoader.getFeatureConfig("alter_table_row_level_security")).thenReturn(rowLevelSecurity);
        when(configLoader.getFeatureConfig("alter_table_cluster_on_index")).thenReturn(clusterOnIndex);
        when(configLoader.getFeatureConfig("alter_table_set_without_cluster")).thenReturn(setWithoutCluster);
        when(configLoader.getFeatureConfig("alter_table_set_without_oids")).thenReturn(setWithoutOids);
        when(configLoader.getFeatureConfig("alter_table_set_tablespace")).thenReturn(setTablespace);
        when(configLoader.getFeatureConfig("alter_table_set_logged")).thenReturn(setLogged);
        when(configLoader.getFeatureConfig("alter_table_set_unlogged")).thenReturn(setUnlogged);
        when(configLoader.getFeatureConfig("alter_table_set_options")).thenReturn(setOptions);
        when(configLoader.getFeatureConfig("alter_table_reset_options")).thenReturn(resetOptions);
        when(configLoader.getFeatureConfig("alter_table_inherit")).thenReturn(inherit);
        when(configLoader.getFeatureConfig("alter_table_no_inherit")).thenReturn(noInherit);
        when(configLoader.getFeatureConfig("alter_table_of")).thenReturn(of);
        when(configLoader.getFeatureConfig("alter_table_not_of")).thenReturn(notOf);
        when(configLoader.getFeatureConfig("alter_table_owner_to")).thenReturn(ownerTo);
        when(configLoader.getFeatureConfig("alter_table_replica_identity")).thenReturn(replicaIdentity);
        when(configLoader.getFeatureConfig("alter_table_attach_partition")).thenReturn(attachPartition);
        when(configLoader.getFeatureConfig("alter_table_detach_partition")).thenReturn(detachPartition);
        when(configLoader.getFeatureConfig("alter_table_only")).thenReturn(alterTableOnly);
        when(configLoader.getFeatureConfig("alter_table_rename")).thenReturn(renameTable);
        when(configLoader.getFeatureConfig("alter_table_set_schema")).thenReturn(setSchema);
        when(configLoader.getFeatureConfig("alter_table_rename_column")).thenReturn(renameColumn);
        when(configLoader.getFeatureConfig("alter_table_column_set_identity")).thenReturn(setIdentity);

        // Test cases
        testSqls.put("ALTER TABLE mytable ADD COLUMN newcol INT;", addColumn);
        testSqls.put("ALTER TABLE mytable DROP COLUMN oldcol;", dropColumn);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol TYPE BIGINT USING numcol::BIGINT;", columnTypeUsing);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol TYPE BIGINT;", columnType);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol SET DEFAULT 0;", columnDefault);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol DROP DEFAULT;", columnDefault);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol SET NOT NULL;", setNotNull);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol DROP NOT NULL;", dropNotNull);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN gencol DROP EXPRESSION;", dropExpression);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol SET STATISTICS 0;", setStatistics);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol SET STORAGE PLAIN;", setStorage);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN gencol ADD GENERATED ALWAYS AS IDENTITY;", addIdentity);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN gencol DROP IDENTITY;", dropIdentity);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN gencol SET GENERATED ALWAYS;", setIdentity);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN gencol RESTART;", setIdentity);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol SET (n_distinct=100);", setAttributeOption);
        testSqls.put("ALTER TABLE mytable ALTER COLUMN numcol RESET (n_distinct);", resetAttributeOption);
        testSqls.put("ALTER TABLE mytable ALTER CONSTRAINT myconstraint DEFERRABLE;", alterConstraint);
        testSqls.put("ALTER TABLE mytable VALIDATE CONSTRAINT myconstraint;", validateConstraint);
        testSqls.put("ALTER TABLE mytable DROP CONSTRAINT myconstraint;", dropConstraint);
        testSqls.put("ALTER TABLE mytable DISABLE TRIGGER mytrigger;", disableTrigger);
        testSqls.put("ALTER TABLE mytable ENABLE TRIGGER mytrigger;", enableTrigger);
        testSqls.put("ALTER TABLE mytable ENABLE REPLICA TRIGGER mytrigger;", enableReplicaTrigger);
        testSqls.put("ALTER TABLE mytable ENABLE ALWAYS TRIGGER mytrigger;", enableAlwaysTrigger);
        testSqls.put("ALTER TABLE mytable DISABLE RULE myrule;", disableRule);
        testSqls.put("ALTER TABLE mytable ENABLE RULE myrule;", enableRule);
        testSqls.put("ALTER TABLE mytable ENABLE REPLICA RULE myrule;", enableReplicaRule);
        testSqls.put("ALTER TABLE mytable ENABLE ALWAYS RULE myrule;", enableAlwaysRule);
        testSqls.put("ALTER TABLE mytable DISABLE ROW LEVEL SECURITY;", rowLevelSecurity);
        testSqls.put("ALTER TABLE mytable ENABLE ROW LEVEL SECURITY;", rowLevelSecurity);
        testSqls.put("ALTER TABLE mytable FORCE ROW LEVEL SECURITY;", rowLevelSecurity);
        testSqls.put("ALTER TABLE mytable NO FORCE ROW LEVEL SECURITY;", rowLevelSecurity);
        testSqls.put("ALTER TABLE mytable CLUSTER ON myindex;", clusterOnIndex);
        testSqls.put("ALTER TABLE mytable SET WITHOUT CLUSTER;", setWithoutCluster);
        testSqls.put("ALTER TABLE mytable SET WITHOUT OIDS;", setWithoutOids);
        testSqls.put("ALTER TABLE mytable SET TABLESPACE mytablespace;", setTablespace);
        testSqls.put("ALTER TABLE mytable SET LOGGED;", setLogged);
        testSqls.put("ALTER TABLE mytable SET UNLOGGED;", setUnlogged);
        testSqls.put("ALTER TABLE mytable SET (fillfactor=70);", setOptions);
        testSqls.put("ALTER TABLE mytable RESET (fillfactor);", resetOptions);
        testSqls.put("ALTER TABLE mytable INHERIT parenttable;", inherit);
        testSqls.put("ALTER TABLE mytable NO INHERIT parenttable;", noInherit);
        testSqls.put("ALTER TABLE mytable OF mytype;", of);
        testSqls.put("ALTER TABLE mytable NOT OF;", notOf);
        testSqls.put("ALTER TABLE mytable OWNER TO newowner;", ownerTo);
        testSqls.put("ALTER TABLE mytable REPLICA IDENTITY FULL;", replicaIdentity);
        testSqls.put("ALTER TABLE mytable ATTACH PARTITION mypartition FOR VALUES FROM (1) TO (100);", attachPartition);
        testSqls.put("ALTER TABLE mytable DETACH PARTITION mypartition;", detachPartition);
        testSqls.put("ALTER TABLE ONLY mytable ADD COLUMN newcol INT;", alterTableOnly);
        testSqls.put("ALTER TABLE IF EXISTS mytable ADD COLUMN newcol INT;", ifNotExistsAlterTable);
        testSqls.put("ALTER TABLE ALL IN TABLESPACE mytablespace OWNED BY myuser SET TABLESPACE newtablespace;", ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE);
    
        for (Map.Entry<String, Feature> entry : testSqls.entrySet()) {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.AltertablestmtContext altertablestmtContext =
                context.stmtblock().stmtmulti().stmt(0).altertablestmt();
            List<StatementResult> resultList = manager.analyzeStatement(altertablestmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }

    // TODO: Add unit tests here for table constraints
    // @Test
    // public void testAlterTableAddConstraint() {
    // }

    @Test
    public void test_alterTableAlterViewAlter()
    {
        AlterTableStmtManager manager = new AlterTableStmtManager(configLoader);
        PostgreSQLParser.AltertablestmtContext ctx = mock(
            PostgreSQLParser.AltertablestmtContext.class
        );
        PostgreSQLParser.Alter_table_cmdsContext alter_table_cmdsContext = mock(
            PostgreSQLParser.Alter_table_cmdsContext.class
        );
        PostgreSQLParser.Alter_table_cmdContext alter_table_cmdContext = mock(
            PostgreSQLParser.Alter_table_cmdContext.class
        );
        List<PostgreSQLParser.Alter_table_cmdContext> list = List.of(alter_table_cmdContext);
        when(ctx.alter_table_cmds()).thenReturn(alter_table_cmdsContext);
        when(alter_table_cmdsContext.alter_table_cmd()).thenReturn(list);
        when(alter_table_cmdContext.ALTER()).thenReturn(new TerminalNodeImpl(null));
        when(ctx.VIEW()).thenReturn(new TerminalNodeImpl(null));
        Feature feature = new Feature("alter_view_alter", false, "ALTER VIEW ALTER OPTION is not supported");
        when(configLoader.getFeatureConfig("alter_view_alter")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterTableAlterViewChangeOwner()
    {
        AlterTableStmtManager manager = new AlterTableStmtManager(configLoader);
        PostgreSQLParser.AltertablestmtContext ctx = mock(
            PostgreSQLParser.AltertablestmtContext.class
        );
        PostgreSQLParser.Alter_table_cmdsContext alter_table_cmdsContext = mock(
            PostgreSQLParser.Alter_table_cmdsContext.class
        );
        PostgreSQLParser.Alter_table_cmdContext alter_table_cmdContext = mock(
            PostgreSQLParser.Alter_table_cmdContext.class
        );
        List<PostgreSQLParser.Alter_table_cmdContext> list = List.of(alter_table_cmdContext);
        when(ctx.alter_table_cmds()).thenReturn(alter_table_cmdsContext);
        when(alter_table_cmdsContext.alter_table_cmd()).thenReturn(list);
        when(alter_table_cmdContext.OWNER()).thenReturn(new TerminalNodeImpl(null));
        when(ctx.VIEW()).thenReturn(new TerminalNodeImpl(null));
        Feature feature = new Feature("alter_view_change_owner", false, "ALTER VIEW CHANGE OWNER is not supported");
        when(configLoader.getFeatureConfig("alter_view_change_owner")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterTableAlterViewReset()
    {
        AlterTableStmtManager manager = new AlterTableStmtManager(configLoader);
        PostgreSQLParser.AltertablestmtContext ctx = mock(
            PostgreSQLParser.AltertablestmtContext.class
        );
        PostgreSQLParser.Alter_table_cmdsContext alter_table_cmdsContext = mock(
            PostgreSQLParser.Alter_table_cmdsContext.class
        );
        PostgreSQLParser.Alter_table_cmdContext alter_table_cmdContext = mock(
            PostgreSQLParser.Alter_table_cmdContext.class
        );
        List<PostgreSQLParser.Alter_table_cmdContext> list = List.of(alter_table_cmdContext);
        when(ctx.alter_table_cmds()).thenReturn(alter_table_cmdsContext);
        when(alter_table_cmdsContext.alter_table_cmd()).thenReturn(list);
        when(alter_table_cmdContext.RESET()).thenReturn(new TerminalNodeImpl(null));
        when(ctx.VIEW()).thenReturn(new TerminalNodeImpl(null));
        Feature feature = new Feature("alter_view_reset", true, null);
        when(configLoader.getFeatureConfig("alter_view_reset")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
        assertEquals(feature.getContext(), result.getFeature().getContext());
    }

    @Test
    public void test_alterTableAlterViewSet()
    {
        AlterTableStmtManager manager = new AlterTableStmtManager(configLoader);
        PostgreSQLParser.AltertablestmtContext ctx = mock(
            PostgreSQLParser.AltertablestmtContext.class
        );
        PostgreSQLParser.Alter_table_cmdsContext alter_table_cmdsContext = mock(
            PostgreSQLParser.Alter_table_cmdsContext.class
        );
        PostgreSQLParser.Alter_table_cmdContext alter_table_cmdContext = mock(
            PostgreSQLParser.Alter_table_cmdContext.class
        );
        List<PostgreSQLParser.Alter_table_cmdContext> list = List.of(alter_table_cmdContext);
        when(ctx.alter_table_cmds()).thenReturn(alter_table_cmdsContext);
        when(alter_table_cmdsContext.alter_table_cmd()).thenReturn(list);
        when(alter_table_cmdContext.SET()).thenReturn(new TerminalNodeImpl(null));
        when(ctx.VIEW()).thenReturn(new TerminalNodeImpl(null));
        Feature feature = new Feature("alter_view_set", true, null);
        when(configLoader.getFeatureConfig("alter_view_set")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
        assertEquals(feature.getContext(), result.getFeature().getContext());
    }

    @Test
    public void test_alterSequenceOwner()
    {
        AlterTableStmtManager manager = new AlterTableStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();

        testSqls.put("ALTER SEQUENCE public.orders_order_id_seq OWNER TO postgres;", new Feature("alter_sequence_owner_to",true, null));
        Feature feature = new Feature("alter_sequence_owner_to", true, null);
        when(configLoader.getFeatureConfig("alter_sequence_owner_to")).thenReturn(feature);

        for (Map.Entry<String, Feature> entry : testSqls.entrySet())
        {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.AltertablestmtContext altertablestmtContext =
                context.stmtblock().stmtmulti().stmt(0).altertablestmt();
            List<StatementResult> resultList = manager.analyzeStatement(altertablestmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }
}
