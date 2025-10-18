package com.example.planning;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public final class PreferencesLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PreferencesLoader() {
    }

    public static PreferenceDataset loadDefaultDataset() {
        try (InputStream in = PreferencesLoader.class.getResourceAsStream("/preferences.json")) {
            if (in == null) {
                throw new IllegalStateException("preferences.json introuvable dans les ressources");
            }
            return MAPPER.readValue(in, PreferenceDataset.class);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger preferences.json", e);
        }
    }
}
