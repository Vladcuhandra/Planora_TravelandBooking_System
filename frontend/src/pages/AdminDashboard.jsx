import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiFetch } from "../api/http";

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [roles, setRoles] = useState(["USER", "ADMIN"]);
  const [statuses, setStatuses] = useState(["ACTIVE", "DELETED"]);
  const [searchEmail, setSearchEmail] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [openCreate, setOpenCreate] = useState(false);
  const [error, setError] = useState(null);
  const [editingField, setEditingField] = useState(null);
  const [editedUserData, setEditedUserData] = useState({});

  useEffect(() => {
    fetchUsers();
  }, [currentPage, searchEmail]);

  const fetchUsers = async () => {
    try {
      const response = await apiFetch('/api/admin', {
        method: 'GET',
        params: {
          page: currentPage,
          searchEmail
        }
      });
      const data = await response.json();
      setUsers(data.users || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      setError("Failed to fetch users.");
    }
  };

  const startEditing = (id, field, currentValue) => {
    setEditingField({ id, field });
    setEditedUserData({ ...editedUserData, [field]: currentValue });
  };

  const handleInputChange = (e, field) => {
    setEditedUserData({
      ...editedUserData,
      [field]: e.target.value
    });
  };

  const handleEditUser = async (id, field, value) => {
    try {
      const updatedUser = users.find(u => u.id === id);
      const userData = {
        ...updatedUser,
        [field]: value
      };

      const response = await apiFetch(`/api/admin/edit`, {
        method: "POST",
        body: JSON.stringify(userData),
        headers: { "Content-Type": "application/json" },
      });

      if (response.status === 200) {
        fetchUsers();
        setEditingField(null);
        setEditedUserData({});
      } else {
        const data = await response.json();
        setError(data.message || "Error editing user.");
      }
    } catch (error) {
      setError("Failed to edit user.");
    }
  };

  const handleDeleteUser = async (id) => {
    if (window.confirm("Are you sure you want to delete this user?")) {
      try {
        const response = await apiFetch(`/api/admin/delete`, {
          method: "POST",
          body: JSON.stringify({ userId: id }),
        });
        if (response.status === 200) {
          fetchUsers();
        } else {
          const data = await response.json();
          setError(data.message || "Error deleting user.");
        }
      } catch (error) {
        setError("Failed to delete user.");
      }
    }
  };

  const handleCreateUser = async (event) => {
    event.preventDefault();
    const form = new FormData(event.target);
    const userData = Object.fromEntries(form.entries());

    try {
      const response = await apiFetch("/api/admin/create", {
        method: "POST",
        body: JSON.stringify(userData),
        headers: {
          "Content-Type": "application/json",
        },
      });
      if (response.status === 201) {
        fetchUsers();
        setOpenCreate(false);
        setEmail('');
        setPassword('');
      } else {
        const data = await response.json();
        setError(data.message || "Error creating user.");
      }
    } catch (error) {
      setError("Failed to create user.");
    }
  };

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
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                  />
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label p-hint mb-1">Role</label>
                  <select className="form-select" name="role" required>
                    <option value="">-- Select --</option>
                    {roles.map((r, index) => (
                      <option key={index} value={r}>{r}</option>
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
              <div className="d-flex">
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
                    <th style={{ width: "130px" }}>Status</th>
                    <th style={{ width: "210px" }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="p-hint">
                        No users found.
                      </td>
                    </tr>
                  ) : (
                    users.map((user) => (
                      <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>
                          {editingField?.id === user.id && editingField?.field === 'email' ? (
                            <div>
                              <input
                                type="email"
                                value={editedUserData.email || user.email}
                                onChange={(e) => handleInputChange(e, 'email')}
                              />
                              <button
                                onClick={() => handleEditUser(user.id, 'email', editedUserData.email || user.email)}
                              >
                                Save
                              </button>
                            </div>
                          ) : (
                            <div onClick={() => startEditing(user.id, 'email', user.email)}>
                              {user.email}
                            </div>
                          )}
                        </td>
                        <td>
                          {editingField?.id === user.id && editingField?.field === 'role' ? (
                            <div>
                              <select
                                value={editedUserData.role || user.role}
                                onChange={(e) => handleInputChange(e, 'role')}
                              >
                                {roles.map(role => (
                                  <option key={role} value={role}>{role}</option>
                                ))}
                              </select>
                              <button
                                onClick={() => handleEditUser(user.id, 'role', editedUserData.role || user.role)}
                              >
                                Save
                              </button>
                            </div>
                          ) : (
                            <div onClick={() => startEditing(user.id, 'role', user.role)}>
                              {user.role}
                            </div>
                          )}
                        </td>
                        <td>
                          {editingField?.id === user.id && editingField?.field === 'password' ? (
                              <div>
                                <input
                                    type="password"
                                    value={editedUserData.password || user.password}
                                    onChange={(e) => handleInputChange(e, 'password')}
                                />
                                <button
                                    onClick={() => handleEditUser(user.id, 'password', editedUserData.password || user.password)}
                                >
                                  Save
                                </button>
                              </div>
                          ) : (
                              <div onClick={() => startEditing(user.id, 'password', user.password)}>
                                {"••••••••"}
                              </div>
                          )}
                        </td>
                        <td>
                          {user.deleted ? 'DELETED' : 'ACTIVE'}
                        </td>
                        <td>
                          <button
                            className="btn btn-danger-soft btn-sm"
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
          </section>
        </main>
      </div>
    </div>
  );
};

export default AdminDashboard;