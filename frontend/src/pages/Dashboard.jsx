import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";
import { logout } from "../api/auth";

export default function Dashboard() {
    const navigate = useNavigate();

    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    async function handleLogout() {
        try {
            await logout(); // clears refresh cookie on backend
        } finally {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("email");
            navigate("/login", { replace: true });
        }
    }

    useEffect(() => {
        let cancelled = false;

        async function loadTrips() {
            setLoading(true);
            setError("");

            try {
                const res = await apiFetch("/api/trips");
                if (!res.ok) {
                    const text = await res.text().catch(() => "");
                    throw new Error(text || `Failed to load trips (HTTP ${res.status})`);
                }

                const data = await res.json(); // expecting an array
                if (!cancelled) setTrips(Array.isArray(data) ? data : []);
            } catch (e) {
                if (!cancelled) setError(e.message || "Failed to load trips");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        loadTrips();
        return () => {
            cancelled = true;
        };
    }, []);

    return (
        <div style={{ maxWidth: 900, margin: "40px auto", fontFamily: "system-ui", padding: 16 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12 }}>
                <h2 style={{ margin: 0 }}>Dashboard</h2>
                <button onClick={handleLogout} style={{ padding: "10px 14px", cursor: "pointer" }}>
                    Log out
                </button>
            </div>

            <div style={{ marginTop: 20 }}>
                <h3 style={{ marginBottom: 10 }}>Trips</h3>

                {loading && <div>Loading trips...</div>}

                {!loading && error && (
                    <div style={{ padding: 10, borderRadius: 8, background: "#ffe5e5" }}>
                        {error}
                    </div>
                )}

                {!loading && !error && trips.length === 0 && <div>No trips found.</div>}

                {!loading && !error && trips.length > 0 && (
                    <div style={{ display: "grid", gap: 12 }}>
                        {trips.map((t) => (
                            <div
                                key={t.id}
                                style={{
                                    border: "1px solid #ddd",
                                    borderRadius: 10,
                                    padding: 14,
                                }}
                            >
                                <div style={{ fontWeight: 700, fontSize: 16 }}>{t.title ?? "Untitled Trip"}</div>

                                {t.description && <div style={{ marginTop: 6 }}>{t.description}</div>}

                                <div style={{ marginTop: 10, fontSize: 13, opacity: 0.8 }}>
                                    <div>Start: {t.startDate ?? t.start_date ?? "—"}</div>
                                    <div>End: {t.endDate ?? t.end_date ?? "—"}</div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}