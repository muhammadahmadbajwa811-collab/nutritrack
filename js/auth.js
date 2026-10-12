/* ==========================================================
   auth.js — register and login (simulated with localStorage)
   NOTE: for demonstration only. Passwords are lightly hashed
   in the browser; a real app would verify them on a server.
   ========================================================== */
'use strict';

const DEMO_EMAIL = 'demo@nutritrack.app';
const DEMO_PASSWORD = 'demo1234';

/* ---------- Registration ---------- */

/** Update the strength bar under the password field. */
function renderPasswordStrength(password) {
  const bar = $('#strengthBar span');
  const text = $('#strengthText');
  if (!bar || !text) return;
  const score = password ? passwordStrength(password) : 0;
  const levels = [
    { label: 'Use 8+ characters with letters and numbers.', color: 'var(--border)' },
    { label: 'Weak password', color: 'var(--btn)' },
    { label: 'Fair password', color: 'var(--warn)' },
    { label: 'Good password', color: 'var(--info)' },
    { label: 'Strong password', color: 'var(--ok)' }
  ];
  bar.style.width = `${score * 25}%`;
  bar.style.background = levels[score].color;
  text.textContent = levels[score].label;
}

/** Validate every field on the register form. Returns true when all pass. */
function validateRegisterForm(form) {
  const f = form.elements;
  const results = [
    validateRequired(f.name, 'Full name', 2),
    validateEmailField(f.email),
    validateRange(f.age, 'Age', 14, 100),
    validateSelect(f.gender, 'a gender'),
    validateRange(f.height, 'Height', 100, 250),
    validateRange(f.weight, 'Weight', 30, 300),
    validateSelect(f.activity, 'an activity level'),
    validateSelect(f.goal, 'a goal')
  ];
  const pw = f.password.value;
  if (pw.length < 8) results.push(setFieldError(f.password, 'Password must be at least 8 characters.'));
  else if (!/\d/.test(pw) || !/[A-Za-z]/.test(pw)) results.push(setFieldError(f.password, 'Include at least one letter and one number.'));
  else results.push(setFieldError(f.password, ''));
  results.push(f.confirm.value === pw && pw !== ''
    ? setFieldError(f.confirm, '')
    : setFieldError(f.confirm, 'Passwords do not match.'));
  if (!f.terms.checked) {
    $('#termsError').textContent = 'Please accept the terms to continue.';
    results.push(false);
  } else {
    $('#termsError').textContent = '';
  }
  return results.every(Boolean);
}

/** Build and save a new user from the register form. */
function createUserFromForm(form) {
  const f = form.elements;
  const profile = {
    name: f.name.value.trim(),
    email: f.email.value.trim().toLowerCase(),
    passwordHash: simpleHash(f.password.value),
    age: Number(f.age.value),
    gender: f.gender.value,
    height: Number(f.height.value),
    weight: Number(f.weight.value),
    activity: f.activity.value,
    goal: f.goal.value,
    stepGoal: 10000,
    waterGoal: 8,
    createdAt: new Date().toISOString()
  };
  profile.calorieGoal = buildGoals(profile).calorieGoal;
  const users = getUsers();
  users.push(profile);
  return saveUsers(users) ? profile : null;
}

function initRegister() {
  const form = $('#registerForm');
  if (!form) return;
  form.elements.password.addEventListener('input', event => renderPasswordStrength(event.target.value));
  form.addEventListener('submit', event => {
    event.preventDefault();
    if (!validateRegisterForm(form)) {
      showToast('Please fix the highlighted fields.', 'error');
      return;
    }
    if (findUser(form.elements.email.value)) {
      setFieldError(form.elements.email, 'An account with this email already exists.');
      return;
    }
    try {
      const user = createUserFromForm(form);
      if (!user) return;
      setSession(user.email);
      showToast(`Welcome, ${user.name.split(' ')[0]}! Your daily goal is ${formatNumber(user.calorieGoal)} kcal.`);
      setTimeout(() => { window.location.href = pageUrl('dashboard'); }, 1200);
    } catch (err) {
      console.error(err);
      showToast('Something went wrong while creating your account.', 'error');
    }
  });
}

/* ---------- Login ---------- */

