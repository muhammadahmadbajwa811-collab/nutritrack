/* ==========================================================
   app.js — shared helpers loaded on every page:
   theme, navbar, toasts, modals, validation, accordion,
   scroll effects, auth state and the "add food" dialog.
   ========================================================== */
'use strict';

/* ---------- Tiny DOM helpers ---------- */
const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

/** URL of an inner page, depending on whether we are on index.html or in /pages. */
function pageUrl(name) {
  return document.body.dataset.depth === '0' ? `pages/${name}.html` : `${name}.html`;
}
function homeUrl() {
  return document.body.dataset.depth === '0' ? 'index.html' : '../index.html';
}

/** Escape user-provided text before putting it in innerHTML. */
function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, ch => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]
  ));
}

function formatNumber(n) { return Math.round(n).toLocaleString('en-US'); }
function round1(n) { return Math.round(n * 10) / 10; }

/** Run fn only after the user stops typing for `wait` ms. */
function debounce(fn, wait = 250) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), wait);
  };
}

/* ---------- Theme (dark / light) ---------- */

function getTheme() {
  return document.documentElement.getAttribute('data-theme') || 'light';
}

/** Apply a theme, update the toggle button and (optionally) remember it. */
function applyTheme(theme, save = true) {
  document.documentElement.setAttribute('data-theme', theme);
  if (save) {
    try { localStorage.setItem('nt_theme', theme); } catch (err) { console.error(err); }
  }
  const btn = $('#themeToggle');
  if (btn) {
    btn.textContent = theme === 'dark' ? '☀️' : '🌙';
    btn.setAttribute('aria-label', theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
  }
}

function toggleTheme() { applyTheme(getTheme() === 'dark' ? 'light' : 'dark'); }

function initTheme() {
  applyTheme(getTheme(), false);
  const btn = $('#themeToggle');
  if (btn) btn.addEventListener('click', toggleTheme);
}

/* ---------- Navbar, scroll-to-top, reveal, counters ---------- */

function initNavbar() {
  const toggle = $('.nav-toggle');
  const menu = $('#navMenu');
  if (toggle && menu) {
    toggle.addEventListener('click', () => {
      const open = menu.classList.toggle('open');
      toggle.setAttribute('aria-expanded', String(open));
    });
    $$('a', menu).forEach(link => link.addEventListener('click', () => {
      menu.classList.remove('open');
      toggle.setAttribute('aria-expanded', 'false');
    }));
  }
  const current = document.body.dataset.page;
  $$('.nav-links a').forEach(link => {
    if (link.dataset.page === current) {
      link.classList.add('active');
      link.setAttribute('aria-current', 'page');
    }
  });
  const header = $('.site-header');
  const onScroll = () => header.classList.toggle('scrolled', window.scrollY > 8);
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
}

function initScrollTop() {
  const btn = $('#scrollTop');
  if (!btn) return;
  window.addEventListener('scroll', () => btn.classList.toggle('show', window.scrollY > 400), { passive: true });
  btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
}

/** Fade elements in as they scroll into view. */
function initReveal() {
  const items = $$('.reveal');
  if (!('IntersectionObserver' in window)) {
    items.forEach(el => el.classList.add('visible'));
    return;
  }
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.12 });
  items.forEach(el => observer.observe(el));
}

/** Count a number up from 0 to data-count. */
function animateCounter(el) {
  const target = Number(el.dataset.count) || 0;
  const suffix = el.dataset.suffix || '';
  const start = performance.now();
  const duration = 1200;
  const step = now => {
    const progress = Math.min((now - start) / duration, 1);
    el.textContent = Math.floor(target * progress).toLocaleString('en-US') + suffix;
    if (progress < 1) requestAnimationFrame(step);
  };
  requestAnimationFrame(step);
}

function initCounters() {
  const counters = $$('[data-count]');
  if (!counters.length) return;
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        animateCounter(entry.target);
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.6 });
  counters.forEach(el => observer.observe(el));
}

/* ---------- Toast notifications ---------- */

function showToast(message, type = 'success', duration = 3500) {
  let box = $('#toastContainer');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toastContainer';
    box.className = 'toast-container';
    document.body.appendChild(box);
  }
  const icons = { success: '✅', error: '⚠️', warning: '🔔', info: 'ℹ️' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.setAttribute('role', type === 'error' ? 'alert' : 'status');
  toast.innerHTML = `<span aria-hidden="true">${icons[type] || ''}</span><p>${escapeHtml(message)}</p>`;
  box.appendChild(toast);
  setTimeout(() => {
    toast.classList.add('hide');
    toast.addEventListener('animationend', () => toast.remove(), { once: true });
  }, duration);
}

/* ---------- Modal dialog ---------- */

function escCloseModal(event) {
  if (event.key === 'Escape') closeModal();
}

/**
 * Open a modal. onConfirm(overlay) may return false to keep it open
 * (for example when validation fails).
 */
