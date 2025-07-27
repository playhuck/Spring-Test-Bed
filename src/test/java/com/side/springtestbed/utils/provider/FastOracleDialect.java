package com.side.springtestbed.utils.provider;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.tool.schema.extract.internal.SequenceInformationExtractorNoOpImpl;
import org.hibernate.tool.schema.extract.spi.SequenceInformationExtractor;

public class FastOracleDialect extends OracleDialect {

    public FastOracleDialect() {
    }

    public FastOracleDialect(DatabaseVersion version) {
        super(version);
    }

    public FastOracleDialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    public SequenceInformationExtractor getSequenceInformationExtractor() {
        return SequenceInformationExtractorNoOpImpl.INSTANCE;
    }
}

