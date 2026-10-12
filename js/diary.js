/* ==========================================================
   diary.js — meal diary by date: change quantity, delete,
   clear day, date navigation
   ========================================================== */
'use strict';

let diaryDate = todayKey();

function renderDiary() {
  const user = getCurrentUser();
  const data = getUserData();
  const entries = data.meals[diaryDate] || [];
  const totals = sumEntries(entries);
  const goal = user.calorieGoal;

  $('#diaryDateLabel').textContent = diaryDate === todayKey() ? `Today, ${formatDateLong(diaryDate)}` : formatDateLong(diaryDate);
  $('#diaryDate').value = diaryDate;
  $('#nextDay').disabled = diaryDate >= todayKey();

  // Day summary
  $('#sumEaten').textContent = formatNumber(totals.calories);
  $('#sumGoal').textContent = formatNumber(goal);
  const bar = $('#sumBar');
  bar.parentElement.classList.toggle('over', totals.calories > goal);
  bar.style.width = `${percentOf(totals.calories, goal)}%`;
  $('#sumMacros').textContent = `Protein ${round1(totals.protein)} g · Carbs ${round1(totals.carbs)} g · Fat ${round1(totals.fat)} g`;

  // One block per meal
  $('#mealBlocks').innerHTML = MEALS.map(meal => {
    const mealEntries = entries.filter(e => e.meal === meal.key);
    const mealKcal = sumEntries(mealEntries).calories;
    return `
      <section class="meal-block panel" aria-labelledby="meal-${meal.key}">
        <div class="meal-head">
          <h3 id="meal-${meal.key}">${meal.emoji} ${meal.label} <span class="muted small">· ${formatNumber(mealKcal)} kcal</span></h3>
          <button class="btn btn-outline btn-sm" type="button" data-add-meal="${meal.key}">+ Add food</button>
        </div>
        ${mealEntries.length
          ? `<ul class="entry-list">${mealEntries.map(renderEntry).join('')}</ul>`
          : '<p class="muted small">Nothing logged for this meal.</p>'}
      </section>`;
  }).join('');
}

function renderEntry(e) {
  return `
    <li class="entry">
      <span class="emoji" aria-hidden="true">${e.emoji}</span>
      <div><div class="entry-name">${escapeHtml(e.name)}</div><div class="entry-sub">${escapeHtml(e.serving)} per serving</div></div>
      <div class="qty" role="group" aria-label="Servings of ${escapeHtml(e.name)}">
        <button type="button" data-qty="-0.5" data-id="${e.id}" aria-label="Decrease servings">−</button>
        <span>${e.qty}</span>
        <button type="button" data-qty="0.5" data-id="${e.id}" aria-label="Increase servings">+</button>
      </div>
      <span class="entry-kcal">${formatNumber(e.calories * e.qty)} kcal</span>
      <button class="icon-btn" type="button" data-delete="${e.id}" aria-label="Remove ${escapeHtml(e.name)}">🗑️</button>
    </li>`;
}

/** Change an entry's servings by delta (minimum 0.5, maximum 20). */
function changeEntryQty(id, delta) {
  const data = getUserData();
  const entry = (data.meals[diaryDate] || []).find(e => e.id === id);
  if (!entry) return;
  entry.qty = Math.min(20, Math.max(0.5, entry.qty + delta));
  saveUserData(data);
  renderDiary();
}

function deleteEntry(id) {
  const data = getUserData();
  data.meals[diaryDate] = (data.meals[diaryDate] || []).filter(e => e.id !== id);
  saveUserData(data);
  showToast('Entry removed.', 'info', 2000);
  renderDiary();
}

function clearDay() {
  const data = getUserData();
  delete data.meals[diaryDate];
  saveUserData(data);
  showToast('Day cleared.', 'info');
  renderDiary();
}

function goToDate(key) {
  if (!key || key > todayKey()) return;
  diaryDate = key;
  renderDiary();
}

function initDiary() {
  if (!getCurrentUser() || !$('#mealBlocks')) return;
  $('#prevDay').addEventListener('click', () => goToDate(shiftDate(diaryDate, -1)));
  $('#nextDay').addEventListener('click', () => goToDate(shiftDate(diaryDate, 1)));
  $('#todayBtn').addEventListener('click', () => goToDate(todayKey()));
  $('#diaryDate').max = todayKey();
  $('#diaryDate').addEventListener('change', event => goToDate(event.target.value));
  $('#clearDay').addEventListener('click', () => {
    if (!(getUserData().meals[diaryDate] || []).length) {
      showToast('This day is already empty.', 'info');
      return;
    }
    confirmAction('Clear this day?', 'All meals logged for this date will be removed.', clearDay, 'Clear day');
  });

  $('#mealBlocks').addEventListener('click', event => {
    const add = event.target.closest('[data-add-meal]');
    const qty = event.target.closest('[data-qty]');
    const del = event.target.closest('[data-delete]');
    if (add) openAddFoodModal({ meal: add.dataset.addMeal, date: diaryDate, onAdded: renderDiary });
    if (qty) changeEntryQty(qty.dataset.id, Number(qty.dataset.qty));
    if (del) confirmAction('Remove this food?', 'It will be deleted from your diary.', () => deleteEntry(del.dataset.delete), 'Remove');
  });
  renderDiary();
}

document.addEventListener('DOMContentLoaded', initDiary);
