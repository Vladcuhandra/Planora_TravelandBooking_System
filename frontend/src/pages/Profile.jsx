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

    const [showCurrentPasswordPrompt, setShowCurrentPasswordPrompt] = useState(false);
    const [selfCurrentPassword, setSelfCurrentPassword] = useState("");
    const [pendingPasswordChange, setPendingPasswordChange] = useState(null);

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

                if (data.superAdmin || data.role === "ADMIN") {
                    setEditableEmail(false);
                    setEditablePassword(false);
                }
            } catch (e) {
                setError(e.message || "Failed to load profile");
            } finally {
                setLoading(false);
            }
        }

        fetchProfile();
    }, [navigate]);

    const handleSaveChanges = (e) => {
        e.preventDefault();
        setError("");

        if (editableEmail || editablePassword) {
            setShowCurrentPasswordPrompt(true);
            return;
        }
    };

    const handleDeleteAccount = (e) => {
        e.preventDefault();

        if (!window.confirm("Delete your account?")) return;

        if (user.superAdmin || user.role === "ADMIN") {
            setError("Super Admin and Admin accounts cannot be deleted.");
            return;
        }

        setPendingPasswordChange("delete");
        setShowCurrentPasswordPrompt(true);
    };

    const handleConfirmCurrentPassword = async () => {
        if (!selfCurrentPassword.trim()) {
            setError("Current password is required.");
            return;
        }

        try {
            if (pendingPasswordChange === "password") {
                const payload = {
                    currentPassword: selfCurrentPassword,
                    newPassword: newPassword,
                };

                const res = await apiFetch(`/api/users/edit/${user.id}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload),
                });

                if (!res.ok) {
                    const msg = await res.text().catch(() => "Update failed");
                    setError(msg);
                    return;
                }

                alert("Password updated successfully");
                setShowCurrentPasswordPrompt(false);
                setPendingPasswordChange(null);
                setNewPassword("");
            } else if (pendingPasswordChange === "delete") {
                const res = await apiFetch(`/api/users/${user.id}`, {
                    method: "DELETE",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ currentPassword: selfCurrentPassword }),
                });

                if (!res.ok) {
                    const msg = await res.text().catch(() => "Delete failed");
                    setError(msg);
                    return;
                }

                alert("Account deleted successfully.");
                navigate("/login");
            } else {
                const payload = {
                    email: newEmail,
                    currentPassword: selfCurrentPassword,
                };

                if (newPassword.trim()) {
                    payload.newPassword = newPassword;
                }

                const res = await apiFetch(`/api/users/edit/${user.id}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload),
                });

                if (!res.ok) {
                    const msg = await res.text().catch(() => "Update failed");
                    setError("Wrong password");
                    return;
                }

                alert("Profile updated successfully");
                setShowCurrentPasswordPrompt(false);
                setEditableEmail(false);
                setEditablePassword(false);
                setNewPassword("");
            }
        } catch (e) {
            setError(e.message || "Operation failed");
        }
    };

    const handleCancelCurrentPassword = () => {
        setShowCurrentPasswordPrompt(false);
        setSelfCurrentPassword("");
    };

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
                                    <div onClick={() => !(user.superAdmin) && setEditableEmail(true)}>
                                        {user.email}
                                    </div>
                                )}
                            </div>

                            {/* Password */}
                            <div className="col-md-6">
                                <label className="form-label">Password</label>
                                {editablePassword ? (
                                    <input
                                        className="form-control"
                                        type="password"
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        placeholder="Optional"
                                    />
                                ) : (
                                    <div onClick={() => !(user.superAdmin) && setEditablePassword(true)}>
                                        ••••••••
                                    </div>
                                )}
                            </div>

                            {/* Save Changes Button - Hide for Super Admin and Admin */}
                            {!(user.superAdmin) && (
                                <div className="col-12">
                                    <button className="btn btn-planora" type="submit">
                                        Save Changes
                                    </button>
                                </div>
                            )}
                        </form>

                        {error && <div className="text-danger mt-2">{error}</div>}

                        <hr className="my-4" />

                        {/* Delete Account Section - Hide for Super Admin and Admin */}
                        {!(user.superAdmin) && (
                            <form onSubmit={handleDeleteAccount}>
                                <button className="btn btn-danger-soft">
                                    Delete My Account
                                </button>
                            </form>
                        )}
                    </section>
                </main>
            </div>

            {showCurrentPasswordPrompt && (
                <div
                    style={{
                        position: "fixed",
                        inset: 0,
                        background: "rgba(0,0,0,0.6)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        zIndex: 9999,
                    }}
                >
                    <div className="p-card p-3" style={{ width: 420, maxWidth: "90vw" }}>
                        <div className="p-subtitle fw-semibold mb-2">Confirm your current password</div>
                        <div className="p-hint mb-2">
                            You’re changing/deleting your account. Enter your current password to confirm.
                        </div>
                        <input
                            className="form-control mb-2"
                            type="password"
                            value={selfCurrentPassword}
                            onChange={(e) => setSelfCurrentPassword(e.target.value)}
                            placeholder="Current password"
                        />
                        {error && <div className="text-danger small mb-2">{error}</div>}
                        <div className="d-flex justify-content-end gap-2">
                            <button className="btn btn-soft btn-sm" type="button" onClick={handleCancelCurrentPassword}>
                                Cancel
                            </button>
                            <button className="btn btn-planora btn-sm" type="button" onClick={handleConfirmCurrentPassword}>
                                Confirm
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