function openModal({ title, body, confirmText = 'Confirm', cancelText = 'Cancel', onConfirm = null, hideActions = false }) {
  closeModal();
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.id = 'modalOverlay';
  overlay.innerHTML = `
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
      <div class="modal-head">
        <h3 id="modalTitle">${escapeHtml(title)}</h3>
        <button class="icon-btn modal-close" type="button" aria-label="Close dialog">✕</button>
      </div>
      <div class="modal-body">${body}</div>
      ${hideActions ? '' : `<div class="modal-actions">
        <button class="btn btn-ghost" type="button" data-modal="cancel">${cancelText}</button>
        <button class="btn btn-primary" type="button" data-modal="confirm">${confirmText}</button>
      </div>`}
    </div>`;
  document.body.appendChild(overlay);
  requestAnimationFrame(() => overlay.classList.add('open'));

  overlay.addEventListener('click', event => {
    if (event.target === overlay || event.target.closest('.modal-close') || event.target.closest('[data-modal="cancel"]')) {
      closeModal();
    }
  });
  const confirmBtn = $('[data-modal="confirm"]', overlay);
  if (confirmBtn) {
    confirmBtn.addEventListener('click', () => {
      if (!onConfirm || onConfirm(overlay) !== false) closeModal();
    });
  }
  document.addEventListener('keydown', escCloseModal);
  const first = $('input, select, textarea', overlay) || confirmBtn;
  if (first) first.focus();
  return overlay;
}

function closeModal() {
  const overlay = $('#modalOverlay');
  if (!overlay) return;
  document.removeEventListener('keydown', escCloseModal);
  overlay.removeAttribute('id');          // so a new modal can be opened right away
  overlay.classList.remove('open');
  setTimeout(() => overlay.remove(), 300);
}

/** Simple yes/no confirmation dialog. */
function confirmAction(title, message, onYes, confirmText = 'Yes, continue') {
  openModal({ title, body: `<p class="muted">${escapeHtml(message)}</p>`, confirmText, onConfirm: () => { onYes(); } });
}

/* ---------- Form validation helpers ---------- */

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

function isValidEmail(value) { return EMAIL_PATTERN.test(String(value).trim()); }

/** Show or clear an error under a field. Returns true when the field is valid. */
function setFieldError(input, message) {
  const group = input.closest('.form-group');
  const slot = group ? $('.field-error', group) : null;
  input.classList.toggle('invalid', Boolean(message));
  input.classList.toggle('valid', !message && input.value.trim() !== '');
  input.setAttribute('aria-invalid', message ? 'true' : 'false');
  if (slot) slot.textContent = message || '';
  return !message;
}

function validateRequired(input, label, minLength = 1) {
  const value = input.value.trim();
  if (!value) return setFieldError(input, `${label} is required.`);
  if (value.length < minLength) return setFieldError(input, `${label} must be at least ${minLength} characters.`);
  return setFieldError(input, '');
}

function validateEmailField(input) {
  const value = input.value.trim();
  if (!value) return setFieldError(input, 'Email is required.');
  if (!isValidEmail(value)) return setFieldError(input, 'Enter a valid email, like name@example.com.');
  return setFieldError(input, '');
}

function validateRange(input, label, min, max) {
  const raw = input.value.trim();
  const value = Number(raw);
  if (raw === '' || Number.isNaN(value)) return setFieldError(input, `${label} is required.`);
  if (value < min || value > max) return setFieldError(input, `${label} must be between ${min} and ${max}.`);
  return setFieldError(input, '');
}

function validateSelect(input, label) {
  return input.value ? setFieldError(input, '') : setFieldError(input, `Choose ${label}.`);
}

/** Score a password from 0 (weak) to 4 (strong). */
function passwordStrength(password) {
  let score = 0;
  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return score;
}

/** Wire up show/hide buttons next to password fields. */
function initPasswordToggles() {
  $$('.toggle-pw').forEach(btn => btn.addEventListener('click', () => {
    const input = $('input', btn.parentElement);
    const show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    btn.textContent = show ? '🙈' : '👁️';
    btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
  }));
}

/** Live character counter: <textarea data-counter="#id" maxlength="500">. */
function initCharCounters() {
  $$('[data-counter]').forEach(field => {
    const output = $(field.dataset.counter);
    if (!output) return;
    const max = Number(field.getAttribute('maxlength')) || 500;
    const update = () => {
      output.textContent = `${field.value.length} / ${max}`;
      output.classList.toggle('danger', field.value.length >= max * 0.9);
    };
    field.addEventListener('input', update);
    update();
  });
}

/* ---------- Accordion ---------- */

function closeAccordionItem(item) {
  item.classList.remove('open');
  $('.accordion-panel', item).style.maxHeight = null;
  $('.accordion-btn', item).setAttribute('aria-expanded', 'false');
}

function initAccordion() {
  $$('.accordion-item').forEach(item => {
    const button = $('.accordion-btn', item);
    const panel = $('.accordion-panel', item);
    button.addEventListener('click', () => {
      const wasOpen = item.classList.contains('open');
      $$('.accordion-item.open').forEach(closeAccordionItem);
      if (!wasOpen) {
        item.classList.add('open');
        panel.style.maxHeight = panel.scrollHeight + 'px';
        button.setAttribute('aria-expanded', 'true');
      }
    });
  });
}

