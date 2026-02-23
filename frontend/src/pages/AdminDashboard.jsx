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

  const [editingField, setEditingField] = useState(null);
  const [editedUserData, setEditedUserData] = useState({});

  const [showCurrentPasswordPrompt, setShowCurrentPasswordPrompt] = useState(false);
  const [selfCurrentPassword, setSelfCurrentPassword] = useState("");
  const [pendingPasswordChange, setPendingPasswordChange] = useState(null);

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
    const response = await apiFetch(`/api/users/edit/${id}`, {
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

      if (user.superAdmin) {
        setError("This user is a Super Admin and cannot be edited.");
        return;
      }

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

        payload.newPassword = newPass;

        if (myEmail && user.email && user.email === myEmail) {
          setPendingPasswordChange({ id: user.id, newPassword: newPass, userEmail: user.email });
          setShowCurrentPasswordPrompt(true);
          return;
        }
      }

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

    const user = users.find((user) => user.id === id);
    if (user && user.superAdmin) {
      setError("You cannot delete a Super Admin.");
      return;
    }

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

  const handleToggleDeletionStatus = async (userId, isDeleted) => {
    try {
      const url = `/api/users/edit/${userId}`;
      const response = await apiFetch(url, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ deleted: !isDeleted }),
      });

      if (response.ok) {
        const updatedData = await response.json();
        setUsers((prevUsers) =>
            prevUsers.map((user) => (user.id === userId ? { ...user, deleted: !isDeleted } : user))
        );
      } else {
        console.error("Failed to update deletion status.");
      }
    } catch (e) {
      console.error("Error toggling deletion status:", e);
    }
  };

  const isAdmin = myEmail && users.find((user) => user.email === myEmail)?.role === "ADMIN";
  const isSuperAdmin = myEmail && users.find((user) => user.email === myEmail)?.role === "SUPER_ADMIN";

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
                    <th style={{ minWidth: "200px" }}>Status</th>
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
                              {user.superAdmin ? (
                                  <span className="text">Super Admin</span>
                              ) : (
                                  editingField?.id === user.id && editingField?.field === "email" ? (
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
                                      <div onClick={() => startEditing(user.id, "email", user.email)}>{user.email}</div>
                                  )
                              )}
                            </td>

                            {/* Role */}
                            <td>
                              {user.superAdmin ? (
                                  <span className="text">Super Admin</span>
                              ) : (
                                  editingField?.id === user.id && editingField?.field === "role" ? (
                                      <div className="d-flex gap-2 align-items-center">
                                        <select
                                            className="form-select form-select-sm"
                                            value={editedUserData.role ?? user.role ?? "USER"}
                                            onChange={(e) => handleInputChange(e, "role")}
                                            disabled={user.role === "SUPER_ADMIN" || !isAdmin}
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
                                            disabled={user.superAdmin}
                                        >
                                          Save
                                        </button>
                                      </div>
                                  ) : (
                                      <div onClick={() => startEditing(user.id, "role", user.role)}>{user.role}</div>
                                  )
                              )}
                            </td>

                            {/* Password */}
                            <td>
                              {user.superAdmin ? (
                                  <span className="text">Not editable</span>
                              ) : (
                                  editingField?.id === user.id && editingField?.field === "password" ? (
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
                                      <div onClick={() => startEditing(user.id, "password", "")}>••••••••</div>
                                  )
                              )}
                            </td>

                            {/* Account Status */}
                            <td>
                              {!user.deleted ? (
                                  <span className="text-success">ACTIVE</span>
                              ) : (
                                  <div>
                              <span className="text-warning">
                                DELETING (Scheduled for {new Date(user.deletionDate).toLocaleDateString()})
                              </span>
                                    <button
                                        className="btn btn-sm btn-primary ms-2"
                                        onClick={() => handleToggleDeletionStatus(user.id, user.deleted)}
                                    >
                                      Restore Account
                                    </button>
                                  </div>
                              )}
                            </td>

                            {/* Actions */}
                            <td>
                              {user.superAdmin ? (
                                  <span className="text">Cannot delete Super Admin</span>
                              ) : (
                                  <button
                                      className="btn btn-danger-soft btn-sm"
                                      type="button"
                                      onClick={() => handleDeleteUser(user.id)}
                                  >
                                    Delete
                                  </button>
                              )}
                            </td>
                          </tr>
                      ))
                  )}
                  </tbody>
                </table>
              </div>
            </section>
          </main>
        </div>
      </div>
  );
};

export default AdminDashboard;
