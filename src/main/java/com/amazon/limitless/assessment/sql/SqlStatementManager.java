// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public interface SqlStatementManager<Ctx extends ParserRuleContext> {
    List<StatementResult> analyzeStatement(Ctx ctx);
}
