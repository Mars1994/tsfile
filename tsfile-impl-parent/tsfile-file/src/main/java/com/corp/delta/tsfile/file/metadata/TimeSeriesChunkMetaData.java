package com.corp.delta.tsfile.file.metadata;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corp.delta.tsfile.file.metadata.converter.IConverter;
import com.corp.delta.tsfile.file.metadata.enums.CompressionTypeName;
import com.corp.delta.tsfile.file.metadata.enums.TSChunkType;
import com.corp.delta.tsfile.format.CompressionType;
import com.corp.delta.tsfile.format.TimeSeriesChunkType;

/**
 * @Description For more information, see com.corp.delta.tsfile.format.TimeSeriesChunkMetaData in
 *              tsfile-format
 * @author XuYi xuyi556677@163.com
 * @date Apr 29, 2016 11:16:40 PM
 */
public class TimeSeriesChunkMetaData
    implements IConverter<com.corp.delta.tsfile.format.TimeSeriesChunkMetaData> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesChunkMetaData.class);

  private TimeSeriesChunkProperties properties;

  private long numRows;
  
  /** 
   * total byte size of all uncompressed pages in this time series chunk (including the headers) 
   */
  private long totalByteSize;

  /**
   * Optional json metadata
   */
  private List<String> jsonMetaData;

  /**
   * Byte offset from beginning of file to first data page
   */
  private long dataPageOffset;

  /**
   * Byte offset from beginning of file to root index page
   */
  private long indexPageOffset;

  /**
   * Byte offset from the beginning of file to first (only) dictionary page
   */
  private long dictionaryPageOffset;

  /**
   * one of TSeriesMetaData and VSeriesMetaData is not null
   */
  private TInTimeSeriesChunkMetaData tInTimeSeriesChunkMetaData;
  private VInTimeSeriesChunkMetaData vInTimeSeriesChunkMetaData;

  public TimeSeriesChunkMetaData() {
    properties = new TimeSeriesChunkProperties();
    jsonMetaData = new ArrayList<String>();
  }

  public TimeSeriesChunkMetaData(String measurementUID, TSChunkType tsChunkGroup, long fileOffset,
      CompressionTypeName compression) {
    this();
    this.properties = new TimeSeriesChunkProperties(measurementUID, tsChunkGroup, fileOffset, compression);
  }

  public TimeSeriesChunkProperties getProperties() {
    return properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.corp.delta.tsfile.file.metadata.converter.IConverter#convertToThrift()
   */
  @Override
  public com.corp.delta.tsfile.format.TimeSeriesChunkMetaData convertToThrift() {
    try {
      com.corp.delta.tsfile.format.TimeSeriesChunkMetaData metadataInThrift = initTimeSeriesChunkMetaDataInThrift();
      if (tInTimeSeriesChunkMetaData != null) {
        metadataInThrift.setTime_tsc(tInTimeSeriesChunkMetaData.convertToThrift());
      }
      if (vInTimeSeriesChunkMetaData != null) {
        metadataInThrift.setValue_tsc(vInTimeSeriesChunkMetaData.convertToThrift());
      }
      return metadataInThrift;
    } catch (Exception e) {
      if (LOGGER.isErrorEnabled())
        LOGGER.error(
            "tsfile-file TimeSeriesChunkMetaData: failed to convert TimeSeriesChunkMetaData from TSFile to thrift, content is {}",
            this, e);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.corp.delta.tsfile.file.metadata.converter.IConverter#convertToTSF(java.lang.Object)
   */
  @Override
  public void convertToTSF(com.corp.delta.tsfile.format.TimeSeriesChunkMetaData metadataInThrift) {
    try {
      initTimeSeriesChunkMetaDataInTSFile(metadataInThrift);
      if(metadataInThrift.getTime_tsc() == null){
        tInTimeSeriesChunkMetaData = null;
      }
      else{
        if (tInTimeSeriesChunkMetaData == null) {
          tInTimeSeriesChunkMetaData = new TInTimeSeriesChunkMetaData();
        }
        tInTimeSeriesChunkMetaData.convertToTSF(metadataInThrift.getTime_tsc());
      }
      if(metadataInThrift.getValue_tsc() == null){
        vInTimeSeriesChunkMetaData = null;
      }
      else {
        if (vInTimeSeriesChunkMetaData == null) {
          vInTimeSeriesChunkMetaData = new VInTimeSeriesChunkMetaData();
        }
        vInTimeSeriesChunkMetaData.convertToTSF(metadataInThrift.getValue_tsc());
      }
    } catch (Exception e) {
      if (LOGGER.isErrorEnabled())
        LOGGER.error(
            "tsfile-file TimeSeriesChunkMetaData: failed to convert TimeSeriesChunkMetaData from thrift to TSFile, content is {}",
            metadataInThrift, e);
    }
  }

  private com.corp.delta.tsfile.format.TimeSeriesChunkMetaData initTimeSeriesChunkMetaDataInThrift() {
    com.corp.delta.tsfile.format.TimeSeriesChunkMetaData metadataInThrift =
        new com.corp.delta.tsfile.format.TimeSeriesChunkMetaData(
            properties.getMeasurementUID(),
            properties.getTsChunkType() == null ? null : TimeSeriesChunkType.valueOf(properties.getTsChunkType().toString()),
            properties.getFileOffset(), 
            properties.getCompression() == null ? null : CompressionType.valueOf(properties.getCompression().toString()));
    metadataInThrift.setNum_rows(numRows);
    metadataInThrift.setTotal_byte_size(totalByteSize);
    metadataInThrift.setJson_metadata(jsonMetaData);
    metadataInThrift.setData_page_offset(dataPageOffset);
    metadataInThrift.setIndex_page_offset(indexPageOffset);
    metadataInThrift.setDictionary_page_offset(dictionaryPageOffset);
    return metadataInThrift;
  }

  private void initTimeSeriesChunkMetaDataInTSFile(
      com.corp.delta.tsfile.format.TimeSeriesChunkMetaData metadataInThrift) {
    properties = new TimeSeriesChunkProperties(
        metadataInThrift.getMeasurement_uid(),
        metadataInThrift.getTimeseries_chunk_type() == null ? null : TSChunkType.valueOf(metadataInThrift.getTimeseries_chunk_type().toString()),
        metadataInThrift.getFile_offset(), 
        metadataInThrift.getCompression_type() == null ? null : CompressionTypeName.valueOf(metadataInThrift.getCompression_type().toString()));
    numRows = metadataInThrift.getNum_rows();
    totalByteSize = metadataInThrift.getTotal_byte_size();
    jsonMetaData = metadataInThrift.getJson_metadata();
    dataPageOffset = metadataInThrift.getData_page_offset();
    indexPageOffset = metadataInThrift.getIndex_page_offset();
    dictionaryPageOffset = metadataInThrift.getDictionary_page_offset();
  }

  @Override
  public String toString() {
    return String.format(
        "TimeSeriesChunkProperties %s, numRows %d, totalByteSize %d, jsonMetaData %s, dataPageOffset %d, indexPageOffset %d, dictionaryPageOffset %s",
        properties, numRows, totalByteSize, jsonMetaData, dataPageOffset, indexPageOffset,
        dictionaryPageOffset);
  }

  public long getNumRows() {
    return numRows;
  }

  public long getTotalByteSize() {
    return totalByteSize;
  }

  public List<String> getJsonMetaData() {
    return jsonMetaData;
  }

  public long getDataPageOffset() {
    return dataPageOffset;
  }

  public long getIndexPageOffset() {
    return indexPageOffset;
  }

  public long getDictionaryPageOffset() {
    return dictionaryPageOffset;
  }

  public void setProperties(TimeSeriesChunkProperties properties) {
    this.properties = properties;
  }

  public void setNumRows(long numRows) {
    this.numRows = numRows;
  }

  public void setTotalByteSize(long totalByteSize) {
    this.totalByteSize = totalByteSize;
  }

  public void setJsonMetaData(List<String> jsonMetaData) {
    this.jsonMetaData = jsonMetaData;
  }

  public void setDataPageOffset(long dataPageOffset) {
    this.dataPageOffset = dataPageOffset;
  }

  public void setIndexPageOffset(long indexPageOffset) {
    this.indexPageOffset = indexPageOffset;
  }

  public void setDictionaryPageOffset(long dictionaryPageOffset) {
    this.dictionaryPageOffset = dictionaryPageOffset;
  }

  public TInTimeSeriesChunkMetaData getTInTimeSeriesChunkMetaData() {
    return tInTimeSeriesChunkMetaData;
  }

  public void setTInTimeSeriesChunkMetaData(TInTimeSeriesChunkMetaData tInTimeSeriesChunkMetaData) {
    this.tInTimeSeriesChunkMetaData = tInTimeSeriesChunkMetaData;
  }

  public VInTimeSeriesChunkMetaData getVInTimeSeriesChunkMetaData() {
    return vInTimeSeriesChunkMetaData;
  }

  public void setVInTimeSeriesChunkMetaData(VInTimeSeriesChunkMetaData vInTimeSeriesChunkMetaData) {
    this.vInTimeSeriesChunkMetaData = vInTimeSeriesChunkMetaData;
  }
}