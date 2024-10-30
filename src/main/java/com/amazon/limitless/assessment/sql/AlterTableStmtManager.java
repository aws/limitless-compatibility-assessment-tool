// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;

import java.util.List;

public class AlterTableStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.AltertablestmtContext> {

    private static Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

    public AlterTableStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.AltertablestmtContext ctx) {
        resultList.clear();
        determineFeatureConfig(ctx);
        addResultIfEmpty();
        return resultList;
    }

    private void determineFeatureConfig(PostgreSQLParser.AltertablestmtContext ctx) {
        if (isMaterializedView(ctx)) {
            featureConfig = configLoader.getFeatureConfig("alter_materialized_view");
        } else if (isIndexAlteration(ctx)) {
            //Below will handle SQLs:
            //ALTER INDEX [ IF EXISTS ] name SET TABLESPACE tablespace_name
            //ALTER INDEX [ IF EXISTS ] name RESET TABLESPACE tablespace_name
            //ALTER INDEX name ATTACH PARTITION index_name
            //ALTER INDEX distributors SET (fillfactor = 75);
            featureConfig = configLoader.getFeatureConfig("alter_table_alter_index");
        } else if (isStandardAlterTable(ctx)) {
            featureConfig = configLoader.getFeatureConfig("alter_table_standard");
            handleStandardAlterTable(ctx);
        } else if (isAlterView(ctx)) {
            handleAlterViewClauses(ctx);
        } else if (isSequence(ctx)) {
            handleSequence(ctx);
        }
    }

    //Below if logic will handle SQLs:
    //ALTER VIEW [ IF EXISTS ] name ALTER [ COLUMN ] column_name
    //ALTER VIEW [ IF EXISTS ] name OWNER TO { new_owner | CURRENT_ROLE | CURRENT_USER | SESSION_USER }
    //ALTER VIEW [ IF EXISTS ] name SET ( view_option_name [= view_option_value] [, ... ] )
    //ALTER VIEW [ IF EXISTS ] name RESET ( view_option_name [, ... ] )
    private void handleAlterViewClauses(PostgreSQLParser.AltertablestmtContext ctx) {
        PostgreSQLParser.Alter_table_cmdContext alter_table_cmdContext = ctx.alter_table_cmds().alter_table_cmd().get(0);
        if (alter_table_cmdContext.ALTER() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_view_alter");
        } else if (alter_table_cmdContext.OWNER() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_view_change_owner");
        } else if (alter_table_cmdContext.RESET() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_view_reset");
        } else if (alter_table_cmdContext.SET() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_view_set");
        }
    }

    private void handleSequence(PostgreSQLParser.AltertablestmtContext ctx) {
        for (PostgreSQLParser.Alter_table_cmdContext alterCmdCtx : ctx.alter_table_cmds().alter_table_cmd())
        {
            if (alterCmdCtx.OWNER() != null)
                featureConfig = configLoader.getFeatureConfig("alter_sequence_owner_to"); 
        }
    }

    private void handleStandardAlterTable(PostgreSQLParser.AltertablestmtContext ctx) {
        if (isAlterTableWithTablespace(ctx)) {
            addResult("unsupported");
        } else {
            handleAlterTableClauses(ctx);
        }
    }

    private void handleAlterTableClauses(PostgreSQLParser.AltertablestmtContext ctx) {
        handleIfExists(ctx);
        handleOnlyRelation(ctx);
        handlePartitionCommands(ctx);
        handleAlterTableCommands(ctx);
    }

    private void handleIfExists(PostgreSQLParser.AltertablestmtContext ctx) {
        if (isIfExists(ctx)) {
            featureConfig = configLoader.getFeatureConfig("parameter_if_not_exists_alter_table");
        }
    }

    private void handleOnlyRelation(PostgreSQLParser.AltertablestmtContext ctx) {
        if (isOnlyRelation(ctx)) {
            addResult("alter_table_only");
        }
    }

    private void handlePartitionCommands(PostgreSQLParser.AltertablestmtContext ctx) {
        if (ctx.partition_cmd() != null) {
            if (ctx.partition_cmd().ATTACH() != null) {
                addResult("alter_table_attach_partition");
            } else if (ctx.partition_cmd().DETACH() != null) {
                addResult("alter_table_detach_partition");
            }
        }
    }

    private void handleAlterTableCommands(PostgreSQLParser.AltertablestmtContext ctx) {
        if (ctx.alter_table_cmds() != null && ctx.alter_table_cmds().alter_table_cmd().size() != 0) {
            for (PostgreSQLParser.Alter_table_cmdContext cmdCtx : ctx.alter_table_cmds().alter_table_cmd()) {
                processTableCommand(cmdCtx);
            }
        }
    }

    private void processTableCommand(PostgreSQLParser.Alter_table_cmdContext cmdCtx) {
        if (isAddColumn(cmdCtx)) {
            featureConfig = configLoader.getFeatureConfig("alter_table_add_column");
        } else if (isColumnCommand(cmdCtx)) {
            processTableColumnCommand(cmdCtx);
        } else {
            processTableCommandBasedOnType(cmdCtx);
        }
    }

    private void processTableCommandBasedOnType(PostgreSQLParser.Alter_table_cmdContext cmdCtx) {
        if (isAddConstraint(cmdCtx)) {
            handleAddConstraint(cmdCtx);
        } else if (isAlterConstraint(cmdCtx)) {
            addResult("alter_table_alter_constraint");
        } else {
            processCommandActions(cmdCtx);
        }
    }

    private void processTableColumnCommand(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (ctx.DROP() != null && ctx.ALTER() == null) {
            featureConfig = configLoader.getFeatureConfig("alter_table_drop_column");
        } else if (ctx.ALTER() != null) {
            handleAlterColumn(ctx);
        }
    }

    private void handleAlterColumn(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (isColumnTypeUsing(ctx)) {
            addResult("alter_table_column_type_using");
        } else if (isDefaultColumn(ctx)) {
            featureConfig = configLoader.getFeatureConfig("alter_table_column_default");
        } else {
            handleAlterColumnAttributes(ctx);
        }
    }

    private void handleAlterColumnAttributes(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (isNotNullAction(ctx)) {
            handleNotNullActions(ctx);
        } else if (isExpressionDrop(ctx)) {
            addResult("alter_table_column_drop_expression");
        } else if (isColumnStatistics(ctx)) {
            addResult("alter_table_column_set_statistics");
        } else if (isRelOptions(ctx)) {
            handleRelOptions(ctx, true);
        } else {
            processColumnStorageAndIdentity(ctx);
        }
    }

    private void handleNotNullActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (ctx.DROP() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_table_column_drop_not_null");
        } else if (ctx.SET() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_table_column_set_not_null");
        }
    }

    private void processColumnStorageAndIdentity(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (isColumnStorage(ctx)) {
            addResult("alter_table_column_set_storage");
        } else if (isIdentityColumn(ctx)) {
            handleIdentityActions(ctx);
        } else if (ctx.alter_identity_column_option_list() != null) {
            handleIdentityColumnOptions(ctx);
        }
    }
    
    private void handleIdentityActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (ctx.ADD_P() != null) {
            addResult("alter_table_column_add_identity");
        } else if (ctx.DROP() != null) {
            addResult("alter_table_column_drop_identity");
        }
    }
    
    private void handleIdentityColumnOptions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        for (PostgreSQLParser.Alter_identity_column_optionContext optionCtx : ctx.alter_identity_column_option_list().alter_identity_column_option())
        {
            if (optionCtx.RESTART() != null || optionCtx.SET() != null) {
                addResult("alter_table_column_set_identity");
                break;
            }
        }
    }

    private void handleAddConstraint(PostgreSQLParser.Alter_table_cmdContext ctx) {
        // TODO: Add validation logic for constraints
    }

    private void processCommandActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (hasTriggerAction(ctx)) {
            handleTriggerActions(ctx);
        } else if (hasRuleAction(ctx)) {
            handleRuleActions(ctx);
        } else if (isInheritAction(ctx)) {
            handleInheritActions(ctx);
        } else if (isOfTypeAction(ctx)) {
            handleOfTypeActions(ctx);
        } else if (isSetAction(ctx)) {
            handleSetActions(ctx);
        } else {
            processRemainingActions(ctx);
        }
    }

    private void processRemainingActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (isReplicaIdentity(ctx)) {
            addResult("alter_table_replica_identity");
        } else if (isClusterOnIndex(ctx)) {
            addResult("alter_table_cluster_on_index");
        } else if (isAlterGenericOptions(ctx)) {
            handleAlterGenericOptions(ctx);
        } else if (isRowLevelSecurity(ctx)) {
            featureConfig = configLoader.getFeatureConfig("alter_table_row_level_security");
        } else if (isConstraintAction(ctx)) {
            handleConstraintActions(ctx);
        } else if (isAlterTableOwner(ctx)) {
            featureConfig = configLoader.getFeatureConfig("alter_table_owner_to");
        } else if (isRelOptions(ctx)) {
            handleRelOptions(ctx, false);
        }
    }

    private void handleConstraintActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        String constraintKeyName = "";
        if (ctx.VALIDATE() != null) {
            constraintKeyName = "validate";
        } else if (ctx.DROP() != null) {
            constraintKeyName = "drop";
        }
        
        if (!constraintKeyName.isEmpty())
            featureConfig = configLoader.getFeatureConfig(String.format("alter_table_%s_constraint", constraintKeyName));
    }

    private void handleAlterGenericOptions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        for (PostgreSQLParser.Alter_generic_option_elemContext elemCtx : ctx.alter_generic_options().alter_generic_option_list().alter_generic_option_elem())
        {
            // some checks here
        }
    }

    private void handleTriggerActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (ctx.DISABLE_P() != null) {
            addResult("alter_table_disable_trigger");
        } else if (ctx.ENABLE_P() != null) {
            if (ctx.ALWAYS() != null) {
                addResult("alter_table_enable_always_trigger");
            } else if (ctx.REPLICA() != null) {
                addResult("alter_table_enable_replica_trigger");
            } else {
                addResult("alter_table_enable_trigger");
            }
        }
    }

    private void handleRuleActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (ctx.ENABLE_P() != null) {
            if (ctx.ALWAYS() != null) {
                addResult("alter_table_enable_always_rule");
            } else if (ctx.REPLICA() != null) {
                addResult("alter_table_enable_replica_rule");
            } else {
                addResult("alter_table_enable_rule");
            }
        } else if (ctx.DISABLE_P() != null) {
            addResult("alter_table_disable_rule");
        }
    }

    private void handleInheritActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        addResult(ctx.NO() != null ? "alter_table_no_inherit" : "alter_table_inherit");
    }

    private void handleOfTypeActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        addResult(ctx.NOT() != null ? "alter_table_not_of" : "alter_table_of");
    }

    private void handleSetActions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        String setKeyName = "";
        if (ctx.TABLESPACE() != null) {
            setKeyName = "tablespace";
        } else if (ctx.WITHOUT() != null) {
            if (ctx.OIDS() != null)
                setKeyName = "without_oids";
            else if (ctx.CLUSTER() != null)
                setKeyName = "without_cluster";
        } else if (ctx.LOGGED() != null) {
            setKeyName = "logged";
        } else if (ctx.UNLOGGED() != null) {
            setKeyName = "unlogged";
        }
        
        if (!setKeyName.isEmpty())
            addResult(String.format("alter_table_set_%s", setKeyName));
    }

    private void handleRelOptions(PostgreSQLParser.Alter_table_cmdContext ctx, boolean isColumnOption) {
        String option = isColumnOption ? getColumnOption(ctx) : getTableOption(ctx);
        if (option != null) {
            addResult(option);
        }
    }
    
    private String getColumnOption(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (ctx.SET() != null) {
            return "alter_table_column_set_attribute_option";
        } else if (ctx.RESET() != null) {
            return "alter_table_column_reset_attribute_option";
        }
        return null;
    }
    
    private String getTableOption(PostgreSQLParser.Alter_table_cmdContext ctx) {
        if (ctx.SET() != null) {
            return "alter_table_set_options";
        } else if (ctx.RESET() != null) {
            return "alter_table_reset_options";
        }
        return null;
    }

    private boolean isAlterView(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.VIEW() != null && ctx.alter_table_cmds() != null && ctx.alter_table_cmds().alter_table_cmd().get(0) != null;
    }

    private boolean isSequence(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.SEQUENCE() != null;
    }

    private boolean isMaterializedView(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.MATERIALIZED() != null;
    }

    private boolean isIndexAlteration(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.INDEX() != null;
    }

    private boolean isStandardAlterTable(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.ALTER() != null && ctx.TABLE() != null && ctx.FOREIGN() == null;
    }

    private boolean isAlterTableWithTablespace(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.ALL() != null && ctx.IN_P() != null && ctx.TABLESPACE() != null;
    }

    private boolean isIfExists(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.IF_P() != null && ctx.EXISTS() != null;
    }

    private boolean isOnlyRelation(PostgreSQLParser.AltertablestmtContext ctx) {
        return ctx.relation_expr() != null && ctx.relation_expr().ONLY() != null;
    }

    private boolean isAddColumn(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.ADD_P() != null && ctx.COLUMN() != null;
    }

    private boolean isColumnCommand(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.opt_column() != null && ctx.opt_column().COLUMN() != null;
    }

    private boolean isColumnTypeUsing(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.TYPE_P() != null && ctx.alter_using() != null && ctx.alter_using().USING() != null;
    }

    private boolean isDefaultColumn(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.alter_column_default() != null && ctx.alter_column_default().DEFAULT() != null;
    }

    private boolean isNotNullAction(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.NOT() != null && ctx.NULL_P() != null;
    }

    private boolean isExpressionDrop(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.DROP() != null && ctx.EXPRESSION() != null;
    }

    private boolean isColumnStatistics(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.SET() != null && ctx.STATISTICS() != null;
    }

    private boolean isColumnStorage(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.SET() != null && ctx.STORAGE() != null;
    }

    private boolean isIdentityColumn(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.IDENTITY_P() != null;
    }

    private boolean isConstraintAction(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.CONSTRAINT() != null;
    }

    private boolean isAddConstraint(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.ADD_P() != null && ctx.tableconstraint() != null && ctx.tableconstraint().constraintelem() != null;
    }
    
    private boolean isAlterConstraint(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.ALTER() != null && ctx.CONSTRAINT() != null;
    }
    
    private boolean isReplicaIdentity(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.REPLICA() != null && ctx.IDENTITY_P() != null;
    }
    
    private boolean isClusterOnIndex(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.CLUSTER() != null && ctx.ON() != null;
    }
    
    private boolean isAlterGenericOptions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.alter_generic_options() != null && ctx.alter_generic_options().alter_generic_option_list() != null && ctx.alter_generic_options().alter_generic_option_list().alter_generic_option_elem().size() != 0;
    }
    
    private boolean isRowLevelSecurity(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.ROW() != null && ctx.LEVEL() != null && ctx.SECURITY() != null;
    }
    
    private boolean isAlterTableOwner(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.OWNER() != null && ctx.TO() != null;
    }

    private boolean hasTriggerAction(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.TRIGGER() != null;
    }
    
    private boolean hasRuleAction(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.RULE() != null;
    }

    private boolean isInheritAction(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.INHERIT() != null;
    }

    private boolean isOfTypeAction(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.OF() != null;
    }

    private boolean isSetAction(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.SET() != null;
    }

    private boolean isRelOptions(PostgreSQLParser.Alter_table_cmdContext ctx) {
        return ctx.reloptions() != null && ctx.reloptions().reloption_list() != null && ctx.reloptions().reloption_list().reloption_elem().size() != 0;
    }

    private void addResultIfEmpty() {
        if (resultList.isEmpty()) {
            addResult();
        }
    }

    private void addResult() {
        StatementResult result = new StatementResult();
        result.setFeature(featureConfig);
        resultList.add(result);
    }
    
    private void addResult(String featureKey) {
        StatementResult result = new StatementResult();
        Feature currFeatureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        if (!featureKey.equals("unsupported")) {
            currFeatureConfig = configLoader.getFeatureConfig(featureKey);
        }
        result.setFeature(currFeatureConfig);
        resultList.add(result);
    }    
}
