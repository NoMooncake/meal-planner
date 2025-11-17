/**
 * -----------------------------------------------------------------------------
 * File Name: ShoppingListPrinter.java
 * Project: meal-planner
 * Description:
 *      Pretty-printer and exporters for {@link ShoppingList}.
 * <p>
 * Author: Yue Wu
 * Date: 2025/11/17
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */
package com.example.mealplanner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pretty-printer and simple exporters for {@link ShoppingList}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Render a grouped, aligned text view for CLI output.</li>
 *   <li>Export the list as a minimal CSV file for spreadsheets.</li>
 * </ul>
 *
 * @since 1.0
 */
public final class ShoppingListPrinter {

    /**
     * Prints a human-friendly view of the shopping list.
     * <p>
     * Items are grouped by {@link Unit} and sorted by name within each group.
     *
     * @param list the shopping list to print
     * @param out  destination stream (e.g. {@code System.out})
     */
    public void printText(ShoppingList list, PrintStream out) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(out, "out");

        Map<Unit, List<ShoppingListItem>> grouped = list.items().stream()
                .sorted(Comparator.comparing(ShoppingListItem::name)
                        .thenComparing(i -> i.unit().name()))
                .collect(Collectors.groupingBy(
                        ShoppingListItem::unit,
                        LinkedHashMap::new,
                        Collectors.toList()));

        out.println("== Shopping List ==");
        if (grouped.isEmpty()) {
            out.println("(nothing to buy 🎉)");
            return;
        }

        for (Map.Entry<Unit, List<ShoppingListItem>> entry : grouped.entrySet()) {
            Unit unit = entry.getKey();
            List<ShoppingListItem> items = entry.getValue();

            out.println();
            out.println("[" + unit + "]");
            for (ShoppingListItem i : items) {
                // 左对齐名称，右对齐数量
                out.printf("  %-18s %8.1f%n", i.name(), i.totalAmount());
            }
        }
    }

    /**
     * Writes the shopping list as a very simple CSV file:
     * <pre>
     * name,amount,unit
     * milk,200.0,ML
     * egg,2.0,PCS
     * </pre>
     *
     * @param list the list to export
     * @param path target CSV path
     * @throws IOException if writing fails
     */
    public void writeCsv(ShoppingList list, Path path) throws IOException {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(path, "path");

        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write("name,amount,unit");
            w.newLine();
            for (ShoppingListItem i : list.items()) {
                w.write(i.name());
                w.write(',');
                w.write(Double.toString(i.totalAmount()));
                w.write(',');
                w.write(i.unit().name());
                w.newLine();
            }
        }
    }
}
