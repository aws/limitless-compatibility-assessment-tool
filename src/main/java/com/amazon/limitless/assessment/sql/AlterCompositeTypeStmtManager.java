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

public class AlterCompositeTypeStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.AltercompositetypestmtContext> {
    public AlterCompositeTypeStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.AltercompositetypestmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

        if (ctx.TYPE_P() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_type");
        }

        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}
