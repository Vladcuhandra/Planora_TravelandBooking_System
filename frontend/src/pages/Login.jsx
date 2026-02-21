import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/auth";

export default function Login() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    async function onSubmit(e) {
        e.preventDefault();
        setError("");

        try {
            const data = await login(email, password);

            // store access token
            localStorage.setItem("accessToken", data.token);
            localStorage.setItem("email", data.email);

            navigate("/dashboard");
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div
            style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontFamily: "system-ui",
            }}
        >
            <div style={{ maxWidth: 420, width: "100%" }}>
                <h2 style={{ marginBottom: 12, textAlign: "center" }}>Sign in</h2>

                {error && (
                    <div
                        style={{
                            padding: 10,
                            background: "#ffe5e5",
                            marginBottom: 12,
                            borderRadius: 8,
                        }}
                    >
                        {error}
                    </div>
                )}

                <form onSubmit={onSubmit} style={{ display: "grid", gap: 12 }}>
                    <label>
                        Email
                        <input
                            style={{ width: "100%", padding: 10, marginTop: 6 }}
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="name@example.com"
                            autoComplete="email"
                        />
                    </label>

                    <label>
                        Password
                        <input
                            style={{ width: "100%", padding: 10, marginTop: 6 }}
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            autoComplete="current-password"
                        />
                    </label>

                    <button style={{ padding: 10, cursor: "pointer" }} type="submit">
                        Sign in
                    </button>
                </form>
            </div>
        </div>
    );
}