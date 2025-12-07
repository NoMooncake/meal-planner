/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlannerGui.java
 * Project: meal-planner
 * Description:
 *      Minimal Swing GUI front-end for the meal planner.
 * <p>
 * Author: Yue Wu
 * Date: 2025/11/17
 * Version: 1.3
 * -----------------------------------------------------------------------------
 */
package com.example.mealplanner;

import com.example.mealplanner.strategy.BudgetAwareStrategy;
import com.example.mealplanner.strategy.MealPlanStrategy;
import com.example.mealplanner.strategy.PantryFirstStrategy;
import com.example.mealplanner.strategy.RandomStrategy;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Swing GUI to demo the meal planner with full feature support.
 *
 * @since 1.0
 */
public final class MealPlannerGui {

    // Shared state
    private static RecipeCatalog catalog = RecipeCatalog.samples();
    private static Pantry pantry = new Pantry();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MealPlannerGui::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Meal Planner GUI");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 650);
        frame.setLocationRelativeTo(null);

        // --- Controls panel ---
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        // Row 0: Days
        JTextField daysField = new JTextField("2", 10);
        c.gridx = 0; c.gridy = 0; controls.add(new JLabel("Days:"), c);
        c.gridx = 1; controls.add(daysField, c);

        // Row 1: Meals (checkboxes)
        JCheckBox breakfastCheck = new JCheckBox("Breakfast");
        JCheckBox lunchCheck = new JCheckBox("Lunch", true); 
        JCheckBox dinnerCheck = new JCheckBox("Dinner", true);
        JPanel mealsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        mealsPanel.add(breakfastCheck);
        mealsPanel.add(lunchCheck);
        mealsPanel.add(dinnerCheck);
        c.gridx = 0; c.gridy = 1; controls.add(new JLabel("Meals:"), c);
        c.gridx = 1; c.gridwidth = 2; controls.add(mealsPanel, c);
        c.gridwidth = 1; // reset

        // Row 2: Seed
        JTextField seedField = new JTextField("7", 10);
        c.gridx = 0; c.gridy = 2; controls.add(new JLabel("Seed:"), c);
        c.gridx = 1; controls.add(seedField, c);

        // Row 3: Strategy
        String[] strategies = { "random", "pantry-first", "budget" };
        JComboBox<String> strategyBox = new JComboBox<>(strategies);
        c.gridx = 0; c.gridy = 3; controls.add(new JLabel("Strategy:"), c);
        c.gridx = 1; controls.add(strategyBox, c);

        // Row 4: Budget
        JTextField budgetField = new JTextField("50.0", 10);
        budgetField.setEnabled(false);
        c.gridx = 0; c.gridy = 4; controls.add(new JLabel("Budget ($):"), c);
        c.gridx = 1; controls.add(budgetField, c);

        // Row 5: Inline Pantry
        JTextField pantryField = new JTextField("", 20);
        c.gridx = 0; c.gridy = 5; controls.add(new JLabel("Pantry (inline):"), c);
        c.gridx = 1; controls.add(pantryField, c);
        c.gridx = 2; controls.add(new JLabel("e.g. milk=200:ML,egg=3:PCS"), c);

        // Row 6: Use catalog.json checkbox
        JCheckBox useCatalogCheck = new JCheckBox("Use catalog.json");
        JLabel catalogLabel = new JLabel("(using built-in samples)");
        catalogLabel.setForeground(Color.GRAY);
        c.gridx = 0; c.gridy = 6; c.gridwidth = 1; controls.add(useCatalogCheck, c);
        c.gridx = 1; controls.add(catalogLabel, c);

        // Row 7: Use pantry.json checkbox
        JCheckBox usePantryCheck = new JCheckBox("Use pantry.json");
        JLabel pantryLabel = new JLabel("(no pantry loaded)");
        pantryLabel.setForeground(Color.GRAY);
        c.gridx = 0; c.gridy = 7; controls.add(usePantryCheck, c);
        c.gridx = 1; controls.add(pantryLabel, c);

        // Row 8: Generate button
        JButton generateBtn = new JButton("Generate Shopping List");
        generateBtn.setFont(generateBtn.getFont().deriveFont(Font.BOLD, 14f));
        c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
        c.insets = new Insets(15, 4, 4, 4);
        controls.add(generateBtn, c);

