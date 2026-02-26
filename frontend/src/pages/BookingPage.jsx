import React, { useEffect, useMemo, useState } from "react";
import { apiFetch } from "../api/http";
import Pagination from "../components/Pagination.jsx";

const BookingPage = () => {
    const [bookings, setBookings] = useState([]);
    const [trips, setTrips] = useState([]);
    const [transports, setTransports] = useState([]);
    const [accommodations, setAccommodations] = useState([]);

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [error, setError] = useState(null);
    const [openCreate, setOpenCreate] = useState(false);

    // Create form state
    const [createDto, setCreateDto] = useState({
        tripId: "",
        bookingType: "",
        status: "",
        startDate: "",
        endDate: "",
        transportId: "",
        accommodationId: "",
    });

    // Inline edit buffer (id -> editable row)
    const [editedRows, setEditedRows] = useState({});

    // Bulk delete selection
    const [selectedIds, setSelectedIds] = useState(new Set());

    useEffect(() => {
        fetchBookings();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [currentPage]);

    useEffect(() => {
        // scoped endpoints like Thymeleaf (admin: all, user: only their trips)
        fetchTrips();
        fetchTransports();
        fetchAccommodations();
    }, []);

    const toLocalInput = (v) => {
        if (!v) return "";
        if (typeof v === "string") {
            const m = v.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2})/);
            return m ? m[1] : v;
        }
        if (Array.isArray(v) && v.length >= 5) {
            const [Y, M, D, h, m] = v;
            const pad = (n) => String(n).padStart(2, "0");
            return `${String(Y).padStart(4, "0")}-${pad(M)}-${pad(D)}T${pad(h)}:${pad(m)}`;
        }
        return "";
    };

    const fromLocalInput = (v) => {
        if (!v) return null;
        if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v)) return `${v}:00`;
        return v;
    };

    const fetchBookings = async () => {
        try {
            setError(null);
            const res = await apiFetch(`/api/bookings?page=${currentPage}&size=10`);
            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                throw new Error(data.message || "Failed to load bookings.");
            }
            const data = await res.json();

            const list = data.bookings || [];
            setBookings(list);
            setTotalPages(data.totalPages || 0);

            // init edit buffer
            const nextEdited = {};
            list.forEach((b) => {
                nextEdited[b.id] = {
                    id: b.id,
                    tripId: b.tripId ?? "",
                    bookingType: b.bookingType ?? "",
                    status: b.status ?? "",
                    startDate: toLocalInput(b.startDate),
                    endDate: toLocalInput(b.endDate),
                    transportId: b.transportId ?? "",
                    accommodationId: b.accommodationId ?? "",
                };
            });
            setEditedRows(nextEdited);

            // reset selection on reload
            setSelectedIds(new Set());
        } catch (e) {
            setError(e.message || "Failed to load bookings.");
        }
    };

    const fetchTrips = async () => {
        try {
            const res = await apiFetch("/api/bookings/trips");
            if (!res.ok) return;
            const data = await res.json();
            setTrips(Array.isArray(data) ? data : []);
        } catch {}
    };

    const fetchTransports = async () => {
        try {
            const res = await apiFetch("/api/bookings/transports");
            if (!res.ok) return;
            const data = await res.json();
            setTransports(Array.isArray(data) ? data : []);
        } catch {}
    };

    const fetchAccommodations = async () => {
        try {
            const res = await apiFetch("/api/bookings/accommodations");
            if (!res.ok) return;
            const data = await res.json();
            setAccommodations(Array.isArray(data) ? data : []);
        } catch {}
    };

    // apply Thymeleaf rules: transport vs accommodation
    const normalizeByType = (dto) => {
        const type = (dto.bookingType || "").toUpperCase();
        const next = { ...dto };

        if (type === "TRANSPORT") {
            next.accommodationId = "";
        } else if (type === "ACCOMMODATION") {
            next.transportId = "";
        }

        return next;
    };

    const createRules = useMemo(() => {
        const type = (createDto.bookingType || "").toUpperCase();
        return {
            transportRequired: type === "TRANSPORT",
            accommodationRequired: type === "ACCOMMODATION",
            transportDisabled: type === "ACCOMMODATION",
            accommodationDisabled: type === "TRANSPORT",
        };
    }, [createDto.bookingType]);

    const rowRules = (row) => {
        const type = (row.bookingType || "").toUpperCase();
        return {
            transportRequired: type === "TRANSPORT",
            accommodationRequired: type === "ACCOMMODATION",
            transportDisabled: type === "ACCOMMODATION",
            accommodationDisabled: type === "TRANSPORT",
        };
    };

    const handleCreateBooking = async (e) => {
        e.preventDefault();
        try {
            setError(null);

            const dto0 = normalizeByType(createDto);

            const payload = {
                tripId: dto0.tripId ? Number(dto0.tripId) : null,
                bookingType: dto0.bookingType,
                status: dto0.status,
                startDate: fromLocalInput(dto0.startDate),
                endDate: fromLocalInput(dto0.endDate),
                transportId: dto0.transportId ? Number(dto0.transportId) : null,
                accommodationId: dto0.accommodationId ? Number(dto0.accommodationId) : null,
            };

            const res = await apiFetch("/api/bookings/save", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (res.status === 201 || res.ok) {
                await fetchBookings();
                setCreateDto({
                    tripId: "",
                    bookingType: dto0.bookingType,
                    status: "",
                    startDate: "",
                    endDate: "",
                    transportId: "",
                    accommodationId: "",
                });
                setOpenCreate(false);
                return;
            }

            const raw = await res.text().catch(() => "");
            let msg = "Error creating booking.";

            try {
                const data = raw ? JSON.parse(raw) : {};
                msg = data.message || msg;
            } catch {
                if (raw) msg = raw;
            }

            setError(msg);
            window.alert(msg);

        } catch (err) {
            const msg = err?.message || "Error creating booking.";
            setError(msg);
            window.alert(msg);
        }
    };

    const updateEditedRow = (id, field, value) => {
        setEditedRows((prev) => {
            const row = { ...(prev[id] || {}) };
            row[field] = value;

            // enforce Thymeleaf row rules when type changes
            if (field === "bookingType") {
                const type = (value || "").toUpperCase();
                if (type === "TRANSPORT") row.accommodationId = "";
                if (type === "ACCOMMODATION") row.transportId = "";
            }

            return { ...prev, [id]: row };
        });
    };

    const handleSaveRow = async (id) => {
        try {
            setError(null);
            const row0 = editedRows[id];
            if (!row0) return;

            const row = normalizeByType(row0);

            const payload = {
                id: row.id,
                tripId: row.tripId ? Number(row.tripId) : null,
                bookingType: row.bookingType,
                status: row.status,
                startDate: fromLocalInput(row.startDate),
                endDate: fromLocalInput(row.endDate),
                transportId: row.transportId ? Number(row.transportId) : null,
                accommodationId: row.accommodationId ? Number(row.accommodationId) : null,
            };

            const res = await apiFetch(`/api/bookings/edit/${id}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (res.ok) {
                await fetchBookings();
                return;
            }

            const data = await res.json().catch(() => ({}));
            setError(data.message || "Error updating booking.");
        } catch (err) {
            setError(err.message || "Error updating booking.");
        }
    };

    const handleDeleteRow = async (id) => {
        if (!window.confirm("Delete this booking?")) return;

        try {
            setError(null);
            const res = await apiFetch(`/api/bookings/${id}`, { method: "DELETE" });

            if (res.status === 204 || res.ok) {
                await fetchBookings();
                return;
            }

            const data = await res.json().catch(() => ({}));
            setError(data.message || "Error deleting booking.");
        } catch (err) {
            setError(err.message || "Error deleting booking.");
        }
    };

    const anySelected = selectedIds.size > 0;
    const allSelected = useMemo(
        () => bookings.length > 0 && selectedIds.size === bookings.length,
        [selectedIds, bookings]
    );

    const toggleSelectAll = (checked) => {
        if (!checked) {
            setSelectedIds(new Set());
            return;
        }
        setSelectedIds(new Set(bookings.map((b) => b.id)));
    };

    const toggleSelectOne = (id, checked) => {
        setSelectedIds((prev) => {
            const next = new Set(prev);
            if (checked) next.add(id);
            else next.delete(id);
            return next;
        });
    };

    const handleBulkDelete = async () => {
        if (!anySelected) return;
        if (!window.confirm("Delete selected bookings?")) return;

        try {
            setError(null);
            const ids = Array.from(selectedIds);

            // backend expects List<Long>, not { ids: [...] }
            const res = await apiFetch("/api/bookings/bulk-delete", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(ids),
            });

            if (res.status === 204 || res.ok) {
                await fetchBookings();
                return;
            }

            const data = await res.json().catch(() => ({}));
            setError(data.message || "Error deleting selected bookings.");
        } catch (err) {
            setError(err.message || "Error deleting selected bookings.");
        }
    };

    const fmtTotal = (v) => {
        if (v === null || v === undefined) return "--";
        const n = Number(v);
        if (Number.isNaN(n)) return String(v);
        return n.toFixed(2);
    };

    const autoFillDatesForCreate = (nextDto) => {
        const type = (nextDto.bookingType || "").toUpperCase();

        // TRANSPORT => use transport departure/arrival
        if (type === "TRANSPORT" && nextDto.transportId) {
            const tr = transports.find((x) => String(x.id) === String(nextDto.transportId));
            if (tr) {
                return {
                    ...nextDto,
                    startDate: toLocalInput(tr.departureTime),
                    endDate: toLocalInput(tr.arrivalTime),
                };
            }
        }

        // ACCOMMODATION => use trip start/end (because accommodation has no dates)
        if (type === "ACCOMMODATION" && nextDto.tripId) {
            const t = trips.find((x) => String(x.id) === String(nextDto.tripId));
            if (t) {
                return {
                    ...nextDto,
                    startDate: toLocalInput(t.startDate),
                    endDate: toLocalInput(t.endDate),
                };
            }
        }

        return nextDto;
    };

    return (
        <div className="container-fluid">
            <div className="row g-3 g-lg-4 py-3">
                <main className="col-12">
                    <div className="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
                        <div>
                            <div className="p-title h4 mb-1">Bookings</div>
                            <div className="p-hint">Create and manage bookings</div>
                        </div>
                    </div>

                    {/* CREATE */}
                    <section className="p-card p-3 p-md-4 mb-3">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">Create booking</div>
                            <button
                                className="btn btn-soft btn-sm"
                                type="button"
                                onClick={() => setOpenCreate((v) => !v)}
                            >
                                {openCreate ? "Hide" : "Show"}
                            </button>
                        </div>

                        {openCreate && (
                            <form onSubmit={handleCreateBooking} className="row g-2 g-md-3">
                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Trip</label>
                                    <select
                                        className="form-select"
                                        value={createDto.tripId}
                                        onChange={(e) => {
                                            const next = autoFillDatesForCreate({ ...createDto, tripId: e.target.value });
                                            setCreateDto(next);
                                        }}
                                        required
                                    >
                                        <option value="">-- Select --</option>
                                        {trips.map((t) => (
                                            <option key={t.id} value={t.id}>
                                                {t.title} (id={t.id})
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Type</label>
                                    <select
                                        className="form-select"
                                        value={createDto.bookingType}
                                        onChange={(e) => {
                                            const nextType = e.target.value;

                                            setCreateDto((prev) => {
                                                const normalized = normalizeByType({
                                                    ...prev,
                                                    bookingType: nextType,
                                                });

                                                // If switching to ACCOMMODATION → remove dates
                                                if (nextType === "ACCOMMODATION") {
                                                    return {
                                                        ...normalized,
                                                        startDate: "",
                                                        endDate: "",
                                                    };
                                                }

                                                // If switching to TRANSPORT → keep state
                                                return normalized;
                                            });
                                        }}
                                        required
                                    >
                                        <option value="">-- Select --</option>
                                        <option value="TRANSPORT">TRANSPORT</option>
                                        <option value="ACCOMMODATION">ACCOMMODATION</option>
                                    </select>
                                </div>

                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Status</label>
                                    <select
                                        className="form-select"
                                        value={createDto.status}
                                        onChange={(e) => setCreateDto({ ...createDto, status: e.target.value })}
                                        required
                                    >
                                        <option value="">-- Select --</option>
                                        <option value="CONFIRMED">CONFIRMED</option>
                                        <option value="CANCELLED">CANCELLED</option>
                                    </select>
                                </div>

                                {createDto.bookingType === "TRANSPORT" && (
                                    <>
                                        <div className="col-12 col-md-4">
                                            <label className="form-label p-hint mb-1">Start</label>
                                            <input
                                                className="form-control"
                                                type="datetime-local"
                                                value={createDto.startDate || ""}
                                                readOnly
                                            />
                                        </div>

                                        <div className="col-12 col-md-4">
                                            <label className="form-label p-hint mb-1">End</label>
                                            <input
                                                className="form-control"
                                                type="datetime-local"
                                                value={createDto.endDate || ""}
                                                readOnly
                                            />
                                        </div>
                                    </>
                                )}

                                {createDto.bookingType === "TRANSPORT" && (
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Transport</label>
                                        <select
                                            className="form-select"
                                            value={createDto.transportId}
                                            onChange={(e) => {
                                                const next = autoFillDatesForCreate({ ...createDto, transportId: e.target.value });
                                                setCreateDto(next);
                                            }}
                                            required
                                            disabled={createRules.transportDisabled}
                                        >
                                            <option value="">{createRules.transportDisabled ? "--" : "-- Select --"}</option>
                                            {transports.map((tr) => (
                                                <option key={tr.id} value={tr.id}>
                                                    {tr.company} ({tr.originAddress} → {tr.destinationAddress}) price={tr.price}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                )}

                                {createDto.bookingType === "ACCOMMODATION" && (
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Accommodation</label>
                                        <select
                                            className="form-select"
                                            value={createDto.accommodationId}
                                            onChange={(e) => {
                                                const next = { ...createDto, accommodationId: e.target.value };
                                                setCreateDto(next);
                                            }}
                                            required
                                            disabled={createRules.accommodationDisabled}
                                        >
                                            <option value="">{createRules.accommodationDisabled ? "--" : "-- Select --"}</option>
                                            {accommodations.map((a) => (
                                                <option key={a.id} value={a.id}>
                                                    {a.name} ({a.address}) price/night={a.pricePerNight}
                                                </option>
                                            ))}status
                                        </select>
                                    </div>
                                )}

                                {error && <div className="text-danger small">{error}</div>}

                                <div className="col-12 d-flex justify-content-end">
                                    <button className="btn btn-planora px-4" type="submit">
                                        Create
                                    </button>
                                </div>
                            </form>
                        )}
                    </section>

                    {/* TABLE */}
                    <section className="p-card p-3 p-md-4">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">All bookings</div>
                            <div className="p-hint">Edit in table → Save</div>
                        </div>

                        <div className="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
                            <div className="p-hint m-0">Select rows and delete them.</div>
                            <button
                                className="btn btn-danger-soft btn-sm"
                                type="button"
                                disabled={!anySelected}
                                onClick={handleBulkDelete}
                            >
                                Delete selected
                            </button>
                        </div>

                        <div className="table-responsive table-responsive-unclip">
                            <table className="table planora-table planora-table--compact table-hover align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th style={{ width: "44px" }}>
                                            <input
                                                id="bookingSelectAll"
                                                className="form-check-input"
                                                type="checkbox"
                                                aria-label="Select all bookings"
                                                checked={allSelected}
                                                onChange={(e) => toggleSelectAll(e.target.checked)}
                                            />
                                        </th>
                                        <th style={{ width: "70px" }}>ID</th>
                                        <th style={{ minWidth: "190px" }}>Trip</th>
                                        <th style={{ minWidth: "140px" }}>Type</th>
                                        <th style={{ minWidth: "140px" }}>Status</th>
                                        <th style={{ minWidth: "240px" }}>Transport</th>
                                        <th style={{ minWidth: "240px" }}>Accommodation</th>
                                        <th style={{ minWidth: "120px" }}>Total</th>
                                        <th style={{ width: "220px" }}>Actions</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {bookings.length === 0 ? (
                                        <tr>
                                            <td colSpan="11" className="p-hint">
                                                No bookings found.
                                            </td>
                                        </tr>
                                    ) : (
                                        bookings.map((b) => {
                                            const row = editedRows[b.id] || {};
                                            const rules = rowRules(row);
                                            return (
                                                <tr key={b.id}>
                                                    <td>
                                                        <input
                                                            className="form-check-input"
                                                            type="checkbox"
                                                            aria-label="Select booking"
                                                            checked={selectedIds.has(b.id)}
                                                            onChange={(e) => toggleSelectOne(b.id, e.target.checked)}
                                                        />
                                                    </td>

                                                    <td>{b.id}</td>

                                                    <td>
                                                        <select
                                                            className="form-select"
                                                            value={row.tripId ?? ""}
                                                            onChange={(e) => updateEditedRow(b.id, "tripId", e.target.value)}
                                                            required
                                                        >
                                                            <option value="">-- Select --</option>
                                                            {trips.map((t) => (
                                                                <option key={t.id} value={t.id}>
                                                                    {t.title} (id={t.id})
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </td>

                                                    <td>
                                                        <select
                                                            className="form-select"
                                                            value={row.bookingType ?? ""}
                                                            onChange={(e) =>
                                                                updateEditedRow(b.id, "bookingType", e.target.value)
                                                            }
                                                            required
                                                        >
                                                            <option value="">-- Select --</option>
                                                            <option value="TRANSPORT">TRANSPORT</option>
                                                            <option value="ACCOMMODATION">ACCOMMODATION</option>
                                                        </select>
                                                    </td>

                                                    <td>
                                                        <select
                                                            className="form-select"
                                                            value={row.status ?? ""}
                                                            onChange={(e) => updateEditedRow(b.id, "status", e.target.value)}
                                                            required
                                                        >
                                                            <option value="">-- Select --</option>
                                                            <option value="PENDING">PENDING</option>
                                                            <option value="CONFIRMED">CONFIRMED</option>
                                                            <option value="CANCELLED">CANCELLED</option>
                                                        </select>
                                                    </td>

                                                    <td>
                                                        <select
                                                            className="form-select"
                                                            value={row.transportId ?? ""}
                                                            onChange={(e) =>
                                                                updateEditedRow(b.id, "transportId", e.target.value)
                                                            }
                                                            required={rules.transportRequired}
                                                            disabled={rules.transportDisabled}
                                                        >
                                                            <option value="">--</option>
                                                            {transports.map((tr) => (
                                                                <option key={tr.id} value={tr.id}>
                                                                    {tr.company} ({tr.originAddress} → {tr.destinationAddress}) price={tr.price}
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </td>

                                                    <td>
                                                        <select
                                                            className="form-select"
                                                            value={row.accommodationId ?? ""}
                                                            onChange={(e) =>
                                                                updateEditedRow(b.id, "accommodationId", e.target.value)
                                                            }
                                                            required={rules.accommodationRequired}
                                                            disabled={rules.accommodationDisabled}
                                                        >
                                                            <option value="">--</option>
                                                            {accommodations.map((a) => (
                                                                <option key={a.id} value={a.id}>
                                                                    {a.name} ({a.address}) price/night={a.pricePerNight}
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </td>

                                                    <td>{fmtTotal(b.totalPrice)}</td>

                                                    <td>
                                                        <div className="d-flex align-items-center gap-2">
                                                            <button
                                                                className="btn btn-soft btn-sm"
                                                                type="button"
                                                                onClick={() => handleSaveRow(b.id)}
                                                            >
                                                                Save
                                                            </button>
                                                            <button
                                                                className="btn btn-danger-soft btn-sm"
                                                                type="button"
                                                                onClick={() => handleDeleteRow(b.id)}
                                                            >
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    )}
                                </tbody>
                            </table>
                        </div>
                        {/* Pagination */}
                        <Pagination
                            currentPage={currentPage}
                            totalPages={totalPages}
                            onPageChange={setCurrentPage}
                        />
                        {error && <div className="text-danger small mt-2">{error}</div>}
                    </section>
                </main>
            </div>
        </div>
    );
};

export default BookingPage;