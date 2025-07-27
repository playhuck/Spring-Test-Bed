//package com.side.springtestbed.hibernate.autocommit;
//
//import com.side.springtestbed.utils.AbstractTest;
//import com.side.springtestbed.utils.conn.FlexyPoolConfiguration;
//import com.side.springtestbed.utils.conn.FlexyPoolDataSource;
//import com.side.springtestbed.utils.data.DataSourcePoolAdapter;
//import jakarta.persistence.*;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import javax.sql.DataSource;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.InputStream;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//public class AutoCommitTest extends AbstractTest {
//
//    private int parseCount = 100;
//
//    private long warmUpDuration = TimeUnit.SECONDS.toNanos(5);
//
//    private long measurementsDuration = TimeUnit.SECONDS.toNanos(15);
//
//    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//    private static final String DATA_FILE_PATH = "data/weather.xml";
//
//    private FlexyPoolDataSource flexyPoolDataSource;
//
//    @Test
//    public void testConnectionLeaseTime() {
//        long warmUpThreshold = System.nanoTime() + warmUpDuration;
//        LOGGER.info("Warming up");
//
//        while (System.nanoTime() < warmUpThreshold) {
//            importForecasts();
//        }
//
//        long measurementsThreshold = System.nanoTime() + measurementsDuration;
//
//        LOGGER.info("Measuring connection lease time");
//        flexyPoolDataSource.start();
//        while (System.nanoTime() < measurementsThreshold) {
//            importForecasts();
//        }
//        flexyPoolDataSource.stop();
//        sleep(500);
//    }
//
//    private void importForecasts() {
//        doInJPA(entityManager -> {
//            List<Forecast> forecasts = null;
//
//            for (int i = 0; i < parseCount; i++) {
//                Document forecastXmlDocument = readXmlDocument(DATA_FILE_PATH);
//                forecasts = parseForecasts(forecastXmlDocument);
//            }
//
//            if (forecasts != null) {
//                for (Forecast forecast : forecasts.subList(0, 50)) {
//                    entityManager.persist(forecast);
//                }
//            }
//        });
//    }
//
//    private Document readXmlDocument(String filePath) {
//        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath)) {
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(inputStream);
//            doc.getDocumentElement().normalize();
//            return doc;
//        } catch (Exception e) {
//            throw new IllegalArgumentException(e);
//        }
//    }
//
//    private List<Forecast> parseForecasts(Document xmlDocument) {
//        NodeList cityNodes = xmlDocument.getElementsByTagName("localitate");
//        List<Forecast> forecasts = new ArrayList<>();
//        for (int i = 0; i < cityNodes.getLength(); i++) {
//            Node cityNode = cityNodes.item(i);
//            String city = cityNode.getAttributes().getNamedItem("nume").getNodeValue();
//
//            NodeList forecastNodes = cityNode.getChildNodes();
//            for (int j = 0; j < forecastNodes.getLength(); j++) {
//                Node forecastNode = forecastNodes.item(j);
//                if (!"prognoza".equals(forecastNode.getNodeName())) {
//                    continue;
//                }
//
//                Forecast forecast = new Forecast();
//                forecast.setCity(city);
//
//                String dateValue = forecastNode.getAttributes().getNamedItem("data").getNodeValue();
//                try {
//                    forecast.setDate(simpleDateFormat.parse(dateValue));
//                } catch (ParseException e) {
//                    throw new IllegalArgumentException(e);
//                }
//
//                NodeList forecastDetailsNodes = forecastNode.getChildNodes();
//                for (int k = 0; k < forecastDetailsNodes.getLength(); k++) {
//                    Node forecastDetailsNode = forecastDetailsNodes.item(k);
//                    switch (forecastDetailsNode.getNodeName()) {
//                        case "temp_min":
//                            forecast.setTemperatureMin(Byte.valueOf(forecastDetailsNode.getTextContent()));
//                            break;
//                        case "temp_max":
//                            forecast.setTemperatureMax(Byte.valueOf(forecastDetailsNode.getTextContent()));
//                            break;
//                        case "fenomen_descriere":
//                            forecast.setDescription(forecastDetailsNode.getTextContent());
//                            break;
//                    }
//                }
//
//                forecasts.add(forecast);
//            }
//        }
//        return forecasts;
//    }
//
//    @Override
//    protected DataSource newDataSource() {
//        DataSource dataSource = super.newDataSource();
//
//        FlexyPoolConfiguration<DataSource> configuration = new FlexyPoolConfiguration.Builder<>(
//                getClass().getSimpleName(), dataSource, DataSourcePoolAdapter.FACTORY)
//                .setMetricLogReporterMillis(TimeUnit.SECONDS.toMillis(15))
//                .build();
//        flexyPoolDataSource = new FlexyPoolDataSource<>(configuration);
//        return flexyPoolDataSource;
//    }
//
//    @Entity(name = "Forecast")
//    public static class Forecast {
//
//        @Id
//        @GeneratedValue
//        private Long id;
//
//        private String city;
//
//        @Temporal(TemporalType.DATE)
//        @Column(name = "forecast_date")
//        private Date date;
//
//        @Column(name = "temperature_min")
//        private byte temperatureMin;
//
//        @Column(name = "temperature_max")
//        private byte temperatureMax;
//
//        private String description;
//
//        public String getCity() {
//            return city;
//        }
//
//        public void setCity(String city) {
//            this.city = city;
//        }
//
//        public Date getDate() {
//            return date;
//        }
//
//        public void setDate(Date date) {
//            this.date = date;
//        }
//
//        public byte getTemperatureMin() {
//            return temperatureMin;
//        }
//
//        public void setTemperatureMin(byte temperatureMin) {
//            this.temperatureMin = temperatureMin;
//        }
//
//        public byte getTemperatureMax() {
//            return temperatureMax;
//        }
//
//        public void setTemperatureMax(byte temperatureMax) {
//            this.temperatureMax = temperatureMax;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//
//        public void setDescription(String description) {
//            this.description = description;
//        }
//    }
//
//}
