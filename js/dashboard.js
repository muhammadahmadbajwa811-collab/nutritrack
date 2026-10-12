/* ==========================================================
   dashboard.js — today's summary, macros, water, steps,
   weekly chart and recent entries
   ========================================================== */
'use strict';

let dashUser = null;
let dashData = null;
const TODAY = todayKey();

/** Reload data from storage, then redraw every card. */
function refreshDashboard() {
  dashUser = getCurrentUser();
  dashData = getUserData();
  renderGreeting();
  renderCalories();
  renderMacros();
  renderWater();
  renderSteps();
  renderWeekChart();
  renderRecent();
}

function renderGreeting() {
  const hour = new Date().getHours();
  const part = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
  $('#greeting').textContent = `${part}, ${dashUser.name.split(' ')[0]}`;
  $('#todayLabel').textContent = formatDateLong(TODAY);
}

/* ---------- Calories ring ---------- */

function renderCalories() {
  const totals = getDayTotals(TODAY, dashData);
  const burned = getBurnedCalories(TODAY, dashUser, dashData);
  const goal = dashUser.calorieGoal;
  const remaining = goal - totals.calories + burned;
  const ring = $('#calRing');
  ring.style.setProperty('--p', percentOf(totals.calories, goal + burned));
  ring.classList.toggle('over', remaining < 0);
  $('#calLeft').textContent = formatNumber(Math.abs(remaining));
  $('#calLeftLabel').textContent = remaining < 0 ? 'kcal over' : 'kcal left';
  $('#kpiGoal').textContent = formatNumber(goal);
  $('#kpiEaten').textContent = formatNumber(totals.calories);
  $('#kpiBurned').textContent = formatNumber(burned);
  const msg = $('#calMessage');
  if (remaining < 0) msg.textContent = 'You are over your goal today. A walk can help balance it.';
  else if (totals.calories === 0) msg.textContent = 'Nothing logged yet. Add your first meal.';
  else msg.textContent = `You have ${formatNumber(remaining)} kcal left for today.`;
}

/* ---------- Macros ---------- */

function renderMacros() {
  const totals = getDayTotals(TODAY, dashData);
  const targets = macroTargets(dashUser.calorieGoal);
  const kcal = { protein: totals.protein * 4, carbs: totals.carbs * 4, fat: totals.fat * 9 };
  const sum = kcal.protein + kcal.carbs + kcal.fat;
  const donut = $('#macroDonut');
  if (sum > 0) {
    const a = (kcal.protein / sum) * 100;
    const b = a + (kcal.carbs / sum) * 100;
    donut.style.background = `conic-gradient(var(--protein) 0 ${a}%, var(--carbs) ${a}% ${b}%, var(--fat) ${b}% 100%)`;
  } else {
    donut.style.background = 'var(--border)';
  }
  const rows = [
    { key: 'protein', label: 'Protein', cls: 'blue' },
    { key: 'carbs', label: 'Carbs', cls: 'amber' },
    { key: 'fat', label: 'Fat', cls: 'green' }
  ];
  $('#macroBars').innerHTML = rows.map(r => `
    <div class="macro-row">
      <div class="row-between"><span>${r.label}</span><b>${round1(totals[r.key])} / ${targets[r.key]} g</b></div>
      <div class="bar ${r.cls}${totals[r.key] > targets[r.key] ? ' over' : ''}"><span style="width:${percentOf(totals[r.key], targets[r.key])}%"></span></div>
    </div>`).join('');
}

/* ---------- Water ---------- */

function renderWater() {
  const goal = dashUser.waterGoal || 8;
  const count = dashData.water[TODAY] || 0;
  $('#waterText').textContent = `${count} of ${goal} glasses`;
  const box = $('#glasses');
  box.innerHTML = Array.from({ length: goal }, (_, i) =>
    `<button class="glass${i < count ? ' filled' : ''}" type="button" data-glass="${i + 1}" aria-label="Set water to ${i + 1} glasses"></button>`
  ).join('');
}

