import { useMemo, useState } from "react";
import bg from "../assets/mountain_aurora.png";

export default function Main() {
    const [step, setStep] = useState(0); // 0=create, 1=details, 2=booking
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [activeTab, setActiveTab] = useState("transport");

    const [booking, setBooking] = useState({
        bookingType: "TRANSPORT",
        startDate: "",
        endDate: "",
        totalPrice: "",
        status: "CONFIRMED",
        tripId: "",
        transportId: "",
        accommodationId: "",
        createdAt: new Date().toISOString().slice(0, 16),
    });

    const isReadyForNext = useMemo(
        () => title.trim() !== "" && description.trim() !== "",
        [title, description]
    );

    const updateBooking = (key, value) =>
        setBooking((p) => ({ ...p, [key]: value }));

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
                {step === 0 && (
                    <button style={styles.primaryButton} onClick={() => setStep(1)}>
                        create
                    </button>
                )}

                {step === 1 && (
                    <div style={styles.card}>
                        <input
                            style={styles.input}
                            placeholder="title"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                        />

                        <textarea
                            style={styles.textarea}
                            placeholder="description"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />

                        {isReadyForNext && (
                            <button style={styles.nextButton} onClick={() => setStep(2)}>
                                next
                            </button>
                        )}
                    </div>
                )}

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
                                    style={styles.input}
                                />
                            </Field>

                            <Field label="tripId">
                                <input
                                    value={booking.tripId}
                                    onChange={(e) => updateBooking("tripId", e.target.value)}
                                    style={styles.input}
                                />
                            </Field>

                            {activeTab === "transport" ? (
                                <Field label="transportId" span={2}>
                                    <input
                                        value={booking.transportId}
                                        onChange={(e) =>
                                            updateBooking("transportId", e.target.value)
                                        }
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
                                        style={styles.input}
                                    />
                                </Field>
                            )}
                        </div>

                        <div style={styles.footerRow}>
                            <button style={styles.primaryButton}>save booking</button>
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
    page: (bg) => ({
        position: "absolute",
        inset: 0,
        backgroundImage: `url(${bg})`,
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
    },

    card: {
        background: "rgba(10,14,25,0.80)",
        padding: 20,
        borderRadius: 14,
        display: "flex",
        flexDirection: "column",
        gap: 12,
        width: 500,
    },

    largePane: {
        background: "rgba(10,14,25,0.82)",
        padding: 20,
        borderRadius: 16,
        width: 980,
        display: "flex",
        flexDirection: "column",
        gap: 14,
    },

    headerRow: {
        display: "flex",
        justifyContent: "space-between",
    },

    h1: { color: "white", fontSize: 20, fontWeight: 700 },
    subtitle: { color: "rgba(255,255,255,0.7)", fontSize: 13 },

    tabs: { display: "flex", gap: 10 },
    tab: {
        padding: "8px 12px",
        borderRadius: 10,
        background: "transparent",
        color: "white",
        cursor: "pointer",
    },
    tabActive: { background: "rgba(255,255,255,0.15)" },

    grid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: 12,
    },

    label: { fontSize: 12, color: "rgba(255,255,255,0.6)" },

    input: {
        padding: 10,
        borderRadius: 10,
        background: "rgba(0,0,0,0.35)",
        color: "white",
        border: "1px solid rgba(255,255,255,0.15)",
    },

    select: {
        padding: 10,
        borderRadius: 10,
        background: "rgba(0,0,0,0.35)",
        color: "white",
    },

    textarea: {
        padding: 10,
        borderRadius: 10,
        background: "rgba(0,0,0,0.35)",
        color: "white",
        minHeight: 120,
    },

    primaryButton: {
        padding: "12px 28px",
        borderRadius: 10,
        background: "rgba(255,255,255,0.12)",
        color: "white",
        cursor: "pointer",
    },

    nextButton: {
        alignSelf: "flex-end",
        padding: "10px 16px",
        borderRadius: 10,
        background: "#6fd1ff",
        color: "#0b1020",
        fontWeight: 600,
    },

    ghostButton: {
        background: "transparent",
        color: "white",
        cursor: "pointer",
    },

    footerRow: { display: "flex", justifyContent: "flex-end" },
};