// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.common.ObjectName;
import com.amazon.limitless.assessment.common.DependencyObject;
import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

public abstract class PostgreSqlStmtBaseManager {
    ConfigLoader configLoader;
    List<StatementResult> resultList;
    StatementResult result;

    public PostgreSqlStmtBaseManager(ConfigLoader configLoader)
    {
        this.configLoader = configLoader;
        resultList = new ArrayList<>();
        result = new StatementResult();
    }
}
