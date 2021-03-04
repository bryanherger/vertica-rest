package com.herger.verticarest;

public class VerticaSql {
	private String fields, table, where, order, group, limit, offset;
	
	public VerticaSql() {
		fields = "*";
		limit = "10";
	}

	public String toSql() {
		String sql = "SELECT "+fields+" FROM "+table;
		if (where != null) {
			sql = sql + " WHERE " + where;
		}
		if (group != null) {
			sql = sql + " GROUP BY " + group;
		}
		if (order != null) {
			sql = sql + " ORDER BY " + order;
		}
		if (offset != null) {
			sql = sql + " OFFSET " + offset;
		}
		if (limit != null) {
			sql = sql + " LIMIT " + limit;
		}
		return sql;
	}
	
	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}
}
