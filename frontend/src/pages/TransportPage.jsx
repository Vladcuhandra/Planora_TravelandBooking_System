import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

const TransportPage = () => {
    const navigate = useNavigate();
    const [transports, setTransports] = useState([]);
    const [transportTypes, setTransportTypes] = useState(["FLIGHT", "TRAIN", "BUS", "SHIP"]);
    const [statuses, setStatuses] = useState(["AVAILABLE", "UNAVAILABLE"]);
    const [openCreate, setOpenCreate] = useState(false);
    const [error, setError] = useState(null);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [user, setUser] = useState(null);
    const [isAdmin, setIsAdmin] = useState(false);
    const [editingField, setEditingField] = useState(null);
    const [editedTransportData, setEditedTransportData] = useState({});

    useEffect(() => {
        fetchUserProfile();
        fetchTransports();
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

    const fetchTransports = async () => {
        try {
            const response = await apiFetch(`/api/transports?page=${currentPage}`);
            const data = await response.json();
            setTransports(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            setError("Failed to fetch transports.");
        }
    };

    const startEditing = (id, field, currentValue) => {
        setEditingField({ id, field });
        setEditedTransportData({ ...editedTransportData, [field]: currentValue });
    };

    const handleInputChange = (e, field) => {
        let value = e.target.value;

        if (field === 'departureTime' || field === 'arrivalTime') {
            setEditedTransportData({
                ...editedTransportData,
                [field]: value
            });
        } else {
            setEditedTransportData({
                ...editedTransportData,
                [field]: value
            });
        }
    };

    const handleEditTransport = async (id, field, value) => {
        try {
            const updatedTransport = transports.find((t) => t.id === id);

            const transportData = {
                ...updatedTransport,
                [field]: value,
            };

            if (editedTransportData.departureTime) {
                transportData.departureTime = editedTransportData.departureTime;
            }

            if (editedTransportData.arrivalTime) {
                transportData.arrivalTime = editedTransportData.arrivalTime;
            }

            const response = await apiFetch(`/api/transports/edit`, {
                method: "POST",
                body: JSON.stringify(transportData),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 200) {
                fetchTransports();
                setEditingField(null);
                setEditedTransportData({});
            } else {
                const data = await response.json();
                setError(data.message || "Error editing transport.");
            }
        } catch (error) {
            setError("Failed to edit transport.");
        }
    };

    const handleDeleteTransport = async (id) => {
        if (window.confirm("Are you sure you want to delete this transport?")) {
            try {
                const response = await apiFetch(`/api/transports/${id}`, { method: "DELETE" });
                if (response.status === 200) {
                    fetchTransports();
                } else {
                    const data = await response.json();
                    setError(data.message || "Error deleting transport.");
                }
            } catch (error) {
                setError("Failed to delete transport.");
            }
        }
    };

    const handleCreateTransport = async (event) => {
        event.preventDefault();
        const form = new FormData(event.target);
        const transportData = Object.fromEntries(form.entries());
        transportData.departureTime = new Date(transportData.departureTime).toISOString();
        transportData.arrivalTime = new Date(transportData.arrivalTime).toISOString();

        try {
            const response = await apiFetch("/api/transports/save", {
                method: "POST",
                body: JSON.stringify(transportData),
                headers: {
                    "Content-Type": "application/json",
                },
            });
            if (response.status === 201) {
                fetchTransports();
                setOpenCreate(false);
            } else {
                const data = await response.json();
                setError(data.message || "Error creating transport.");
            }
        } catch (error) {
            setError("Failed to create transport.");
        }
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="container-fluid">
            <div className="row g-3 g-lg-4 py-3">
                <main className="col-12">
                    <div className="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
                        <div>
                            <div className="p-title h4 mb-1">Transport</div>
                            <div className="p-hint">
                                {isAdmin ? "Create, edit, and delete transport" : "View available transports"}
                            </div>
                        </div>
                    </div>

                    {/* Create Transport - Admin only */}
                    {isAdmin && (
                        <section className="p-card p-3 p-md-4 mb-3">
                            <div className="d-flex align-items-center justify-content-between mb-2">
                                <div className="p-subtitle fw-semibold">Create transport</div>
                                <button
                                    className="btn btn-soft btn-sm"
                                    type="button"
                                    onClick={() => setOpenCreate(!openCreate)}
                                >
                                    {openCreate ? "Hide" : "Show"}
                                </button>
                            </div>
                            {openCreate && (
                                <form onSubmit={handleCreateTransport} className="row g-2 g-md-3 mt-1">
                                    {/* Form fields */}
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Transport Type</label>
                                        <select className="form-select" name="transportType" required>
                                            <option value="">-- Select --</option>
                                            {transportTypes.map((t, index) => (
                                                <option key={index} value={t}>
                                                    {t}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Company</label>
                                        <input className="form-control" type="text" name="company" required />
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
                                        <label className="form-label p-hint mb-1">From</label>
                                        <input className="form-control" type="text" name="originAddress" required />
                                    </div>
                                    <div className="col-12 col-md-6">
                                        <label className="form-label p-hint mb-1">To</label>
                                        <input className="form-control" type="text" name="destinationAddress" required />
                                    </div>
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Departure</label>
                                        <input className="form-control" type="datetime-local" name="departureTime" required />
                                    </div>
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Arrival</label>
                                        <input className="form-control" type="datetime-local" name="arrivalTime" required />
                                    </div>
                                    <div className="col-6 col-md-2">
                                        <label className="form-label p-hint mb-1">Seats</label>
                                        <input className="form-control" type="number" name="seat" min="1" required />
                                    </div>
                                    <div className="col-6 col-md-2">
                                        <label className="form-label p-hint mb-1">Price</label>
                                        <input className="form-control" type="number" name="price" step="0.01" min="0" required />
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

                    {/* Transport List */}
                    <section className="p-card p-3 p-md-4">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">Available transports</div>
                        </div>
                        <div className="table-responsive table-responsive-unclip">
                            <table className="table planora-table planora-table--compact table-hover align-middle mb-0">
                                <thead>
                                <tr>
                                    <th style={{ width: "80px" }}>ID</th>
                                    <th style={{ width: "160px" }}>Type</th>
                                    <th style={{ minWidth: "180px" }}>Company</th>
                                    <th style={{ minWidth: "120px", maxWidth: "160px" }}>From</th>
                                    <th style={{ minWidth: "120px", maxWidth: "160px" }}>To</th>
                                    <th style={{ minWidth: "170px", maxWidth: "190px" }}>Departure</th>
                                    <th style={{ minWidth: "170px", maxWidth: "190px" }}>Arrival</th>
                                    <th style={{ width: "110px" }}>Seats</th>
                                    <th style={{ width: "130px" }}>Price</th>
                                    <th style={{ width: "170px" }}>Status</th>
                                    {isAdmin && <th style={{ width: "210px" }}>Deletion</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {transports.length === 0 ? (
                                    <tr>
                                        <td colSpan="11" className="p-hint">
                                            No transports found.
                                        </td>
                                    </tr>
                                ) : (
                                    transports.map((t) => (
                                        <tr key={t.id}>
                                            <td>{t.id}</td>

                                            {/* Editable Fields */}
                                            {['transportType', 'company', 'originAddress', 'destinationAddress', 'departureTime', 'arrivalTime', 'seat', 'price', 'status'].map((field) => (
                                                <td key={field}>
                                                    {editingField?.id === t.id && editingField?.field === field ? (
                                                        <div>
                                                            {field === 'transportType' || field === 'status' ? (
                                                                <select
                                                                    value={editedTransportData[field] || t[field]}
                                                                    onChange={(e) => handleInputChange(e, field)}
                                                                >
                                                                    {(field === 'transportType' ? transportTypes : statuses).map((option) => (
                                                                        <option key={option} value={option}>
                                                                            {option}
                                                                        </option>
                                                                    ))}
                                                                </select>
                                                            ) : field === 'departureTime' || field === 'arrivalTime' ? (
                                                                <input
                                                                    type="datetime-local"
                                                                    value={editedTransportData[field] || t[field]}
                                                                    onChange={(e) => handleInputChange(e, field)}
                                                                />
                                                            ) : (
                                                                <input
                                                                    type={field === 'departureTime' || field === 'arrivalTime' ? 'datetime-local' : 'text'}
                                                                    value={editedTransportData[field] || t[field]}
                                                                    onChange={(e) => handleInputChange(e, field)}
                                                                />
                                                            )}
                                                            <button
                                                                onClick={() => handleEditTransport(t.id, field, editedTransportData[field] || t[field])}
                                                            >
                                                                Save
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        // Only allow editing for admins
                                                        isAdmin ? (
                                                            <div onClick={() => startEditing(t.id, field, t[field])}>
                                                                {field === 'departureTime' || field === 'arrivalTime'
                                                                    ? t[field]
                                                                    : t[field]}
                                                            </div>
                                                        ) : (
                                                            <div>{field === 'departureTime' || field === 'arrivalTime' ? t[field] : t[field]}</div>
                                                        )
                                                    )}
                                                </td>
                                            ))}

                                            {/* Admin Actions */}
                                            {isAdmin && (
                                                <td>
                                                    <button
                                                        className="btn btn-danger-soft btn-sm"
                                                        onClick={() => handleDeleteTransport(t.id)}
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

export default TransportPage;