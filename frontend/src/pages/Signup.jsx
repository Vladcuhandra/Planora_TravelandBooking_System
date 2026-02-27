import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";
import logo from "../assets/logo.png";

export default function Signup() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");

    const isValidEmail = (value) => /\S+@\S+\.\S+/.test(value);

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");

        if (!isValidEmail(email)) {
            setError("Please enter a valid email");
            return;
        }

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        if (!password || password.length < 6) {
            setError("Password must be at least 6 characters");
            return;
        }

        try {
            const response = await apiFetch("/api/auth/signup", {
                method: "POST",
                body: JSON.stringify({ email, password, confirmPassword }),
                headers: { "Content-Type": "application/json" },
            });

            if (response.status === 200 || response.status === 201) {
                navigate("/login");
            } else {
                const msg = await response.text();
                setError(msg || "Something went wrong");
            }
        } catch (err) {
            setError("An error occurred: " + err.message);
        }
    }

    return (
        <div className="p-auth-wrap">
            <div className="p-card p-auth-card">
                <div className="auth-header d-flex align-items-center mb-3">
                    <div className="auth-logo-wrap">
                        <img className="p-logo" src={logo} alt="Planora logo"  />
                    </div>
                    <div className="auth-text-wrap">
                        <div className="p-title h5 mb-0">Create account</div>
                        <div className="p-hint">Join Planora in a minute</div>
                    </div>
                </div>

                {error && <div className="p-alert error">{error}</div>}

                <form onSubmit={handleSubmit} className="d-grid gap-3">
                    <div>
                        <label className="form-label p-hint mb-1">Email</label>
                        <input
                            className="form-control"
                            type="email"
                            placeholder="name@example.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div>
                        <label className="form-label p-hint mb-1">Password</label>
                        <input
                            className="form-control"
                            type="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div>
                        <label className="form-label p-hint mb-1">Confirm password</label>
                        <input
                            className="form-control"
                            type="password"
                            placeholder="••••••••"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button className="btn btn-planora py-2" type="submit">
                        Create account
                    </button>

                    <div className="text-center p-hint">
                        Already have an account? <a href="/login">Sign in</a>
                    </div>
                </form>
            </div>
        </div>
    );
}