import React, { useEffect, useMemo, useState } from "react";
import { apiFetch } from "../api/http";

// If you already have jwt-decode installed, you can use it.
// If not, this tiny decoder works for reading the email (sub) from JWT.
function getEmailFromAccessToken() {
  const token = localStorage.getItem("accessToken");
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    const json = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    // many JWTs store email as "sub"
    return json.sub || json.email || null;
  } catch {
    return null;
  }
}

const PAGE_SIZE = 10;

const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [email, setEmail] = useState("");
  const [createPassword, setCreatePassword] = useState("");
  const [roles] = useState(["USER", "ADMIN"]);

  const [searchEmail, setSearchEmail] = useState("");
  const [currentPage, setCurrentPage] = useState(0);

  const [openCreate, setOpenCreate] = useState(false);
  const [error, setError] = useState(null);

  const [editingField, setEditingField] = useState(null); // { id, field }
  const [editedUserData, setEditedUserData] = useState({});

  // For self password change requirement
  const [showCurrentPasswordPrompt, setShowCurrentPasswordPrompt] = useState(false);
  const [selfCurrentPassword, setSelfCurrentPassword] = useState("");
  const [pendingPasswordChange, setPendingPasswordChange] = useState(null); // { id, newPassword, userEmail }

  const myEmail = useMemo(() => getEmailFromAccessToken(), []);

  useEffect(() => {
    fetchUsers();
  }, []);

  const filteredUsers = useMemo(() => {
    const q = searchEmail.trim().toLowerCase();
    if (!q) return users;
    return users.filter((u) => (u.email || "").toLowerCase().includes(q));
  }, [users, searchEmail]);

  const totalPages = useMemo(() => {
    return Math.max(1, Math.ceil(filteredUsers.length / PAGE_SIZE));
  }, [filteredUsers.length]);

  const pagedUsers = useMemo(() => {
    const start = currentPage * PAGE_SIZE;
    return filteredUsers.slice(start, start + PAGE_SIZE);
  }, [filteredUsers, currentPage]);

  useEffect(() => {
    if (currentPage > totalPages - 1) setCurrentPage(0);
  }, [totalPages, currentPage]);

  // -------------------------
  // API calls
  // -------------------------
  const fetchUsers = async () => {
    try {
      setError(null);
      const response = await apiFetch("/api/users", { method: "GET" });
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || "Failed to fetch users.");
      }
      const data = await response.json();
      setUsers(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message || "Failed to fetch users.");
    }
  };

  const updateUser = async (id, payload) => {
    const response = await apiFetch(`/api/users/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (response.ok) return { ok: true };

    const data = await response.json().catch(() => ({}));
    return { ok: false, message: data.message || "Update failed." };
  };

  const deleteUser = async (id) => {
    const response = await apiFetch(`/api/users/${id}`, { method: "DELETE" });
    if (response.ok) return { ok: true };

    const data = await response.json().catch(() => ({}));
    return { ok: false, message: data.message || "Delete failed." };
  };

  const createUser = async (userData) => {
    const response = await apiFetch("/api/users", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });

    if (response.status === 201 || response.ok) return { ok: true };

    const data = await response.json().catch(() => ({}));
    return { ok: false, message: data.message || "Create failed." };
  };

  // -------------------------
  // UI handlers
  // -------------------------
  const startEditing = (id, field, currentValue) => {
    setEditingField({ id, field });
    setEditedUserData((prev) => ({ ...prev, [field]: currentValue ?? "" }));
  };

  const handleInputChange = (e, field) => {
    setEditedUserData((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleEditUser = async (user, field) => {
    try {
      setError(null);

      // Build minimal payload matching UserProfileUpdateRequest
      const payload = {};

      if (field === "email") {
        const value = (editedUserData.email ?? user.email ?? "").trim();
        if (!value) {
          setError("Email cannot be empty.");
          return;
        }
        payload.email = value;
      }

      if (field === "role") {
        const value = editedUserData.role ?? user.role;
        if (!value) {
          setError("Role cannot be empty.");
          return;
        }
        payload.role = value;
      }

      if (field === "password") {
        const newPass = editedUserData.password ?? "";
        if (!newPass.trim()) {
          setError("New password cannot be empty.");
          return;
        }

        // Spring expects newPassword
        payload.newPassword = newPass;

        // If editing your OWN password, backend requires currentPassword
        const isSelf = myEmail && user.email && user.email === myEmail;
        if (isSelf) {
          // show prompt to collect current password
          setPendingPasswordChange({ id: user.id, newPassword: newPass, userEmail: user.email });
          setShowCurrentPasswordPrompt(true);
          return; // stop here until current password is provided
        }
      }

      // Execute update immediately for non-self password changes (or email/role)
      const result = await updateUser(user.id, payload);

      if (result.ok) {
        await fetchUsers();
        setEditingField(null);
        setEditedUserData({});
      } else {
        setError(result.message);
      }
    } catch (e) {
      setError(e.message || "Failed to edit user.");
    }
  };

  const confirmSelfPasswordChange = async () => {
    try {
      setError(null);
      if (!pendingPasswordChange) return;

      if (!selfCurrentPassword.trim()) {
        setError("Current password is required to change your own password.");
        return;
      }

      const payload = {
        newPassword: pendingPasswordChange.newPassword,
        currentPassword: selfCurrentPassword,
      };

      const result = await updateUser(pendingPasswordChange.id, payload);

      if (result.ok) {
        await fetchUsers();
        setEditingField(null);
        setEditedUserData({});
        setShowCurrentPasswordPrompt(false);
        setSelfCurrentPassword("");
        setPendingPasswordChange(null);
      } else {
        setError(result.message);
      }
    } catch (e) {
      setError(e.message || "Failed to change password.");
    }
  };

  const cancelSelfPasswordChange = () => {
    setShowCurrentPasswordPrompt(false);
    setSelfCurrentPassword("");
    setPendingPasswordChange(null);
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm("Are you sure you want to delete this user?")) return;

    const result = await deleteUser(id);
    if (result.ok) {
      await fetchUsers();
    } else {
      setError(result.message);
    }
  };

  const handleCreateUser = async (event) => {
    event.preventDefault();

    const form = new FormData(event.target);
    const userData = Object.fromEntries(form.entries());

    const result = await createUser(userData);
    if (result.ok) {
      await fetchUsers();
      setOpenCreate(false);
      setEmail("");
      setCreatePassword("");
    } else {
      setError(result.message);
    }
  };

  // -------------------------
  // Render
  // -------------------------
  return (
      <div className="container-fluid">
        <div className="row g-3 g-lg-4 py-3">
          <main className="col-12">
            <div className="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
              <div>
                <div className="p-title h4 mb-1">User Management</div>
                <div className="p-hint">Create, edit, and delete users</div>
              </div>
            </div>

            {/* Create User */}
            <section className="p-card p-3 p-md-4 mb-3">
              <div className="d-flex align-items-center justify-content-between mb-2">
                <div className="p-subtitle fw-semibold">Create user</div>
                <button
                    className="btn btn-soft btn-sm"
                    type="button"
                    onClick={() => setOpenCreate(!openCreate)}
                >
                  {openCreate ? "Hide" : "Show"}
                </button>
              </div>

              {openCreate && (
                  <form onSubmit={handleCreateUser} className="row g-2 g-md-3 mt-1">
                    <div className="col-12 col-md-4">
                      <label className="form-label p-hint mb-1">Email</label>
                      <input
                          className="form-control"
                          type="email"
                          name="email"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          placeholder="user@example.com"
                          required
                      />
                    </div>

                    <div className="col-12 col-md-4">
                      <label className="form-label p-hint mb-1">Password</label>
                      <input
                          className="form-control"
                          type="password"
                          name="password"
                          value={createPassword}
                          onChange={(e) => setCreatePassword(e.target.value)}
                          placeholder="••••••••"
                          required
                      />
                    </div>

                    <div className="col-12 col-md-4">
                      <label className="form-label p-hint mb-1">Role</label>
                      <select className="form-select" name="role" required>
                        <option value="">-- Select --</option>
                        {roles.map((r) => (
                            <option key={r} value={r}>
                              {r}
                            </option>
                        ))}
                      </select>
                    </div>

                    {error && <div className="text-danger small">{error}</div>}

                    <div className="col-12 d-flex justify-content-end">
                      <button className="btn btn-planora px-4" type="submit">
                        Create
                      </button>
                    </div>
                  </form>
              )}
            </section>

            {/* Users Table */}
            <section className="p-card p-3 p-md-4">
              <div className="d-flex align-items-center justify-content-between mb-2">
                <div className="p-subtitle fw-semibold">User list</div>
                <div className="d-flex gap-2 align-items-center">
                  <input
                      type="text"
                      className="form-control"
                      value={searchEmail}
                      onChange={(e) => setSearchEmail(e.target.value)}
                      placeholder="Search by email"
                  />
                </div>
              </div>

              <div className="table-responsive table-responsive-unclip">
                <table className="table planora-table planora-table--compact table-hover align-middle mb-0">
                  <thead>
                  <tr>
                    <th style={{ width: "80px" }}>ID</th>
                    <th style={{ minWidth: "200px" }}>Email</th>
                    <th style={{ width: "130px" }}>Role</th>
                    <th style={{ minWidth: "200px" }}>Password</th>
                    <th style={{ width: "210px" }}>Actions</th>
                  </tr>
                  </thead>

                  <tbody>
                  {pagedUsers.length === 0 ? (
                      <tr>
                        <td colSpan="5" className="p-hint">
                          No users found.
                        </td>
                      </tr>
                  ) : (
                      pagedUsers.map((user) => (
                          <tr key={user.id}>
                            <td>{user.id}</td>

                            {/* Email */}
                            <td>
                              {editingField?.id === user.id && editingField?.field === "email" ? (
                                  <div className="d-flex gap-2 align-items-center">
                                    <input
                                        className="form-control form-control-sm"
                                        type="email"
                                        value={editedUserData.email ?? user.email ?? ""}
                                        onChange={(e) => handleInputChange(e, "email")}
                                    />
                                    <button
                                        className="btn btn-sm btn-planora"
                                        type="button"
                                        onClick={() => handleEditUser(user, "email")}
                                    >
                                      Save
                                    </button>
                                  </div>
                              ) : (
                                  <div onClick={() => startEditing(user.id, "email", user.email)}>
                                    {user.email}
                                  </div>
                              )}
                            </td>

                            {/* Role */}
                            <td>
                              {editingField?.id === user.id && editingField?.field === "role" ? (
                                  <div className="d-flex gap-2 align-items-center">
                                    <select
                                        className="form-select form-select-sm"
                                        value={editedUserData.role ?? user.role ?? "USER"}
                                        onChange={(e) => handleInputChange(e, "role")}
                                    >
                                      {roles.map((r) => (
                                          <option key={r} value={r}>
                                            {r}
                                          </option>
                                      ))}
                                    </select>
                                    <button
                                        className="btn btn-sm btn-planora"
                                        type="button"
                                        onClick={() => handleEditUser(user, "role")}
                                    >
                                      Save
                                    </button>
                                  </div>
                              ) : (
                                  <div onClick={() => startEditing(user.id, "role", user.role)}>
                                    {user.role}
                                  </div>
                              )}
                            </td>

                            {/* Password */}
                            <td>
                              {editingField?.id === user.id && editingField?.field === "password" ? (
                                  <div className="d-flex gap-2 align-items-center">
                                    <input
                                        className="form-control form-control-sm"
                                        type="password"
                                        value={editedUserData.password ?? ""}
                                        onChange={(e) => handleInputChange(e, "password")}
                                        placeholder="New password"
                                    />
                                    <button
                                        className="btn btn-sm btn-planora"
                                        type="button"
                                        onClick={() => handleEditUser(user, "password")}
                                    >
                                      Save
                                    </button>
                                  </div>
                              ) : (
                                  <div onClick={() => startEditing(user.id, "password", "")}>
                                    {"••••••••"}
                                  </div>
                              )}
                            </td>

                            {/* Actions */}
                            <td>
                              <button
                                  className="btn btn-danger-soft btn-sm"
                                  type="button"
                                  onClick={() => handleDeleteUser(user.id)}
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                      ))
                  )}
                  </tbody>
                </table>
              </div>

              {/* Client-side pagination controls */}
              <div className="d-flex justify-content-between align-items-center mt-3">
                <div className="p-hint">
                  Showing {pagedUsers.length} of {filteredUsers.length}
                </div>
                <div className="d-flex gap-2">
                  <button
                      className="btn btn-soft btn-sm"
                      type="button"
                      disabled={currentPage <= 0}
                      onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  >
                    Prev
                  </button>
                  <div className="p-hint align-self-center">
                    Page {currentPage + 1} / {totalPages}
                  </div>
                  <button
                      className="btn btn-soft btn-sm"
                      type="button"
                      disabled={currentPage >= totalPages - 1}
                      onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                  >
                    Next
                  </button>
                </div>
              </div>

              {error && <div className="text-danger small mt-2">{error}</div>}
            </section>

            {/* Current password prompt (only when changing YOUR OWN password) */}
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
                      You’re changing your own password. Enter your current password to confirm.
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
                      <button className="btn btn-soft btn-sm" type="button" onClick={cancelSelfPasswordChange}>
                        Cancel
                      </button>
                      <button className="btn btn-planora btn-sm" type="button" onClick={confirmSelfPasswordChange}>
                        Confirm
                      </button>
                    </div>
                  </div>
                </div>
            )}
          </main>
        </div>
      </div>
  );
};

export default AdminDashboard;