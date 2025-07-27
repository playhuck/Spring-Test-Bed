package com.side.springtestbed.utils.conn;

public interface ConnectionDecoratorFactoryService {
    int loadingIndex();

    ConnectionDecoratorFactory load();
}

