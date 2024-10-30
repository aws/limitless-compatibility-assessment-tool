// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.util.ColumnDefStmtUtil;
import com.amazon.limitless.assessment.util.ConstraintsStmtUtil;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import com.amazon.limitless.assessment.common.DependencyObject;
import com.amazon.limitless.assessment.common.ObjectName;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class CreateTableStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.CreatestmtContext> {

    public CreateTableStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.CreatestmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        ObjectName objectName;
        String fullObjectName;
        Feature featureConfig = configLoader.getFeatureConfig("create_table_standard");

        // unsupported
        if (ctx.OF() != null && ctx.PARTITION() == null)
            updateResultsWithFeature("create_table_of_type", result, resultList);

        // unsupported
        PostgreSQLParser.OncommitoptionContext onCommitOptionCtx = ctx.oncommitoption();
        if (onCommitOptionCtx != null)
        {
            String featureName = "";
            if (onCommitOptionCtx.DROP() != null)
                featureName = "parameter_on_commit_drop";
            else if (onCommitOptionCtx.DELETE_P() != null)
                featureName = "parameter_on_commit_delete_rows";
            else if (onCommitOptionCtx.PRESERVE() != null)
                featureName = "parameter_on_commit_preserve_rows";

            if (!featureName.isEmpty())
                updateResultsWithFeature(featureName, result, resultList);
        }

        if (ctx.IF_P() != null && ctx.NOT() != null && ctx.EXISTS() != null)
            featureConfig = configLoader.getFeatureConfig("parameter_if_not_exists_create_table");
        
        PostgreSQLParser.OptwithContext optWithCtx = ctx.optwith();
        if (optWithCtx != null && (optWithCtx.WITH() != null || optWithCtx.WITHOUT() != null))
            featureConfig = configLoader.getFeatureConfig("storage_parameter");
        
        // unsupported
        PostgreSQLParser.PartitionspecContext partitionSpecCtx;
        if (ctx.optpartitionspec() != null && (partitionSpecCtx = ctx.optpartitionspec().partitionspec()) != null && partitionSpecCtx.PARTITION() != null && partitionSpecCtx.BY() != null)
            updateResultsWithFeature("parameter_partition_by", result, resultList);

        // unsupported
        PostgreSQLParser.OpttablespaceContext optTablespaceCtx;
        if ((optTablespaceCtx = ctx.opttablespace()) != null  && optTablespaceCtx.TABLESPACE() != null)
            updateResultsWithFeature("parameter_tablespace", result, resultList);

        // unsupported
        PostgreSQLParser.OptinheritContext optInheritCtx;
        if ((optInheritCtx = ctx.optinherit()) != null && optInheritCtx.INHERITS() != null)
            updateResultsWithFeature("parameter_inherits", result, resultList);

        // unsupported
        PostgreSQLParser.Table_access_method_clauseContext tableAccessMethodCtx;
        if ((tableAccessMethodCtx = ctx.table_access_method_clause()) != null && tableAccessMethodCtx.USING() != null)
            updateResultsWithFeature("parameter_using_method", result, resultList);

        // unsupported
        PostgreSQLParser.OpttempContext optTempCtx;
        if ((optTempCtx = ctx.opttemp()) != null && (optTempCtx.TEMPORARY() != null || optTempCtx.TEMP() != null))
            updateResultsWithFeature("create_temporary_table", result, resultList);

        if (ctx.opttableelementlist() != null && ctx.opttableelementlist().tableelementlist() != null)
        {
            List<PostgreSQLParser.TableelementContext> tableElementList = ctx.opttableelementlist().tableelementlist().tableelement();
            if (tableElementList.size() != 0)
            {
                for (PostgreSQLParser.TableelementContext tableElementCtx : tableElementList)
                {
                    if (tableElementCtx.columnDef() != null)
                    {
                        PostgreSQLParser.ColumnDefContext columnDefCtx = tableElementCtx.columnDef(); 
                        resultList.addAll(ColumnDefStmtUtil.analyzeColumnDefStatement(columnDefCtx, true, configLoader));
                    }
                    else if (tableElementCtx.tableconstraint() != null)
                    {
                        PostgreSQLParser.TableconstraintContext tableConstraintCtx = tableElementCtx.tableconstraint();
                        resultList.add(ConstraintsStmtUtil.analyzeConstraintStatement(tableConstraintCtx, true, false, configLoader));
                    }
                    else if (tableElementCtx.tablelikeclause() != null)
                    {
                        PostgreSQLParser.TablelikeclauseContext tableLikeClauseCtx = tableElementCtx.tablelikeclause();
                        
                        // unsupported
                        if (tableLikeClauseCtx.LIKE() != null)
                            updateResultsWithFeature("create_table_like", result, resultList);
                    }
                }
            }
        }

        // Remove all supported Features from featureList. If all feature are supported, we will have empty list and mark as supported below
        ListIterator<StatementResult> iter = resultList.listIterator();
        while(iter.hasNext()){
            if(iter.next().getFeature().supported()){
                iter.remove();
            }
        }

        if (resultList.isEmpty())
        {
            result.setFeature(featureConfig);
            resultList.add(result);
            objectName = getObjectName(ctx);
            fullObjectName = DependencyObject.generateFullObjectName(objectName.getNamespace(), objectName.getObjectName());
            if (fullObjectName!=null)
                DependencyObject.setObject(fullObjectName,"table");
        }
        
        return resultList;
    }

    /**
     * Helper function to update the result list on the basis of featureName
     */
    private void updateResultsWithFeature(String featureName, StatementResult result, List<StatementResult> resultList) {
        Feature featureConfig = configLoader.getFeatureConfig(featureName);
        result.setFeature(featureConfig);
        resultList.add(result);
    }

    private static ObjectName getObjectName(PostgreSQLParser.CreatestmtContext ctx)
    {
        String namespace = ctx.qualified_name(0).colid().identifier().Identifier().getText();

        String objectName;
        if (ctx.qualified_name(0).indirection() != null) {
            objectName = ctx.qualified_name(0).indirection().indirection_el(0).attr_name().collabel().identifier().getText();
            return new ObjectName(namespace, objectName);
        }
        objectName = namespace;
        return new ObjectName(objectName);
    }
}
