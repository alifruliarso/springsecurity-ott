package com.galapea.techblog.ott.model;

import java.util.List;
import java.util.Map;

public class GridDbQueryResult {
  private List<Map<String, Object>> results;
  private List<String> columns;

  public GridDbQueryResult() {}

  public List<Map<String, Object>> getResults() {
    return results;
  }

  public void setResults(List<Map<String, Object>> results) {
    this.results = results;
  }

  public List<String> getColumns() {
    return columns;
  }

  public void setColumns(List<String> columns) {
    this.columns = columns;
  }
}
