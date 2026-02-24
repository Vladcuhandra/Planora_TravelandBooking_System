import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

const TripPage = () => {
    const navigate = useNavigate();
    const [trips, setTrips] = useState([]);
    const [users, setUsers] = useState([]);
    const [error, setError] = useState(null);
    const [openCreate, setOpenCreate] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [user, setUser] = useState(null);

    // Inline edit buffer (id -> editable row)
    const [editedRows, setEditedRows] = useState({});

    // Bulk delete selection
    const [selectedIds, setSelectedIds] = useState(new Set());

    useEffect(() => {
        fetchUserProfile();
        fetchTrips();
        fetchUsers();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [currentPage]);

    // API LocalDateTime -> datetime-local input value (YYYY-MM-DDTHH:mm)
    const toLocalInput = (v) => {
        if (!v) return "";

        if (typeof v === "string") {
            if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v)) return v;
            const m = v.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2})/);
            return m ? m[1] : v;
        }

        // Sometimes LocalDateTime is serialized as [YYYY,MM,DD,hh,mm,ss]
        if (Array.isArray(v) && v.length >= 5) {
            const [Y, M, D, h, m] = v;
            const pad = (n) => String(n).padStart(2, "0");
            return `${String(Y).padStart(4, "0")}-${pad(M)}-${pad(D)}T${pad(h)}:${pad(m)}`;
        }

        return "";
    };

    // datetime-local input value -> backend LocalDateTime ISO string with seconds
    const fromLocalInput = (v) => {
        if (!v) return null;
        if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v)) return `${v}:00`;
        return v;
    };

    const fetchUserProfile = async () => {
        try {
            const res = await apiFetch("/api/users/profile", { method: "GET" });
            if (res.ok) {
                const data = await res.json();
                setUser(data);
                setIsAdmin(data.role === "ADMIN");
            } else {
                setError("Failed to load user profile.");
                navigate("/login");
            }
        } catch (err) {
            setError("An error occurred: " + err.message);
            navigate("/login");
        }
    };

    const fetchTrips = async () => {
        try {
            const response = await apiFetch(`/api/trips?page=${currentPage}`);
            const data = await response.json();

            setTrips(data.trips || []);
            setTotalPages(data.totalPages || 0);

            if (typeof data.currentPage === "number") setCurrentPage(data.currentPage);

            const nextEdited = {};
            (data.trips || []).forEach((t) => {
                nextEdited[t.id] = {
                    id: t.id,
                    title: t.title ?? "",
                    description: t.description ?? "",
                    startDate: toLocalInput(t.startDate),
                    endDate: toLocalInput(t.endDate),
                    userId: t.userId ?? "",
                };
            });

            setEditedRows(nextEdited);
            setSelectedIds(new Set());
        } catch (e) {
            setError("Failed to fetch trips.");
        }
    };

    const fetchUsers = async () => {
        try {
            setError(null);
            const response = await apiFetch("/api/users", { method: "GET" });
            if (!response.ok) {
                const data = await response.json().catch(() => ({}));
                throw new Error(data.message || "Failed to fetch users.");
            }
            const data = await response.json();
            setUsers(Array.isArray(data) ? data : []);
        } catch (e) {
            setError(e.message || "Failed to fetch users.");
        }
    };

    const handleCreateTrip = async (event) => {
        event.preventDefault();
        const form = new FormData(event.target);
        const tripData = Object.fromEntries(form.entries());

        tripData.startDate = fromLocalInput(tripData.startDate);
        tripData.endDate = fromLocalInput(tripData.endDate);

        // Backend enforces non-admin userId to logged-in user; keep UI consistent.
        if (!isAdmin) tripData.userId = user.id;

        try {
            const response = await apiFetch("/api/trips/save", {
                method: "POST",
                body: JSON.stringify(tripData),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 201) {
                await fetchTrips();
                setOpenCreate(false);
                setError(null);
                event.target.reset();
            } else {
                const data = await response.json().catch(() => ({}));
                setError(data.message || "Error creating trip.");
            }
        } catch (e) {
            setError(e.message || "Failed to create trip.");
        }
    };

    const updateEditedRow = (id, field, value) => {
        setEditedRows((prev) => ({
            ...prev,
            [id]: { ...prev[id], [field]: value },
        }));
    };

    const handleSaveTrip = async (id) => {
        try {
            const row = editedRows[id];
            if (!row) return;

            const payload = {
                id: row.id,
                title: row.title,
                description: row.description,
                startDate: fromLocalInput(row.startDate),
                endDate: fromLocalInput(row.endDate),
                userId: isAdmin ? Number(row.userId) : user.id,
            };

            const response = await apiFetch(`/api/trips/edit/${id}`, {
                method: "PUT",
                body: JSON.stringify(payload),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 200) {
                await fetchTrips();
                setError(null);
            } else {
                const data = await response.json().catch(() => ({}));
                setError(data.message || "Error updating trip.");
            }
        } catch (e) {
            setError(e.message || "Failed to update trip.");
        }
    };

    const handleDeleteTrip = async (id) => {
        if (!window.confirm("Are you sure you want to delete this trip?")) return;

        try {
            const response = await apiFetch(`/api/trips/delete/${id}`, { method: "DELETE" });
            if (response.status === 200) {
                await fetchTrips();
                setError(null);
            } else {
                const data = await response.json().catch(() => ({}));
                setError(data.message || "Error deleting trip.");
            }
        } catch (e) {
            setError(e.message || "Failed to delete trip.");
        }
    };

    const anySelected = selectedIds.size > 0;
    const allSelected = useMemo(
        () => trips.length > 0 && selectedIds.size === trips.length,
        [selectedIds, trips]
    );

    const toggleSelectAll = (checked) => {
        if (!checked) {
            setSelectedIds(new Set());
            return;
        }
        setSelectedIds(new Set(trips.map((t) => t.id)));
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
        if (!window.confirm("Delete selected trips?")) return;

        try {
            const ids = Array.from(selectedIds);
            const response = await apiFetch("/api/trips/bulk-delete", {
                method: "POST",
                body: JSON.stringify(ids),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 200) {
                await fetchTrips();
                setError(null);
            } else {
                const data = await response.json().catch(() => ({}));
                setError(data.message || "Error deleting trips.");
            }
        } catch (e) {
            setError(e.message || "Failed to bulk delete trips.");
        }
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="container-fluid">
            <div className="row g-3 g-lg-4 py-3">
                <main className="col-12">
                    <div className="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
                        <div>
                            <div className="p-title h4 mb-1">Trips</div>
                            <div className="p-hint">
                                {isAdmin ? "Create, edit and delete trips" : "View and manage your trips"}
                            </div>
                        </div>
                    </div>

                    {/* Create Trip */}
                    <section className="p-card p-3 p-md-4 mb-3">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">Create trip</div>
                            <button
                                className="btn btn-soft btn-sm"
                                type="button"
                                onClick={() => setOpenCreate(!openCreate)}
                            >
                                {openCreate ? "Hide" : "Show"}
                            </button>
                        </div>

                        {openCreate && (
                            <form onSubmit={handleCreateTrip} className="row g-2 g-md-3 mt-1">
                                <div className="col-12 col-md-6">
                                    <label className="form-label p-hint mb-1">Title</label>
                                    <input className="form-control" type="text" name="title" required />
                                </div>

                                <div className="col-12 col-md-6">
                                    <label className="form-label p-hint mb-1">Description</label>
                                    <input className="form-control" type="text" name="description" required />
                                </div>

                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Start Date</label>
                                    <input className="form-control" type="datetime-local" name="startDate" required />
                                </div>

                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">End Date</label>
                                    <input className="form-control" type="datetime-local" name="endDate" required />
                                </div>

                                {isAdmin && (
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">User</label>
                                        <select className="form-select" name="userId" required>
                                            <option value="">-- Select --</option>
                                            {users.map((u) => (
                                                <option key={u.id} value={u.id}>
                                                    {u.email}
                                                </option>
                                            ))}
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

                    {/* Trips List */}
                    <section className="p-card p-3 p-md-4">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">All trips</div>
                        </div>

                        {/* Bulk delete bar */}
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
                                                className="form-check-input"
                                                type="checkbox"
                                                aria-label="Select all trips"
                                                checked={allSelected}
                                                onChange={(e) => toggleSelectAll(e.target.checked)}
                                            />
                                        </th>
                                        <th style={{ width: "80px" }}>ID</th>
                                        <th style={{ minWidth: "180px" }}>Title</th>
                                        <th style={{ minWidth: "240px" }}>Description</th>
                                        <th style={{ minWidth: "190px" }}>Start Date</th>
                                        <th style={{ minWidth: "190px" }}>End Date</th>
                                        <th style={{ minWidth: "240px" }}>User</th>
                                        <th style={{ width: "210px" }}>Actions</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {trips.length === 0 ? (
                                        <tr>
                                            <td colSpan="8" className="p-hint">
                                                No trips found.
                                            </td>
                                        </tr>
                                    ) : (
                                        trips.map((trip) => (
                                            <tr key={trip.id}>
                                                <td>
                                                    <input
                                                        className="form-check-input"
                                                        type="checkbox"
                                                        aria-label="Select trip"
                                                        checked={selectedIds.has(trip.id)}
                                                        onChange={(e) => toggleSelectOne(trip.id, e.target.checked)}
                                                    />
                                                </td>

                                                <td>{trip.id}</td>

                                                <td>
                                                    <input
                                                        className="form-control"
                                                        type="text"
                                                        name="title"
                                                        value={editedRows[trip.id]?.title ?? ""}
                                                        onChange={(e) => updateEditedRow(trip.id, "title", e.target.value)}
                                                        required
                                                    />
                                                </td>

                                                <td>
                                                    <input
                                                        className="form-control"
                                                        type="text"
                                                        name="description"
                                                        value={editedRows[trip.id]?.description ?? ""}
                                                        onChange={(e) =>
                                                            updateEditedRow(trip.id, "description", e.target.value)
                                                        }
                                                        required
                                                    />
                                                </td>

                                                <td>
                                                    <input
                                                        className="form-control"
                                                        type="datetime-local"
                                                        name="startDate"
                                                        value={editedRows[trip.id]?.startDate ?? ""}
                                                        onChange={(e) =>
                                                            updateEditedRow(trip.id, "startDate", e.target.value)
                                                        }
                                                        required
                                                    />
                                                </td>

                                                <td>
                                                    <input
                                                        className="form-control"
                                                        type="datetime-local"
                                                        name="endDate"
                                                        value={editedRows[trip.id]?.endDate ?? ""}
                                                        onChange={(e) =>
                                                            updateEditedRow(trip.id, "endDate", e.target.value)
                                                        }
                                                        required
                                                    />
                                                </td>

                                                <td>
                                                    {isAdmin ? (
                                                        <select
                                                            className="form-select"
                                                            name="userId"
                                                            value={editedRows[trip.id]?.userId ?? ""}
                                                            onChange={(e) =>
                                                                updateEditedRow(trip.id, "userId", e.target.value)
                                                            }
                                                            required
                                                        >
                                                            <option value="">-- Select --</option>
                                                            {users.map((u) => (
                                                                <option key={u.id} value={u.id}>
                                                                    {u.email}
                                                                </option>
                                                            ))}
                                                        </select>
                                                    ) : (
                                                        <span className="p-hint">{user?.email || trip.userId}</span>
                                                    )}
                                                </td>

                                                <td>
                                                    <div className="d-flex align-items-center gap-2">
                                                        <button
                                                            className="btn btn-soft btn-sm"
                                                            type="button"
                                                            onClick={() => handleSaveTrip(trip.id)}
                                                        >
                                                            Save
                                                        </button>
                                                        <button
                                                            className="btn btn-danger-soft btn-sm"
                                                            type="button"
                                                            onClick={() => handleDeleteTrip(trip.id)}
                                                        >
                                                            Delete
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        {totalPages > 1 && (
                            <div className="d-flex align-items-center justify-content-between mt-3">
                                <button
                                    className="btn btn-soft"
                                    onClick={() => setCurrentPage(Math.max(currentPage - 1, 0))}
                                    disabled={currentPage === 0}
                                >
                                    ← Previous
                                </button>

                                <div className="p-hint text-center flex-grow-1">
                                    Page {currentPage + 1} of {totalPages}
                                </div>

                                <button
                                    className="btn btn-soft"
                                    onClick={() => setCurrentPage(Math.min(currentPage + 1, totalPages - 1))}
                                    disabled={currentPage === totalPages - 1}
                                >
                                    Next →
                                </button>
                            </div>
                        )}
                    </section>
                </main>
            </div>
        </div>
    );
};

export default TripPage;