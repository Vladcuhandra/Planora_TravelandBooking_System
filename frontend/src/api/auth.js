const API_BASE = "https://localhost:8443";

export async function login(email, password) {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ email, password }),
    });

    if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || "Login failed");
    }

    return res.json(); // { token, email }
}

export async function refresh() {
    const res = await fetch(`${API_BASE}/api/auth/refresh`, {
        method: "POST",
        credentials: "include", // ✅ sends refresh cookie
    });

    if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || "Refresh failed");
    }

    return res.json(); // { accessToken }
}

export async function logout() {
    const res = await fetch(`${API_BASE}/api/auth/logout`, {
        method: "POST",
        credentials: "include",
    });
    return res.ok;
}