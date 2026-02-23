import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

export default function Profile() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [editableEmail, setEditableEmail] = useState(false);
    const [editablePassword, setEditablePassword] = useState(false);
    const [editableRole, setEditableRole] = useState(false);
    const [newEmail, setNewEmail] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [newRole, setNewRole] = useState("");

    useEffect(() => {
        async function fetchUserProfile() {
            try {
                const res = await apiFetch("/api/users/profile", { method: "GET" });
                if (res.ok) {
                    const data = await res.json();
                    setUser(data);
                    setNewEmail(data.email);
                    setNewRole(data.role);
                } else {
                    setError("Failed to load profile.");
                    navigate("/login");
                }
            } catch (err) {
                setError("An error occurred: " + err.message);
                navigate("/login");
            } finally {
                setLoading(false);
            }
        }

        fetchUserProfile();
    }, [navigate]);

    const handleSaveChanges = async (e) => {
        e.preventDefault();
        setError("");

        const updatedUser = {
            email: newEmail,
            currentPassword: e.target.currentPassword.value,
            newPassword: newPassword,
        };

        if (user.role !== "USER" && !user.superAdmin) {
            updatedUser.role = newRole;
        }

        try {
            const res = await apiFetch("/api/user/edit", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(updatedUser),
            });

            if (res.ok) {
                alert("Profile updated successfully!");
                setEditableEmail(false);
                setEditablePassword(false);
                setEditableRole(false);
            } else {
                const msg = await res.text();
                setError(msg);
            }
        } catch (err) {
            setError("An error occurred: " + err.message);
        }
    };

    const handleDeleteAccount = async (e) => {
        e.preventDefault();
        if (window.confirm("Are you sure you want to delete your account? This cannot be undone.")) {
            const currentPassword = e.target.elements.currentPassword.value;

            const payload = { currentPassword };

            try {
                const res = await apiFetch("/api/user/delete", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(payload),
                });

                if (res.ok) {
                    alert("Account deleted successfully.");
                    navigate("/login");
                } else {
                    const msg = await res.text();
                    setError(msg);
                }
            } catch (err) {
                setError("An error occurred: " + err.message);
            }
        }
    };

    if (loading) return <div>Loading...</div>;

    if (!user) return <div>No user found. Redirecting...</div>;

    return (
        <div className="planora-dark">
            <div className="container-fluid">
                <div className="row g-3 g-lg-4 py-3">
                    <main className="col-12">
                        <div className="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
                            <div>
                                <div className="p-title h4 mb-1">Profile</div>
                                {!user.superAdmin && (
                                    <div className="p-hint">Update profile, set new password</div>
                                )}
                            </div>
                        </div>

                        <section className="p-card p-3 p-md-4">
                            <div className="d-flex align-items-center justify-content-between mb-2">
                                <div className="p-subtitle fw-semibold">Account details</div>
                            </div>

                            <form onSubmit={handleSaveChanges} className="row g-3">
                                {/* Email */}
                                <div className="col-12 col-md-6">
                                    <label className="form-label p-hint mb-1">Email:&nbsp;</label>
                                    {editableEmail && !user.superAdmin ? (
                                        <input
                                            className="form-control"
                                            type="email"
                                            name="email"
                                            value={newEmail}
                                            onChange={(e) => setNewEmail(e.target.value)}
                                            required
                                        />
                                    ) : (
                                        <span
                                            className={`editable-text ${user.superAdmin ? "text" : ""}`}
                                            onClick={() => !user.superAdmin && setEditableEmail(true)}
                                        >
                                            {user.email}
                                        </span>
                                    )}
                                </div>

                                {/* Role (Admins only, but not for superAdmin) */}
                                {!user.superAdmin && user.role !== "USER" && (
                                    <div className="col-12 col-md-6">
                                        <label className="form-label p-hint mb-1">Role</label>
                                        {editableRole ? (
                                            <select
                                                className="form-select"
                                                name="role"
                                                value={newRole}
                                                onChange={(e) => setNewRole(e.target.value)}
                                            >
                                                <option value="USER">USER</option>
                                                <option value="ADMIN">ADMIN</option>
                                            </select>
                                        ) : (
                                            <span
                                                className={`editable-text ${user.superAdmin ? "text" : ""}`}
                                                onClick={() => !user.superAdmin && setEditableRole(true)}
                                            >
                                                {user.role}
                                            </span>
                                        )}
                                    </div>
                                )}

                                {/* Password */}
                                <div className="col-12 col-md-6">
                                    <label className="form-label p-hint mb-1">Password:</label>
                                    {editablePassword && !user.superAdmin ? (
                                        <input
                                            className="form-control"
                                            type="password"
                                            name="password"
                                            value={newPassword}
                                            onChange={(e) => setNewPassword(e.target.value)}
                                            placeholder="New password (optional)"
                                        />
                                    ) : (
                                        <span
                                            className={`editable-text ${user.superAdmin ? "text" : ""}`}
                                            onClick={() => !user.superAdmin && setEditablePassword(true)}
                                        >
                                            {" ••••••••"}
                                        </span>
                                    )}
                                </div>

                                {/* Current Password */}
                                {!user.superAdmin && (
                                    <div className="col-12 col-md-6">
                                        <label className="form-label p-hint mb-1">Current Password *</label>
                                        <input
                                            className="form-control"
                                            type="password"
                                            name="currentPassword"
                                            placeholder="Enter your current password"
                                            required
                                        />
                                    </div>
                                )}

                                {/* Actions */}
                                {!user.superAdmin && (
                                    <div className="col-12 d-flex flex-wrap gap-2 justify-content-start">
                                        <button className="btn btn-planora px-4" type="submit">
                                            Save Changes
                                        </button>
                                    </div>
                                )}
                            </form>

                            {/* Danger zone: Delete account */}
                            {!user.superAdmin && (
                                <>
                                    <hr className="p-divider my-4" />
                                    <div>
                                        <div className="p-hint mb-2">Danger zone</div>
                                        <form onSubmit={handleDeleteAccount} className="m-0">
                                            <div className="mb-2">
                                                <input
                                                    type="password"
                                                    name="currentPassword"
                                                    className="form-control"
                                                    placeholder="Enter your current password"
                                                    required
                                                />
                                            </div>

                                            <button type="submit" className="btn btn-danger-soft">
                                                Delete My Account
                                            </button>
                                        </form>
                                    </div>
                                </>
                            )}
                        </section>
                    </main>
                </div>
            </div>
        </div>
    );
}