        // Row 9: Export CSV button
        JButton exportCsvBtn = new JButton("Export to list.csv");
        c.gridx = 0; c.gridy = 9; c.gridwidth = 2;
        c.insets = new Insets(4, 4, 4, 4);
        controls.add(exportCsvBtn, c);

        // --- Output area ---
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(outputArea);

        // --- Status bar ---
        JLabel statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(statusBar, BorderLayout.SOUTH);

        // --- Strategy change listener: enable/disable budget field ---
        strategyBox.addActionListener(e -> {
            String selected = (String) strategyBox.getSelectedItem();
            budgetField.setEnabled("budget".equals(selected));
        });

        // --- Catalog checkbox listener ---
        useCatalogCheck.addActionListener(e -> {
            if (useCatalogCheck.isSelected()) {
                try {
                    Path path = Path.of("catalog.json");
                    catalog = com.example.mealplanner.io.RecipeCatalogJson.fromFile(path);
                    catalogLabel.setText("catalog.json (" + catalog.size() + " recipes)");
                    catalogLabel.setForeground(new Color(0, 128, 0));
                    statusBar.setText("Loaded catalog.json");
                } catch (Exception ex) {
                    useCatalogCheck.setSelected(false);
                    JOptionPane.showMessageDialog(frame,
                            "Failed to load catalog.json:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                catalog = RecipeCatalog.samples();
                catalogLabel.setText("(using built-in samples)");
                catalogLabel.setForeground(Color.GRAY);
                statusBar.setText("Using built-in sample recipes");
            }
        });

        // --- Pantry checkbox listener ---
        usePantryCheck.addActionListener(e -> {
            if (usePantryCheck.isSelected()) {
                try {
                    Path path = Path.of("pantry.json");
                    pantry = com.example.mealplanner.io.PantryJson.fromFile(path);
                    pantryLabel.setText("pantry.json (" + pantry.snapshot().size() + " items)");
                    pantryLabel.setForeground(new Color(0, 128, 0));
                    statusBar.setText("Loaded pantry.json");
                } catch (Exception ex) {
                    usePantryCheck.setSelected(false);
                    JOptionPane.showMessageDialog(frame,
                            "Failed to load pantry.json:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                pantry = new Pantry();
                pantryLabel.setText("(no pantry loaded)");
                pantryLabel.setForeground(Color.GRAY);
                statusBar.setText("Pantry cleared");
            }
        });

        // --- Generate button ---
        final ShoppingList[] lastList = { null }; // holder for export
        generateBtn.addActionListener(e -> {
            try {
                int days = Integer.parseInt(daysField.getText().trim());
                if (days <= 0) throw new IllegalArgumentException("Days must be > 0");

                long seed = Long.parseLong(seedField.getText().trim());

                // Build meals array from checkboxes
                List<MealType> mealsList = new ArrayList<>();
                if (breakfastCheck.isSelected()) mealsList.add(MealType.BREAKFAST);
                if (lunchCheck.isSelected()) mealsList.add(MealType.LUNCH);
                if (dinnerCheck.isSelected()) mealsList.add(MealType.DINNER);
                if (mealsList.isEmpty()) {
                    throw new IllegalArgumentException("Please select at least one meal type");
                }
                MealType[] meals = mealsList.toArray(new MealType[0]);

                // Build working pantry: start with loaded, then overlay inline
                Pantry workingPantry = new Pantry();
                // Copy from loaded pantry
                for (var entry : pantry.snapshot().entrySet()) {
                    String key = entry.getKey();
                    double amt = entry.getValue();
                    int sep = key.lastIndexOf('|');
                    String name = key.substring(0, sep);
                    Unit unit = Unit.valueOf(key.substring(sep + 1));
                    workingPantry.add(name, amt, unit);
                }
                // Overlay inline pantry
                String inlineSpec = pantryField.getText().trim();
                if (!inlineSpec.isEmpty()) {
                    AppParseHelpers.parseInlinePantry(inlineSpec, workingPantry);
                }

                // Select strategy
                String strategyName = (String) strategyBox.getSelectedItem();
                MealPlanStrategy strategy;
                switch (strategyName) {
                    case "pantry-first" -> strategy = new PantryFirstStrategy(workingPantry, new Random(seed));
                    case "budget" -> {
                        double budget = Double.parseDouble(budgetField.getText().trim());
                        if (budget <= 0) throw new IllegalArgumentException("Budget must be > 0");
                        strategy = new BudgetAwareStrategy(PriceBook.samples(), budget);
                    }
                    default -> strategy = new RandomStrategy(new Random(seed));
                }

                MealPlannerService service = new MealPlannerService(catalog, strategy);
                MealPlan plan = service.plan(days, meals);
                ShoppingList list = new GroceryService().buildFrom(plan, workingPantry);
                lastList[0] = list;

                // Render output
                StringBuilder sb = new StringBuilder();
                sb.append("=== Meal Plan ===\n");
                for (MealSlot slot : plan.slots()) {
                    sb.append(String.format("Day %d [%s]: %s%n",
                            slot.dayIndex() + 1, slot.type(), slot.recipe().name()));
                }
                sb.append("\n");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                new ShoppingListPrinter().printText(list, ps);
                ps.flush();
                sb.append(baos);

                // Calculate total cost
                PriceBook prices = PriceBook.samples();
                double totalCost = 0.0;
                for (ShoppingListItem item : list.items()) {
                    double unitPrice = prices.unitPrice(item.name(), item.unit());
                    if (!Double.isNaN(unitPrice)) {
                        totalCost += unitPrice * item.totalAmount();
                    }
                }
                sb.append("\n");
                sb.append("========================================\n");
                sb.append(String.format("Estimated Total Cost: $%.2f%n", totalCost));
                sb.append("========================================\n");

                outputArea.setText(sb.toString());
                outputArea.setCaretPosition(0);
                statusBar.setText("Generated plan for " + days + " days, " + meals.length + " meals/day using " + strategyName);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Export CSV button ---
        exportCsvBtn.addActionListener(e -> {
            if (lastList[0] == null) {
                JOptionPane.showMessageDialog(frame, "Generate a shopping list first!",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Path csvPath = Path.of("list.csv");
                PriceBook prices = PriceBook.samples();
                double totalCost = 0.0;

                try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(
                        csvPath, java.nio.charset.StandardCharsets.UTF_8)) {
                    // Header with price columns
                    w.write("name,amount,unit,unit_price,subtotal");
                    w.newLine();

                    for (ShoppingListItem item : lastList[0].items()) {
                        double unitPrice = prices.unitPrice(item.name(), item.unit());
                        double subtotal = 0.0;
                        String unitPriceStr = "";
                        String subtotalStr = "";

                        if (!Double.isNaN(unitPrice)) {
                            subtotal = unitPrice * item.totalAmount();
                            totalCost += subtotal;
                            unitPriceStr = String.format("%.4f", unitPrice);
                            subtotalStr = String.format("%.2f", subtotal);
                        }

                        w.write(item.name());
                        w.write(',');
                        w.write(Double.toString(item.totalAmount()));
                        w.write(',');
                        w.write(item.unit().name());
                        w.write(',');
                        w.write(unitPriceStr);
                        w.write(',');
                        w.write(subtotalStr);
                        w.newLine();
                    }

                    // Total row
                    w.write("TOTAL,,,," + String.format("%.2f", totalCost));
                    w.newLine();
                }

                statusBar.setText("Exported to list.csv (Total: $" + String.format("%.2f", totalCost) + ")");
                JOptionPane.showMessageDialog(frame,
                        "Shopping list saved to:\n" + csvPath.toAbsolutePath() + "\n\nTotal Cost: $" + String.format("%.2f", totalCost),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to export CSV:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    /**
     * Helpers for parsing GUI inputs.
     */
    private static final class AppParseHelpers {
        static void parseInlinePantry(String spec, Pantry target) {
            if (spec == null || spec.isBlank()) return;
            String[] entries = spec.split(",");
            for (String entry : entries) {
                entry = entry.trim();
                if (entry.isEmpty()) continue;
                String[] nv = entry.split("=", 2);
                if (nv.length != 2) throw new IllegalArgumentException("Bad pantry entry: " + entry);
                String name = nv[0].trim();
                String[] au = nv[1].split(":", 2);
                if (au.length != 2) throw new IllegalArgumentException("Bad pantry entry: " + entry);
                double amount = Double.parseDouble(au[0].trim());
                Unit unit = Unit.valueOf(au[1].trim().toUpperCase(Locale.ROOT));
                target.add(name, amount, unit);
            }
        }
    }
}
