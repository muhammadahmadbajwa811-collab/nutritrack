/* ==========================================================
   data.js — static data: food database, meals, categories
   Nutrition values are approximate averages per serving.
   Row format: [id, name, category, emoji, serving, kcal, protein, carbs, fat]
   ========================================================== */
'use strict';

const FOOD_ROWS = [
  // Fruits
  [1, 'Apple', 'Fruits', '🍎', '1 medium', 95, 0.5, 25, 0.3],
  [2, 'Banana', 'Fruits', '🍌', '1 medium', 105, 1.3, 27, 0.4],
  [3, 'Orange', 'Fruits', '🍊', '1 medium', 62, 1.2, 15, 0.2],
  [4, 'Mango', 'Fruits', '🥭', '1 cup sliced', 99, 1.4, 25, 0.6],
  [5, 'Watermelon', 'Fruits', '🍉', '1 cup', 46, 0.9, 11.5, 0.2],
  [6, 'Grapes', 'Fruits', '🍇', '1 cup', 104, 1.1, 27, 0.2],
  [7, 'Strawberries', 'Fruits', '🍓', '1 cup', 49, 1, 12, 0.5],
  [8, 'Dates', 'Fruits', '🌴', '3 dates', 70, 0.6, 18, 0.1],
  // Vegetables
  [9, 'Broccoli', 'Vegetables', '🥦', '1 cup', 55, 3.7, 11, 0.6],
  [10, 'Carrot', 'Vegetables', '🥕', '1 medium', 25, 0.6, 6, 0.1],
  [11, 'Cucumber', 'Vegetables', '🥒', '1 cup sliced', 16, 0.7, 3.8, 0.1],
  [12, 'Tomato', 'Vegetables', '🍅', '1 medium', 22, 1.1, 4.8, 0.2],
  [13, 'Spinach (cooked)', 'Vegetables', '🥬', '1 cup', 41, 5.3, 6.8, 0.5],
  [14, 'Boiled potato', 'Vegetables', '🥔', '1 medium', 130, 3, 30, 0.2],
  [15, 'Sweet corn', 'Vegetables', '🌽', '1 cob', 90, 3.3, 19, 1.4],
  // Grains
  [16, 'White rice (cooked)', 'Grains', '🍚', '1 cup', 205, 4.3, 45, 0.4],
  [17, 'Brown rice (cooked)', 'Grains', '🍚', '1 cup', 216, 5, 45, 1.8],
  [18, 'Roti / Chapati', 'Grains', '🫓', '1 piece', 104, 3, 18, 3],
  [19, 'Naan', 'Grains', '🫓', '1 piece', 260, 9, 45, 5],
  [20, 'Oatmeal (cooked)', 'Grains', '🥣', '1 cup', 158, 6, 27, 3.2],
  [21, 'Whole wheat bread', 'Grains', '🍞', '1 slice', 80, 4, 14, 1],
  [22, 'Pasta (cooked)', 'Grains', '🍝', '1 cup', 220, 8, 43, 1.3],
  [23, 'Cornflakes', 'Grains', '🥣', '1 cup', 100, 2, 24, 0.2],
  // Protein
  [24, 'Boiled egg', 'Protein', '🥚', '1 large', 78, 6.3, 0.6, 5.3],
  [25, 'Chicken breast (grilled)', 'Protein', '🍗', '100 g', 165, 31, 0, 3.6],
  [26, 'Beef steak (grilled)', 'Protein', '🥩', '100 g', 250, 26, 0, 15],
  [27, 'Salmon', 'Protein', '🐟', '100 g', 208, 20, 0, 13],
  [28, 'Tuna (canned)', 'Protein', '🐟', '100 g', 116, 26, 0, 1],
  [29, 'Lentils / Daal (cooked)', 'Protein', '🫘', '1 cup', 230, 18, 40, 0.8],
  [30, 'Chickpeas (cooked)', 'Protein', '🫘', '1 cup', 269, 15, 45, 4],
  [31, 'Almonds', 'Protein', '🌰', '28 g', 164, 6, 6, 14],
  [32, 'Peanut butter', 'Protein', '🥜', '2 tbsp', 188, 8, 6, 16],
  // Dairy
  [33, 'Whole milk', 'Dairy', '🥛', '1 cup', 149, 8, 12, 8],
  [34, 'Plain yogurt', 'Dairy', '🥛', '1 cup', 149, 8.5, 11.4, 8],
  [35, 'Cheddar cheese', 'Dairy', '🧀', '1 slice', 113, 7, 0.4, 9],
  [36, 'Paneer', 'Dairy', '🧀', '100 g', 265, 18, 1.2, 21],
  [37, 'Butter', 'Dairy', '🧈', '1 tbsp', 102, 0.1, 0, 11.5],
  // Snacks
  [38, 'Potato chips', 'Snacks', '🍟', '1 small bag', 152, 2, 15, 10],
  [39, 'Dark chocolate', 'Snacks', '🍫', '20 g', 120, 1.5, 9, 8.5],
  [40, 'Biscuits', 'Snacks', '🍪', '2 pieces', 100, 1.2, 14, 4.5],
  [41, 'Popcorn', 'Snacks', '🍿', '3 cups', 93, 3, 19, 1],
  [42, 'Vanilla ice cream', 'Snacks', '🍦', '1/2 cup', 137, 2.3, 16, 7],
  [43, 'Granola bar', 'Snacks', '🍫', '1 bar', 120, 2, 18, 4.5],
  // Drinks
  [44, 'Green tea', 'Drinks', '🍵', '1 cup', 2, 0, 0.5, 0],
  [45, 'Milk tea (chai)', 'Drinks', '☕', '1 cup', 90, 2.5, 12, 3.5],
  [46, 'Black coffee', 'Drinks', '☕', '1 cup', 2, 0.3, 0, 0],
  [47, 'Orange juice', 'Drinks', '🧃', '1 cup', 112, 1.7, 26, 0.5],
  [48, 'Cola', 'Drinks', '🥤', '1 can', 140, 0, 39, 0],
  [49, 'Sweet lassi', 'Drinks', '🥛', '1 glass', 250, 8, 35, 8],
  [50, 'Sugarcane juice', 'Drinks', '🧃', '1 glass', 180, 0.5, 45, 0],
  [51, 'Protein shake', 'Drinks', '🥤', '1 scoop', 120, 24, 3, 1.5],
  // Desi (Pakistani) dishes
  [52, 'Chicken biryani', 'Desi', '🍛', '1 plate', 490, 22, 60, 17],
  [53, 'Chicken karahi', 'Desi', '🍗', '1 serving', 350, 28, 8, 22],
  [54, 'Nihari', 'Desi', '🍲', '1 bowl', 450, 28, 12, 33],
  [55, 'Daal chawal', 'Desi', '🍛', '1 plate', 420, 14, 70, 8],
  [56, 'Aloo paratha', 'Desi', '🫓', '1 piece', 300, 6, 40, 13],
  [57, 'Plain paratha', 'Desi', '🫓', '1 piece', 260, 5, 34, 11],
  [58, 'Samosa', 'Desi', '🥟', '1 piece', 260, 4, 28, 15],
  [59, 'Seekh kabab', 'Desi', '🍢', '2 pieces', 220, 16, 4, 15],
  [60, 'Chicken tikka', 'Desi', '🍢', '4 pieces', 190, 28, 4, 7],
  [61, 'Chana chaat', 'Desi', '🥗', '1 bowl', 250, 10, 38, 6],
  [62, 'Halwa puri', 'Desi', '🍽️', '1 plate', 600, 9, 75, 30],
  [63, 'Gulab jamun', 'Desi', '🍡', '1 piece', 150, 2, 22, 6],
  [64, 'Kheer', 'Desi', '🍮', '1 bowl', 280, 7, 40, 10],
  [65, 'Chicken shawarma', 'Desi', '🌯', '1 wrap', 450, 25, 40, 20],
  // Fast food
  [66, 'Beef burger', 'Fast Food', '🍔', '1 burger', 540, 25, 40, 28],
  [67, 'Pizza slice', 'Fast Food', '🍕', '1 slice', 285, 12, 36, 10],
  [68, 'French fries', 'Fast Food', '🍟', '1 medium', 365, 4, 48, 17],
  [69, 'Fried chicken', 'Fast Food', '🍗', '1 piece', 320, 25, 11, 19],
  [70, 'Club sandwich', 'Fast Food', '🥪', '1 sandwich', 450, 24, 38, 22]
];

