/* ==========================================================
   calc.js — pure calculation functions (no DOM access).
   Kept separate so the same logic can be reused in a future
   mobile app.
   ========================================================== */
'use strict';

const ACTIVITY_LEVELS = {
  sedentary: { label: 'Sedentary (little or no exercise)', factor: 1.2 },
  light: { label: 'Light (1-3 days a week)', factor: 1.375 },
  moderate: { label: 'Moderate (3-5 days a week)', factor: 1.55 },
  active: { label: 'Active (6-7 days a week)', factor: 1.725 }
};

const GOALS = {
  lose: { label: 'Lose weight', delta: -500 },
  maintain: { label: 'Maintain weight', delta: 0 },
  gain: { label: 'Gain weight', delta: 400 }
};

/** Basal metabolic rate, Mifflin-St Jeor equation. */
function calculateBMR({ weight, height, age, gender }) {
  const base = 10 * weight + 6.25 * height - 5 * age;
  return gender === 'female' ? base - 161 : base + 5;
}

/** Total daily energy expenditure = BMR x activity factor. */
function calculateTDEE(bmr, activity) {
  const level = ACTIVITY_LEVELS[activity] || ACTIVITY_LEVELS.light;
  return bmr * level.factor;
}

/** Daily calorie goal for the chosen goal, never below a safe minimum. */
function calculateGoalCalories(tdee, goal) {
  const delta = (GOALS[goal] || GOALS.maintain).delta;
  return Math.max(1200, Math.round((tdee + delta) / 10) * 10);
}

/** One-step helper: body data in, calorie goal out. */
function buildGoals(profile) {
  const bmr = calculateBMR(profile);
  const tdee = calculateTDEE(bmr, profile.activity);
  return { bmr: Math.round(bmr), tdee: Math.round(tdee), calorieGoal: calculateGoalCalories(tdee, profile.goal) };
}

/** Daily macro targets in grams: 25% protein, 50% carbs, 25% fat. */
function macroTargets(calories) {
  return {
    protein: Math.round((calories * 0.25) / 4),
    carbs: Math.round((calories * 0.5) / 4),
    fat: Math.round((calories * 0.25) / 9)
  };
}

function calculateBMI(weight, height) {
  const m = height / 100;
  return weight / (m * m);
}

function bmiCategory(bmi) {
  if (bmi < 18.5) return 'Underweight';
  if (bmi < 25) return 'Healthy weight';
  if (bmi < 30) return 'Overweight';
  return 'Obese';
}

/** Rough calories burned by walking the given number of steps. */
function stepsToCalories(steps, weight) {
  return steps * weight * 0.00057;
}

/** Distance in km; stride length is estimated from height. */
function stepsToKm(steps, height) {
  const strideCm = height * 0.415;
  return (steps * strideCm) / 100000;
}

/** Calories burned in a workout: MET x 3.5 x weight / 200 x minutes. */
function exerciseCalories(met, weight, minutes) {
  return Math.round((met * 3.5 * weight / 200) * minutes);
}

/** Percentage helper that never returns NaN or more than 100. */
function percentOf(value, total) {
  if (!total) return 0;
  return Math.max(0, Math.min(100, (value / total) * 100));
}
