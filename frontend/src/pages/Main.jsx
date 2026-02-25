import { useMemo, useState } from "react";

export default function Main() {
    // Step 0/1/2
    const [step, setStep] = useState(0); // 0=create, 1=details, 2=booking

    // Step 1: title/description
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");

    // Step 2: booking type tab
    const [activeTab, setActiveTab] = useState("transport"); // "transport" | "accommodation"

    // Booking form (no id)
    const [booking, setBooking] = useState({
        bookingType: "TRANSPORT", // TRANSPORT | ACCOMMODATION
        startDate: "",
        endDate: "",
        totalPrice: "",
        status: "CONFIRMED", // CONFIRMED | CANCELLED
        tripId: "",
        transportId: "",
        accommodationId: "",
        createdAt: new Date().toISOString().slice(0, 16),
    });

    const isReadyForNext = useMemo(() => {
        return title.trim() !== "" && description.trim() !== "";
    }, [title, description]);

    const handleNext = () => {
        if (!isReadyForNext) return;
        setStep(2);
    };

    const updateBooking = (key, value) => {
        setBooking((prev) => ({ ...prev, [key]: value }));
    };

    const switchTab = (tab) => {
        setActiveTab(tab);
        if (tab === "transport") {
            setBooking((prev) => ({
                ...prev,
                bookingType: "TRANSPORT",
                accommodationId: "",
            }));
        } else {
            setBooking((prev) => ({
                ...prev,
                bookingType: "ACCOMMODATION",
                transportId: "",
            }));
        }
    };

    const isTransport = activeTab === "transport";

    return (
        <div style={styles.page}>
            {/* STEP 0 */}
            {step === 0 && (
                <button style={styles.primaryButton} onClick={() => setStep(1)}>
                    create
                </button>
            )}

            {/* STEP 1 */}
            {step === 1 && (
                <div style={styles.card}>
                    <input
                        type="text"
                        placeholder="title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        style={styles.input}
                    />

                    <textarea
                        placeholder="description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        style={styles.textarea}
                    />

                    {isReadyForNext && (
                        <button style={styles.nextButton} onClick={handleNext}>
                            next
                        </button>
                    )}
                </div>
            )}

            {/* STEP 2 */}
            {step === 2 && (
                <div style={styles.largePane}>
                    <div style={styles.headerRow}>
                        <div>
                            <div style={styles.h1}>{title}</div>
                            <div style={styles.subtitle}>{description}</div>
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

                    <div style={styles.tabBody}>
                        <div style={styles.form}>
                            <div style={styles.grid}>
                                <Field label="bookingType">
                                    <input
                                        value={booking.bookingType}
                                        readOnly
                                        style={{ ...styles.input, opacity: 0.9 }}
                                    />
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

                                <Field label="startDate">
                                    <input
                                        type="datetime-local"
                                        value={booking.startDate}
                                        onChange={(e) => updateBooking("startDate", e.target.value)}
                                        style={styles.input}
                                    />
                                </Field>

                                <Field label="endDate">
                                    <input
                                        type="datetime-local"
                                        value={booking.endDate}
                                        onChange={(e) => updateBooking("endDate", e.target.value)}
                                        style={styles.input}
                                    />
                                </Field>

                                <Field label="totalPrice">
                                    <input
                                        type="number"
                                        step="0.01"
                                        value={booking.totalPrice}
                                        onChange={(e) => updateBooking("totalPrice", e.target.value)}
                                        placeholder="0.00"
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
                                        placeholder="Trip ID"
                                        style={styles.input}
                                    />
                                </Field>

                                {isTransport ? (
                                    <Field label="transportId" span={2}>
                                        <input
                                            value={booking.transportId}
                                            onChange={(e) =>
                                                updateBooking("transportId", e.target.value)
                                            }
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
                                        const payload = {
                                            bookingType: booking.bookingType,
                                            startDate: booking.startDate || null,
                                            endDate: booking.endDate || null,
                                            totalPrice:
                                                booking.totalPrice === "" ? 0 : Number(booking.totalPrice),
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

                                        console.log("Booking payload:", payload);
                                        alert("Booking form captured (check console).");
                                    }}
                                >
                                    save booking
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

function Field({ label, children, span = 1 }) {
    return (
        <div style={{ ...styles.field, gridColumn: span === 2 ? "span 2" : "span 1" }}>
            <div style={styles.label}>{label}</div>
            {children}
        </div>
    );
}

const styles = {
    page: {
        width: "100%",
        minHeight: "100%",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "24px",
    },

    card: {
        width: "min(520px, 100%)",
        padding: "20px",
        borderRadius: "14px",
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(255,255,255,0.05)",
        boxShadow: "0 12px 40px rgba(0,0,0,0.35)",
        display: "flex",
        flexDirection: "column",
        gap: "12px",
    },

    largePane: {
        width: "min(980px, 100%)",
        padding: "20px",
        borderRadius: "16px",
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(255,255,255,0.05)",
        boxShadow: "0 12px 40px rgba(0,0,0,0.35)",
        display: "flex",
        flexDirection: "column",
        gap: "14px",
    },

    headerRow: {
        display: "flex",
        alignItems: "flex-start",
        justifyContent: "space-between",
        gap: "12px",
    },

    h1: {
        color: "white",
        fontSize: "20px",
        fontWeight: 700,
        lineHeight: 1.2,
    },

    subtitle: {
        marginTop: "6px",
        color: "rgba(255,255,255,0.7)",
        fontSize: "13px",
    },

    tabs: {
        display: "flex",
        gap: "10px",
        padding: "6px",
        borderRadius: "12px",
        border: "1px solid rgba(255,255,255,0.10)",
        background: "rgba(0,0,0,0.18)",
        width: "fit-content",
    },

    tab: {
        padding: "8px 12px",
        borderRadius: "10px",
        border: "1px solid transparent",
        background: "transparent",
        color: "rgba(255,255,255,0.8)",
        cursor: "pointer",
        fontWeight: 600,
    },

    tabActive: {
        background: "rgba(255,255,255,0.08)",
        border: "1px solid rgba(255,255,255,0.12)",
        color: "white",
    },

    tabBody: {
        paddingTop: "6px",
    },

    form: {
        display: "flex",
        flexDirection: "column",
        gap: "14px",
    },

    grid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "12px",
    },

    field: {
        display: "flex",
        flexDirection: "column",
        gap: "6px",
    },

    label: {
        fontSize: "12px",
        color: "rgba(255,255,255,0.65)",
    },

    input: {
        padding: "10px 12px",
        borderRadius: "10px",
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(0,0,0,0.25)",
        color: "white",
        outline: "none",
    },

    select: {
        padding: "10px 12px",
        borderRadius: "10px",
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(0,0,0,0.25)",
        color: "white",
        outline: "none",
    },

    textarea: {
        padding: "10px 12px",
        borderRadius: "10px",
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(0,0,0,0.25)",
        color: "white",
        outline: "none",
        minHeight: "140px",
        resize: "vertical",
    },

    footerRow: {
        display: "flex",
        justifyContent: "flex-end",
    },

    primaryButton: {
        padding: "12px 28px",
        fontSize: "16px",
        cursor: "pointer",
        borderRadius: "10px",
        border: "1px solid rgba(255,255,255,0.18)",
        background: "rgba(255,255,255,0.06)",
        color: "white",
    },

    nextButton: {
        marginTop: "8px",
        padding: "10px",
        borderRadius: "10px",
        border: "none",
        background: "linear-gradient(135deg, #4f8cff, #6fd1ff)",
        color: "#0b1020",
        fontWeight: "600",
        cursor: "pointer",
        alignSelf: "flex-end",
    },

    ghostButton: {
        padding: "10px 12px",
        borderRadius: "10px",
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(255,255,255,0.04)",
        color: "white",
        cursor: "pointer",
        height: "fit-content",
    },
};