/* Convert compact rows into readable food objects */
const FOODS = FOOD_ROWS.map(([id, name, category, emoji, serving, calories, protein, carbs, fat]) => (
  { id, name, category, emoji, serving, calories, protein, carbs, fat }
));

/* Meal slots used by the diary and the "add food" dialog */
const MEALS = [
  { key: 'breakfast', label: 'Breakfast', emoji: '🍳' },
  { key: 'lunch', label: 'Lunch', emoji: '🥗' },
  { key: 'dinner', label: 'Dinner', emoji: '🍽️' },
  { key: 'snacks', label: 'Snacks', emoji: '🍿' }
];

/* Exercise types with MET values (used to estimate calories burned) */
const EXERCISES = [
  { key: 'walking', label: 'Walking', met: 3.5 },
  { key: 'running', label: 'Running', met: 9.8 },
  { key: 'cycling', label: 'Cycling', met: 7.5 },
  { key: 'swimming', label: 'Swimming', met: 8 },
  { key: 'gym', label: 'Gym workout', met: 6 },
  { key: 'football', label: 'Football', met: 7 },
  { key: 'cricket', label: 'Cricket', met: 4.8 },
  { key: 'yoga', label: 'Yoga', met: 3 }
];

/* Distinct food categories for the filter chips */
const CATEGORIES = Array.from(new Set(FOODS.map(f => f.category)));
