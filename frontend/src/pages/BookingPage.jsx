import React, { useState, useEffect } from "react";
import { apiFetch } from "../api/http";

const BookingsPage = () => {
    const [bookings, setBookings] = useState([]);
    const [selectedType, setSelectedType] = useState("");
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [trips, setTrips] = useState([]);
    const [transports, setTransports] = useState([]);
    const [accommodations, setAccommodations] = useState([]);
    const [bookingDto, setBookingDto] = useState({
        tripId: "",
        status: "",
        startDate: "",
        endDate: "",
        transportId: "",
        accommodationId: "",
    });
    const [error, setError] = useState("");

    useEffect(() => {
        fetchBookings();
        fetchTrips();
        fetchTransports();
        fetchAccommodations();
    }, [currentPage, selectedType]);

    const fetchBookings = async () => {
        try {
            const response = await apiFetch(`/api/bookings?page=${currentPage}&type=${selectedType}`);
            setBookings(response.content);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error("Error fetching bookings:", error);
        }
    };

    const fetchTrips = async () => {
        try {
            const response = await apiFetch("/api/trips");
            setTrips(response);
        } catch (error) {
            console.error("Error fetching trips:", error);
        }
    };

    const fetchTransports = async () => {
        try {
            const response = await apiFetch("/api/transports");
            setTransports(response);
        } catch (error) {
            console.error("Error fetching transports:", error);
        }
    };

    const fetchAccommodations = async () => {
        try {
            const response = await apiFetch("/api/accommodations");
            setAccommodations(response);
        } catch (error) {
            console.error("Error fetching accommodations:", error);
        }
    };

    const handleCreateBooking = async (event) => {
        event.preventDefault();
        try {
            await apiFetch("/api/bookings/save", {
                method: "POST",
                body: JSON.stringify(bookingDto),
            });
            fetchBookings(); // Reload the bookings after creation
        } catch (err) {
            setError("Error creating booking. Please try again.");
        }
    };

    const handleEditBooking = async (id, updatedBooking) => {
        try {
            await apiFetch(`/api/bookings/edit/${id}`, {
                method: "POST",
                body: JSON.stringify(updatedBooking),
            });
            fetchBookings();
        } catch (err) {
            setError("Error updating booking. Please try again.");
        }
    };

    const handleDeleteBooking = async (id) => {
        try {
            await apiFetch(`/api/bookings/${id}`, {
                method: "DELETE",
            });
            fetchBookings();
        } catch (err) {
            setError("Error deleting booking. Please try again.");
        }
    };

    const handleBulkDelete = async (selectedIds) => {
        try {
            await apiFetch("/api/bookings/bulk-delete", {
                method: "POST",
                body: JSON.stringify({ ids: selectedIds }),
            });
            fetchBookings();
        } catch (err) {
            setError("Error deleting selected bookings. Please try again.");
        }
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

                    {/* Create Booking Section */}
                    <section className="p-card p-3 p-md-4 mb-3">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">Create booking</div>

                            <button
                                className="btn btn-soft btn-sm"
                                type="button"
                                data-bs-toggle="collapse"
                                data-bs-target="#createBookingCollapse"
                                aria-expanded="false"
                            >
                                Show
                            </button>
                        </div>

                        <div className="collapse" id="createBookingCollapse">
                            {/* Booking Type Selection */}
                            <form onSubmit={handleCreateBooking} className="row g-2 g-md-3 align-items-end mb-3">
                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Booking Type</label>
                                    <select
                                        className="form-select"
                                        value={selectedType}
                                        onChange={(e) => setSelectedType(e.target.value)}
                                    >
                                        <option value="">-- Select --</option>
                                        <option value="TRANSPORT">TRANSPORT</option>
                                        <option value="ACCOMMODATION">ACCOMMODATION</option>
                                    </select>
                                </div>

                                <div className="col-12 col-md-2">
                                    <button className="btn btn-soft w-100" type="submit">Apply</button>
                                </div>
                            </form>

                            {/* Booking Form */}
                            <form onSubmit={handleCreateBooking} className="row g-2 g-md-3">
                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Trip</label>
                                    <select
                                        className="form-select"
                                        value={bookingDto.tripId}
                                        onChange={(e) => setBookingDto({ ...bookingDto, tripId: e.target.value })}
                                        required
                                    >
                                        <option value="">-- Select --</option>
                                        {trips.map((trip) => (
                                            <option key={trip.id} value={trip.id}>
                                                {trip.title} (id={trip.id})
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Status</label>
                                    <select
                                        className="form-select"
                                        value={bookingDto.status}
                                        onChange={(e) => setBookingDto({ ...bookingDto, status: e.target.value })}
                                        required
                                    >
                                        <option value="">-- Select --</option>
                                        <option value="PENDING">PENDING</option>
                                        <option value="CONFIRMED">CONFIRMED</option>
                                        <option value="CANCELLED">CANCELLED</option>
                                    </select>
                                </div>

                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">Start date</label>
                                    <input
                                        className="form-control"
                                        type="datetime-local"
                                        value={bookingDto.startDate}
                                        onChange={(e) => setBookingDto({ ...bookingDto, startDate: e.target.value })}
                                    />
                                </div>

                                <div className="col-12 col-md-4">
                                    <label className="form-label p-hint mb-1">End date</label>
                                    <input
                                        className="form-control"
                                        type="datetime-local"
                                        value={bookingDto.endDate}
                                        onChange={(e) => setBookingDto({ ...bookingDto, endDate: e.target.value })}
                                    />
                                </div>

                                {/* Conditional Form Fields */}
                                {selectedType === "TRANSPORT" && (
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Transport</label>
                                        <select
                                            className="form-select"
                                            value={bookingDto.transportId}
                                            onChange={(e) => setBookingDto({ ...bookingDto, transportId: e.target.value })}
                                            required
                                        >
                                            <option value="">-- Select --</option>
                                            {transports.map((transport) => (
                                                <option key={transport.id} value={transport.id}>
                                                    {transport.company} ({transport.originAddress} → {transport.destinationAddress})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                )}

                                {selectedType === "ACCOMMODATION" && (
                                    <div className="col-12 col-md-4">
                                        <label className="form-label p-hint mb-1">Accommodation</label>
                                        <select
                                            className="form-select"
                                            value={bookingDto.accommodationId}
                                            onChange={(e) => setBookingDto({ ...bookingDto, accommodationId: e.target.value })}
                                            required
                                        >
                                            <option value="">-- Select --</option>
                                            {accommodations.map((acc) => (
                                                <option key={acc.id} value={acc.id}>
                                                    {acc.name} ({acc.address})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                )}

                                <div className="col-12 d-flex justify-content-end">
                                    <button className="btn btn-planora px-4" type="submit">Create</button>
                                </div>
                                {error && <div className="text-danger small">{error}</div>}
                            </form>
                        </div>
                    </section>

                    {/* Bookings Table */}
                    <section className="p-card p-3 p-md-4">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                            <div className="p-subtitle fw-semibold">All bookings</div>
                            <div className="p-hint">Edit in table → Save</div>
                        </div>

                        <div className="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
                            <div className="p-hint m-0">Select rows and delete them.</div>
                            <button
                                className="btn btn-danger-soft btn-sm"
                                onClick={() => handleBulkDelete(bookings.filter(b => b.selected).map(b => b.id))}
                                disabled={bookings.filter(b => b.selected).length === 0}
                            >
                                Delete selected
                            </button>
                        </div>

                        <div className="table-responsive">
                            <table className="table table-hover align-middle mb-0">
                                <thead>
                                <tr>
                                    <th style={{ width: "44px" }}>
                                        <input
                                            id="bookingSelectAll"
                                            className="form-check-input"
                                            type="checkbox"
                                            onChange={() => {
                                                const newBookings = bookings.map((b) => ({
                                                    ...b,
                                                    selected: !b.selected,
                                                }));
                                                setBookings(newBookings);
                                            }}
                                        />
                                    </th>
                                    <th style={{ width: "70px" }}>ID</th>
                                    <th>Trip</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                    <th>Start</th>
                                    <th>End</th>
                                    <th>Transport</th>
                                    <th>Accommodation</th>
                                    <th>Total</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {bookings.map((booking) => (
                                    <tr key={booking.id}>
                                        <td>
                                            <input
                                                className="form-check-input"
                                                type="checkbox"
                                                checked={booking.selected}
                                                onChange={() => {
                                                    const newBookings = bookings.map((b) =>
                                                        b.id === booking.id ? { ...b, selected: !b.selected } : b
                                                    );
                                                    setBookings(newBookings);
                                                }}
                                            />
                                        </td>
                                        <td>{booking.id}</td>
                                        <td>{booking.tripTitle}</td>
                                        <td>{booking.bookingType}</td>
                                        <td>{booking.status}</td>
                                        <td>{new Date(booking.startDate).toLocaleString()}</td>
                                        <td>{new Date(booking.endDate).toLocaleString()}</td>
                                        <td>{booking.transportCompany || "--"}</td>
                                        <td>{booking.accommodationName || "--"}</td>
                                        <td>{booking.totalPrice}</td>
                                        <td>
                                            <button
                                                className="btn btn-soft btn-sm"
                                                onClick={() => handleEditBooking(booking.id, booking)}
                                            >
                                                Save
                                            </button>
                                            <button
                                                className="btn btn-danger-soft btn-sm"
                                                onClick={() => handleDeleteBooking(booking.id)}
                                            >
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        <div className="d-flex justify-content-between mt-3">
                            <button
                                className="btn btn-soft"
                                disabled={currentPage === 0}
                                onClick={() => setCurrentPage(currentPage - 1)}
                            >
                                ← Previous
                            </button>
                            <div className="p-hint text-center flex-grow-1">
                                Page {currentPage + 1} of {totalPages}
                            </div>
                            <button
                                className="btn btn-soft"
                                disabled={currentPage + 1 >= totalPages}
                                onClick={() => setCurrentPage(currentPage + 1)}
                            >
                                Next →
                            </button>
                        </div>
                    </section>
                </main>
            </div>
        </div>
    );
};

export default BookingsPage;
