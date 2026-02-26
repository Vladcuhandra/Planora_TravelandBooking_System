import { refresh } from "./auth";
import {clearAccessToken, getAccessToken} from "./tokenStore.js";

const API_BASE = "https://localhost:8443";

let refreshPromise = null;

async function getNewToken() {
    if (!refreshPromise) {
        refreshPromise = refresh()
            .then((r) => r.accessToken)
            .finally(() => {
                refreshPromise = null;
            });
    }
    return refreshPromise;
}

export async function apiFetch(path, options = {}) {
    const doFetch = (token) =>
        fetch(`${API_BASE}${path}`, {
            ...options,
            headers: {
                ...(options.headers || {}),
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
            credentials: "include",
        });

    // try current token
    let res = await doFetch(getAccessToken());
    if (res.status !== 401) return res;

    const newToken = await getNewToken();
    res = await doFetch(newToken);

    if (res.status === 401) {
        clearAccessToken();
        throw new Error("Session expired. Please log in again.");
    }

    return res;
}