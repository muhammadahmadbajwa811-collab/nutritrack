/* ==========================================================
   activity.js — step counter (manual + optional live sensor),
   exercise log with MET calories, workout stopwatch
   ========================================================== */
'use strict';

let actUser = null;
let actData = null;
const ACT_TODAY = todayKey();

function reloadActivity() {
  actUser = getCurrentUser();
  actData = getUserData();
}

/* ======================= STEPS ======================= */

function renderSteps() {
  const goal = actUser.stepGoal || 10000;
  const steps = actData.steps[ACT_TODAY] || 0;
  const ring = $('#stepRing');
  ring.style.setProperty('--p', percentOf(steps, goal));
  ring.classList.toggle('green', steps >= goal);
  $('#stepCount').textContent = formatNumber(steps);
  $('#stepGoalText').textContent = `of ${formatNumber(goal)} steps`;
  $('#stepKm').textContent = round1(stepsToKm(steps, actUser.height));
  $('#stepKcal').textContent = formatNumber(stepsToCalories(steps, actUser.weight));
  $('#stepStreak').textContent = calculateStreak(goal);
  renderStepChart(goal);
}

/** Number of consecutive days (ending today or yesterday) with steps >= goal. */
function calculateStreak(goal) {
  let streak = 0;
  let day = ACT_TODAY;
  if ((actData.steps[day] || 0) < goal) day = shiftDate(day, -1);   // today may still be in progress
  while ((actData.steps[day] || 0) >= goal) {
    streak++;
    day = shiftDate(day, -1);
  }
  return streak;
}

function renderStepChart(goal) {
  const days = lastNDays(7);
  const values = days.map(d => actData.steps[d] || 0);
  const max = Math.max(goal, ...values) * 1.1;
  $('#stepChart').innerHTML = days.map((d, i) => `
    <div class="chart-col">
      <div class="chart-box">
        <div class="chart-bar${values[i] >= goal ? ' hit' : ''}${d === ACT_TODAY ? ' today' : ''}" style="height:${(values[i] / max) * 100}%" title="${formatDateLong(d)}: ${formatNumber(values[i])} steps"></div>
      </div>
      <span class="chart-label">${weekdayShort(d)}</span>
      <span class="chart-val">${values[i] ? formatNumber(values[i]) : '-'}</span>
    </div>`).join('');
  $('#stepGoalLine').style.bottom = `${44 + (goal / max) * 176}px`;
}

/** Add (or subtract) steps for today and save. */
function changeSteps(amount) {
  const goal = actUser.stepGoal || 10000;
  const before = actData.steps[ACT_TODAY] || 0;
  const after = Math.max(0, before + amount);
  actData.steps[ACT_TODAY] = after;
  saveUserData(actData);
  renderSteps();
  if (before < goal && after >= goal) showToast('Step goal reached. Great job! 🎉');
}

function setExactSteps(event) {
  event.preventDefault();
  const input = $('#exactSteps');
  if (!validateRange(input, 'Steps', 0, 100000)) return;
  actData.steps[ACT_TODAY] = Math.round(Number(input.value));
  saveUserData(actData);
  input.value = '';
  setFieldError(input, '');
  input.classList.remove('valid');
  renderSteps();
  showToast('Steps updated.');
}

/* ---- Optional live tracking with the phone's motion sensor ---- */

const motion = { on: false, lastStepTime: 0, pending: 0 };
const flushLiveSteps = debounce(() => {
  if (motion.pending > 0) {
    changeSteps(motion.pending);
    motion.pending = 0;
  }
}, 1500);

/** Count a step each time acceleration spikes (simple peak detection). */
function onDeviceMotion(event) {
  const a = event.accelerationIncludingGravity;
  if (!a || a.x === null) return;
  const magnitude = Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z);
  const now = Date.now();
  if (magnitude > 12.5 && now - motion.lastStepTime > 350) {
    motion.lastStepTime = now;
    motion.pending++;
    flushLiveSteps();
  }
}

async function toggleLiveTracking() {
  const btn = $('#liveToggle');
  if (motion.on) {
    window.removeEventListener('devicemotion', onDeviceMotion);
    motion.on = false;
    btn.textContent = 'Start live tracking';
    $('#liveStatus').textContent = 'Live tracking is off.';
    return;
  }
  if (!('DeviceMotionEvent' in window)) {
    showToast('Live tracking needs a phone with a motion sensor. Use manual entry instead.', 'warning', 5000);
    return;
  }
  try {
    // iOS asks for permission first
    if (typeof DeviceMotionEvent.requestPermission === 'function') {
      const result = await DeviceMotionEvent.requestPermission();
      if (result !== 'granted') {
        showToast('Motion access was denied. Use manual entry instead.', 'warning');
        return;
      }
    }
    window.addEventListener('devicemotion', onDeviceMotion);
    motion.on = true;
    btn.textContent = 'Stop live tracking';
    $('#liveStatus').textContent = 'Live tracking is on. Keep this page open while you walk.';
  } catch (err) {
    console.error(err);
    showToast('Could not start live tracking on this device.', 'error');
  }
}

