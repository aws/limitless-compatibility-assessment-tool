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

public class IndexStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.IndexstmtContext> {

    public IndexStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.IndexstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        // Below logic will handle create index SQL
        Feature featureConfig = configLoader.getFeatureConfig("index_create");

        // Below logic will handle index types
        if (ctx.access_method_clause().USING() != null) {
            String indexType = ctx.access_method_clause().name().colid().identifier().Identifier().getText();
            if (!configLoader.isSupportedIndexType(indexType)) {
                featureConfig = new Feature("", false, String.format("Index type %s is not supported", indexType));
                result.setFeature(featureConfig);
                resultList.add(result);
            }
        }

        // Below logic will handle create unique index SQL
        if (ctx.opt_unique().UNIQUE() != null) {
            featureConfig = configLoader.getFeatureConfig("index_create_unique");
        }

        if (resultList.isEmpty())
        {
            result.setFeature(featureConfig);
            resultList.add(result);
        }
        return resultList;
    }
}
