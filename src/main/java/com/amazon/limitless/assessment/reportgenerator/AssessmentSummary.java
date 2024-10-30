// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.reportgenerator;

import lombok.Getter;
import lombok.Setter;

import com.amazon.limitless.assessment.configloader.Feature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentSummary {
    @Getter
    @Setter
    private static int totalStatementCount = 0;

    private static int count = 0;
    List<String> statements = new ArrayList<>();
    Map<Integer, List<String>> unsupportedStatements = new HashMap<>();
    List<Integer> supportedStatements = new ArrayList<>();
    List<StatementResult> unableToProcessStatements = new ArrayList<>();

    private AssessmentSummary() { }

    private static class AssessmentSummaryHelper {
        private static final AssessmentSummary INSTANCE = new AssessmentSummary();
    }

    public static AssessmentSummary getInstance() {
        return AssessmentSummaryHelper.INSTANCE;
    }

    public void addToUnableToProcessStatements(StatementResult stmt)
    {
        unableToProcessStatements.add(stmt);
    }

    public void processResult(StatementResult stmt, int index)
    {
        List<StatementResult> stmtList = new ArrayList<>();
        stmtList.add(stmt);
        processResult(stmtList, index);
    }

    public void processResult(List<StatementResult> stmtList, int index)
    {
        for (StatementResult stmt : stmtList)
        {
            if (stmt.getFeature().isSupported())
            {
                supportedStatements.add(index);
            }
            else {
                unsupportedStatements.computeIfAbsent(index, k -> new ArrayList<>()).add(stmt.getFeature().getErrorMessage());
            }
        }
        totalStatementCount++;
    }

    public void storeInput(String stmt)
    {
        statements.add(stmt);
    }

    public String getInput(int idx)
    {
        return statements.get(idx);
    }
}