/* ======================= EXERCISE ======================= */

function renderExercises() {
  const list = actData.activities[ACT_TODAY] || [];
  const total = list.reduce((sum, a) => sum + a.calories, 0);
  $('#exerciseTotal').textContent = `${formatNumber(total)} kcal burned from workouts today`;
  $('#exerciseList').innerHTML = list.length
    ? list.map(a => `
      <li class="entry" style="grid-template-columns:1fr auto auto">
        <div><div class="entry-name">${escapeHtml(a.label)}</div><div class="entry-sub">${a.minutes} minutes</div></div>
        <span class="entry-kcal">${formatNumber(a.calories)} kcal</span>
        <button class="icon-btn" type="button" data-delete-activity="${a.id}" aria-label="Remove ${escapeHtml(a.label)}">🗑️</button>
      </li>`).join('')
    : '<li class="empty"><h3>No workouts today</h3><p>Log one below or time a session with the stopwatch.</p></li>';
}

/** Save a workout (used by the form and by the stopwatch). */
function logExercise(typeKey, minutes) {
  const type = EXERCISES.find(e => e.key === typeKey);
  if (!type) return false;
  actData.activities[ACT_TODAY] = actData.activities[ACT_TODAY] || [];
  actData.activities[ACT_TODAY].push({
    id: Date.now().toString(36), type: type.key, label: type.label, minutes,
    calories: exerciseCalories(type.met, actUser.weight, minutes)
  });
  saveUserData(actData);
  renderExercises();
  return true;
}

function handleExerciseSubmit(event) {
  event.preventDefault();
  const minutes = $('#exMinutes');
  if (!validateRange(minutes, 'Minutes', 1, 600)) return;
  if (logExercise($('#exType').value, Math.round(Number(minutes.value)))) {
    showToast('Workout logged.');
    minutes.value = '';
    minutes.classList.remove('valid');
  }
}

function deleteExercise(id) {
  actData.activities[ACT_TODAY] = (actData.activities[ACT_TODAY] || []).filter(a => a.id !== id);
  saveUserData(actData);
  renderExercises();
}

/* ======================= STOPWATCH ======================= */

const watch = { running: false, startedAt: 0, elapsed: 0, timer: null };

function formatClock(ms) {
  const total = Math.floor(ms / 1000);
  const h = String(Math.floor(total / 3600)).padStart(2, '0');
  const m = String(Math.floor((total % 3600) / 60)).padStart(2, '0');
  const s = String(total % 60).padStart(2, '0');
  return `${h}:${m}:${s}`;
}

function tickWatch() {
  const ms = watch.elapsed + (watch.running ? Date.now() - watch.startedAt : 0);
  $('#stopwatch').textContent = formatClock(ms);
}

function toggleWatch() {
  if (watch.running) {
    watch.elapsed += Date.now() - watch.startedAt;
    watch.running = false;
    clearInterval(watch.timer);
    $('#watchToggle').textContent = 'Resume';
  } else {
    watch.startedAt = Date.now();
    watch.running = true;
    watch.timer = setInterval(tickWatch, 250);
    $('#watchToggle').textContent = 'Pause';
  }
  tickWatch();
}

function resetWatch() {
  clearInterval(watch.timer);
  Object.assign(watch, { running: false, startedAt: 0, elapsed: 0 });
  $('#watchToggle').textContent = 'Start';
  tickWatch();
}

/** Turn the timed session into a logged workout. */
function logWatchSession() {
  const ms = watch.elapsed + (watch.running ? Date.now() - watch.startedAt : 0);
  const minutes = Math.round(ms / 60000);
  if (minutes < 1) {
    showToast('Time at least one minute before logging.', 'warning');
    return;
  }
  logExercise($('#exType').value, minutes);
  showToast(`${minutes}-minute workout logged.`);
  resetWatch();
}

/* ======================= START-UP ======================= */

function initActivity() {
  if (!getCurrentUser() || !$('#stepRing')) return;
  reloadActivity();
  renderSteps();
  renderExercises();

  $$('[data-steps]').forEach(btn => btn.addEventListener('click', () => changeSteps(Number(btn.dataset.steps))));
  $('#exactForm').addEventListener('submit', setExactSteps);
  $('#liveToggle').addEventListener('click', toggleLiveTracking);

  $('#exType').innerHTML = EXERCISES.map(e => `<option value="${e.key}">${e.label}</option>`).join('');
  $('#exerciseForm').addEventListener('submit', handleExerciseSubmit);
  $('#exerciseList').addEventListener('click', event => {
    const del = event.target.closest('[data-delete-activity]');
    if (del) deleteExercise(del.dataset.deleteActivity);
  });

  $('#watchToggle').addEventListener('click', toggleWatch);
  $('#watchReset').addEventListener('click', resetWatch);
  $('#watchLog').addEventListener('click', logWatchSession);
}

document.addEventListener('DOMContentLoaded', initActivity);
