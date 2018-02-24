package weather.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class WeatherGatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherGatherService.class);

    private List<WeatherDataProvider> weatherDataProviders;
    private EntityManager entityManager;
    @Value("${app.weather.cities}")
    private List<String> cities;

    @Autowired
    public WeatherGatherService(List<WeatherDataProvider> weatherDataProviders, EntityManager entityManager) {
        this.weatherDataProviders = weatherDataProviders;
        this.entityManager = entityManager;
    }

    @Scheduled(fixedRateString = "PT1M")
    @Transactional()
    public void gatherWeatherData() {
        logger.info("Initiating gathering weather data");
        cities.forEach(city ->
            weatherDataProviders.forEach(wdp -> {
                logger.info("Gathering weather data for {} using {}", city, wdp.getClass().getName());
                wdp.getCurrentWeather(city).thenAccept(entityManager::persist);
                wdp.getForecast(city).thenAccept(list -> list.forEach(entityManager::persist));
            })
        );
    }

}