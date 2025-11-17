/**
 * -----------------------------------------------------------------------------
 * File Name: App.java
 * Project: meal-planner
 * Description:
 *      Main application class for the meal planner.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.1
 * -----------------------------------------------------------------------------
 */

package com.example.mealplanner;

import com.example.mealplanner.strategy.BudgetAwareStrategy;
import com.example.mealplanner.strategy.MealPlanStrategy;
import com.example.mealplanner.strategy.PantryFirstStrategy;
import com.example.mealplanner.strategy.RandomStrategy;

import java.util.*;
import java.util.Comparator;
import java.util.Locale;
import java.io.IOException;


/**
 * Command-line entry point for the Meal Planner prototype.
 *
 * <p><strong>Service:</strong> plan meals → aggregate ingredients → subtract pantry → print the shopping list.</p>
 *
 * <p><strong>CLI flags</strong> (see also {@link #printHelp()}):</p>
 * <ul>
 *   <li>{@code --days N} — number of days (default: 2)</li>
 *   <li>{@code --meals csv} — comma-separated meal types; allowed: breakfast,lunch,dinner (default: lunch,dinner)</li>
 *   <li>{@code --seed N} — random seed for reproducible plans (default: 7)</li>
 *   <li>{@code --pantry spec} — existing stock, e.g. {@code "milk=200:ML,egg=1:PCS"}</li>
 *   <li>{@code --pantry-file path} — load pantry JSON from file</li>
 *   <li>{@code --save-pantry path} — save pantry JSON after planning</li>
 *   <li>{@code --catalog-file path} — load recipe catalog JSON from file</li>
 *   <li>{@code --strategy kind} — {@code random | pantry-first | budget} (default: {@code random})</li>
 *   <li>{@code --budget amount} — total budget for {@code budget} strategy</li>
 * </ul>
 *
 * @since 1.0
 */
public class App {

    /**
     * Available planning strategies for the CLI.
     */
    private enum StrategyKind {
        RANDOM,
        PANTRY_FIRST,
        BUDGET
    }

    /**
     * Runs the CLI application. Parses arguments, builds the plan with the configured strategy,
     * aggregates the shopping list (with optional pantry subtraction), and prints the result.
     *
     * @param args command-line arguments; see {@link #printHelp()} for supported options
     * @throws IllegalArgumentException if required option values are missing/invalid
     */
    public static void main(String[] args) {
        try {
            Config cfg = parseArgs(args);

            // -----------------------------------------------------------------
            // Load / overlay pantry (JSON file + inline spec)
            // -----------------------------------------------------------------
            /*
             * Loading order:
             *   1) If --pantry-file is provided, load Pantry from JSON via PantryJson.fromFile.
             *   2) If --pantry is also provided, overlay those entries on top of the loaded pantry
             *      (i.e., add/override amounts for the same identity).
             * Saving:
             *   If --save-pantry is provided, serialize the in-memory pantry back to the given path.
             */
            final Pantry pantry;
            if (cfg.pantryFileIn != null) {
                try {
                    pantry = com.example.mealplanner.io.PantryJson.fromFile(
                            java.nio.file.Path.of(cfg.pantryFileIn));
                } catch (Exception ioe) {
                    throw new IllegalArgumentException("Failed to read pantry file: "
                            + cfg.pantryFileIn + " (" + ioe.getMessage() + ")");
                }
            } else {
                pantry = new Pantry();
            }

            // overlay inline --pantry spec, if any
            if (cfg.pantry != null) {
                for (var e : cfg.pantry.snapshot().entrySet()) {
                    String k = e.getKey();
                    double v = e.getValue();
                    int sep = k.lastIndexOf('|');
                    String n = k.substring(0, sep);
                    Unit u = Unit.valueOf(k.substring(sep + 1));
                    pantry.add(n, v, u);
                }
            }

            // -----------------------------------------------------------------
            // Load catalog (JSON or in-memory samples)
            // -----------------------------------------------------------------
            final RecipeCatalog catalog;
            if (cfg.catalogFileIn != null) {
                try {
                    catalog = com.example.mealplanner.io.RecipeCatalogJson.fromFile(
                            java.nio.file.Path.of(cfg.catalogFileIn));
                } catch (Exception ioe) {
                    throw new IllegalArgumentException("Failed to read catalog file: "
                            + cfg.catalogFileIn + " (" + ioe.getMessage() + ")");
                }
            } else {
                catalog = RecipeCatalog.samples();
            }

            // -----------------------------------------------------------------
            // Choose strategy: random / pantry-first / budget
            // -----------------------------------------------------------------
            MealPlanStrategy strategy;
            Random rng = new Random(cfg.seed);

            switch (cfg.strategyKind) {
                case PANTRY_FIRST -> strategy = new PantryFirstStrategy(pantry, rng);
                case BUDGET -> {
                    if (cfg.budget == null) {
                        throw new IllegalArgumentException(
                                "--budget is required when --strategy budget is used");
                    }
                    PriceBook prices = PriceBook.samples();
                    strategy = new BudgetAwareStrategy(prices, cfg.budget);
                }
                case RANDOM -> strategy = new RandomStrategy(rng);
                default -> throw new IllegalStateException("Unknown strategy: " + cfg.strategyKind);
            }


            var service = new MealPlannerService(catalog, strategy);

            // Build plan
            MealPlan plan = service.plan(cfg.days, cfg.meals);

            // Build shopping list with pantry subtraction
            ShoppingList list = new GroceryService().buildFrom(plan, pantry);

            // Pretty print & optional CSV export
            ShoppingListPrinter printer = new ShoppingListPrinter();
            printer.printText(list, System.out);

            if (cfg.csvOut != null) {
                try {
                    java.nio.file.Path csvPath = java.nio.file.Path.of(cfg.csvOut);
                    printer.writeCsv(list, csvPath);
                    System.out.println();
                    System.out.println("[saved CSV to " + csvPath.toAbsolutePath() + "]");
                } catch (IOException ioe) {
                    throw new IllegalArgumentException(
                            "Failed to write CSV file: " + cfg.csvOut + " (" + ioe.getMessage() + ")");
                }
            }

            // Optionally save pantry snapshot
            if (cfg.pantryFileOut != null) {
                try {
                    com.example.mealplanner.io.PantryJson.toFile(
                            pantry, java.nio.file.Path.of(cfg.pantryFileOut));
                } catch (Exception ioe) {
                    throw new IllegalArgumentException("Failed to save pantry file: "
                            + cfg.pantryFileOut + " (" + ioe.getMessage() + ")");
                }
            }

        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
            printHelp();
            System.exit(2);
        }
    }

