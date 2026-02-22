import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

const AccommodationPage = () => {
    const navigate = useNavigate();
    const [accommodations, setAccommodations] = useState([]);
    const [accommodationTypes, setAccommodationTypes] = useState(["HOTEL", "HOSTEL", "AIRBNB", "GUESTHOUSE", "INTERNET_CAFE"]);
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
            const res = await apiFetch("/api/user", { method: "GET" });
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

    const fetchAccommodations = async () => {
        try {
            const response = await apiFetch(`/api/accommodations?page=${currentPage}`);
            const data = await response.json();
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

        if (field === 'rating') {
            setEditedAccommodationData({
                ...editedAccommodationData,
                [field]: value
            });
        } else {
            setEditedAccommodationData({
                ...editedAccommodationData,
                [field]: value
            });
        }
    };

    const handleEditAccommodation = async (id, field, value) => {
        try {
            const updatedAccommodation = accommodations.find((a) => a.id === id);

            const accommodationData = {
                ...updatedAccommodation,
                [field]: value,
            };

            const response = await apiFetch(`/api/accommodations/edit`, {
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

    if (!user) return <div>Loading...</div>;

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
                                <button
                                    className="btn btn-soft btn-sm"
                                    type="button"
                                    onClick={() => setOpenCreate(!openCreate)}
                                >
                                    {openCreate ? "Hide" : "Show"}
                                </button>
                            </div>
                            {openCreate && (
                                <form onSubmit={handleCreateAccommodation} className="row g-2 g-md-3 mt-1">
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
                                        <label className="form-label p-hint mb-1">Rooms</label>
                                        <input className="form-control" type="number" name="room" min="0" required />
                                    </div>
                                    <div className="col-6 col-md-2">
                                        <label className="form-label p-hint mb-1">Price</label>
                                        <input className="form-control" type="number" step="0.01" min="0" name="pricePerNight" required />
                                    </div>
                                    {error && <div className="text-danger small">{error}</div>}
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
                                    <th style={{ width: "110px" }}>Rooms</th>
                                    <th style={{ width: "140px" }}>Price / Night</th>
                                    <th style={{ width: "170px" }}>Status</th>
                                    {isAdmin && <th style={{ width: "210px" }}>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {accommodations.length === 0 ? (
                                    <tr>
                                        <td colSpan="10" className="p-hint">
                                            No accommodations found.
                                        </td>
                                    </tr>
                                ) : (
                                    accommodations.map((a) => (
                                        <tr key={a.id}>
                                            <td>{a.id}</td>
                                            {/* Editable Fields */}
                                            {['accommodationType', 'name', 'city', 'address', 'rating', 'room', 'pricePerNight', 'status'].map((field) => (
                                                <td key={field}>
                                                    {editingField?.id === a.id && editingField?.field === field ? (
                                                        <div>
                                                            {field === 'accommodationType' || field === 'status' ? (
                                                                <select
                                                                    value={editedAccommodationData[field] || a[field]}
                                                                    onChange={(e) => handleInputChange(e, field)}
                                                                >
                                                                    {(field === 'accommodationType' ? accommodationTypes : statuses).map((option) => (
                                                                        <option key={option} value={option}>
                                                                            {option}
                                                                        </option>
                                                                    ))}
                                                                </select>
                                                            ) : (
                                                                <input
                                                                    type="text"
                                                                    value={editedAccommodationData[field] || a[field]}
                                                                    onChange={(e) => handleInputChange(e, field)}
                                                                />
                                                            )}
                                                            <button
                                                                onClick={() => handleEditAccommodation(a.id, field, editedAccommodationData[field] || a[field])}
                                                            >
                                                                Save
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        // Only allow editing for admins
                                                        isAdmin ? (
                                                            <div onClick={() => startEditing(a.id, field, a[field])}>
                                                                {a[field]}
                                                            </div>
                                                        ) : (
                                                            <div>{a[field]}</div>
                                                        )
                                                    )}
                                                </td>
                                            ))}
                                            {/* Admin Actions */}
                                            {isAdmin && (
                                                <td>
                                                    <button
                                                        className="btn btn-danger-soft btn-sm"
                                                        onClick={() => handleDeleteAccommodation(a.id)}
                                                    >
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