/** Click on glass n sets water to n (click the last filled glass to remove it). */
function setWater(n) {
  const current = dashData.water[TODAY] || 0;
  dashData.water[TODAY] = n === current ? n - 1 : n;
  saveUserData(dashData);
  renderWater();
  if (dashData.water[TODAY] === (dashUser.waterGoal || 8)) showToast('Water goal reached. Nice work!');
}

/* ---------- Steps ---------- */

function renderSteps() {
  const goal = dashUser.stepGoal || 10000;
  const steps = dashData.steps[TODAY] || 0;
  const ring = $('#stepRing');
  ring.style.setProperty('--p', percentOf(steps, goal));
  ring.classList.toggle('green', steps >= goal);
  $('#stepCount').textContent = formatNumber(steps);
  $('#stepGoalText').textContent = `of ${formatNumber(goal)} steps`;
  $('#stepKm').textContent = round1(stepsToKm(steps, dashUser.height));
  $('#stepKcal').textContent = formatNumber(stepsToCalories(steps, dashUser.weight));
}

function addSteps(amount) {
  const goal = dashUser.stepGoal || 10000;
  const before = dashData.steps[TODAY] || 0;
  dashData.steps[TODAY] = before + amount;
  saveUserData(dashData);
  renderSteps();
  renderCalories();
  if (before < goal && dashData.steps[TODAY] >= goal) showToast('Step goal reached. 🎉');
}

/* ---------- Weekly calorie chart ---------- */

function renderWeekChart() {
  const days = lastNDays(7);
  const values = days.map(d => Math.round(getDayTotals(d, dashData).calories));
  const goal = dashUser.calorieGoal;
  const max = Math.max(goal, ...values) * 1.1;
  $('#weekChart').innerHTML = days.map((d, i) => `
    <div class="chart-col">
      <div class="chart-box">
        <div class="chart-bar${d === TODAY ? ' today' : ''}" style="height:${(values[i] / max) * 100}%" title="${formatDateLong(d)}: ${values[i]} kcal"></div>
      </div>
      <span class="chart-label">${weekdayShort(d)}</span>
      <span class="chart-val">${values[i] ? formatNumber(values[i]) : '-'}</span>
    </div>`).join('');
  // Goal line: 44px of labels sit under the bars, the rest (176px) is bar space
  $('#goalLine').style.bottom = `${44 + (goal / max) * 176}px`;
  const logged = values.filter(v => v > 0);
  $('#weekAvg').textContent = logged.length
    ? `Average on logged days: ${formatNumber(logged.reduce((a, b) => a + b, 0) / logged.length)} kcal`
    : 'No meals logged this week yet.';
}

/* ---------- Recent entries ---------- */

function renderRecent() {
  const entries = (dashData.meals[TODAY] || []).slice().reverse().slice(0, 5);
  $('#recentList').innerHTML = entries.length
    ? entries.map(e => `
      <li class="entry" style="grid-template-columns:auto 1fr auto">
        <span class="emoji" aria-hidden="true">${e.emoji}</span>
        <div><div class="entry-name">${escapeHtml(e.name)}</div><div class="entry-sub">${e.meal} · ${e.qty} x ${escapeHtml(e.serving)}</div></div>
        <span class="entry-kcal">${formatNumber(e.calories * e.qty)}</span>
      </li>`).join('')
    : '<li class="empty"><h3>No meals yet</h3><p>Use "Add food" to log your first meal today.</p></li>';
}

/* ---------- Events ---------- */

function initDashboard() {
  if (!getCurrentUser()) return;
  $$('[data-add-food]').forEach(btn => btn.addEventListener('click', () => openAddFoodModal({ onAdded: refreshDashboard })));
  $('#glasses').addEventListener('click', event => {
    const glass = event.target.closest('.glass');
    if (glass) setWater(Number(glass.dataset.glass));
  });
  $$('[data-steps]').forEach(btn => btn.addEventListener('click', () => addSteps(Number(btn.dataset.steps))));
  refreshDashboard();
}

document.addEventListener('DOMContentLoaded', initDashboard);
