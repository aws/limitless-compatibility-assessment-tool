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

public class CreateSchemaStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.CreateschemastmtContext> {

    /**
     * TODO(chsaikia@): Improve how one DDL might need to store multiple feature contexts.
     * For eg: CreateSchema needs to track create_schema_with_elements as well.
     */
    public CreateSchemaStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.CreateschemastmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = configLoader.getFeatureConfig("create_schema");
        if (ctx.optschemaeltlist().schema_stmt().size() != 0)
        {
            featureConfig = configLoader.getFeatureConfig("create_schema_with_elements");
        }
        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}