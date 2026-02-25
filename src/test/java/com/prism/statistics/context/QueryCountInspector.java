package com.prism.statistics.context;

import java.util.concurrent.atomic.AtomicInteger;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import java.util.List;

public class QueryCountInspector implements QueryExecutionListener {

    private final AtomicInteger insertQueryCount = new AtomicInteger(0);

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo queryInfo : queryInfoList) {
            String query = queryInfo.getQuery().trim().toLowerCase();
            if (query.startsWith("insert")) {
                insertQueryCount.incrementAndGet();
            }
        }
    }

    public int getInsertQueryCount() {
        return insertQueryCount.get();
    }

    public void reset() {
        insertQueryCount.set(0);
    }
}
