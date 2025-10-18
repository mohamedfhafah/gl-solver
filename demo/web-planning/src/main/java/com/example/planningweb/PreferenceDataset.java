package com.example.planningweb;

import java.util.List;
import java.util.Map;

public record PreferenceDataset(List<String> activities, List<PreferenceEntry> friends) {
    public record PreferenceEntry(String name, Map<String, Integer> preferences) {
    }
}
