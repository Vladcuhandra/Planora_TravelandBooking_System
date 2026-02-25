import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

const AccommodationPage = () => {
    const navigate = useNavigate();
    const [accommodations, setAccommodations] = useState([]);
    const [accommodationTypes, setAccommodationTypes] = useState([
        "HOTEL",
        "HOSTEL",
        "AIRBNB",
        "GUESTHOUSE",
        "INTERNET_CAFE",
    ]);
    const [statuses, setStatuses] = useState(["AVAILABLE", "UNAVAILABLE"]);
    const [openCreate, setOpenCreate] = useState(false);
    const [error, setError] = useState(null);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [user, setUser] = useState(null);
    const [isAdmin, setIsAdmin] = useState(false);
    const [editingField, setEditingField] = useState(null);
    const [editedAccommodationData, setEditedAccommodationData] = useState({});

    useEffect(() => {
        fetchUserProfile();
        fetchAccommodations();
    }, [currentPage]);

    const fetchUserProfile = async () => {
        try {
            const res = await apiFetch("/api/users/profile", { method: "GET" });
            if (res.status === 401) {
                navigate("/login");
                return;
            }
            if (!res.ok) {
                const txt = await res.text().catch(() => "");
                setError(`Failed to load user profile. (${res.status}) ${txt}`);
                return;
            }
            const data = await res.json();
            setUser(data);
            setIsAdmin(data.role === "ADMIN");
        } catch (err) {
            setError("Profile request failed: " + err.message);
        }
    };

    const fetchAccommodations = async () => {
        try {
            const response = await apiFetch(`/api/accommodations?page=${currentPage}`);
            const data = await response.json();
            console.log("Accommodations fetched:", data);
            setAccommodations(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            setError("Failed to fetch accommodations.");
        }
    };

    const startEditing = (id, field, currentValue) => {
        setEditingField({ id, field });
        setEditedAccommodationData({ ...editedAccommodationData, [field]: currentValue });
    };

    const handleInputChange = (e, field) => {
        let value = e.target.value;
        setEditedAccommodationData({ ...editedAccommodationData, [field]: value });
    };

    const handleEditAccommodation = async (id, field, value) => {
        try {
            const updatedAccommodation = accommodations.find((a) => a.id === id);
            const accommodationData = { ...updatedAccommodation, [field]: value };

            if (editedAccommodationData.startTime) {
                accommodationData.startTime = editedAccommodationData.startTime;
            }
            if (editedAccommodationData.endTime) {
                accommodationData.endTime = editedAccommodationData.endTime;
            }

            const response = await apiFetch("/api/accommodations/edit", {
                method: "POST",
                body: JSON.stringify(accommodationData),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 200) {
                fetchAccommodations();
                setEditingField(null);
                setEditedAccommodationData({});
            } else {
                const data = await response.json();
                setError(data.message || "Error editing accommodation.");
            }
        } catch (error) {
            setError("Failed to edit accommodation.");
        }
    };

    const handleDeleteAccommodation = async (id) => {
        if (window.confirm("Are you sure you want to delete this accommodation?")) {
            try {
                const response = await apiFetch(`/api/accommodations/${id}`, { method: "DELETE" });
                if (response.status === 200) {
                    fetchAccommodations();
                } else {
                    const data = await response.json();
                    setError(data.message || "Error deleting accommodation.");
                }
            } catch (error) {
                setError("Failed to delete accommodation.");
            }
        }
    };

    const handleCreateAccommodation = async (event) => {
        event.preventDefault();
        const form = new FormData(event.target);
        const accommodationData = Object.fromEntries(form.entries());
        accommodationData.startTime = new Date(accommodationData.startTime).toISOString();
        accommodationData.endTime = new Date(accommodationData.endTime).toISOString();

        try {
            const response = await apiFetch("/api/accommodations/save", {
                method: "POST",
                body: JSON.stringify(accommodationData),
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (response.status === 201) {
                fetchAccommodations();
                setOpenCreate(false);
            } else {
                const data = await response.json();
                setError(data.message || "Error creating accommodation.");
            }
        } catch (error) {
            setError("Failed to create accommodation.");
        }
    };

    if (error) return <div className="text-danger p-3">{error}</div>;
    if (!user) return <div className="p-3">Loading...</div>;

    return (
        <div className="container-fluid">
            <div className="row g-3 g-lg-4 py-3">
                <main className="col-12">
                    <div className="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
                        <div>
                            <div className="p-title h4 mb-1">Accommodations</div>
                            <div className="p-hint">
                                {isAdmin ? "Create, edit, and delete accommodations" : "View available accommodations"}
                            </div>
                        </div>
                    </div>

                    {/* Create Accommodation - Admin only */}
                    {isAdmin && (
                        <section className="p-card p-3 p-md-4 mb-3">
                            <div className="d-flex align-items-center justify-content-between mb-2">
                                <div className="p-subtitle fw-semibold">Create accommodation</div>
                                <button className="btn btn-soft btn-sm" type="button" onClick={() => setOpenCreate(!openCreate)}>
                                    {openCreate ? "Hide" : "Show"}
                                </button>
                            </div>
                            {openCreate && (
                                <form onSubmit={handleCreateAccommodation} className="row g-2 g-md-3 mt-1">
                                    {/* Form fields */}
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Accommodation Type</label>
                                        <select className="form-select" name="accommodationType" required>
                                            <option value="">-- Select --</option>
                                            {accommodationTypes.map((t, index) => (
                                                <option key={index} value={t}>
                                                    {t}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Name</label>
                                        <input className="form-control" type="text" name="name" required />
                                    </div>
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Status</label>
                                        <select className="form-select" name="status" required>
                                            <option value="">-- Select --</option>
                                            {statuses.map((s, index) => (
                                                <option key={index} value={s}>
                                                    {s}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    {/* Additional form fields */}
                                    <div className="col-12 col-md-6">
                                        <label className="form-label p-hint mb-1">City</label>
                                        <input className="form-control" type="text" name="city" required />
                                    </div>
                                    <div className="col-12 col-md-6">
                                        <label className="form-label p-hint mb-1">Address</label>
                                        <input className="form-control" type="text" name="address" required />
                                    </div>
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Rating</label>
                                        <input className="form-control" type="number" step="0.1" min="0" max="10" name="rating" required />
                                    </div>
                                    <div className="col-6 col-md-2">
                                        <label className="form-label p-hint mb-1">Room</label>
                                        <input className="form-control" type="number" name="room" min="0" required />
                                    </div>
                                    <div className="col-6 col-md-2">
                                        <label className="form-label p-hint mb-1">Price</label>
                                        <input className="form-control" type="number" step="0.01" min="0" name="pricePerNight" required />
                                    </div>
                                    <div className="col-12 col-md-6">
                                        <label className="form-label p-hint mb-1">Start Time</label>
                                        <input className="form-control" type="datetime-local" name="startTime" required />
                                    </div>
                                    <div className="col-12 col-md-6">
                                        <label className="form-label p-hint mb-1">End Time</label>
                                        <input className="form-control" type="datetime-local" name="endTime" required />
                                    </div>
                                    <div className="col-12 d-flex justify-content-end">
                                        <button className="btn btn-planora px-4" type="submit">
                                            Create
                                        </button>
                                    </div>
                                </form>
                            )}
                        </section>
                    )}

                    {/* Accommodation List */}
                    <section className="p-card p-3 p-md-4">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">Available accommodations</div>
                        </div>
                        <div className="table-responsive table-responsive-unclip">
                            <table className="table planora-table planora-table--compact table-hover align-middle mb-0">
                                <thead>
                                <tr>
                                    <th style={{ width: "80px" }}>ID</th>
                                    <th style={{ width: "170px" }}>Type</th>
                                    <th style={{ minWidth: "180px" }}>Name</th>
                                    <th style={{ minWidth: "140px" }}>City</th>
                                    <th style={{ minWidth: "220px" }}>Address</th>
                                    <th style={{ width: "110px" }}>Rating</th>
                                    <th style={{ width: "110px" }}>Room</th>
                                    <th style={{ width: "140px" }}>Price / Night</th>
                                    <th style={{ width: "170px" }}>Status</th>
                                    <th style={{ width: "170px" }}>Start Time</th>
                                    <th style={{ width: "170px" }}>End Time</th>
                                    {isAdmin && <th style={{ width: "210px" }}>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {accommodations.length === 0 ? (
                                    <tr>
                                        <td colSpan="12" className="p-hint">No accommodations found.</td>
                                    </tr>
                                ) : (
                                    accommodations.map((a) => (
                                        <tr key={a.id}>
                                            <td>{a.id}</td>
                                            {/* Editable Fields */}
                                            {['accommodationType', 'name', 'city', 'address', 'rating', 'room', 'pricePerNight', 'status', 'startTime', 'endTime'].map((field) => (
                                                <td key={field}>
                                                    {editingField?.id === a.id && editingField?.field === field ? (
                                                        <div>
                                                            {field === "startTime" || field === "endTime" ? (
                                                                <input
                                                                    className="form-control form-control-sm"
                                                                    type="datetime-local"
                                                                    value={editedAccommodationData[field] || a[field]}
                                                                    onChange={(e) => handleInputChange(e, field)}
                                                                />
                                                            ) : (
                                                                <input
                                                                    className="form-control form-control-sm"
                                                                    type="text"
                                                                    value={editedAccommodationData[field] || a[field]}
                                                                    onChange={(e) => handleInputChange(e, field)}
                                                                />
                                                            )}
                                                            <button
                                                                className="btn btn-sm btn-planora"
                                                                type="button"
                                                                onClick={() => handleEditAccommodation(a.id, field, editedAccommodationData[field] || a[field])}
                                                            >
                                                                Save
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        isAdmin ? (
                                                            <div onClick={() => startEditing(a.id, field, a[field])}>
                                                                {field === "startTime" || field === "endTime" ? a[field] : a[field]}
                                                            </div>
                                                        ) : (
                                                            <div>{field === "startTime" || field === "endTime" ? a[field] : a[field]}</div>
                                                        )
                                                    )}
                                                </td>
                                            ))}
                                            {/* Admin Actions */}
                                            {isAdmin && (
                                                <td>
                                                    <button className="btn btn-danger-soft btn-sm" onClick={() => handleDeleteAccommodation(a.id)}>
                                                        Delete
                                                    </button>
                                                </td>
                                            )}
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    </section>
                </main>
            </div>
        </div>
    );
};

export default AccommodationPage;

