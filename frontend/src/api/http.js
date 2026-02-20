import { refresh } from "./auth";

const API_BASE = "https://localhost:8443";

export async function apiFetch(path, options = {}) {
    const token = localStorage.getItem("accessToken");

    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            ...(options.headers || {}),
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        credentials: "include", // refresh cookie can be used if needed
    });

    // if access token expired -> try refresh once
    if (res.status === 401) {
        try {
            const r = await refresh(); // { accessToken }
            localStorage.setItem("accessToken", r.accessToken);

            const retryRes = await fetch(`${API_BASE}${path}`, {
                ...options,
                headers: {
                    ...(options.headers || {}),
                    Authorization: `Bearer ${r.accessToken}`,
                },
                credentials: "include",
            });

            return retryRes;
        } catch {
            // refresh failed → kick back to login
            localStorage.removeItem("accessToken");
            throw new Error("Session expired. Please log in again.");
        }
    }

    return res;
}