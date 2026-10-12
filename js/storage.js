/* ==========================================================
   storage.js — localStorage wrapper, accounts, per-user data
   Every read/write is wrapped in try/catch so a blocked or
   corrupted localStorage never breaks the app.
   ========================================================== */
'use strict';

const LS = {
  users: 'nt_users',
  session: 'nt_session',
  messages: 'nt_messages',
  dataPrefix: 'nt_data_'
};

/* ---------- Low-level helpers ---------- */

/** Read and parse JSON from localStorage; returns fallback on any error. */
function readStorage(key, fallback = null) {
  try {
    const raw = localStorage.getItem(key);
    return raw === null ? fallback : JSON.parse(raw);
  } catch (err) {
    console.error(`Could not read "${key}":`, err);
    return fallback;
  }
}

/** Stringify and save JSON; shows an error toast when saving fails. */
function writeStorage(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
    return true;
  } catch (err) {
    console.error(`Could not save "${key}":`, err);
    if (typeof showToast === 'function') {
      showToast('Could not save your data. Storage may be full or disabled in this browser.', 'error');
    }
    return false;
  }
}

/** Tiny non-cryptographic hash. Demo only: a real app hashes on the server. */
function simpleHash(text) {
  let hash = 5381;
  for (let i = 0; i < text.length; i++) {
    hash = ((hash << 5) + hash + text.charCodeAt(i)) | 0;
  }
  return String(hash >>> 0);
}

/* ---------- Accounts ---------- */

function getUsers() { return readStorage(LS.users, []); }
function saveUsers(users) { return writeStorage(LS.users, users); }

function findUser(email) {
  const clean = String(email || '').trim().toLowerCase();
  return getUsers().find(u => u.email === clean) || null;
}

function getCurrentUser() {
  const email = readStorage(LS.session, null);
  return email ? findUser(email) : null;
}

function setSession(email) { return writeStorage(LS.session, email); }

function clearSession() {
  try { localStorage.removeItem(LS.session); } catch (err) { console.error(err); }
}

/** Merge new fields into the logged-in user's record. */
function updateUser(patch) {
  const email = readStorage(LS.session, null);
  const users = getUsers();
  const index = users.findIndex(u => u.email === email);
  if (index < 0) return null;
  users[index] = { ...users[index], ...patch };
  saveUsers(users);
  return users[index];
}

/* ---------- Per-user tracking data ---------- */

function emptyUserData() {
  return { meals: {}, water: {}, steps: {}, activities: {}, favorites: [], customFoods: [] };
}

function getUserData() {
  const user = getCurrentUser();
  if (!user) return emptyUserData();
  return { ...emptyUserData(), ...readStorage(LS.dataPrefix + user.email, {}) };
}

function saveUserData(data) {
  const user = getCurrentUser();
  return user ? writeStorage(LS.dataPrefix + user.email, data) : false;
}

/* ---------- Date helpers (dates are stored as YYYY-MM-DD) ---------- */

function dateKey(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function todayKey() { return dateKey(new Date()); }

function parseKey(key) {
  const [y, m, d] = key.split('-').map(Number);
  return new Date(y, m - 1, d);
}

function shiftDate(key, days) {
  const date = parseKey(key);
  date.setDate(date.getDate() + days);
  return dateKey(date);
}

/** Array of the last n date keys ending at `end` (oldest first). */
function lastNDays(n, end = todayKey()) {
  const days = [];
  for (let i = n - 1; i >= 0; i--) days.push(shiftDate(end, -i));
  return days;
}

function formatDateLong(key) {
  return parseKey(key).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
}

function weekdayShort(key) {
  return parseKey(key).toLocaleDateString('en-US', { weekday: 'short' });
}

/* ---------- Foods and meal entries ---------- */

function getAllFoods() { return FOODS.concat(getUserData().customFoods); }

function findFood(id) {
  return getAllFoods().find(f => String(f.id) === String(id)) || null;
}

/** Build a diary entry. Per-serving values are stored, totals = value x qty. */
function makeEntry(food, meal, qty) {
  return {
    id: Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
    foodId: food.id, name: food.name, emoji: food.emoji, serving: food.serving,
    meal, qty,
    calories: food.calories, protein: food.protein, carbs: food.carbs, fat: food.fat
  };
}

/** Add a food to the diary for a date. Returns true when saved. */
function addMealEntry(foodId, meal, qty, date = todayKey()) {
  const food = findFood(foodId);
  if (!food) throw new Error('Food not found');
  const data = getUserData();
  if (!data.meals[date]) data.meals[date] = [];
  data.meals[date].push(makeEntry(food, meal, qty));
  return saveUserData(data);
}

function sumEntries(entries) {
  return entries.reduce((t, e) => ({
    calories: t.calories + e.calories * e.qty,
    protein: t.protein + e.protein * e.qty,
    carbs: t.carbs + e.carbs * e.qty,
    fat: t.fat + e.fat * e.qty
  }), { calories: 0, protein: 0, carbs: 0, fat: 0 });
}

function getDayTotals(date, data = getUserData()) {
  return sumEntries(data.meals[date] || []);
}

/** Calories burned in a day = logged exercise + estimated from steps. */
function getBurnedCalories(date, user, data = getUserData()) {
  const exercise = (data.activities[date] || []).reduce((sum, a) => sum + a.calories, 0);
  const steps = stepsToCalories(data.steps[date] || 0, user.weight);
  return Math.round(exercise + steps);
}
