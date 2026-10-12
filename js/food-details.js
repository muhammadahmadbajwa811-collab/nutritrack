/* ==========================================================
   food-details.js — one food: nutrition facts, serving
   adjuster, add to diary, favorites, similar foods
   ========================================================== */
'use strict';

let detailFood = null;
let detailQty = 1;

/** Read ?id= from the address bar. */
function getFoodIdFromUrl() {
  return new URLSearchParams(window.location.search).get('id');
}

/** Percentage of calories from each macro. */
function macroSplit(food) {
  const p = food.protein * 4, c = food.carbs * 4, f = food.fat * 9;
  const total = p + c + f || 1;
  return { p: (p / total) * 100, c: (c / total) * 100, f: (f / total) * 100 };
}

function renderDetail() {
  const food = detailFood;
  const q = detailQty;
  const split = macroSplit(food);
  const isFav = getUserData().favorites.includes(food.id);
  document.title = `${food.name} | NutriTrack`;
  $('#detail').innerHTML = `
    <div class="detail-grid">
      <div class="panel detail-hero">
        <div class="big-emoji" aria-hidden="true">${food.emoji}</div>
        <span class="badge">${escapeHtml(food.category)}</span>
        <h1 style="margin:10px 0 4px">${escapeHtml(food.name)}</h1>
        <p class="muted">Serving: ${escapeHtml(food.serving)}</p>
        <div class="row" style="justify-content:center;margin-top:18px">
          <div class="qty" role="group" aria-label="Number of servings">
            <button type="button" id="qtyMinus" aria-label="Fewer servings">−</button>
            <span id="qtyValue" aria-live="polite">${q}</span>
            <button type="button" id="qtyPlus" aria-label="More servings">+</button>
          </div>
          <button class="fav-btn" id="detailFav" type="button" aria-pressed="${isFav}" aria-label="${isFav ? 'Remove from' : 'Add to'} favorites">${isFav ? '❤️' : '🤍'}</button>
        </div>
      </div>
      <div>
        <div class="panel">
          <h3>Nutrition facts</h3>
          <table class="facts">
            <tr class="big"><th scope="row">Calories</th><td id="fCal">${formatNumber(food.calories * q)} kcal</td></tr>
            <tr><th scope="row">Protein</th><td id="fPro">${round1(food.protein * q)} g</td></tr>
            <tr><th scope="row">Carbohydrates</th><td id="fCar">${round1(food.carbs * q)} g</td></tr>
            <tr><th scope="row">Fat</th><td id="fFat">${round1(food.fat * q)} g</td></tr>
          </table>
          <p class="small muted" style="margin-top:14px">Where the calories come from</p>
          <div class="split-bar" role="img" aria-label="Protein ${Math.round(split.p)}%, carbs ${Math.round(split.c)}%, fat ${Math.round(split.f)}%">
            <span style="width:${split.p}%;background:var(--protein)"></span>
            <span style="width:${split.c}%;background:var(--carbs)"></span>
            <span style="width:${split.f}%;background:var(--fat)"></span>
          </div>
          <ul class="legend">
            <li><i style="background:var(--protein)"></i>Protein ${Math.round(split.p)}%</li>
            <li><i style="background:var(--carbs)"></i>Carbs ${Math.round(split.c)}%</li>
            <li><i style="background:var(--fat)"></i>Fat ${Math.round(split.f)}%</li>
          </ul>
        </div>
        <div class="panel" style="margin-top:20px">
          <h3>Add to your diary</h3>
          <div class="row">
            <select id="detailMeal" class="select" style="width:auto;flex:1" aria-label="Meal">${mealOptionsHtml(guessMeal())}</select>
            <button class="btn btn-primary" id="detailAdd" type="button">Add ${q} serving${q === 1 ? '' : 's'}</button>
          </div>
        </div>
      </div>
    </div>`;
  bindDetailEvents();
}

/** Update only the numbers when the quantity changes (no full re-render). */
function updateQuantity(newQty) {
  detailQty = Math.min(20, Math.max(0.5, newQty));
  const food = detailFood;
  $('#qtyValue').textContent = detailQty;
  $('#fCal').textContent = `${formatNumber(food.calories * detailQty)} kcal`;
  $('#fPro').textContent = `${round1(food.protein * detailQty)} g`;
  $('#fCar').textContent = `${round1(food.carbs * detailQty)} g`;
  $('#fFat').textContent = `${round1(food.fat * detailQty)} g`;
  $('#detailAdd').textContent = `Add ${detailQty} serving${detailQty === 1 ? '' : 's'}`;
}

function addDetailFoodToDiary() {
  if (!getCurrentUser()) {
    showToast('Log in to add foods to your diary.', 'warning');
    return;
  }
  const meal = $('#detailMeal').value;
  try {
    addMealEntry(detailFood.id, meal, detailQty);
    showToast(`${detailFood.name} added to ${meal}.`);
  } catch (err) {
    console.error(err);
    showToast('Could not add this food.', 'error');
  }
}

function toggleDetailFavorite() {
  if (!getCurrentUser()) {
    showToast('Log in to save favorites.', 'warning');
    return;
  }
  const data = getUserData();
  const i = data.favorites.indexOf(detailFood.id);
  if (i >= 0) data.favorites.splice(i, 1); else data.favorites.push(detailFood.id);
  saveUserData(data);
  renderDetail();
}

function bindDetailEvents() {
  $('#qtyMinus').addEventListener('click', () => updateQuantity(detailQty - 0.5));
  $('#qtyPlus').addEventListener('click', () => updateQuantity(detailQty + 0.5));
  $('#detailAdd').addEventListener('click', addDetailFoodToDiary);
  $('#detailFav').addEventListener('click', toggleDetailFavorite);
}

function renderSimilar() {
  const similar = getAllFoods()
    .filter(f => f.category === detailFood.category && f.id !== detailFood.id)
    .slice(0, 4);
  $('#similar').innerHTML = similar.map(f => `
    <a class="food-card" href="food-details.html?id=${f.id}">
      <div class="food-emoji" aria-hidden="true">${f.emoji}</div>
      <h3>${escapeHtml(f.name)}</h3>
      <div class="food-kcal">${f.calories} <small>kcal / ${escapeHtml(f.serving)}</small></div>
    </a>`).join('');
  $('#similarWrap').hidden = similar.length === 0;
}

function initFoodDetails() {
  if (!$('#detail')) return;
  detailFood = findFood(getFoodIdFromUrl());
  if (!detailFood) {
    $('#detail').innerHTML = `<div class="empty"><h3>Food not found</h3><p>That food is not in the database.</p><a class="btn btn-primary" style="margin-top:14px" href="foods.html">Browse all foods</a></div>`;
    $('#similarWrap').hidden = true;
    return;
  }
  renderDetail();
  renderSimilar();
}

document.addEventListener('DOMContentLoaded', initFoodDetails);
