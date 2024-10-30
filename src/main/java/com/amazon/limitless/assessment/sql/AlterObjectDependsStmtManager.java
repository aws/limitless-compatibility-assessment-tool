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

public class AlterObjectDependsStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.AlterobjectdependsstmtContext> {

    public AlterObjectDependsStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.AlterobjectdependsstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        if (ctx.TRIGGER() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_trigger_depends_on");
        }
        else if (ctx.MATERIALIZED() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_materialized_view_depends_on");
        }
        // Below if logic will handle SQLs:
        // ALTER INDEX name [ NO ] DEPENDS ON EXTENSION extension_name
        else if (ctx.INDEX() != null)
        {
            featureConfig = configLoader.getFeatureConfig("alter_object_depend_alter_index");
        }

        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}
