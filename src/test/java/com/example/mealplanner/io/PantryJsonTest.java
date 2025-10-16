/**
 * -----------------------------------------------------------------------------
 * File Name: PantryJsonTest.java
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PantryJson load/save round-trip and basic error handling.
 */
class PantryJsonTest {

    /** Helper: compare two snapshot maps with a small tolerance for doubles. */
    private static void assertSnapshotEquals(Map<String, Double> a, Map<String, Double> b) {
        assertEquals(a.size(), b.size(), "snapshot sizes differ");
        a.forEach((k, v) -> {
            assertTrue(b.containsKey(k), "missing key: " + k);
            assertEquals(v, b.get(k), 1e-9, "different value for key: " + k);
        });
    }

    @Test
    void roundTrip_preservesEntries(@TempDir Path tmp) throws IOException {
        // given
        Pantry p = new Pantry()
                .add("milk", 200, Unit.ML)
                .add("egg", 2, Unit.PCS)
                .add("rice", 500, Unit.G);
        Path file = tmp.resolve("pantry.json");

        // when: save → load
        PantryJson.toFile(p, file);
        Pantry loaded = PantryJson.fromFile(file);

        // then
        assertSnapshotEquals(p.snapshot(), loaded.snapshot());
    }

    @Test
    void load_handlesCaseInsensitivityAndNormalization(@TempDir Path tmp) throws IOException {
        String json = """
        {
          "stock": [
            { "name": "  Milk  ", "amount": 200, "unit": "ml" },
            { "name": "EGG",     "amount":  2,  "unit": "pcs" }
          ]
        }
        """;
        Path file = tmp.resolve("pantry.json");
        Files.writeString(file, json);

        Pantry p = PantryJson.fromFile(file);

        assertEquals(200.0, p.snapshot().get("milk|ML"), 1e-9);
        assertEquals(2.0,   p.snapshot().get("egg|PCS"), 1e-9);
    }

    @Test
    void load_missingFile_throwsIOException(@TempDir Path tmp) {
        Path missing = tmp.resolve("no_such.json");
        assertThrows(IOException.class, () -> PantryJson.fromFile(missing));
    }

    @Test
    void load_badUnit_throwsIllegalArgumentException(@TempDir Path tmp) throws IOException {
        String json = """
        { "stock": [ { "name": "water", "amount": 100, "unit": "LITER" } ] }
        """;
        Path file = tmp.resolve("bad.json");
        Files.writeString(file, json);

        assertThrows(IllegalArgumentException.class, () -> PantryJson.fromFile(file));
    }

    @Test
    void save_outputsExpectedShape(@TempDir Path tmp) throws IOException {
        Pantry p = new Pantry().add("olive oil", 50, Unit.ML);
        Path file = tmp.resolve("out.json");

        PantryJson.toFile(p, file);
        String out = Files.readString(file);


        assertTrue(out.contains("\"stock\""));
        assertTrue(out.toLowerCase().contains("olive oil"));
        assertTrue(out.contains("\"unit\""));
        assertTrue(out.contains("\"ML\""));
        assertTrue(out.contains("50"));
    }
}