function initLogin() {
  const form = $('#loginForm');
  if (!form) return;
  form.addEventListener('submit', event => {
    event.preventDefault();
    const email = form.elements.email;
    const password = form.elements.password;
    const okEmail = validateEmailField(email);
    const okPass = validateRequired(password, 'Password');
    if (!okEmail || !okPass) return;
    const user = findUser(email.value);
    if (!user || user.passwordHash !== simpleHash(password.value)) {
      $('#loginAlert').hidden = false;
      return;
    }
    $('#loginAlert').hidden = true;
    setSession(user.email);
    showToast(`Welcome back, ${user.name.split(' ')[0]}!`);
    setTimeout(() => { window.location.href = pageUrl('dashboard'); }, 900);
  });

  const demoBtn = $('#demoLogin');
  if (demoBtn) demoBtn.addEventListener('click', loginAsDemo);
}

/* ---------- Demo account (so evaluators can see a full dashboard) ---------- */

/** One day of sample meals: [meal, foodId, servings]. */
const DEMO_DAYS = [
  { meals: [['breakfast', 56, 1], ['breakfast', 45, 1], ['lunch', 52, 1], ['dinner', 53, 1], ['dinner', 18, 2], ['snacks', 1, 1]], steps: 8200, water: 7 },
  { meals: [['breakfast', 24, 2], ['breakfast', 21, 2], ['lunch', 55, 1], ['dinner', 60, 1], ['dinner', 16, 1], ['snacks', 40, 1]], steps: 6400, water: 6 },
  { meals: [['breakfast', 20, 1], ['breakfast', 2, 1], ['lunch', 66, 1], ['lunch', 68, 1], ['dinner', 29, 1], ['dinner', 18, 2]], steps: 11200, water: 8 },
  { meals: [['breakfast', 56, 1], ['lunch', 61, 1], ['lunch', 47, 1], ['dinner', 54, 1], ['snacks', 58, 1]], steps: 9300, water: 5 },
  { meals: [['breakfast', 24, 2], ['breakfast', 33, 1], ['lunch', 25, 1.5], ['lunch', 16, 1], ['dinner', 59, 1], ['snacks', 41, 1]], steps: 10400, water: 8 },
  { meals: [['breakfast', 20, 1], ['lunch', 52, 1], ['dinner', 53, 1], ['dinner', 18, 1], ['snacks', 42, 1]], steps: 5200, water: 6 }
];

/** Create the demo user with 6 days of sample data and a partly filled today. */
function seedDemoAccount() {
  const profile = {
    name: 'Demo User', email: DEMO_EMAIL, passwordHash: simpleHash(DEMO_PASSWORD),
    age: 21, gender: 'male', height: 175, weight: 70, activity: 'light', goal: 'maintain',
    stepGoal: 10000, waterGoal: 8, createdAt: new Date().toISOString()
  };
  profile.calorieGoal = buildGoals(profile).calorieGoal;
  const users = getUsers();
  users.push(profile);
  saveUsers(users);

  const data = emptyUserData();
  const today = todayKey();
  DEMO_DAYS.forEach((day, i) => {
    const date = shiftDate(today, -(DEMO_DAYS.length - i));
    data.meals[date] = day.meals.map(([meal, id, qty]) => makeEntry(FOODS.find(f => f.id === id), meal, qty));
    data.steps[date] = day.steps;
    data.water[date] = day.water;
  });
  data.meals[today] = [['breakfast', 24, 2], ['breakfast', 18, 1], ['breakfast', 45, 1], ['lunch', 52, 1]]
    .map(([meal, id, qty]) => makeEntry(FOODS.find(f => f.id === id), meal, qty));
  data.steps[today] = 3800;
  data.water[today] = 3;
  data.activities[shiftDate(today, -2)] = [{ id: 'seed1', type: 'running', label: 'Running', minutes: 25, calories: exerciseCalories(9.8, 70, 25) }];
  data.favorites = [24, 52, 60];
  writeStorage(LS.dataPrefix + DEMO_EMAIL, data);
}

function loginAsDemo() {
  try {
    if (!findUser(DEMO_EMAIL)) seedDemoAccount();
    setSession(DEMO_EMAIL);
    showToast('Signed in with the demo account.');
    setTimeout(() => { window.location.href = pageUrl('dashboard'); }, 700);
  } catch (err) {
    console.error(err);
    showToast('Could not create the demo account.', 'error');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  initRegister();
  initLogin();
  // Already logged in? No need to see the login or register form.
  if (getCurrentUser() && ($('#registerForm') || $('#loginForm'))) {
    window.location.replace(pageUrl('dashboard'));
  }
});
