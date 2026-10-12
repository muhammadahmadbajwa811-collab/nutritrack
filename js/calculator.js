/* ==========================================================
   calculator.js — free calorie + BMI calculator (Features page)
   and live FAQ filter (FAQ page)
   ========================================================== */
'use strict';

/* ---------- Calorie and BMI calculator ---------- */

function validateCalculator(form) {
  const f = form.elements;
  return [
    validateRange(f.age, 'Age', 14, 100),
    validateSelect(f.gender, 'a gender'),
    validateRange(f.height, 'Height', 100, 250),
    validateRange(f.weight, 'Weight', 30, 300)
  ].every(Boolean);
}

/** Show the calculated numbers in the result panel. */
function renderCalculatorResult(profile) {
  const goals = buildGoals(profile);
  const bmi = calculateBMI(profile.weight, profile.height);
  const macros = macroTargets(goals.calorieGoal);
  $('#calcResult').innerHTML = `
    <h3>Your daily plan</h3>
    <p class="muted">Based on the Mifflin-St Jeor equation. These are estimates, not medical advice.</p>
    <div class="result-grid">
      <div class="result-box"><strong>${formatNumber(goals.calorieGoal)}</strong><span>kcal per day (goal)</span></div>
      <div class="result-box"><strong>${formatNumber(goals.bmr)}</strong><span>kcal at rest (BMR)</span></div>
      <div class="result-box"><strong>${formatNumber(goals.tdee)}</strong><span>kcal to maintain weight</span></div>
      <div class="result-box"><strong>${round1(bmi)}</strong><span>BMI: ${bmiCategory(bmi)}</span></div>
    </div>
    <p class="small muted" style="margin-top:16px">Suggested macros: ${macros.protein} g protein, ${macros.carbs} g carbs, ${macros.fat} g fat.</p>
    <a class="btn btn-primary" style="margin-top:16px" href="register.html" data-auth="guest">Save this goal: create account</a>`;
  updateAuthUI();
}

function initCalculator() {
  const form = $('#calcForm');
  if (!form) return;
  form.addEventListener('submit', event => {
    event.preventDefault();
    if (!validateCalculator(form)) return;
    const f = form.elements;
    renderCalculatorResult({
      age: Number(f.age.value), gender: f.gender.value, height: Number(f.height.value),
      weight: Number(f.weight.value), activity: f.activity.value, goal: f.goal.value
    });
  });
}

/* ---------- FAQ filter ---------- */

function initFaqFilter() {
  const input = $('#faqSearch');
  if (!input) return;
  input.addEventListener('input', debounce(() => {
    const query = input.value.trim().toLowerCase();
    let visible = 0;
    $$('.accordion-item').forEach(item => {
      const match = item.textContent.toLowerCase().includes(query);
      item.hidden = !match;
      if (match) visible++;
    });
    $('#faqEmpty').hidden = visible > 0;
  }, 150));
}

document.addEventListener('DOMContentLoaded', () => {
  initCalculator();
  initFaqFilter();
});
