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
  }

  // DOM ready
  document.addEventListener("DOMContentLoaded", initAll);

  window.PlanoraUI = { refresh: initAll }; // Expose a method to refresh the UI (e.g., after dynamic content changes)
})();
