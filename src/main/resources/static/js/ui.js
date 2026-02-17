// static/js/ui.js
(() => {
  const DEFAULT_LONG_LIST_THRESHOLD = 10;

  function shouldEnableSearch(selectEl) {
    // 1) Manual forcing for search in dropdown lists
    // <select data-search="true">  -> always search
    const forced = selectEl.dataset.search;
    if (forced === "true") return true;
    if (forced === "false") return false;

    const options = Array.from(selectEl.options || []);
    const meaningfulCount = options.filter(o => {
      const val = (o.value ?? "").trim();
      const text = (o.text ?? "").trim();
      return !o.disabled && (val !== "" || text !== "");
    }).length;

    const threshold = Number(selectEl.dataset.searchThreshold || DEFAULT_LONG_LIST_THRESHOLD);
    return meaningfulCount > threshold;
  }

  function initChoicesForSelect(selectEl) {
    if (!window.Choices) return;
    if (selectEl.dataset.choicesInitialized === "true") return;

    const enableSearch = shouldEnableSearch(selectEl);

    const instance = new Choices(selectEl, {
      searchEnabled: enableSearch,
      itemSelectText: "",
      shouldSort: false,
      placeholder: true,
      allowHTML: false,
      searchPlaceholderValue: "Search...",
      //to-do : Add noResultsText: "No results found"
    });

    selectEl.dataset.choicesInitialized = "true";
    selectEl._choicesInstance = instance;
  }

  function initAll() {
    document.querySelectorAll("select.form-select").forEach(initChoicesForSelect);
    document.querySelectorAll('.editable-text').forEach(span => {
      span.onclick = () => editField(span);
    });
    document.querySelectorAll('.editable-input').forEach(input => {
      input.onblur = () => saveField(input);
    });
  }

  function editField(span) {
    const input = span.nextElementSibling;
    span.classList.add('d-none');
    input.classList.remove('d-none');

    if (input.tagName === "SELECT" && input._choicesInstance) {
      const instance = input._choicesInstance;
      instance.input.focus();
    } else {
      input.focus();
    }
  }

  function saveField(input) {
    const span = input.previousElementSibling;

    if (input.type === 'password') {
      span.textContent = '••••••••';
    } else if (input.tagName === 'SELECT' && input._choicesInstance) {
      span.textContent = input.options[input.selectedIndex].text;
    } else {
      span.textContent = input.value;
    }

    input.classList.add('d-none');
    span.classList.remove('d-none');
  }

  // DOM ready
  document.addEventListener("DOMContentLoaded", initAll);

  window.PlanoraUI = { refresh: initAll }; // Expose a method to refresh the UI (e.g., after dynamic content changes)
})();
