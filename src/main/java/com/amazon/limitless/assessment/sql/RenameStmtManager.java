// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;

import java.util.ArrayList;
import java.util.List;

public class RenameStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.RenamestmtContext> {

    public RenameStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.RenamestmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;
        // Below if logic will handle SQLs:
        // ALTER INDEX [ IF EXISTS ] name RENAME TO new_name
        if (ctx.INDEX() != null) {
            featureConfig = configLoader.getFeatureConfig("rename_alter_index");
        }
        // Below if logic will handle SQLs:
        // ALTER VIEW [ IF EXISTS ] name RENAME [ COLUMN ] column_name TO new_column_name
        // ALTER VIEW [ IF EXISTS ] name RENAME TO new_name
        else if (ctx.VIEW() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_view_rename");
        }
        else if (ctx.TYPE_P() != null) {
            featureConfig = configLoader.getFeatureConfig("alter_type");
        }

        // handle ALTER TABLE SQLs
        else if (ctx.TABLE() != null && ctx.RENAME() != null && ctx.FOREIGN() == null)
        {
            if (ctx.opt_column() != null && ctx.opt_column().COLUMN() != null)
            {
                if (ctx.relation_expr() != null && ctx.relation_expr().ONLY() != null)
                {
                    featureConfig = configLoader.getFeatureConfig("alter_table_only");
                    result.setFeature(featureConfig);
                    resultList.add(result);  
                }
                else
                    featureConfig = configLoader.getFeatureConfig("alter_table_rename_column");
            }
            else if (ctx.CONSTRAINT() != null)
                featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
            else
                featureConfig = configLoader.getFeatureConfig("alter_table_rename");
        }

        if (resultList.isEmpty())
        {
            result.setFeature(featureConfig);
            resultList.add(result);
        }

        return resultList;
    }
}
