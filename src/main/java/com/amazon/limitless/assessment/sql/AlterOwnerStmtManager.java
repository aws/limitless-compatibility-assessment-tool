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

public class AlterOwnerStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.AlterownerstmtContext> {
    public AlterOwnerStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.AlterownerstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        if (ctx.SCHEMA() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_schema_owner_to");
        }
        else if (ctx.AGGREGATE() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_aggregate_owner_to");
        }
        else if (ctx.STATISTICS() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_statistics_owner_to");
        }
        else if (ctx.SUBSCRIPTION() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_subscription_owner_to");
        }
        else if (ctx.COLLATION() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_collation_owner_to");
        }
        else if (ctx.FUNCTION() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_function");
        }
        else if (ctx.TYPE_P() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_type");
        }

        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}
