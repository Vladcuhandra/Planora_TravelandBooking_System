const API_BASE = "https://127.0.0.1:8443";

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
        credentials: "include",
    });

    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message || "Refresh failed");

    const newToken = data.token || data.accessToken;
    if (!newToken) throw new Error("Refresh response missing token");

    localStorage.setItem("accessToken", newToken);
    return { accessToken: newToken };
}

export async function logout() {
    // clear local tokens on the client
    await fetch(`${API_BASE}/api/auth/logout`, {
        method: "POST",
        credentials: "include",
    }).catch(() => {});
}