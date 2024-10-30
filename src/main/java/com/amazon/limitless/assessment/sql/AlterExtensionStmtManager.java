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

public class AlterExtensionStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.AlterextensionstmtContext> {

    public AlterExtensionStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.AlterextensionstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        String extensionName = ctx.name().colid().identifier().Identifier().getText().trim();
        Feature featureConfig = configLoader.getFeatureConfig("alter_extension_update_version");

        // Check if the extension is supported or not
        boolean isSupportedExtension = configLoader.isSupportedExtension(extensionName);
        if (!isSupportedExtension)
            featureConfig = new Feature("", false, String.format("Extension %s is not supported", extensionName));

        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}
