import { useEffect, useMemo, useState } from "react";
import { apiFetch } from "../api/http";
import bg from "../assets/mountain_aurora.png";

async function postJson(path, body) {
    const res = await apiFetch(path, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });

    const text = await res.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!res.ok) {
        const msg =
            (data && data.message) ||
            (typeof data === "string" ? data : null) ||
            `Request failed: ${res.status}`;
        throw new Error(msg);
    }

    return data;
}

// Try multiple GET endpoints until one works
async function getJsonFirstWorking(paths) {
    let lastErr = null;
    for (const path of paths) {
        try {
            const res = await apiFetch(path, { method: "GET" });
            if (!res.ok) {
                lastErr = new Error(`GET ${path} -> ${res.status}`);
                continue;
            }
            const data = await res.json();
            return { path, data };
        } catch (e) {
            lastErr = e;
        }
    }
    throw lastErr || new Error("No working endpoint");
}

function normalizeList(data) {
    if (!data) return [];
    if (Array.isArray(data)) return data;

    // common wrappers
    if (Array.isArray(data.items)) return data.items;
    if (Array.isArray(data.data)) return data.data;
    if (Array.isArray(data.list)) return data.list;
    if (Array.isArray(data.transports)) return data.transports;
    if (Array.isArray(data.accommodations)) return data.accommodations;
    if (Array.isArray(data.content)) return data.content; // pageable

    return [];
}

