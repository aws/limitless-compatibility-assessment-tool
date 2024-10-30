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

public class CreateAmStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.CreateamstmtContext> {

    public CreateAmStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.CreateamstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = configLoader.getFeatureConfig("create_access_method");
        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}