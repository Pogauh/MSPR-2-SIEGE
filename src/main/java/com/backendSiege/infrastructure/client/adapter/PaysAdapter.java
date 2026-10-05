package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.Tolerances;
import com.backendSiege.domain.ports.PaysPort;
import com.backendSiege.infrastructure.config.PaysProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/** Les pays configurés dans {@code app.country-adapters.mapping}. */
@Component
public class PaysAdapter implements PaysPort {

    private final PaysProperties properties;

    public PaysAdapter(PaysProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<Pays> findAll() {
        return properties.mapping().entrySet().stream()
                .map(entry -> new Pays(entry.getKey(), entry.getValue().url(), tolerances(entry.getValue().tolerences())))
                .toList();
    }

    private static Tolerances tolerances(PaysProperties.Tolerances tolerences) {
        if (tolerences == null) {
            return null;
        }
        return new Tolerances(tolerences.temperatureIdeale(), tolerences.humiditeIdeale(),
                tolerences.toleranceTemperature(), tolerences.toleranceHumidite());
    }
}