export default function Main() {
    const [step, setStep] = useState(0); // 0=create, 1=trip, 2=details+booking
    const [activeTab, setActiveTab] = useState("transport"); // transport | accommodation

    // TRIP (TripDTO)
    const [trip, setTrip] = useState({
        title: "",
        description: "",
        startDate: "",
        endDate: "",
    });

    // BOOKING (BookingDTO minimal)
    const [booking, setBooking] = useState({
        status: "CONFIRMED",
        totalPrice: "",
    });

    // Existing resources to select from
    const [transports, setTransports] = useState([]);
    const [accommodations, setAccommodations] = useState([]);
    const [selectedTransportId, setSelectedTransportId] = useState("");
    const [selectedAccommodationId, setSelectedAccommodationId] = useState("");

    const [loadingList, setLoadingList] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    const updateTrip = (k, v) => setTrip((p) => ({ ...p, [k]: v }));
    const updateBooking = (k, v) => setBooking((p) => ({ ...p, [k]: v }));

    const tripReady = useMemo(() => {
        return (
            trip.title.trim() &&
            trip.description.trim() &&
            trip.startDate &&
            trip.endDate &&
            trip.startDate <= trip.endDate
        );
    }, [trip]);

    const onNext = () => {
        if (!tripReady) return;
        setStep(2);
    };

    const switchTab = (tab) => {
        setActiveTab(tab);
        setError("");
    };

    // Load existing transports/accommodations when reaching step 2
    useEffect(() => {
        if (step !== 2) return;

        const load = async () => {
            setError("");
            setLoadingList(true);
            try {
                const transportPaths = [
                    "/api/transports",
                    "/api/transports/all",
                    "/api/transports/list",
                    "/api/transports/getAll",
                ];
                const accommodationPaths = [
                    "/api/accommodations",
                    "/api/accommodations/all",
                    "/api/accommodations/list",
                    "/api/accommodations/getAll",
                ];

                const [tRes, aRes] = await Promise.allSettled([
                    getJsonFirstWorking(transportPaths),
                    getJsonFirstWorking(accommodationPaths),
                ]);

                if (tRes.status === "fulfilled") {
                    setTransports(normalizeList(tRes.value.data));
                } else {
                    setTransports([]);
                }

                if (aRes.status === "fulfilled") {
                    setAccommodations(normalizeList(aRes.value.data));
                } else {
                    setAccommodations([]);
                }
            } catch (e) {
                setError(
                    "Could not load transport/accommodation lists. Check your GET endpoints in controllers."
                );
            } finally {
                setLoadingList(false);
            }
        };

        load();
    }, [step]);

    const selectionReady =
        activeTab === "transport"
            ? String(selectedTransportId || "").trim() !== ""
            : String(selectedAccommodationId || "").trim() !== "";

    const saveAll = async () => {
        setError("");
        setSaving(true);

        try {
            if (!tripReady) {
                throw new Error("Please complete the trip details first.");
            }

            if (!selectionReady) {
                throw new Error(
                    activeTab === "transport"
                        ? "Please select an existing transport."
                        : "Please select an existing accommodation."
                );
            }

            // Trip payload (TripDTO)
            const tripPayload = {
                title: trip.title,
                description: trip.description,
                startDate: trip.startDate,
                endDate: trip.endDate,
            };

            // Booking payload (BookingDTO)
            // NOTE: we send BOTH status + bookingStatus to avoid mismatch with your DTO.
            const bookingPayload = {
                bookingType: activeTab === "transport" ? "TRANSPORT" : "ACCOMMODATION",

                status: booking.status,
                bookingStatus: booking.status,

                startDate: trip.startDate,
                endDate: trip.endDate,

                createdAt: new Date().toISOString(),

                totalPrice: booking.totalPrice === "" ? 0 : Number(booking.totalPrice),

                transportId: activeTab === "transport" ? Number(selectedTransportId) : null,
                accommodationId:
                    activeTab === "accommodation" ? Number(selectedAccommodationId) : null,
            };

            await postJson("/api/main/save-trip-and-booking", {
                trip: tripPayload,
                booking: bookingPayload,
            });

            alert(
                "Saved: Trip + Booking (linked to existing " +
                (activeTab === "transport" ? "Transport" : "Accommodation") +
                ") "
            );

            // reset
            setStep(0);
            setTrip({ title: "", description: "", startDate: "", endDate: "" });
            setBooking({ status: "CONFIRMED", totalPrice: "" });
            setSelectedTransportId("");
            setSelectedAccommodationId("");
        } catch (e) {
            setError(e.message || "Failed to save");
        } finally {
            setSaving(false);
        }
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
                            placeholder="Trip title"
                            value={trip.title}
                            onChange={(e) => updateTrip("title", e.target.value)}
                        />

                        <textarea
                            style={styles.textarea}
                            placeholder="Trip description"
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
                            <button style={styles.nextButton} onClick={onNext}>
                                next
                            </button>
                        )}
                    </div>
                )}

                {step === 2 && (
                    <div style={styles.largePane}>
                        <div style={styles.headerRow}>
                            <div>
                                <div style={styles.h1}>{trip.title}</div>
                                <div style={styles.subtitle}>{trip.description}</div>
                                <div style={styles.meta}>
                                    Trip: {trip.startDate} → {trip.endDate}
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
                                Transport
                            </button>
                            <button
                                style={{
                                    ...styles.tab,
                                    ...(activeTab === "accommodation" ? styles.tabActive : {}),
                                }}
                                onClick={() => switchTab("accommodation")}
                            >
                                Accommodation
                            </button>
                        </div>

                        {/* Booking fields */}
                        <div style={styles.grid}>
                            <Field label="booking status">
                                <select
                                    style={styles.select}
                                    value={booking.status}
                                    onChange={(e) => updateBooking("status", e.target.value)}
                                >
                                    <option value="CONFIRMED">CONFIRMED</option>
                                    <option value="CANCELLED">CANCELLED</option>
                                </select>
                            </Field>

                            <Field label="totalPrice">
                                <input
                                    style={styles.input}
                                    type="number"
                                    step="0.01"
                                    value={booking.totalPrice}
                                    onChange={(e) => updateBooking("totalPrice", e.target.value)}
                                />
                            </Field>
                        </div>

                        {/* Selection */}
                        <div style={styles.formBlock}>
                            {loadingList && <div style={styles.meta}>Loading available options…</div>}

                            {activeTab === "transport" ? (
                                <div style={styles.grid}>
                                    <Field label="Select existing transport" span={2}>
                                        <select
                                            style={styles.select}
                                            value={selectedTransportId}
                                            onChange={(e) => setSelectedTransportId(e.target.value)}
                                        >
                                            <option value="">-- choose transport --</option>
                                            {transports.map((t) => (
                                                <option key={t.id} value={t.id}>
                                                    #{t.id} • {t.transportType || "TYPE"} •{" "}
                                                    {t.company || "Company"} •{" "}
                                                    {(t.originAddress || "").slice(0, 20)} →{" "}
                                                    {(t.destinationAddress || "").slice(0, 20)}
                                                </option>
                                            ))}
                                        </select>
                                        {transports.length === 0 && !loadingList && (
                                            <div style={styles.warn}>
                                                No transports found (check your GET endpoint path).
                                            </div>
                                        )}
                                    </Field>
                                </div>
                            ) : (
                                <div style={styles.grid}>
                                    <Field label="Select existing accommodation" span={2}>
                                        <select
                                            style={styles.select}
                                            value={selectedAccommodationId}
                                            onChange={(e) => setSelectedAccommodationId(e.target.value)}
                                        >
                                            <option value="">-- choose accommodation --</option>
                                            {accommodations.map((a) => (
                                                <option key={a.id} value={a.id}>
                                                    #{a.id} • {a.accommodationType || "TYPE"} •{" "}
                                                    {a.name || "Name"} • {a.city || "City"}
                                                </option>
                                            ))}
                                        </select>
                                        {accommodations.length === 0 && !loadingList && (
                                            <div style={styles.warn}>
                                                No accommodations found (check your GET endpoint path).
                                            </div>
                                        )}
                                    </Field>
                                </div>
                            )}
                        </div>

                        {!selectionReady && (
                            <div style={styles.warn}>
                                {activeTab === "transport"
                                    ? "Select a transport to continue."
                                    : "Select an accommodation to continue."}
                            </div>
                        )}

                        {error && <div style={styles.errorBox}>{error}</div>}

                        <div style={styles.footerRow}>
                            <button
                                style={styles.primaryButton}
                                onClick={saveAll}
                                disabled={saving || !selectionReady}
                            >
                                {saving ? "saving..." : "save booking"}
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
    headerRow: { display: "flex", justifyContent: "space-between", gap: 12 },
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
    grid: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 },
    twoCol: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 },
    formBlock: { paddingTop: 6 },
    label: { fontSize: 12, color: "rgba(255,255,255,0.6)", marginBottom: 6 },
    warn: { color: "rgba(255,180,180,0.95)", fontSize: 12, paddingTop: 2 },
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
    errorBox: {
        marginTop: 6,
        padding: 10,
        borderRadius: 10,
        border: "1px solid rgba(255,120,120,0.35)",
        background: "rgba(120,0,0,0.25)",
        color: "rgba(255,220,220,0.95)",
        fontSize: 13,
    },
};