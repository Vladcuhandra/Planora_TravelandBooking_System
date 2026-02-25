import { useMemo, useState } from "react";
import bg from "../assets/mountain_aurora.png";

export default function Main() {
    const [step, setStep] = useState(0); // 0=create, 1=trip, 2=booking
    const [activeTab, setActiveTab] = useState("transport"); // transport | accommodation

    // Trip model (matches Trip.java: title, description, startDate, endDate)
    const [trip, setTrip] = useState({
        title: "",
        description: "",
        startDate: "",
        endDate: "",
    });

    // Booking model (matches Booking.java: bookingType, totalPrice, status, trip, transport, accommodation, createdAt)
    const [booking, setBooking] = useState({
        bookingType: "TRANSPORT",
        totalPrice: "",
        status: "CONFIRMED",
        tripId: "",
        transportId: "",
        accommodationId: "",
        createdAt: new Date().toISOString().slice(0, 16),
    });

    const updateTrip = (key, value) => setTrip((p) => ({ ...p, [key]: value }));
    const updateBooking = (key, value) =>
        setBooking((p) => ({ ...p, [key]: value }));

    const tripReady = useMemo(() => {
        const ok =
            trip.title.trim() !== "" &&
            trip.description.trim() !== "" &&
            trip.startDate !== "" &&
            trip.endDate !== "";
        if (!ok) return false;

        // optional sanity check: start <= end (string compare works for datetime-local ISO format)
        return trip.startDate <= trip.endDate;
    }, [trip]);

    const switchTab = (tab) => {
        setActiveTab(tab);
        setBooking((p) => ({
            ...p,
            bookingType: tab === "transport" ? "TRANSPORT" : "ACCOMMODATION",
            transportId: tab === "transport" ? p.transportId : "",
            accommodationId: tab === "accommodation" ? p.accommodationId : "",
        }));
    };

    return (
        <div style={styles.page(bg)}>
            <div style={styles.overlay} />
            <div style={styles.content}>
                {/* STEP 0 */}
                {step === 0 && (
                    <button style={styles.primaryButton} onClick={() => setStep(1)}>
                        create
                    </button>
                )}

                {/* STEP 1: TRIP FORM (includes start/end datetime) */}
                {step === 1 && (
                    <div style={styles.card}>
                        <input
                            style={styles.input}
                            placeholder="title"
                            value={trip.title}
                            onChange={(e) => updateTrip("title", e.target.value)}
                        />

                        <textarea
                            style={styles.textarea}
                            placeholder="description"
                            value={trip.description}
                            onChange={(e) => updateTrip("description", e.target.value)}
                        />

                        <div style={styles.twoCol}>
                            <div>
                                <div style={styles.label}>startDate</div>
                                <input
                                    type="datetime-local"
                                    style={styles.input}
                                    value={trip.startDate}
                                    onChange={(e) => updateTrip("startDate", e.target.value)}
                                />
                            </div>

                            <div>
                                <div style={styles.label}>endDate</div>
                                <input
                                    type="datetime-local"
                                    style={styles.input}
                                    value={trip.endDate}
                                    onChange={(e) => updateTrip("endDate", e.target.value)}
                                />
                            </div>
                        </div>

                        {trip.startDate && trip.endDate && trip.startDate > trip.endDate && (
                            <div style={styles.warn}>End date must be after start date.</div>
                        )}

                        {tripReady && (
                            <button style={styles.nextButton} onClick={() => setStep(2)}>
                                next
                            </button>
                        )}
                    </div>
                )}

                {/* STEP 2: BOOKING FORM */}
                {step === 2 && (
                    <div style={styles.largePane}>
                        <div style={styles.headerRow}>
                            <div>
                                <div style={styles.h1}>{trip.title}</div>
                                <div style={styles.subtitle}>{trip.description}</div>
                                <div style={styles.meta}>
                                    Trip: {trip.startDate || "—"} → {trip.endDate || "—"}
                                </div>
                            </div>

                            <button style={styles.ghostButton} onClick={() => setStep(1)}>
                                back
                            </button>
                        </div>

                        <div style={styles.tabs}>
                            <button
                                style={{
                                    ...styles.tab,
                                    ...(activeTab === "transport" ? styles.tabActive : {}),
                                }}
                                onClick={() => switchTab("transport")}
                            >
                                Transport booking
                            </button>
                            <button
                                style={{
                                    ...styles.tab,
                                    ...(activeTab === "accommodation" ? styles.tabActive : {}),
                                }}
                                onClick={() => switchTab("accommodation")}
                            >
                                Accommodation booking
                            </button>
                        </div>

                        <div style={styles.grid}>
                            <Field label="bookingType">
                                <input readOnly value={booking.bookingType} style={styles.input} />
                            </Field>

                            <Field label="status">
                                <select
                                    value={booking.status}
                                    onChange={(e) => updateBooking("status", e.target.value)}
                                    style={styles.select}
                                >
                                    <option value="CONFIRMED">CONFIRMED</option>
                                    <option value="CANCELLED">CANCELLED</option>
                                </select>
                            </Field>

                            <Field label="totalPrice">
                                <input
                                    type="number"
                                    step="0.01"
                                    value={booking.totalPrice}
                                    onChange={(e) => updateBooking("totalPrice", e.target.value)}
                                    style={styles.input}
                                />
                            </Field>

                            <Field label="createdAt">
                                <input
                                    type="datetime-local"
                                    value={booking.createdAt}
                                    onChange={(e) => updateBooking("createdAt", e.target.value)}
                                    style={styles.input}
                                />
                            </Field>

                            <Field label="tripId" span={2}>
                                <input
                                    value={booking.tripId}
                                    onChange={(e) => updateBooking("tripId", e.target.value)}
                                    placeholder="Trip ID (if already saved)"
                                    style={styles.input}
                                />
                            </Field>

                            {activeTab === "transport" ? (
                                <Field label="transportId" span={2}>
                                    <input
                                        value={booking.transportId}
                                        onChange={(e) => updateBooking("transportId", e.target.value)}
                                        placeholder="Transport ID"
                                        style={styles.input}
                                    />
                                </Field>
                            ) : (
                                <Field label="accommodationId" span={2}>
                                    <input
                                        value={booking.accommodationId}
                                        onChange={(e) =>
                                            updateBooking("accommodationId", e.target.value)
                                        }
                                        placeholder="Accommodation ID"
                                        style={styles.input}
                                    />
                                </Field>
                            )}
                        </div>

                        <div style={styles.footerRow}>
                            <button
                                style={styles.primaryButton}
                                onClick={() => {
                                    // Example payloads (match your entities)
                                    const tripPayload = {
                                        title: trip.title,
                                        description: trip.description,
                                        startDate: trip.startDate,
                                        endDate: trip.endDate,
                                    };

                                    const bookingPayload = {
                                        bookingType: booking.bookingType,
                                        totalPrice: booking.totalPrice === "" ? 0 : Number(booking.totalPrice),
                                        status: booking.status,
                                        trip: booking.tripId ? { id: Number(booking.tripId) } : null,
                                        transport:
                                            booking.bookingType === "TRANSPORT" && booking.transportId
                                                ? { id: Number(booking.transportId) }
                                                : null,
                                        accommodation:
                                            booking.bookingType === "ACCOMMODATION" &&
                                            booking.accommodationId
                                                ? { id: Number(booking.accommodationId) }
                                                : null,
                                        createdAt: booking.createdAt || null,
                                    };

                                    console.log("Trip payload:", tripPayload);
                                    console.log("Booking payload:", bookingPayload);
                                    alert("Captured. Check console for payloads.");
                                }}
                            >
                                save booking
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

function Field({ label, children, span = 1 }) {
    return (
        <div style={{ gridColumn: span === 2 ? "span 2" : "span 1" }}>
            <div style={styles.label}>{label}</div>
            {children}
        </div>
    );
}

const styles = {
    page: (bgUrl) => ({
        position: "absolute",
        inset: 0,
        backgroundImage: `url(${bgUrl})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
    }),

    overlay: {
        position: "absolute",
        inset: 0,
        background:
            "linear-gradient(180deg, rgba(10,14,25,0.15), rgba(10,14,25,0.30))",
    },

    content: {
        position: "relative",
        height: "100%",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        padding: 24,
    },

    card: {
        background: "rgba(10,14,25,0.80)",
        padding: 20,
        borderRadius: 14,
        display: "flex",
        flexDirection: "column",
        gap: 12,
        width: "min(560px, 92vw)",
    },

    largePane: {
        background: "rgba(10,14,25,0.82)",
        padding: 20,
        borderRadius: 16,
        width: "min(980px, 92vw)",
        display: "flex",
        flexDirection: "column",
        gap: 14,
    },

    headerRow: {
        display: "flex",
        justifyContent: "space-between",
        gap: 12,
    },

    h1: { color: "white", fontSize: 20, fontWeight: 700 },
    subtitle: { color: "rgba(255,255,255,0.7)", fontSize: 13, marginTop: 4 },
    meta: { color: "rgba(255,255,255,0.65)", fontSize: 12, marginTop: 6 },

    tabs: { display: "flex", gap: 10 },
    tab: {
        padding: "8px 12px",
        borderRadius: 10,
        background: "transparent",
        color: "white",
        cursor: "pointer",
        border: "1px solid rgba(255,255,255,0.12)",
    },
    tabActive: { background: "rgba(255,255,255,0.15)" },

    grid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: 12,
    },

    twoCol: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: 12,
    },

    label: { fontSize: 12, color: "rgba(255,255,255,0.6)", marginBottom: 6 },

    warn: {
        color: "rgba(255,180,180,0.95)",
        fontSize: 12,
        paddingTop: 2,
    },

    input: {
        width: "100%",
        padding: 10,
        borderRadius: 10,
        background: "rgba(0,0,0,0.35)",
        color: "white",
        border: "1px solid rgba(255,255,255,0.15)",
        outline: "none",
    },

    select: {
        width: "100%",
        padding: 10,
        borderRadius: 10,
        background: "rgba(0,0,0,0.35)",
        color: "white",
        border: "1px solid rgba(255,255,255,0.15)",
        outline: "none",
    },

    textarea: {
        width: "100%",
        padding: 10,
        borderRadius: 10,
        background: "rgba(0,0,0,0.35)",
        color: "white",
        border: "1px solid rgba(255,255,255,0.15)",
        outline: "none",
        minHeight: 120,
        resize: "vertical",
    },

    primaryButton: {
        padding: "12px 28px",
        borderRadius: 10,
        background: "rgba(255,255,255,0.12)",
        color: "white",
        cursor: "pointer",
        border: "1px solid rgba(255,255,255,0.14)",
    },

    nextButton: {
        alignSelf: "flex-end",
        padding: "10px 16px",
        borderRadius: 10,
        background: "#6fd1ff",
        color: "#0b1020",
        fontWeight: 700,
        cursor: "pointer",
        border: "none",
    },

    ghostButton: {
        background: "transparent",
        color: "white",
        cursor: "pointer",
        border: "1px solid rgba(255,255,255,0.14)",
        borderRadius: 10,
        padding: "10px 12px",
        height: "fit-content",
    },

    footerRow: { display: "flex", justifyContent: "flex-end" },
};