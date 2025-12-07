/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlannerGui.java
 * Project: meal-planner
 * Description:
 *      Minimal Swing GUI front-end for the meal planner.
 * <p>
 * Author: Yue Wu
 * Date: 2025/11/17
 * Version: 2.0
 * -----------------------------------------------------------------------------
 */
package com.example.mealplanner;

import com.example.mealplanner.strategy.MealPlanStrategy;
import com.example.mealplanner.strategy.RandomStrategy;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Random;

/**
 * Minimal Swing GUI to demo the meal planner without CLI.
 *
 * <p>This is intentionally small and focused:
 * it wires a few text fields to {@link MealPlannerService} and
 * renders the {@link ShoppingList} using {@link ShoppingListPrinter}.</p>
 *
 * @since 1.0
 */
public final class MealPlannerGui {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MealPlannerGui::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Meal Planner GUI");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);

        // --- Controls panel ---
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField daysField = new JTextField("2");
        JTextField mealsField = new JTextField("lunch,dinner");

        String[] strategies = { "random" /* 先只接 random，后面你可以再接 pantry-first/budget */ };
        JComboBox<String> strategyBox = new JComboBox<>(strategies);

        JTextField budgetField = new JTextField("20.0"); // 对 random 暂时忽略
        budgetField.setEnabled(false); // 暂时禁用，等你接好 budget strategy 再打开

        c.gridx = 0; c.gridy = 0; controls.add(new JLabel("Days:"), c);
        c.gridx = 1; controls.add(daysField, c);

        c.gridx = 0; c.gridy = 1; controls.add(new JLabel("Meals (csv):"), c);
        c.gridx = 1; controls.add(mealsField, c);

        c.gridx = 0; c.gridy = 2; controls.add(new JLabel("Strategy:"), c);
        c.gridx = 1; controls.add(strategyBox, c);

        c.gridx = 0; c.gridy = 3; controls.add(new JLabel("Budget ($):"), c);
        c.gridx = 1; controls.add(budgetField, c);

        JButton generateBtn = new JButton("Generate Shopping List");
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        controls.add(generateBtn, c);

        // --- Output area ---
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(outputArea);

        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);

        // --- Wire button ---
        generateBtn.addActionListener(e -> {
            try {
                int days = Integer.parseInt(daysField.getText().trim());
                MealType[] meals = AppParseHelpers.parseMealsForGui(mealsField.getText());

                // 先直接用 RandomStrategy + samples catalog + 空 pantry
                MealPlanStrategy strategy = new RandomStrategy(new Random(7L));
                MealPlannerService service = new MealPlannerService(
                        RecipeCatalog.samples(), strategy);

                MealPlan plan = service.plan(days, meals);
                ShoppingList list = new GroceryService().buildFrom(plan, new Pantry());

                // 用同一个 Printer 渲染到 String，然后丢进文本框
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                new ShoppingListPrinter().printText(list, ps);
                ps.flush();

                outputArea.setText(baos.toString());
                outputArea.setCaretPosition(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    /**
     * Tiny helper using the same rules as App.parseMeals, but without exiting the JVM.
     */
    private static final class AppParseHelpers {
        static MealType[] parseMealsForGui(String csv) {
            if (csv == null || csv.isBlank()) {
                return new MealType[]{ MealType.LUNCH, MealType.DINNER };
            }
            String[] parts = csv.split(",");
            MealType[] result = new MealType[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String p = parts[i].trim().toUpperCase(Locale.ROOT);
                result[i] = MealType.valueOf(p);
            }
            return result;
        }
    }
}