    // ---- parsing ----

    /**
     * Immutable config holder produced by {@link #parseArgs(String[])}.
     */
    private record Config(int days,
                          MealType[] meals,
                          long seed,
                          Pantry pantry,
                          String pantryFileIn,
                          String pantryFileOut,
                          String catalogFileIn,
                          StrategyKind strategyKind,
                          Double budget,
                          String csvOut) {}

    /**
     * Parses CLI arguments into a {@link Config}.
     * <p>Defaults: days=2, meals=lunch,dinner, seed=7, strategy=random, no budget.</p>
     *
     * @param args raw command-line args (may be {@code null})
     * @return populated {@link Config}
     * @throws IllegalArgumentException if an option is unknown, missing its value, or has an invalid value
     */
    private static Config parseArgs(String[] args) {
        if (args == null) args = new String[0];
        if (Arrays.asList(args).contains("-h") || Arrays.asList(args).contains("--help")) {
            printHelp();
            System.exit(0);
        }

        int days = 2;
        MealType[] meals = new MealType[]{ MealType.LUNCH, MealType.DINNER };
        long seed = 7L;
        Pantry pantry = null;
        String pantryFileIn = null;
        String pantryFileOut = null;
        String catalogFileIn = null;
        StrategyKind strategyKind = StrategyKind.RANDOM;
        Double budget = null;
        String csvOut = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--days" -> {
                    ensureValue(args, i, a);
                    days = Integer.parseInt(args[++i]);
                    if (days <= 0) throw new IllegalArgumentException("--days must be > 0");
                }
                case "--meals" -> {
                    ensureValue(args, i, a);
                    meals = parseMeals(args[++i]);
                }
                case "--seed" -> {
                    ensureValue(args, i, a);
                    seed = Long.parseLong(args[++i]);
                }
                case "--pantry" -> {
                    ensureValue(args, i, a);
                    pantry = parsePantry(args[++i]);
                }
                case "--pantry-file" -> {
                    ensureValue(args, i, a);
                    pantryFileIn = args[++i];
                }
                case "--save-pantry" -> {
                    ensureValue(args, i, a);
                    pantryFileOut = args[++i];
                }
                case "--catalog-file" -> {
                    ensureValue(args, i, a);
                    catalogFileIn = args[++i];
                }
                case "--strategy" -> {
                    ensureValue(args, i, a);
                    String s = args[++i].toLowerCase(Locale.ROOT);
                    switch (s) {
                        case "random" -> strategyKind = StrategyKind.RANDOM;
                        case "pantry-first", "pantry_first", "pantryfirst" ->
                                strategyKind = StrategyKind.PANTRY_FIRST;
                        case "budget" -> strategyKind = StrategyKind.BUDGET;
                        default -> throw new IllegalArgumentException(
                                "Unknown strategy: " + s + " (use random|pantry-first|budget)");
                    }
                }
                case "--csv-out" -> {
                    ensureValue(args, i, a);
                    csvOut = args[++i];
                }
                case "--budget" -> {
                    ensureValue(args, i, a);
                    double b = Double.parseDouble(args[++i]);
                    if (b <= 0) throw new IllegalArgumentException("--budget must be > 0");
                    budget = b;
                }
                default -> throw new IllegalArgumentException("Unknown option: " + a);
            }
        }
        return new Config(days, meals, seed, pantry, pantryFileIn, pantryFileOut,
                catalogFileIn, strategyKind, budget, csvOut);
    }

    /**
     * Ensures an option {@code opt} has a following value in {@code args}.
     *
     * @param args argv array
     * @param i    current index of the option
     * @param opt  option name for error message
     * @throws IllegalArgumentException if the next value is missing
     */
    private static void ensureValue(String[] args, int i, String opt) {
        if (i + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + opt);
    }

    /**
     * Parses a comma-separated list of meal types into an array of {@link MealType}.
     * Accepts case-insensitive tokens: {@code breakfast}, {@code lunch}, {@code dinner}.
     *
     * @param csv e.g. {@code "breakfast,lunch,dinner"}
     * @return ordered {@link MealType} array
     * @throws IllegalArgumentException if empty/blank or contains an unknown meal type
     */
    private static MealType[] parseMeals(String csv) {
        if (csv == null || csv.isBlank()) throw new IllegalArgumentException("--meals must not be empty");
        String[] parts = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (parts.length == 0) throw new IllegalArgumentException("--meals must not be empty");

        MealType[] result = new MealType[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].toUpperCase(Locale.ROOT);
            try {
                result[i] = MealType.valueOf(p);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown meal type: " + parts[i]
                        + " (use breakfast,lunch,dinner)");
            }
        }
        return result;
    }

    /**
     * Parses a pantry specification string into a {@link Pantry}.
     * <p>Format: comma-separated entries of {@code name=amount:UNIT}, where UNIT ∈ {PCS,G,ML}.</p>
     * <p>Example: {@code "milk=200:ML,egg=1:PCS"}</p>
     *
     * @param spec pantry spec string; may be blank/empty (treated as no stock)
     * @return a {@link Pantry} populated with the parsed entries
     * @throws IllegalArgumentException if any entry has a bad shape, amount, or unit
     */
    private static Pantry parsePantry(String spec) {
        Pantry pantry = new Pantry();
        if (spec == null || spec.isBlank()) return pantry;

        String[] entries = Arrays.stream(spec.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

        for (String e : entries) {
            String[] nv = e.split("=", 2);
            if (nv.length != 2) throw new IllegalArgumentException("Bad pantry entry: " + e);
            String name = nv[0].trim();
            String[] au = nv[1].split(":", 2);
            if (au.length != 2) throw new IllegalArgumentException("Bad pantry entry: " + e);

            double amount;
            try { amount = Double.parseDouble(au[0].trim()); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Bad amount: " + au[0]);
            }

            Unit unit;
            try { unit = Unit.valueOf(au[1].trim().toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Bad unit: " + au[1] + " (use PCS|G|ML)");
            }

            pantry.add(name, amount, unit);
        }
        return pantry;
    }

    /** Prints CLI usage and examples. Safe to call at any time. */
    private static void printHelp() {
        System.out.println("""
                Meal Planner CLI

                Usage:
                  java -cp target/meal-planner-0.1.0.jar com.example.mealplanner.App [options]

                Options:
                  --days N              Number of days (default: 2)
                  --meals csv           Comma-separated meals (default: lunch,dinner)
                                        Allowed: breakfast,lunch,dinner
                  --seed N              Random seed for reproducible plans (default: 7)

                  --pantry spec         Existing stock, comma-separated entries:
                                        name=amount:UNIT   (UNIT = PCS|G|ML)
                                        e.g. --pantry "milk=200:ML,egg=1:PCS"
                  --pantry-file path    Load pantry JSON from file
                  --save-pantry path    Save current pantry JSON to file

                  --catalog-file path   Load recipe catalog JSON from file

                  --strategy kind       Planning strategy:
                                          random        (default)
                                          pantry-first  (prefer recipes using pantry items)
                                          budget        (respect total --budget)
                  --budget amount       Total budget used by 'budget' strategy
                  --csv-out path         Also export shopping list as CSV to the given path
                

                  -h, --help            Show this help

                Examples:
                  # 3 days, breakfast & dinner, fixed seed
                  mvn -q exec:java -Dexec.args="--days 3 --meals breakfast,dinner --seed 42"

                  # Pantry-first strategy with inline pantry
                  mvn -q exec:java -Dexec.args="--days 2 --meals lunch --strategy pantry-first --pantry milk=200:ML,egg=1:PCS"

                  # Budget-aware strategy with total budget 50.0
                  mvn -q exec:java -Dexec.args="--days 3 --meals lunch,dinner --strategy budget --budget 50.0"
                """);
    }
}
