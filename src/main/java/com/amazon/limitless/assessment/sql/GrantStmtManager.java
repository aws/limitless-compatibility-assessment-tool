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

public class GrantStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.GrantstmtContext> {


    public GrantStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    /**
     * @param ctx
     * @return
     */
    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.GrantstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;
        if (ctx.privilege_target().DOMAIN_P() != null)
        {
            featureConfig = configLoader.getFeatureConfig("grant_privilege_on_domain");
        }
        else if (ctx.privilege_target().LANGUAGE() != null)
        {
            featureConfig = configLoader.getFeatureConfig("grant_privilege_on_language");
        }
        else if (ctx.privilege_target().LARGE_P() != null)
        {
            featureConfig = configLoader.getFeatureConfig("grant_privilege_on_largeobject");
        }
        else if ((ctx.privilege_target().PROCEDURE() != null) || (ctx.privilege_target().PROCEDURES() != null))
        {
            featureConfig = configLoader.getFeatureConfig("grant_privilege_on_procedure");
        }
        else if ((ctx.privilege_target().ROUTINE() != null) || (ctx.privilege_target().ROUTINES() != null))
        {
            featureConfig = configLoader.getFeatureConfig("grant_privilege_on_routine");
        }
        else if ((ctx.privilege_target().TYPE_P() != null))
        {
            featureConfig = configLoader.getFeatureConfig("grant_privilege_on_type");
        }
        else if ((ctx.privilege_target().FOREIGN() != null))
        {
            if (ctx.privilege_target().SERVER() != null)
            {
                featureConfig = configLoader.getFeatureConfig("grant_privilege_on_foreign_server");
            }
            else if ((ctx.privilege_target().DATA_P() != null) && (ctx.privilege_target().WRAPPER() != null))
            {
                featureConfig = configLoader.getFeatureConfig("grant_privilege_on_fdw");
            }
        }
        // TODO(chsaikia@): Add "UNABLE TO REVIEW" for GRANT statement on database and tablespace
        // TODO(chsaikia@): Reuse same feature config for "REVOKE" statements
        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}
