// static/js/bookings.js
(() => {
  function normalize(v) {
    return (v || "").toUpperCase().trim();
  }

  function setBlockVisible(el, visible) {
    if (!el) return;
    el.style.display = visible ? "" : "none";
  }

  function clearHiddenSelect(blockEl, selectName) {
    if (!blockEl) return;
    if (blockEl.style.display !== "none") return;

    // in Thymeleaf th:field generates names like bookingDto.transportId,
    // search select by name  (contains)
    const select =
      blockEl.querySelector(`select[name="${selectName}"]`) ||
      blockEl.querySelector(`select[name$=".${selectName}"]`) ||
      blockEl.querySelector(`select[name*="${selectName}"]`);

    if (select) select.value = "";
  }

  function applyVisibility(bookingTypeEl, transportBlock, accommodationBlock) {
    const v = normalize(bookingTypeEl?.value);

    const isTransport = v === "TRANSPORT";
    const isAccommodation = v === "ACCOMMODATION";

    if (isTransport) {
      setBlockVisible(transportBlock, true);
      setBlockVisible(accommodationBlock, false);
      clearHiddenSelect(accommodationBlock, "accommodationId");
    } else if (isAccommodation) {
      setBlockVisible(transportBlock, false);
      setBlockVisible(accommodationBlock, true);
      clearHiddenSelect(transportBlock, "transportId");
    } else {
      setBlockVisible(transportBlock, true);
      setBlockVisible(accommodationBlock, true);
    }

    // in case there is ui plugins/inits
    if (window.PlanoraUI && typeof window.PlanoraUI.refresh === "function") {
      window.PlanoraUI.refresh();
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    const bookingType = document.getElementById("bookingTypeSelect");
    const transportBlock = document.getElementById("transportBlock");
    const accommodationBlock = document.getElementById("accommodationBlock");

    // if page/form is other - return
    if (!bookingType || !transportBlock || !accommodationBlock) return;

    bookingType.addEventListener("change", () =>
      applyVisibility(bookingType, transportBlock, accommodationBlock)
    );

    applyVisibility(bookingType, transportBlock, accommodationBlock);
  });
})();