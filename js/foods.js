/* ==========================================================
   foods.js — food database: search, filter, sort, favorites,
   custom foods and add-to-diary
   ========================================================== */
'use strict';

const foodState = { query: '', category: 'All', sort: 'name', favoritesOnly: false };

function getFavorites() { return getUserData().favorites; }

/** Apply search, category, favorites and sort to the full list. */
function getFilteredFoods() {
  const favorites = getFavorites();
  const query = foodState.query.trim().toLowerCase();
  let list = getAllFoods().filter(f => {
    const matchesQuery = !query || f.name.toLowerCase().includes(query) || f.category.toLowerCase().includes(query);
    const matchesCategory = foodState.category === 'All' || f.category === foodState.category;
    const matchesFavorite = !foodState.favoritesOnly || favorites.includes(f.id);
    return matchesQuery && matchesCategory && matchesFavorite;
  });
  const sorters = {
    name: (a, b) => a.name.localeCompare(b.name),
    'cal-asc': (a, b) => a.calories - b.calories,
    'cal-desc': (a, b) => b.calories - a.calories,
    'protein-desc': (a, b) => b.protein - a.protein
  };
  list = list.slice().sort(sorters[foodState.sort] || sorters.name);
  return list;
}

function createFoodCard(food, isFavorite) {
  return `
    <article class="food-card">
      <div class="food-top">
        <div class="food-emoji" aria-hidden="true">${food.emoji}</div>
        <button class="fav-btn" type="button" data-fav="${food.id}" aria-pressed="${isFavorite}" aria-label="${isFavorite ? 'Remove from' : 'Add to'} favorites">${isFavorite ? '❤️' : '🤍'}</button>
      </div>
      <div><span class="badge">${escapeHtml(food.category)}</span></div>
      <h3>${escapeHtml(food.name)}</h3>
      <div class="food-kcal">${food.calories} <small>kcal / ${escapeHtml(food.serving)}</small></div>
      <div class="pills">
        <span class="pill p">P ${food.protein} g</span><span class="pill c">C ${food.carbs} g</span><span class="pill f">F ${food.fat} g</span>
      </div>
      <div class="food-actions">
        <a class="btn btn-outline btn-sm" href="food-details.html?id=${food.id}">Details</a>
        <button class="btn btn-primary btn-sm" type="button" data-add="${food.id}">Add</button>
      </div>
    </article>`;
}

function renderFoods() {
  const list = getFilteredFoods();
  const favorites = getFavorites();
  $('#resultCount').textContent = `Showing ${list.length} of ${getAllFoods().length} foods`;
  $('#foodGrid').innerHTML = list.map(f => createFoodCard(f, favorites.includes(f.id))).join('');
  $('#foodEmpty').hidden = list.length > 0;
}

function renderCategoryChips() {
  const cats = ['All', ...CATEGORIES, ...(getUserData().customFoods.length ? ['My foods'] : [])];
  $('#categoryChips').innerHTML = cats.map(c =>
    `<button class="chip${c === foodState.category ? ' active' : ''}" type="button" data-cat="${c}">${c}</button>`
  ).join('');
}

/** Heart or un-heart a food (needs an account). */
function toggleFavorite(id) {
  if (!getCurrentUser()) {
    showToast('Log in to save favorites.', 'warning');
    return;
  }
  const data = getUserData();
  const i = data.favorites.indexOf(id);
  if (i >= 0) data.favorites.splice(i, 1); else data.favorites.push(id);
  saveUserData(data);
  showToast(i >= 0 ? 'Removed from favorites.' : 'Saved to favorites.', 'info', 2000);
  renderFoods();
}

/* ---------- Custom food dialog ---------- */

