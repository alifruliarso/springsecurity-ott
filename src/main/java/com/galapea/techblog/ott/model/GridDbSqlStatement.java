package com.galapea.techblog.ott.model;

public class GridDbSqlStatement {
  private String stmt;

  public GridDbSqlStatement() {}

  public GridDbSqlStatement(String stmt) {
    this.stmt = stmt;
  }

  public String getStmt() {
    return stmt;
  }

  public void setStmt(String stmt) {
    this.stmt = stmt;
  }
}
