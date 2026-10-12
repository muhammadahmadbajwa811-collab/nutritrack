/* ==========================================================
   profile.js — edit profile and goals, BMI, theme choice,
   export data, reset data, delete account
   ========================================================== */
'use strict';

/** Fill the form from the saved user. */
function fillProfileForm(user) {
  const f = $('#profileForm').elements;
  f.name.value = user.name;
  f.age.value = user.age;
  f.gender.value = user.gender;
  f.height.value = user.height;
  f.weight.value = user.weight;
  f.activity.value = user.activity;
  f.goal.value = user.goal;
  f.calorieGoal.value = user.calorieGoal;
  f.stepGoal.value = user.stepGoal || 10000;
  f.waterGoal.value = user.waterGoal || 8;
  $('#profileEmail').textContent = user.email;
}

function renderBmi(user) {
  const bmi = calculateBMI(user.weight, user.height);
  const goals = buildGoals(user);
  $('#bmiValue').textContent = round1(bmi);
  $('#bmiLabel').textContent = bmiCategory(bmi);
  // Needle position on a 15 to 40 scale
  $('#bmiMarker').style.left = `${Math.min(100, Math.max(0, ((bmi - 15) / 25) * 100))}%`;
  $('#suggestedGoal').textContent = formatNumber(goals.calorieGoal);
  $('#bmrValue').textContent = formatNumber(goals.bmr);
}

function renderProfileStats() {
  const data = getUserData();
  const loggedDays = Object.keys(data.meals).filter(d => data.meals[d].length).length;
  const foodsLogged = Object.values(data.meals).reduce((n, list) => n + list.length, 0);
  $('#statDays').textContent = loggedDays;
  $('#statFoods').textContent = foodsLogged;
  $('#statFavs').textContent = data.favorites.length;
  $('#statCustom').textContent = data.customFoods.length;
}

function validateProfileForm(form) {
  const f = form.elements;
  return [
    validateRequired(f.name, 'Full name', 2),
    validateRange(f.age, 'Age', 14, 100),
    validateRange(f.height, 'Height', 100, 250),
    validateRange(f.weight, 'Weight', 30, 300),
    validateRange(f.calorieGoal, 'Calorie goal', 1000, 6000),
    validateRange(f.stepGoal, 'Step goal', 1000, 50000),
    validateRange(f.waterGoal, 'Water goal', 1, 20)
  ].every(Boolean);
}

function saveProfile(event) {
  event.preventDefault();
  const form = event.currentTarget;
  if (!validateProfileForm(form)) {
    showToast('Please fix the highlighted fields.', 'error');
    return;
  }
  const f = form.elements;
  const updated = updateUser({
    name: f.name.value.trim(), age: Number(f.age.value), gender: f.gender.value,
    height: Number(f.height.value), weight: Number(f.weight.value),
    activity: f.activity.value, goal: f.goal.value,
    calorieGoal: Math.round(Number(f.calorieGoal.value)),
    stepGoal: Math.round(Number(f.stepGoal.value)),
    waterGoal: Math.round(Number(f.waterGoal.value))
  });
  if (updated) {
    renderBmi(updated);
    showToast('Profile saved.');
  }
}

/** Replace the calorie goal field with a fresh calculation from the body data. */
function recalculateGoal() {
  const f = $('#profileForm').elements;
  const ok = [
    validateRange(f.age, 'Age', 14, 100),
    validateRange(f.height, 'Height', 100, 250),
    validateRange(f.weight, 'Weight', 30, 300)
  ].every(Boolean);
  if (!ok) return;
  const goals = buildGoals({
    age: Number(f.age.value), gender: f.gender.value, height: Number(f.height.value),
    weight: Number(f.weight.value), activity: f.activity.value, goal: f.goal.value
  });
  f.calorieGoal.value = goals.calorieGoal;
  setFieldError(f.calorieGoal, '');
  showToast(`Goal recalculated: ${formatNumber(goals.calorieGoal)} kcal. Save to keep it.`, 'info');
}

/** Download all of the user's data as a JSON file. */
function exportData() {
  try {
    const user = getCurrentUser();
    const { passwordHash, ...safeUser } = user;
    const blob = new Blob([JSON.stringify({ profile: safeUser, data: getUserData() }, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `nutritrack-${todayKey()}.json`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    showToast('Your data was downloaded.');
  } catch (err) {
    console.error(err);
    showToast('Could not export your data.', 'error');
  }
}

function resetData() {
  saveUserData(emptyUserData());
  renderProfileStats();
  showToast('All tracking data was reset.', 'info');
}

function deleteAccount() {
  const user = getCurrentUser();
  try {
    localStorage.removeItem(LS.dataPrefix + user.email);
  } catch (err) { console.error(err); }
  saveUsers(getUsers().filter(u => u.email !== user.email));
  clearSession();
  window.location.href = homeUrl();
}

function initThemeChoice() {
  const select = $('#themeChoice');
  select.value = getTheme();
  select.addEventListener('change', () => {
    applyTheme(select.value);
    showToast(`${select.value === 'dark' ? 'Dark' : 'Light'} mode on.`, 'info', 2000);
  });
}

function initProfile() {
  const user = getCurrentUser();
  if (!user || !$('#profileForm')) return;
  fillProfileForm(user);
  renderBmi(user);
  renderProfileStats();
  initThemeChoice();
  $('#profileForm').addEventListener('submit', saveProfile);
  $('#recalcGoal').addEventListener('click', recalculateGoal);
  $('#exportData').addEventListener('click', exportData);
  $('#resetData').addEventListener('click', () => confirmAction('Reset all data?', 'Your meals, steps, water, workouts and favorites will be deleted. Your account stays.', resetData, 'Reset data'));
  $('#deleteAccount').addEventListener('click', () => confirmAction('Delete your account?', 'Your account and all its data will be permanently removed from this browser.', deleteAccount, 'Delete account'));
}

document.addEventListener('DOMContentLoaded', initProfile);
