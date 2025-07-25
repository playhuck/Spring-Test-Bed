package com.side.springtestbed.hibernate.autocommit;

import org.w3c.dom.Document;

import java.util.List;

public class AutoCommitTest {

    private void importForecasts() {
        doInJPA(entityManager -> {
            List<Forecast> forecasts = null;

            for (int i = 0; i < parseCount; i++) {
                Document forecastXmlDocument = readXmlDocument(DATA_FILE_PATH);
                forecasts = parseForecasts(forecastXmlDocument);
            }

            if (forecasts != null) {
                for (Forecast forecast : forecasts.subList(0, 50)) {
                    entityManager.persist(forecast);
                }
            }
        });
    }

}