function openCustomFoodModal() {
  if (!getCurrentUser()) {
    showToast('Log in to add your own foods.', 'warning');
    return;
  }
  openModal({
    title: 'Add your own food',
    confirmText: 'Save food',
    body: `
      <form id="customFoodForm" novalidate>
        <div class="form-group"><label for="cfName">Food name</label><input id="cfName" class="input" maxlength="40" placeholder="e.g. Mom's daal"><span class="field-error"></span></div>
        <div class="form-row">
          <div class="form-group"><label for="cfServing">Serving size</label><input id="cfServing" class="input" maxlength="20" placeholder="1 bowl"><span class="field-error"></span></div>
          <div class="form-group"><label for="cfCal">Calories (kcal)</label><input id="cfCal" class="input" type="number" min="0" max="3000"><span class="field-error"></span></div>
        </div>
        <div class="form-row form-row-3">
          <div class="form-group"><label for="cfP">Protein (g)</label><input id="cfP" class="input" type="number" min="0" max="300" value="0"><span class="field-error"></span></div>
          <div class="form-group"><label for="cfC">Carbs (g)</label><input id="cfC" class="input" type="number" min="0" max="500" value="0"><span class="field-error"></span></div>
          <div class="form-group"><label for="cfF">Fat (g)</label><input id="cfF" class="input" type="number" min="0" max="300" value="0"><span class="field-error"></span></div>
        </div>
      </form>`,
    onConfirm: ov => {
      const fields = ['#cfName', '#cfServing', '#cfCal', '#cfP', '#cfC', '#cfF'].map(s => $(s, ov));
      const [name, serving, cal, p, c, f] = fields;
      const ok = [
        validateRequired(name, 'Name', 2), validateRequired(serving, 'Serving size'),
        validateRange(cal, 'Calories', 0, 3000), validateRange(p, 'Protein', 0, 300),
        validateRange(c, 'Carbs', 0, 500), validateRange(f, 'Fat', 0, 300)
      ].every(Boolean);
      if (!ok) return false;
      const data = getUserData();
      data.customFoods.push({
        id: 'c' + Date.now(), name: name.value.trim(), category: 'My foods', emoji: '🍴',
        serving: serving.value.trim(), calories: Number(cal.value),
        protein: Number(p.value), carbs: Number(c.value), fat: Number(f.value)
      });
      saveUserData(data);
      showToast(`${name.value.trim()} added to your foods.`);
      renderCategoryChips();
      renderFoods();
      return true;
    }
  });
}

/* ---------- Events ---------- */

function initFoods() {
  if (!$('#foodGrid')) return;
  renderCategoryChips();
  renderFoods();

  $('#foodSearch').addEventListener('input', debounce(event => {
    foodState.query = event.target.value;
    renderFoods();
  }, 200));
  $('#foodSort').addEventListener('change', event => { foodState.sort = event.target.value; renderFoods(); });
  $('#favOnly').addEventListener('click', event => {
    foodState.favoritesOnly = !foodState.favoritesOnly;
    event.currentTarget.classList.toggle('active', foodState.favoritesOnly);
    event.currentTarget.setAttribute('aria-pressed', String(foodState.favoritesOnly));
    renderFoods();
  });
  $('#categoryChips').addEventListener('click', event => {
    const chip = event.target.closest('.chip');
    if (!chip) return;
    foodState.category = chip.dataset.cat;
    renderCategoryChips();
    renderFoods();
  });
  $('#foodGrid').addEventListener('click', event => {
    const fav = event.target.closest('[data-fav]');
    const add = event.target.closest('[data-add]');
    if (fav) {
      const raw = fav.dataset.fav;
      toggleFavorite(Number.isNaN(Number(raw)) ? raw : Number(raw));
    }
    if (add) {
      const raw = add.dataset.add;
      openAddFoodModal({ foodId: raw });
    }
  });
  $('#addCustomFood').addEventListener('click', openCustomFoodModal);
  $('#clearFilters').addEventListener('click', () => {
    Object.assign(foodState, { query: '', category: 'All', sort: 'name', favoritesOnly: false });
    $('#foodSearch').value = '';
    $('#foodSort').value = 'name';
    $('#favOnly').classList.remove('active');
    renderCategoryChips();
    renderFoods();
  });
}

document.addEventListener('DOMContentLoaded', initFoods);
