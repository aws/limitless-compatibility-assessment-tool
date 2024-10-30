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

import java.lang.Math;

public class CreateSeqStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.CreateseqstmtContext> {

    private long defaultMinDataTypeValue;
    private long defaultMaxDataTypeValue;

    public CreateSeqStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.CreateseqstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();

        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

        PostgreSQLParser.OpttempContext optTempCtx;
        if ((optTempCtx = ctx.opttemp()) != null && (optTempCtx.TEMPORARY() != null || optTempCtx.TEMP() != null || optTempCtx.UNLOGGED() != null))
        {  
            featureConfig = configLoader.getFeatureConfig("create_temp_sequence");
        }   
        else if (ctx.optseqoptlist() != null)
        {
            featureConfig = configLoader.getFeatureConfig("create_sequence");
            if (featureConfig.isSupported())
            {
                featureConfig = validate_sequence(ctx);
            }
        }
        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }


    private void setDefaultMinMax (String asType) {
        switch (asType.toLowerCase()) {
            case "smallint":
                defaultMinDataTypeValue = Short.MIN_VALUE;
                defaultMaxDataTypeValue = Short.MAX_VALUE;
                break;
            case "int": case "integer":
                defaultMinDataTypeValue = Integer.MIN_VALUE;
                defaultMaxDataTypeValue = Integer.MAX_VALUE;
                break;
            default: 
                defaultMinDataTypeValue = Long.MIN_VALUE;
                defaultMaxDataTypeValue = Long.MAX_VALUE;
                break;
        }
    }

    private long calculateSequenceSize(Long increment_by, Long minValue, Long maxValue, Long startWith) {
        long sequenceSize = 0;
        if (increment_by < 0)
        {
            sequenceSize = (minValue - startWith) - 1;
        }
        else if (increment_by > 0)
        {
            sequenceSize = (maxValue - startWith) + 1;
        }

        // BIGINT_MIN value is out of range when converted to positive.
        if (sequenceSize <= Long.MIN_VALUE) sequenceSize = Long.MIN_VALUE + 1; 
        return Math.abs(sequenceSize);
    }

    private Feature validate_sequence(PostgreSQLParser.CreateseqstmtContext ctx) {
        String asType = "bigint";
        long cache = 1;
        long increment_by = 1;
        long maxValue = Long.MAX_VALUE;
        long minValue = 1;
        long startWith = 1;
        boolean noMaxValue = false;
        boolean noMinValue = false;
        long defaultMaxChunkSize = 250000;
        int defaultMinChunkSize = 2;
        long finalChunkSize = 0;
        long sequenceSize = 0;
        long check_chunk_boundary = 0;
        long numRouters = 2;
        int minSequenceSize = 16; // As minimum chunk size is 2 and we are assuming 2R/4S config by default
        String errorMsg = "";

        for (PostgreSQLParser.SeqoptelemContext seqOptElemContext: ctx.optseqoptlist().seqoptlist().seqoptelem())
            {  
                if (seqOptElemContext.simpletypename() != null)
                {
                    asType = seqOptElemContext.simpletypename().getText();
                }
                else if (seqOptElemContext.START() != null)
                {
                    startWith = Long.parseLong(seqOptElemContext.numericonly().getText()); 
                }
                else if (seqOptElemContext.INCREMENT()!=null)
                {
                    increment_by = Long.parseLong(seqOptElemContext.numericonly().getText());
                }
                else if (seqOptElemContext.NO() != null && seqOptElemContext.MINVALUE() != null)
                {
                    noMinValue = true;
                }
                else if (seqOptElemContext.MINVALUE () != null)
                {
                    minValue = Long.parseLong(seqOptElemContext.numericonly().getText()); 
                }
                else if (seqOptElemContext.NO() != null && seqOptElemContext.MAXVALUE() != null)
                {
                    noMaxValue = true;
                }
                else if (seqOptElemContext.MAXVALUE() != null)
                {
                    maxValue = Long.parseLong(seqOptElemContext.numericonly().getText());
                }
                else if (seqOptElemContext.CACHE() != null)
                {
                    cache = Long.parseLong(seqOptElemContext.numericonly().getText());
                }
                else if (seqOptElemContext.CYCLE() !=null || (seqOptElemContext.NO() != null && seqOptElemContext.CYCLE() != null))
                {
                    Feature featureConfig = configLoader.getFeatureConfig("create_sequence_cycle");
                    return featureConfig;
                }
            }

            setDefaultMinMax(asType);

            if (increment_by < 0 && noMinValue)
            {
                minValue = defaultMinDataTypeValue;
            }
            else if (increment_by > 0 && noMaxValue)
            {
                maxValue = defaultMaxDataTypeValue;
            }

            sequenceSize = calculateSequenceSize(increment_by, minValue, maxValue, startWith);
            
            // To calculate the minimum required chunk size.
            finalChunkSize = Math.min(defaultMaxChunkSize, (long) (0.5 * (sequenceSize/(2 * numRouters))));

            check_chunk_boundary = increment_by * finalChunkSize;

            Feature featureConfig = new Feature("create_sequence", true, "");
            if (increment_by == 0)
            {
                errorMsg = "INCREMENT must not be zero";
                featureConfig.setSupported(false);
            }
            else if (sequenceSize<minSequenceSize) 
            { 
                errorMsg = String.format("Not enough values to create distributed sequence. Please specify sequence size to be greater than 2 * chunk_size * sequence_increment * num_routers."+
                "chunk_size=%d,sequence_increment=%d,num_routers=%d", finalChunkSize, (long) increment_by, numRouters);
                featureConfig.setSupported(false);
            }
            else if (cache>finalChunkSize){
                errorMsg = String.format("CACHE parameter must be less than chunk size: %d", finalChunkSize);
                featureConfig.setSupported(false);
            }
            else if (increment_by < 0 && check_chunk_boundary < defaultMinDataTypeValue)
            {   errorMsg = String.format("MINVALUE (%d) is out of range for sequence data type %s", check_chunk_boundary, asType);
                featureConfig.setSupported(false);
            }
            else if (increment_by > 0 && check_chunk_boundary > defaultMaxDataTypeValue) 
            {   errorMsg = String.format("MAXVALUE (%d) is out of range for sequence data type %s", check_chunk_boundary, asType);
                featureConfig.setSupported(false);
            }
            featureConfig.setErrorMessage(errorMsg);
            return featureConfig;
        }
}