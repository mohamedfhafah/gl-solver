package com.example.planningweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public final class PreferencesLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PreferencesLoader() {
    }

    public static PreferenceDataset loadDefaultDataset() {
        try {
            var resource = new ClassPathResource("preferences.json");
            return MAPPER.readValue(resource.getInputStream(), PreferenceDataset.class);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger preferences.json", e);
        }
    }
}
