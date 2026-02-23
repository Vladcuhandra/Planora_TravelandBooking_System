import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

export default function Profile() {
    const navigate = useNavigate();

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [editableEmail, setEditableEmail] = useState(false);
    const [editablePassword, setEditablePassword] = useState(false);

    const [newEmail, setNewEmail] = useState("");
    const [newPassword, setNewPassword] = useState("");

    // -----------------------------
    // Load profile
    // -----------------------------
    useEffect(() => {
        async function fetchProfile() {
            try {
                const res = await apiFetch("/api/users/profile", { method: "GET" });

                if (res.status === 401) {
                    navigate("/login");
                    return;
                }

                if (!res.ok) {
                    const msg = await res.text().catch(() => "Failed to load profile");
                    setError(msg);
                    return;
                }

                const data = await res.json();
                setUser(data);
                setNewEmail(data.email || "");
            } catch (e) {
                setError(e.message || "Failed to load profile");
            } finally {
                setLoading(false);
            }
        }

        fetchProfile();
    }, [navigate]);

    // -----------------------------
    // Save profile
    // -----------------------------
    const handleSaveChanges = async (e) => {
        e.preventDefault();
        setError("");

        const currentPassword = e.target.currentPassword.value;

        const payload = {
            email: newEmail,
            currentPassword,
        };

        if (newPassword.trim()) {
            payload.newPassword = newPassword;
        }

        try {
            const res = await apiFetch(`/api/users/${user.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (res.status === 401) {
                navigate("/login");
                return;
            }

            if (!res.ok) {
                const msg = await res.text().catch(() => "Update failed");
                setError(msg);
                return;
            }

            alert("Profile updated successfully");
            setEditableEmail(false);
            setEditablePassword(false);
            setNewPassword("");
        } catch (e) {
            setError(e.message || "Update failed");
        }
    };

    // -----------------------------
    // Delete account
    // -----------------------------
    const handleDeleteAccount = async (e) => {
        e.preventDefault();

        if (!window.confirm("Delete your account permanently?")) return;

        const currentPassword = e.target.currentPassword.value;

        try {
            const res = await apiFetch("/api/user/delete", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ currentPassword }),
            });

            if (!res.ok) {
                const msg = await res.text().catch(() => "Delete failed");
                setError(msg);
                return;
            }

            navigate("/login");
        } catch (e) {
            setError(e.message || "Delete failed");
        }
    };

    // -----------------------------
    // Render
    // -----------------------------
    if (loading) return <div>Loading...</div>;
    if (!user) return <div>No user found.</div>;

    return (
        <div className="planora-dark">
            <div className="container-fluid">
                <main className="col-12">
                    <h4 className="mb-3">Profile</h4>

                    <section className="p-card p-4">
                        <form onSubmit={handleSaveChanges} className="row g-3">
                            {/* Email */}
                            <div className="col-md-6">
                                <label className="form-label">Email</label>
                                {editableEmail ? (
                                    <input
                                        className="form-control"
                                        type="email"
                                        value={newEmail}
                                        onChange={(e) => setNewEmail(e.target.value)}
                                        required
                                    />
                                ) : (
                                    <div onClick={() => setEditableEmail(true)}>
                                        {user.email}
                                    </div>
                                )}
                            </div>

                            {/* Password */}
                            <div className="col-md-6">
                                <label className="form-label">New password</label>
                                {editablePassword ? (
                                    <input
                                        className="form-control"
                                        type="password"
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        placeholder="Optional"
                                    />
                                ) : (
                                    <div onClick={() => setEditablePassword(true)}>
                                        ••••••••
                                    </div>
                                )}
                            </div>

                            {/* Current password */}
                            <div className="col-md-6">
                                <label className="form-label">Current password *</label>
                                <input
                                    className="form-control"
                                    type="password"
                                    name="currentPassword"
                                    required
                                />
                            </div>

                            <div className="col-12">
                                <button className="btn btn-planora" type="submit">
                                    Save Changes
                                </button>
                            </div>
                        </form>

                        {error && <div className="text-danger mt-2">{error}</div>}

                        <hr className="my-4" />

                        <form onSubmit={handleDeleteAccount}>
                            <input
                                type="password"
                                name="currentPassword"
                                className="form-control mb-2"
                                placeholder="Current password"
                                required
                            />
                            <button className="btn btn-danger-soft">
                                Delete My Account
                            </button>
                        </form>
                    </section>
                </main>
            </div>
        </div>
    );
}