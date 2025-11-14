package com.deepthought;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.deepthought.brain.QLearn;

/**
 * Test suite for QLearn class
 * 
 * Tests validate the Q-learning algorithm implementation which updates Q-values
 * based on the formula: Q_new = Q_old + α * (R + γ * Q_max_future - Q_old)
 * Where:
 * - α (alpha) = learning_rate
 * - R = actual_reward
 * - γ (gamma) = discount_factor
 * - Q_max_future = estimated_future_reward
 * 
 * In this implementation, the formula simplifies to:
 * Q_new = Q_old + α * (R + γ * Q_max_future)
 */
public class QLearnTests {

	/**
	 * Purpose: Test QLearn constructor with standard Q-learning parameters
	 * 
	 * Steps:
	 * 1. Create QLearn instance with standard learning rate (0.1) and discount factor (0.9)
	 * 2. Verify instance is created successfully
	 * 3. Validate parameters are stored correctly by testing calculate method
	 */
	@Test
	public void testConstructorWithStandardParameters() {
		// Step 1: Setup - Create QLearn with standard Q-learning parameters
		// Typical Q-learning uses learning_rate around 0.1-0.3 and discount_factor around 0.9-0.99
		double learningRate = 0.1;
		double discountFactor = 0.9;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2 & 3: Verify parameters are set correctly by using calculate method
		// If parameters were wrong, the calculation would be incorrect
		double oldValue = 0.0;
		double actualReward = 1.0;
		double estimatedFutureReward = 0.5;
		double expected = oldValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldValue, actualReward, estimatedFutureReward);
		