/* ---------- Tabs ---------- */

function initTabs() {
  $$('.tabs').forEach(group => {
    const tabs = $$('.tab', group);
    tabs.forEach(tab => tab.addEventListener('click', () => {
      tabs.forEach(t => {
        t.classList.toggle('active', t === tab);
        t.setAttribute('aria-selected', String(t === tab));
        $('#' + t.dataset.tab).classList.toggle('active', t === tab);
      });
    }));
  });
}

/* ---------- Auth state ---------- */

/** Show/hide elements marked data-auth="user" or data-auth="guest". */
function updateAuthUI() {
  const user = getCurrentUser();
  $$('[data-auth]').forEach(el => {
    el.hidden = el.dataset.auth === 'user' ? !user : Boolean(user);
  });
  $$('[data-user-name]').forEach(el => { el.textContent = user ? user.name.split(' ')[0] : ''; });
  document.body.classList.add('auth-ready');
}

/** Send visitors without an account away from protected pages. */
function requireAuth() {
  if (document.body.dataset.protected === 'true' && !getCurrentUser()) {
    window.location.replace(pageUrl('login'));
    return false;
  }
  return true;
}

function logout() {
  clearSession();
  showToast('You have been logged out.', 'info');
  setTimeout(() => { window.location.href = homeUrl(); }, 700);
}

/* ---------- "Add food" dialog (used on several pages) ---------- */

/** Pick a sensible default meal from the time of day. */
function guessMeal() {
  const hour = new Date().getHours();
  if (hour < 11) return 'breakfast';
  if (hour < 16) return 'lunch';
  if (hour < 21) return 'dinner';
  return 'snacks';
}

function mealOptionsHtml(selected) {
  return MEALS.map(m => `<option value="${m.key}" ${m.key === selected ? 'selected' : ''}>${m.emoji} ${m.label}</option>`).join('');
}

/**
 * Open the add-to-diary dialog.
 * opts: { foodId, date, meal, onAdded }
 */
function openAddFoodModal(opts = {}) {
  if (!getCurrentUser()) {
    showToast('Log in to add foods to your diary.', 'warning');
    return;
  }
  const date = opts.date || todayKey();
  const foodOptions = getAllFoods().map(f =>
    `<option value="${f.id}" ${String(f.id) === String(opts.foodId) ? 'selected' : ''}>${f.emoji} ${escapeHtml(f.name)} (${f.calories} kcal)</option>`
  ).join('');

  const overlay = openModal({
    title: 'Add to diary',
    confirmText: 'Add food',
    body: `
      <form id="addFoodForm" novalidate>
        <div class="form-group"><label for="afFood">Food</label><select id="afFood" class="select">${foodOptions}</select></div>
        <div class="form-row">
          <div class="form-group"><label for="afMeal">Meal</label><select id="afMeal" class="select">${mealOptionsHtml(opts.meal || guessMeal())}</select></div>
          <div class="form-group"><label for="afQty">Servings</label><input id="afQty" class="input" type="number" min="0.5" max="20" step="0.5" value="1"><span class="field-error"></span></div>
        </div>
        <p class="muted small" id="afPreview" aria-live="polite"></p>
      </form>`,
    onConfirm: ov => {
      const qtyInput = $('#afQty', ov);
      if (!validateRange(qtyInput, 'Servings', 0.5, 20)) return false;
      const foodId = $('#afFood', ov).value;
      const meal = $('#afMeal', ov).value;
      try {
        addMealEntry(foodId, meal, Number(qtyInput.value), date);
      } catch (err) {
        showToast('Could not add that food. Please try again.', 'error');
        return false;
      }
      const food = findFood(foodId);
      showToast(`${food.name} added to ${meal}.`);
      const user = getCurrentUser();
      if (getDayTotals(date).calories > user.calorieGoal) {
        showToast('You are over your calorie goal for this day.', 'warning');
      }
      if (typeof opts.onAdded === 'function') opts.onAdded();
      return true;
    }
  });

  const updatePreview = () => {
    const food = findFood($('#afFood', overlay).value);
    const qty = Number($('#afQty', overlay).value) || 0;
    if (food) $('#afPreview', overlay).textContent = `${food.serving} x ${qty} = ${formatNumber(food.calories * qty)} kcal`;
  };
  $('#afFood', overlay).addEventListener('change', updatePreview);
  $('#afQty', overlay).addEventListener('input', updatePreview);
  $('#addFoodForm', overlay).addEventListener('submit', event => event.preventDefault());
  updatePreview();
}

/* ---------- Start-up ---------- */

function initApp() {
  if (!requireAuth()) return;
  initTheme();
  initNavbar();
  updateAuthUI();
  const logoutBtn = $('#logoutBtn');
  if (logoutBtn) logoutBtn.addEventListener('click', logout);
  initScrollTop();
  initReveal();
  initCounters();
  initAccordion();
  initTabs();
  initPasswordToggles();
  initCharCounters();
  const year = $('#year');
  if (year) year.textContent = new Date().getFullYear();
}

document.addEventListener('DOMContentLoaded', initApp);
