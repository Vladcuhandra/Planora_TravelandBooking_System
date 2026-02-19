// static/js/ui.js
(() => {
  function initAll() {
    // 1) Inline-edit: click to edit
    document.querySelectorAll(".editable-text").forEach((span) => {
      if (span.dataset.planoraBound) return;
      span.addEventListener("click", () => editField(span));
      span.dataset.planoraBound = "1";
    });

    // 2) Inline-edit: blur / enter to save
    document.querySelectorAll(".editable-input").forEach((input) => {
      if (input.dataset.planoraBound) return;

      input.addEventListener("blur", () => saveField(input));
      input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          input.blur();
        }
      });

      input.dataset.planoraBound = "1";
    });

    // 3) Sidebar: close AND navigate (works on mobile + desktop)
    bindSidebarNavigation();
  }

  function bindSidebarNavigation() {
    const sidebarEl = document.getElementById("sidebar");
    if (!sidebarEl || !window.bootstrap) return;

    // bind once
    if (sidebarEl.dataset.planoraNavBound) return;
    sidebarEl.dataset.planoraNavBound = "1";

    const offcanvas = bootstrap.Offcanvas.getOrCreateInstance(sidebarEl);

    sidebarEl.addEventListener("click", (e) => {
      const link = e.target.closest("a");
      if (!link) return;

      const href = link.getAttribute("href");
      if (!href || href === "#" || href.startsWith("javascript:")) return;

      if (link.target && link.target !== "_self") return;

      e.preventDefault();

      const go = () => (window.location.href = href);

      const isShown = sidebarEl.classList.contains("show");
      if (!isShown) {
        go();
        return;
      }

      const onHidden = () => {
        sidebarEl.removeEventListener("hidden.bs.offcanvas", onHidden);
        go();
      };

      sidebarEl.addEventListener("hidden.bs.offcanvas", onHidden);
      offcanvas.hide();
    });
  }

  function editField(span) {
    const input = span.nextElementSibling;
    if (!input) return;

    span.classList.add("d-none");
    input.classList.remove("d-none");
    input.focus();
  }

  function saveField(input) {
    const span = input.previousElementSibling;
    if (!span) return;

    if (input.type === "password") {
      span.textContent = "••••••••";
    } else if (input.tagName === "SELECT") {
      const opt = input.options[input.selectedIndex];
      span.textContent = opt ? opt.text : "";
    } else {
      span.textContent = input.value;
    }

    input.classList.add("d-none");
    span.classList.remove("d-none");
  }

  document.addEventListener("DOMContentLoaded", initAll);

  // Expose refresh for dynamic UI updates
  window.PlanoraUI = { refresh: initAll };

   /*// Topbar: visible only at the very top of the page
     (function initTopbarOnlyAtTop() {
       const topbar = document.querySelector(".p-topbar");
       if (!topbar) return;

       const SHOW_AT_TOP_PX = 25; // tolerance for "top"

       function update() {
         if (window.scrollY <= SHOW_AT_TOP_PX) {
           topbar.classList.remove("is-hidden");
         } else {
           topbar.classList.add("is-hidden");
         }
       }*/

       // Initial state
       update();

       window.addEventListener("scroll", update, { passive: true });
       window.addEventListener("resize", update);
     })();

})();
