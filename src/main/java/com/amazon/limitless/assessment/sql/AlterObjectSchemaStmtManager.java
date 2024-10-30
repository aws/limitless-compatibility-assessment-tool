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

public class AlterObjectSchemaStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.AlterobjectschemastmtContext> {

    public AlterObjectSchemaStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.AlterobjectschemastmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

        if (ctx.EXTENSION() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_extension_set_schema");
            List<PostgreSQLParser.NameContext> extensionNameList = ctx.name();
            boolean isSupportedExtension = true;

            for (PostgreSQLParser.NameContext nameCtx : extensionNameList)
            {
                String extensionName = nameCtx.colid().identifier().Identifier().getText().trim();
                // Check if the extension is supported or not
                isSupportedExtension = configLoader.isSupportedExtension(extensionName);
                if (!isSupportedExtension)
                {
                    featureConfig = new Feature("", false, String.format("Extension %s is not supported", extensionName));
                    result.setFeature(featureConfig);
                    resultList.add(result);
                }
            }
        }
        //Below if logic will handle SQLs:
        //ALTER VIEW [ IF EXISTS ] name SET SCHEMA new_schema
        else if (ctx.VIEW() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_view_change_schema");
        }

        else if (ctx.TYPE_P() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_type");
        }

        // handle ALTER TABLE SQLs
        else if (ctx.ALTER() != null && ctx.TABLE() != null && ctx.FOREIGN() == null)
        {
            if (ctx.SET() != null && ctx.SCHEMA() != null)
                featureConfig = configLoader.getFeatureConfig("alter_table_set_schema");
        }

        if (resultList.isEmpty())
        {
            result.setFeature(featureConfig);
            resultList.add(result);
        }
        return resultList;
    }
}
