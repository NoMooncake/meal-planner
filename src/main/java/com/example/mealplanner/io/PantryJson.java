/**
 * -----------------------------------------------------------------------------
 * File Name: PantryJson.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/15
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.io;

import com.example.mealplanner.Pantry;
import com.example.mealplanner.Unit;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

/**
 * JSON load/save utilities for {@link Pantry}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Load a {@link Pantry} from a JSON file matching {@link PantryDto} schema.</li>
 *   <li>Save a {@link Pantry} snapshot to a JSON file (pretty-printed).</li>
 * </ul>
 *
 * <p>Design notes:</p>
 * <ul>
 *   <li>During load, unit tokens are normalized via {@link Unit#valueOf(String)} using upper-case.</li>
 *   <li>Name normalization is handled by the domain model ({@link Pantry#add(String, double, Unit)}).</li>
 *   <li>Domain validation errors are surfaced as {@link IllegalArgumentException} from domain calls.</li>
 * </ul>
 *
 * @since 1.0
 */
public final class PantryJson {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PantryJson() { /* no instances */ }

    /**
     * Loads a {@link Pantry} from a JSON file.
     *
     * @param path path to a file that matches the {@link PantryDto} schema
     * @return populated {@link Pantry}; never {@code null}
     * @throws IOException              if I/O or parsing fails
     * @throws IllegalArgumentException if a DTO entry is semantically invalid for the domain
     */
    public static Pantry fromFile(Path path) throws IOException {
        var dto = MAPPER.readValue(path.toFile(), PantryDto.class);
        Pantry p = new Pantry();
        if (dto != null && dto.stock != null) {
            for (var e : dto.stock) {
                if (e == null) continue;
                // Normalize unit token; domain will normalize name & validate amount
                p.add(e.name, e.amount, Unit.valueOf(e.unit.toUpperCase()));
            }
        }
        return p;
    }

    /**
     * Saves the provided {@link Pantry} to a JSON file using {@link Pantry#snapshot()}.
     *
     * <p>Key interpretation: snapshot keys are "normalizedName|UNIT"; this method splits them
     * to recover the two fields for the JSON representation.</p>
     *
     * @param p    non-null pantry to serialize
     * @param path output path to write (will be overwritten)
     * @throws IOException          if writing fails
     * @throws NullPointerException if {@code p} or {@code path} is {@code null}
     */
    public static void toFile(Pantry p, Path path) throws IOException {
        PantryDto dto = new PantryDto();
        dto.stock = p.snapshot().entrySet().stream().map(e -> {
            PantryDto.StockEntry se = new PantryDto.StockEntry();
            String key = e.getKey(); // normalizedName|UNIT
            int idx = key.lastIndexOf('|');
            se.name = key.substring(0, idx);
            se.unit = key.substring(idx + 1);
            se.amount = e.getValue();
            return se;
        }).toList();
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), dto);
    }
}
