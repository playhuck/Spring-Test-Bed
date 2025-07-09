package com.side.springtestbed.common.listener;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FilterQueryListener implements QueryExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger("DataSource.logger");

    @Override
    public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {}

    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {

        List<Boolean> isDDLQuery = list.stream().map(q -> isDDLQuery(q.getQuery())).toList();

        logger.error("isDDL getQuery : {}", list.get(0).getQuery());
        if(isDDLQuery.isEmpty()) print(executionInfo, list);

    }

    private boolean isDDLQuery(String query) {

        String trimQuery = query.trim().toLowerCase();

        return
                trimQuery.startsWith("create table") ||
                        trimQuery.startsWith("drop table") ||
                        trimQuery.startsWith("create index") ||
                        trimQuery.startsWith("drop index");
    }

    private void print(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name:").append(execInfo.getDataSourceName())
                .append(", Connection:").append(execInfo.getConnectionId())
                .append(", Time:").append(execInfo.getElapsedTime()).append("ms")
                .append(", Success:").append(execInfo.isSuccess())
                .append("\n");

        for (QueryInfo queryInfo : queryInfoList) {
            sb.append("Query:[\"").append(queryInfo.getQuery()).append("\"]\n");
            if (!queryInfo.getParametersList().isEmpty()) {
                sb.append("Params:").append(queryInfo.getParametersList()).append("\n");
            }
        }

        // 슬로우 쿼리 체크 (300ms 이상)
        if (execInfo.getElapsedTime() > 300) {
            logger.warn("SLOW QUERY DETECTED: {}", sb);
        } else {
            logger.info(sb.toString());
        }
    }

}
