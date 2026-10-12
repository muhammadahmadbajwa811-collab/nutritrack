/* ==========================================================
   home.js — landing page: interactive plate demo + tips slider
   ========================================================== */
'use strict';

/* ---------- Interactive plate demo ---------- */

const DEMO_GOAL = 2000;
const DEMO_FOODS = [
  { emoji: '🥚', name: 'Boiled egg', kcal: 78 },
  { emoji: '🫓', name: 'Roti', kcal: 104 },
  { emoji: '🍛', name: 'Chicken biryani', kcal: 490 },
  { emoji: '🍎', name: 'Apple', kcal: 95 },
  { emoji: '☕', name: 'Milk tea', kcal: 90 },
  { emoji: '🍕', name: 'Pizza slice', kcal: 285 }
];

function initPlateDemo() {
  const ring = $('#demoRing');
  if (!ring) return;
  const left = $('#demoLeft');
  const label = $('#demoLabel');
  const log = $('#demoLog');
  const chips = $('#demoChips');
  let eaten = 0;
  let items = [];

  /** Redraw the ring, the number and the list from current state. */
  function render() {
    const remaining = DEMO_GOAL - eaten;
    ring.style.setProperty('--p', Math.min(eaten / DEMO_GOAL * 100, 100));
    ring.classList.toggle('over', remaining < 0);
    left.textContent = formatNumber(Math.abs(remaining));
    label.textContent = remaining < 0 ? 'kcal over' : 'kcal left';
    log.innerHTML = items.length
      ? items.map(i => `<li><span>${i.emoji} ${escapeHtml(i.name)}</span><b>${i.kcal}</b></li>`).join('')
      : '<li class="muted">Tap a food below to add it.</li>';
    log.scrollTop = log.scrollHeight;
  }

  function addDemoFood(food) {
    eaten += food.kcal;
    items.push(food);
    render();
    ring.classList.remove('pop');
    void ring.offsetWidth;                // restart the animation
    ring.classList.add('pop');
  }

  chips.innerHTML = DEMO_FOODS.map((f, i) =>
    `<button class="chip" type="button" data-index="${i}">${f.emoji} ${escapeHtml(f.name)} · ${f.kcal}</button>`
  ).join('');
  chips.addEventListener('click', event => {
    const chip = event.target.closest('.chip');
    if (chip) addDemoFood(DEMO_FOODS[Number(chip.dataset.index)]);
  });
  $('#demoReset').addEventListener('click', () => { eaten = 0; items = []; render(); });
  render();
}

/* ---------- Tips slider ---------- */

function initSlider() {
  const slider = $('#tipSlider');
  if (!slider) return;
  const track = $('.slides', slider);
  const slides = $$('.slide', slider);
  const dotsBox = $('.slider-dots', slider);
  let index = 0;
  let timer = null;

  function goTo(i) {
    index = (i + slides.length) % slides.length;
    track.style.transform = `translateX(-${index * 100}%)`;
    $$('.dot', dotsBox).forEach((d, n) => {
      d.classList.toggle('active', n === index);
      d.setAttribute('aria-current', String(n === index));
    });
  }

  function startAutoplay() {
    stopAutoplay();
    timer = setInterval(() => goTo(index + 1), 6000);
  }
  function stopAutoplay() { clearInterval(timer); }

  dotsBox.innerHTML = slides.map((_, n) => `<button class="dot" type="button" aria-label="Show tip ${n + 1}"></button>`).join('');
  $$('.dot', dotsBox).forEach((dot, n) => dot.addEventListener('click', () => { goTo(n); startAutoplay(); }));
  $('.slider-prev', slider).addEventListener('click', () => { goTo(index - 1); startAutoplay(); });
  $('.slider-next', slider).addEventListener('click', () => { goTo(index + 1); startAutoplay(); });
  slider.addEventListener('mouseenter', stopAutoplay);
  slider.addEventListener('mouseleave', startAutoplay);
  goTo(0);
  startAutoplay();
}

document.addEventListener('DOMContentLoaded', () => {
  const foodCounter = $('[data-count-foods]');
  if (foodCounter) foodCounter.dataset.count = FOODS.length;
  initPlateDemo();
  initSlider();
});
