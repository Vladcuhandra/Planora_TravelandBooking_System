import { useEffect, useState } from "react";
import { apiFetch } from "../api/http";

export default function Dashboard() {
    const [msg, setMsg] = useState("");

    useEffect(() => {
        (async () => {
            try {
                const res = await apiFetch("/api/trips");
                setMsg("API status: " + res.status);
            } catch (e) {
                setMsg(e.message);
            }
        })();
    }, []);

    return <div style={{ padding: 20 }}>{msg || "Loading..."}</div>;
}