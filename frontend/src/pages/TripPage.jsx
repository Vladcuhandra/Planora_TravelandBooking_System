import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

const TripPage = () => {
    const navigate = useNavigate();
    const [trips, setTrips] = useState([]);
    const [users, setUsers] = useState([]);
    const [error, setError] = useState(null);
    const [openCreate, setOpenCreate] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const [editingField, setEditingField] = useState(null);
    const [editedTripData, setEditedTripData] = useState({});
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [user, setUser] = useState(null);

    useEffect(() => {
        fetchUserProfile();
        fetchTrips();
        fetchUsers();
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

    const fetchTrips = async () => {
        try {
            const response = await apiFetch(`/api/trips?page=${currentPage}`);
            const data = await response.json();
            setTrips(data.trips || []);
            setTotalPages(data.totalPages || 0);
            console.log("Fetched trips: ", data);
        } catch (error) {
            setError("Failed to fetch trips.");
        }
    };

    const fetchUsers = async () => {
        if (isAdmin) {
            try {
                const response = await apiFetch(`/api/admin`);
                const data = await response.json();
                console.log("Fetched users: ", data);
                setUsers(data);
            } catch (error) {
                setError("Failed to fetch users.");
            }
        }
    };

    const handleCreateTrip = async (event) => {
        event.preventDefault();
        const form = new FormData(event.target);
        const tripData = Object.fromEntries(form.entries());
        tripData.startDate = new Date(tripData.startDate).toISOString();
        tripData.endDate = new Date(tripData.endDate).toISOString();
        tripData.userId = isAdmin ? tripData.userId : user.id;

        try {
            const response = await apiFetch("/api/trips/save", {
                method: "POST",
                body: JSON.stringify(tripData),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 201) {
                fetchTrips();
                setOpenCreate(false);
            } else {
                const data = await response.json();
                setError(data.message || "Error creating trip.");
            }
        } catch (error) {
            setError("Failed to create trip.");
        }
    };

    const startEditing = (id, field, currentValue) => {
        setEditingField({ id, field });
        setEditedTripData({ ...editedTripData, [field]: currentValue });
    };

    const handleInputChange = (e, field) => {
        let value = e.target.value;
        setEditedTripData({ ...editedTripData, [field]: value });
    };

    const handleEditTrip = async (id, field, value) => {
        try {
            const updatedTrip = trips.find((t) => t.id === id);
            const tripData = { ...updatedTrip, [field]: value };

            const response = await apiFetch(`/api/trips/edit`, {
                method: "POST",
                body: JSON.stringify(tripData),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 200) {
                fetchTrips();
                setEditingField(null);
                setEditedTripData({});
            } else {
                const data = await response.json();
                setError(data.message || "Error editing trip.");
            }
        } catch (error) {
            setError("Failed to edit trip.");
        }
    };

    const handleDeleteTrip = async (id) => {
        if (window.confirm("Are you sure you want to delete this trip?")) {
            try {
                const response = await apiFetch(`/api/trips/${id}`, { method: "DELETE" });
                if (response.status === 200) {
                    fetchTrips();
                } else {
                    const data = await response.json();
                    setError(data.message || "Error deleting trip.");
                }
            } catch (error) {
                setError("Failed to delete trip.");
            }
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
                            <div className="p-hint">{isAdmin ? "Create, edit, and delete trips" : "View and manage your trips"}</div>
                        </div>
                    </div>

                    {/* Create Trip - Admin only */}
                    {isAdmin && (
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
                                    {/* Form fields */}
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
                                                {users.map((user) => (
                                                    <option key={user.id} value={user.id}>
                                                        {user.email}
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
                    )}

                    {/* Trips List */}
                    <section className="p-card p-3 p-md-4">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">Your trips</div>
                        </div>
                        <div className="table-responsive table-responsive-unclip">
                            <table className="table planora-table planora-table--compact table-hover align-middle mb-0">
                                <thead>
                                <tr>
                                    <th style={{ width: "80px" }}>ID</th>
                                    <th style={{ minWidth: "180px" }}>Title</th>
                                    <th style={{ minWidth: "240px" }}>Description</th>
                                    <th style={{ minWidth: "190px" }}>Start Date</th>
                                    <th style={{ minWidth: "190px" }}>End Date</th>
                                    <th style={{ minWidth: "240px" }}>User</th>
                                    {isAdmin && <th style={{ width: "210px" }}>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {trips.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="p-hint">
                                            No trips found.
                                        </td>
                                    </tr>
                                ) : (
                                    trips.map((trip) => (
                                        <tr key={trip.id}>
                                            <td>{trip.id}</td>
                                            <td>
                                                <input
                                                    className="form-control"
                                                    type="text"
                                                    name="title"
                                                    value={editingField?.id === trip.id && editingField?.field === "title" ? editedTripData.title : trip.title}
                                                    onChange={(e) => handleInputChange(e, "title")}
                                                    onBlur={() => handleEditTrip(trip.id, "title", editedTripData.title)}
                                                    disabled={editingField?.id !== trip.id}
                                                    required
                                                />
                                            </td>
                                            <td>
                                                <input
                                                    className="form-control"
                                                    type="text"
                                                    name="description"
                                                    value={editingField?.id === trip.id && editingField?.field === "description" ? editedTripData.description : trip.description}
                                                    onChange={(e) => handleInputChange(e, "description")}
                                                    onBlur={() => handleEditTrip(trip.id, "description", editedTripData.description)}
                                                    disabled={editingField?.id !== trip.id}
                                                    required
                                                />
                                            </td>
                                            <td>
                                                <input
                                                    className="form-control"
                                                    type="datetime-local"
                                                    name="startDate"
                                                    value={editingField?.id === trip.id && editingField?.field === "startDate" ? editedTripData.startDate : trip.startDate}
                                                    onChange={(e) => handleInputChange(e, "startDate")}
                                                    onBlur={() => handleEditTrip(trip.id, "startDate", editedTripData.startDate)}
                                                    disabled={editingField?.id !== trip.id}
                                                    required
                                                />
                                            </td>
                                            <td>
                                                <input
                                                    className="form-control"
                                                    type="datetime-local"
                                                    name="endDate"
                                                    value={editingField?.id === trip.id && editingField?.field === "endDate" ? editedTripData.endDate : trip.endDate}
                                                    onChange={(e) => handleInputChange(e, "endDate")}
                                                    onBlur={() => handleEditTrip(trip.id, "endDate", editedTripData.endDate)}
                                                    disabled={editingField?.id !== trip.id}
                                                    required
                                                />
                                            </td>
                                            <td>{trip.userId}</td>
                                            {isAdmin && (
                                                <td>
                                                    <button
                                                        className="btn btn-soft btn-sm"
                                                        onClick={() => startEditing(trip.id, "title", trip.title)}
                                                    >
                                                        Edit
                                                    </button>
                                                    <button
                                                        className="btn btn-danger-soft btn-sm"
                                                        onClick={() => handleDeleteTrip(trip.id)}
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
