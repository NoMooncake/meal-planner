/**
 * -----------------------------------------------------------------------------
 * File Name: SmokeTest.java
 * Project: meal-planner
 * Description:
 *      Quick smoke test for Maven + JUnit5 setup.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Quick smoke test for Maven + JUnit5 setup. */
public class SmokeTest {
    @Test
    void junitWorks() {
        assertTrue(2 + 2 == 4);
    }
}
