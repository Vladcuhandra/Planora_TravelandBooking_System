// static/js/ui.js
(() => {
  function initAll() {
    document.querySelectorAll('.editable-text').forEach(span => {
      span.onclick = () => editField(span);
    });

    document.querySelectorAll('.editable-input').forEach(input => {
      input.onblur = () => saveField(input);

      // allow saving by Enter for text inputs
      input.onkeydown = (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          input.blur();
        }
      };
    });
  }

  function editField(span) {
    const input = span.nextElementSibling;
    if (!input) return;

    span.classList.add('d-none');
    input.classList.remove('d-none');

    input.focus();
  }

  function saveField(input) {
    const span = input.previousElementSibling;
    if (!span) return;

    if (input.type === 'password') {
      span.textContent = '••••••••';
    } else if (input.tagName === 'SELECT') {
      const opt = input.options[input.selectedIndex];
      span.textContent = opt ? opt.text : '';
    } else {
      span.textContent = input.value;
    }

    input.classList.add('d-none');
    span.classList.remove('d-none');
  }

  document.addEventListener("DOMContentLoaded", initAll);

  // Expose a method to refresh the UI (e.g., after dynamic content changes)
  window.PlanoraUI = { refresh: initAll };
})();
