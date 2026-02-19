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

    // 4) Highlight current page in sidebar (mini + drawer)
    highlightActiveNav();
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

  // Adds .active to .p-nav-link matching current path
  function highlightActiveNav() {
    const links = document.querySelectorAll(".p-nav-link");
    if (!links.length) return;

    const normalize = (p) => (p || "").replace(/\/+$/, "") || "/";
    const currentPath = normalize(window.location.pathname);

    links.forEach((a) => {
      // some anchors may not have href (or can be '#')
      const rawHref = a.getAttribute("href");
      if (!rawHref || rawHref === "#" || rawHref.startsWith("javascript:")) return;

      let linkPath = "";
      try {
        linkPath = normalize(new URL(rawHref, window.location.origin).pathname);
      } catch (e) {
        // ignore malformed href
        return;
      }

      // exact match OR subpath match (e.g., /bookings/123 should activate /bookings)
      const isActive =
        linkPath !== "/" &&
        (currentPath === linkPath || currentPath.startsWith(linkPath + "/"));

      // Special case: if you ever have a real "/" nav item
      const isHomeActive = linkPath === "/" && currentPath === "/";

      if (isActive || isHomeActive) a.classList.add("active");
      else a.classList.remove("active");
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
})();
