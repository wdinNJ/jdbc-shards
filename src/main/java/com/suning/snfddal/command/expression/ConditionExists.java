/*
 * Copyright 2004-2014 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.suning.snfddal.command.expression;

import com.suning.snfddal.command.dml.Query;
import com.suning.snfddal.dbobject.table.ColumnResolver;
import com.suning.snfddal.dbobject.table.TableFilter;
import com.suning.snfddal.engine.Session;
import com.suning.snfddal.result.LocalResult;
import com.suning.snfddal.util.StringUtils;
import com.suning.snfddal.value.Value;
import com.suning.snfddal.value.ValueBoolean;

/**
 * An 'exists' condition as in WHERE EXISTS(SELECT ...)
 */
public class ConditionExists extends Condition {

    private final Query query;

    public ConditionExists(Query query) {
        this.query = query;
    }

    @Override
    public Value getValue(Session session) {
        query.setSession(session);
        LocalResult result = query.query(1);
        session.addTemporaryResult(result);
        boolean r = result.getRowCount() > 0;
        return ValueBoolean.get(r);
    }

    @Override
    public Expression optimize(Session session) {
        query.prepare();
        return this;
    }

    @Override
    public String getSQL() {
        return "EXISTS(\n" + StringUtils.indent(query.getPlanSQL(), 4, false) + ")";
    }

    @Override
    public void updateAggregate(Session session) {
        // TODO exists: is it allowed that the subquery contains aggregates?
        // probably not
        // select id from test group by id having exists (select * from test2
        // where id=count(test.id))
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        query.mapColumns(resolver, level + 1);
    }

    @Override
    public void setEvaluatable(TableFilter tableFilter, boolean b) {
        query.setEvaluatable(tableFilter, b);
    }

    @Override
    public boolean isEverything(ExpressionVisitor visitor) {
        return query.isEverything(visitor);
    }

    @Override
    public int getCost() {
        return query.getCostAsExpression();
    }

}
