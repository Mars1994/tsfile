package com.corp.delta.tsfile.file.metadata;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.corp.delta.tsfile.file.metadata.enums.TSDataType;
import com.corp.delta.tsfile.file.metadata.enums.TSFreqType;
import com.corp.delta.tsfile.file.metadata.utils.Utils;
import com.corp.delta.tsfile.format.DataType;
import com.corp.delta.tsfile.format.FreqType;
import com.corp.delta.tsfile.format.TimeSeries;

public class TimeSeriesMetadataTest {
  public static final String measurementUID = "sensor01";
  public static final String deltaObjectType = "dev1";
  public static final int typeLength = 1024;

  @Test
  public void testConvertToThrift() {
    for (TSDataType dataType : TSDataType.values()) {
      TimeSeriesMetadata timeSeries =
          new TimeSeriesMetadata(measurementUID, dataType, deltaObjectType);
      Utils.isTimeSeriesEqual(timeSeries, timeSeries.convertToThrift());

      for (TSFreqType freqType : TSFreqType.values()) {
        timeSeries.setFreqType(freqType);
        Utils.isTimeSeriesEqual(timeSeries, timeSeries.convertToThrift());
        timeSeries.setTypeLength(typeLength);
        Utils.isTimeSeriesEqual(timeSeries, timeSeries.convertToThrift());

        List<Integer> frequencies = new ArrayList<Integer>();
        timeSeries.setFrequencies(frequencies);
        Utils.isTimeSeriesEqual(timeSeries, timeSeries.convertToThrift());

        frequencies.add(132);
        frequencies.add(432);
        frequencies.add(35435);
        timeSeries.setFrequencies(frequencies);
        Utils.isTimeSeriesEqual(timeSeries, timeSeries.convertToThrift());

        timeSeries.setFrequencies(null);
        Utils.isTimeSeriesEqual(timeSeries, timeSeries.convertToThrift());
      }
    }
  }

  @Test
  public void testConvertToTSF() {
    for (DataType dataType : DataType.values()) {
      TimeSeries timeSeries = new TimeSeries(measurementUID, dataType, deltaObjectType);
      TimeSeriesMetadata tsTimeSeries = new TimeSeriesMetadata();
      tsTimeSeries.convertToTSF(timeSeries);
      Utils.isTimeSeriesEqual(tsTimeSeries, timeSeries);

      for (FreqType freqType : FreqType.values()) {
        timeSeries.setFreq_type(freqType);
        tsTimeSeries.convertToTSF(timeSeries);
        Utils.isTimeSeriesEqual(tsTimeSeries, timeSeries);

        timeSeries.setType_length(typeLength);
        tsTimeSeries.convertToTSF(timeSeries);
        Utils.isTimeSeriesEqual(tsTimeSeries, timeSeries);

        List<Integer> frequencies = new ArrayList<Integer>();
        timeSeries.setFrequencies(frequencies);
        tsTimeSeries.convertToTSF(timeSeries);
        Utils.isTimeSeriesEqual(tsTimeSeries, timeSeries);

        frequencies.add(132);
        frequencies.add(432);
        frequencies.add(35435);
        timeSeries.setFrequencies(frequencies);
        tsTimeSeries.convertToTSF(timeSeries);
        Utils.isTimeSeriesEqual(tsTimeSeries, timeSeries);

        timeSeries.setFrequencies(null);
        tsTimeSeries.convertToTSF(timeSeries);
        Utils.isTimeSeriesEqual(tsTimeSeries, timeSeries);
      }
    }
  }
}
