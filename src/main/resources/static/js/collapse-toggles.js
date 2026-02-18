// static/js/collapse-toggles.js
(() => {
  const SELECTOR = '[data-collapse-toggle]';

  function setBtnText(btn, isOpen) {
    const showText = btn.getAttribute('data-text-show') || 'Show';
    const hideText = btn.getAttribute('data-text-hide') || 'Hide';

    btn.textContent = isOpen ? hideText : showText;
    btn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
  }

  function syncAll() {
    document.querySelectorAll(SELECTOR).forEach((btn) => {
      const targetId = btn.getAttribute('data-collapse-toggle');
      if (!targetId) return;

      const collapseEl = document.getElementById(targetId);
      if (!collapseEl) return;

      setBtnText(btn, collapseEl.classList.contains('show'));
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    if (!window.bootstrap) return;
    syncAll();
  });

  // Bootstrap events
  document.addEventListener('shown.bs.collapse', (e) => {
    const id = e.target?.id;
    if (!id) return;

    document.querySelectorAll(`${SELECTOR}[data-collapse-toggle="${CSS.escape(id)}"]`)
      .forEach((btn) => setBtnText(btn, true));
  });

  document.addEventListener('hidden.bs.collapse', (e) => {
    const id = e.target?.id;
    if (!id) return;

    document.querySelectorAll(`${SELECTOR}[data-collapse-toggle="${CSS.escape(id)}"]`)
      .forEach((btn) => setBtnText(btn, false));
  });
})();