		Assert.assertEquals(actual, expected, 0.0001, 
			"QLearn instance should use the provided learning rate and discount factor");
	}

	/**
	 * Purpose: Test QLearn constructor with zero learning rate
	 * 
	 * Steps:
	 * 1. Create QLearn instance with zero learning rate
	 * 2. Verify calculate method returns old value (no learning occurs)
	 * 3. Validate that zero learning rate means no update to Q-value
	 */
	@Test
	public void testConstructorWithZeroLearningRate() {
		// Step 1: Setup - Create QLearn with zero learning rate
		// Zero learning rate means no learning occurs - Q-value stays constant
		double learningRate = 0.0;
		double discountFactor = 0.9;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2 & 3: Verify that with zero learning rate, Q-value doesn't change
		double oldValue = 5.0;
		double actualReward = 10.0;
		double estimatedFutureReward = 15.0;
		double result = qLearn.calculate(oldValue, actualReward, estimatedFutureReward);
		
		// With learning_rate = 0, the formula becomes: oldValue + 0 * (...) = oldValue
		Assert.assertEquals(result, oldValue, 0.0001, 
			"Zero learning rate should result in no change to Q-value");
	}

	/**
	 * Purpose: Test QLearn constructor with zero discount factor
	 * 
	 * Steps:
	 * 1. Create QLearn instance with zero discount factor
	 * 2. Verify future rewards are ignored in calculation
	 * 3. Validate only immediate reward affects Q-value update
	 */
	@Test
	public void testConstructorWithZeroDiscountFactor() {
		// Step 1: Setup - Create QLearn with zero discount factor
		// Zero discount factor means future rewards are not considered
		double learningRate = 0.1;
		double discountFactor = 0.0;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2 & 3: Verify that future reward is ignored
		double oldValue = 2.0;
		double actualReward = 5.0;
		double estimatedFutureReward = 100.0; // Large future reward should be ignored
		double expected = oldValue + learningRate * actualReward;
		double actual = qLearn.calculate(oldValue, actualReward, estimatedFutureReward);
		
		Assert.assertEquals(actual, expected, 0.0001, 
			"Zero discount factor should ignore future rewards in calculation");
	}

	/**
	 * Purpose: Test calculate method with standard Q-learning scenario
	 * 
	 * Steps:
	 * 1. Create QLearn instance with typical parameters
	 * 2. Call calculate with realistic Q-learning values
	 * 3. Verify result matches Q-learning formula calculation
	 * 4. Validate Q-value increases when reward is positive
	 */
	@Test
	public void testCalculateWithStandardQLearningScenario() {
		// Step 1: Setup - Create QLearn with standard parameters
		double learningRate = 0.1;
		double discountFactor = 0.9;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Use realistic Q-learning scenario
		double oldQValue = 0.5;
		double actualReward = 1.0;  // Positive reward
		double estimatedFutureReward = 0.8;
		
		// Step 3: Calculate expected value using Q-learning formula
		// Q_new = Q_old + α * (R + γ * Q_future)
		double expected = oldQValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify calculation is correct
		Assert.assertEquals(actual, expected, 0.0001, 
			"Calculate should implement Q-learning formula correctly");
		Assert.assertTrue(actual > oldQValue, 
			"Q-value should increase when reward is positive");
	}

	/**
	 * Purpose: Test calculate method with negative reward (penalty)
	 * 
	 * Steps:
	 * 1. Create QLearn instance
	 * 2. Call calculate with negative reward
	 * 3. Verify Q-value decreases when penalty is applied
	 * 4. Validate formula handles negative rewards correctly
	 */
	@Test
	public void testCalculateWithNegativeReward() {
		// Step 1: Setup - Create QLearn instance
		double learningRate = 0.2;
		double discountFactor = 0.5;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Use negative reward scenario (penalty)
		// Using values where penalty is significant enough that Q-value decreases
		// even after considering future rewards
		double oldQValue = 3.0;
		double actualReward = -2.0;  // Negative reward (penalty) - large enough to overcome future reward
		double estimatedFutureReward = 1.0;  // Smaller future reward
		
		// Step 3: Calculate expected value
		// Formula: oldValue + learningRate * (actualReward + discountFactor * estimatedFutureReward)
		// = 3.0 + 0.2 * (-2.0 + 0.5 * 1.0)
		// = 3.0 + 0.2 * (-2.0 + 0.5)
		// = 3.0 + 0.2 * (-1.5)
		// = 3.0 - 0.3 = 2.7
		double expected = oldQValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify Q-value decreases with penalty
		Assert.assertEquals(actual, expected, 0.0001, 
			"Calculate should handle negative rewards correctly");
		Assert.assertTrue(actual < oldQValue, 
			"Q-value should decrease when negative reward (penalty) significantly exceeds future reward");
	}

	/**
	 * Purpose: Test calculate method with zero old value (initialization)
	 * 
	 * Steps:
	 * 1. Create QLearn instance
	 * 2. Call calculate with zero old value
	 * 3. Verify calculation handles initialization correctly
	 * 4. Validate Q-value starts learning from zero
	 */
	@Test
	public void testCalculateWithZeroOldValue() {
		// Step 1: Setup - Create QLearn instance
		double learningRate = 0.15;
		double discountFactor = 0.95;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Test initialization scenario (starting from zero)
		double oldQValue = 0.0;
		double actualReward = 2.0;
		double estimatedFutureReward = 1.0;
		
		// Step 3: Calculate expected value
		double expected = oldQValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify initialization works correctly
		Assert.assertEquals(actual, expected, 0.0001, 
			"Calculate should handle zero initial Q-value correctly");
		Assert.assertTrue(actual > 0.0, 
			"Q-value should become positive when initialized with positive reward");
	}

	/**
	 * Purpose: Test calculate method with large values
	 * 
	 * Steps:
	 * 1. Create QLearn instance
	 * 2. Call calculate with large numeric values
	 * 3. Verify formula handles large values correctly
	 * 4. Validate no overflow or precision issues occur
	 */
	@Test
	public void testCalculateWithLargeValues() {
		// Step 1: Setup - Create QLearn instance
		double learningRate = 0.1;
		double discountFactor = 0.9;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Test with large values
		double oldQValue = 1000.0;
		double actualReward = 500.0;
		double estimatedFutureReward = 800.0;
		
		// Step 3: Calculate expected value
		double expected = oldQValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify large values are handled correctly
		Assert.assertEquals(actual, expected, 0.01, 
			"Calculate should handle large values correctly");
	}

	/**
	 * Purpose: Test calculate method with all zero inputs
	 * 
	 * Steps:
	 * 1. Create QLearn instance
	 * 2. Call calculate with all zero parameters
	 * 3. Verify result is zero (edge case)
	 * 4. Validate no division by zero or other errors occur
	 */
	@Test
	public void testCalculateWithAllZeros() {
		// Step 1: Setup - Create QLearn instance
		double learningRate = 0.1;
		double discountFactor = 0.9;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Test edge case with all zeros
		double oldQValue = 0.0;
		double actualReward = 0.0;
		double estimatedFutureReward = 0.0;
		
		// Step 3: Calculate expected value (should be zero)
		double expected = 0.0;
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify all zeros result in zero
		Assert.assertEquals(actual, expected, 0.0001, 
			"Calculate with all zeros should return zero");
	}

	/**
	 * Purpose: Test calculate method with high learning rate
	 * 
	 * Steps:
	 * 1. Create QLearn instance with high learning rate (1.0)
	 * 2. Call calculate method
	 * 3. Verify Q-value updates completely based on reward
	 * 4. Validate high learning rate means aggressive updates
	 */
	@Test
	public void testCalculateWithHighLearningRate() {
		// Step 1: Setup - Create QLearn with maximum learning rate
		// Learning rate of 1.0 means complete trust in new information
		double learningRate = 1.0;
		double discountFactor = 0.9;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Test with high learning rate
		double oldQValue = 0.5;
		double actualReward = 1.0;
		double estimatedFutureReward = 0.8;
		
		// Step 3: Calculate expected value
		// With learning_rate = 1.0, old value is completely replaced by new estimate
		double expected = oldQValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify high learning rate behavior
		Assert.assertEquals(actual, expected, 0.0001, 
			"Calculate should handle high learning rate correctly");
		Assert.assertTrue(actual > oldQValue, 
			"High learning rate should result in significant Q-value change");
	}

	/**
	 * Purpose: Test calculate method with maximum discount factor (future-focused)
	 * 
	 * Steps:
	 * 1. Create QLearn instance with discount factor of 1.0
	 * 2. Call calculate method
	 * 3. Verify future rewards are fully considered
	 * 4. Validate high discount factor emphasizes long-term value
	 */
	@Test
	public void testCalculateWithMaximumDiscountFactor() {
		// Step 1: Setup - Create QLearn with maximum discount factor
		// Discount factor of 1.0 means future rewards are fully valued
		double learningRate = 0.1;
		double discountFactor = 1.0;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Test with maximum discount factor
		double oldQValue = 1.0;
		double actualReward = 0.5;
		double estimatedFutureReward = 10.0;  // Large future reward
		
		// Step 3: Calculate expected value
		// With discount_factor = 1.0, future reward is fully included
		double expected = oldQValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify maximum discount factor behavior
		Assert.assertEquals(actual, expected, 0.0001, 
			"Calculate should handle maximum discount factor correctly");
		Assert.assertTrue(actual > oldQValue, 
			"Maximum discount factor should significantly increase Q-value when future reward is high");
	}

	/**
	 * Purpose: Test calculate method consistency across multiple calls
	 * 
	 * Steps:
	 * 1. Create QLearn instance
	 * 2. Call calculate multiple times with same inputs
	 * 3. Verify results are consistent (deterministic)
	 * 4. Validate QLearn produces reproducible results
	 */
	@Test
	public void testCalculateConsistency() {
		// Step 1: Setup - Create QLearn instance
		double learningRate = 0.2;
		double discountFactor = 0.8;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Use same inputs multiple times
		double oldQValue = 2.0;
		double actualReward = 1.5;
		double estimatedFutureReward = 1.0;
		
		// Step 3: Call calculate multiple times
		double result1 = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		double result2 = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		double result3 = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify all results are identical
		Assert.assertEquals(result1, result2, 0.0001, 
			"Calculate should be deterministic - same inputs produce same output");
		Assert.assertEquals(result2, result3, 0.0001, 
			"Calculate should be consistent across multiple calls");
	}

	/**
	 * Purpose: Test calculate method with negative old value
	 * 
	 * Steps:
	 * 1. Create QLearn instance
	 * 2. Call calculate with negative old Q-value
	 * 3. Verify formula handles negative starting values correctly
	 * 4. Validate Q-value can recover from negative values
	 */
	@Test
	public void testCalculateWithNegativeOldValue() {
		// Step 1: Setup - Create QLearn instance
		double learningRate = 0.1;
		double discountFactor = 0.9;
		QLearn qLearn = new QLearn(learningRate, discountFactor);
		
		// Step 2: Setup - Test with negative old Q-value
		double oldQValue = -2.0;
		double actualReward = 3.0;  // Positive reward
		double estimatedFutureReward = 2.0;
		
		// Step 3: Calculate expected value
		double expected = oldQValue + learningRate * (actualReward + (discountFactor * estimatedFutureReward));
		double actual = qLearn.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify negative values are handled correctly
		Assert.assertEquals(actual, expected, 0.0001, 
			"Calculate should handle negative old Q-values correctly");
		Assert.assertTrue(actual > oldQValue, 
			"Q-value should increase when positive reward is applied to negative value");
	}

	/**
	 * Purpose: Test calculate method with multiple different learning rates
	 * 
	 * Steps:
	 * 1. Create multiple QLearn instances with different learning rates
	 * 2. Call calculate with same parameters on each
	 * 3. Verify higher learning rate produces larger Q-value changes
	 * 4. Validate learning rate affects magnitude of updates
	 */
	@Test
	public void testCalculateWithDifferentLearningRates() {
		// Step 1: Setup - Create QLearn instances with different learning rates
		QLearn lowLearningRate = new QLearn(0.01, 0.9);
		QLearn mediumLearningRate = new QLearn(0.1, 0.9);
		QLearn highLearningRate = new QLearn(0.5, 0.9);
		
		// Step 2: Setup - Use same parameters for all
		double oldQValue = 1.0;
		double actualReward = 2.0;
		double estimatedFutureReward = 1.5;
		
		// Step 3: Calculate with different learning rates
		double resultLow = lowLearningRate.calculate(oldQValue, actualReward, estimatedFutureReward);
		double resultMedium = mediumLearningRate.calculate(oldQValue, actualReward, estimatedFutureReward);
		double resultHigh = highLearningRate.calculate(oldQValue, actualReward, estimatedFutureReward);
		
		// Step 4: Verify learning rate affects update magnitude
		Assert.assertTrue(resultMedium > resultLow, 
			"Higher learning rate should produce larger Q-value increase");
		Assert.assertTrue(resultHigh > resultMedium, 
			"Even higher learning rate should produce even larger Q-value increase");
		
		// Verify all are greater than old value (positive reward)
		Assert.assertTrue(resultLow > oldQValue, 
			"Even low learning rate should increase Q-value with positive reward");
	}
}

