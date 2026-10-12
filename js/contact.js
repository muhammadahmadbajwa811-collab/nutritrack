/* ==========================================================
   contact.js — contact form validation + storing messages
   ========================================================== */
'use strict';

function validateContactForm(form) {
  const f = form.elements;
  return [
    validateRequired(f.name, 'Name', 2),
    validateEmailField(f.email),
    validateSelect(f.subject, 'a subject'),
    validateRequired(f.message, 'Message', 10)
  ].every(Boolean);
}

/** Save the message in localStorage (there is no server in this project). */
function saveContactMessage(form) {
  const f = form.elements;
  const messages = readStorage(LS.messages, []);
  messages.push({
    id: Date.now(),
    name: f.name.value.trim(),
    email: f.email.value.trim(),
    subject: f.subject.value,
    message: f.message.value.trim(),
    sentAt: new Date().toISOString()
  });
  return writeStorage(LS.messages, messages);
}

function initContactForm() {
  const form = $('#contactForm');
  if (!form) return;

  // Validate each field as soon as the user leaves it
  $$('input, select, textarea', form).forEach(field => {
    field.addEventListener('blur', () => {
      if (field.name === 'email') validateEmailField(field);
      else if (field.name === 'message') validateRequired(field, 'Message', 10);
      else if (field.name === 'subject') validateSelect(field, 'a subject');
      else if (field.name === 'name') validateRequired(field, 'Name', 2);
    });
  });

  form.addEventListener('submit', event => {
    event.preventDefault();
    if (!validateContactForm(form)) {
      showToast('Please fix the highlighted fields.', 'error');
      return;
    }
    if (saveContactMessage(form)) {
      $('#contactSuccess').hidden = false;
      showToast('Message sent. We will reply within two days.');
      form.reset();
      $$('.valid, .invalid', form).forEach(el => el.classList.remove('valid', 'invalid'));
      initCharCounters();
    }
  });
}

document.addEventListener('DOMContentLoaded', initContactForm);
