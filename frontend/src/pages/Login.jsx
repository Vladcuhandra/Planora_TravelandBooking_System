import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/auth";
import { restoreAccount } from '../api/auth';
import {setAccessToken} from "../api/tokenStore.js";

export default function Login() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isRestoring, setIsRestoring] = useState(false);
    const [restorePassword, setRestorePassword] = useState("");

    const isValidEmail = (value) => /\S+@\S+\.\S+/.test(value);

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");

        if (!isValidEmail(email) && email !== "admin") {
            setError("Please enter a valid email");
            return;
        }

        try {
            const data = await login(email, password);
            setAccessToken(data.token);
            localStorage.setItem("email", data.email);
            navigate("/main", { replace: true });
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleRestoreSubmit(e) {
        e.preventDefault();
        setError("");

        if (!isValidEmail(email)) {
            setError("Please enter a valid email for account restoration");
            return;
        }

        try {
            const data = await restoreAccount(email, restorePassword);
            localStorage.setItem("accessToken", data.token);
            localStorage.setItem("email", data.email);

            navigate("/main", { replace: true });
        } catch (err) {
            setError(err.message || "An error occurred while restoring the account.");
        }
    }

    return (
        <div className="p-auth-wrap">
            <div className="p-card p-auth-card">
                <div className="auth-header d-flex align-items-center mb-3">
                    <div className="auth-logo-wrap">
                        <img className="p-logo" src="/img/logo.png" alt="Planora logo" />
                    </div>
                    <div className="auth-text-wrap">
                        <div className="p-title h5 mb-0">{isRestoring ? "Restore your account" : "Sign in to Planora"}</div>
                    </div>
                </div>

                {error && <div className="p-alert error mb-3">{error}</div>}

                {isRestoring ? (
                    <form onSubmit={handleRestoreSubmit} className="d-grid gap-3">
                        <div>
                            <label className="form-label p-hint mb-1">Email</label>
                            <input
                                className="form-control"
                                type="text"
                                placeholder="name@example.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <div>
                            <label className="form-label p-hint mb-1">Confirm Password</label>
                            <input
                                className="form-control"
                                type="password"
                                placeholder="••••••••"
                                value={restorePassword}
                                onChange={(e) => setRestorePassword(e.target.value)}
                                required
                            />
                        </div>

                        <button className="btn btn-planora py-2" type="submit">
                            Restore Account
                        </button>

                        <div className="text-center p-hint">
                            <a href="#" onClick={() => setIsRestoring(false)}>Back to Sign In</a>
                        </div>
                    </form>
                ) : (
                    <form onSubmit={handleSubmit} className="d-grid gap-3">
                        <div>
                            <label className="form-label p-hint mb-1">Email</label>
                            <input
                                className="form-control"
                                type="text"
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

                        <button className="btn btn-planora py-2" type="submit">
                            Sign in
                        </button>

                        <div className="text-center p-hint">
                            New to Planora? <a href="/signup">Create an account</a>
                        </div>
                        <div className="text-center p-hint">
                            Forgot your account? <a href="#" onClick={() => setIsRestoring(true)}>Restore Account</a>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}