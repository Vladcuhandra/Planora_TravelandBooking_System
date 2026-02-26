import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Layout from "./layout/Layout";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Profile from "./pages/Profile";
import AdminDashboard from "./pages/AdminDashboard";
import TransportPage from "./pages/TransportPage";
import AccommodationPage from "./pages/AccommodationPage";
import TripPage from "./pages/TripPage";
import BookingPage from "./pages/BookingPage";
import { apiFetch } from "./api/http.js";
import { useEffect, useState } from "react";
import Main from "./pages/Main.jsx";

// Protect routes that require authentication
function RequireAuth({ children }) {
    const token = localStorage.getItem("accessToken");
    return token ? children : <Navigate to="/login" replace />;
}

// Root route behaves like "post-auth landing page"
function HomeRedirect() {
    const token = localStorage.getItem("accessToken");
    return <Navigate to={token ? "/main" : "/login"} replace />;
}

function RequireAdmin({ children }) {
    const [isAdmin, setIsAdmin] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchUserProfile = async () => {
            try {
                const res = await apiFetch("/api/users/profile", { method: "GET" });
                if (res.ok) {
                    const data = await res.json();
                    setIsAdmin(data.role === "ADMIN");
                } else {
                    setIsAdmin(false);
                }
            } catch (err) {
                setIsAdmin(false);
            } finally {
                setLoading(false);
            }
        };
        fetchUserProfile();
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    return isAdmin ? children : <Navigate to="/login" replace />;
}

export default function App() {
    const [error, setError] = useState(null);
    const [user, setUser] = useState(null);

    useEffect(() => {
        fetchUserProfile();
    }, []);

    const fetchUserProfile = async () => {
        try {
            const res = await apiFetch(`/api/users/profile`, { method: "GET" });
            if (res.ok) {
                const data = await res.json();
                console.log("User Profile:", data);
                setUser(data);
            } else {
                setError("Failed to load user profile.");
            }
        } catch (err) {
            setError("An error occurred: " + err.message);
        }
    };

    return (
        <BrowserRouter>
            <Routes>
                {/* Root = authenticated landing page */}
                <Route path="/" element={<HomeRedirect />} />

                {/* Auth routes */}
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />

                {/* App layout */}
                <Route element={<Layout />}>
                    <Route
                        path="/profile"
                        element={
                            <RequireAuth>
                                <Profile />
                            </RequireAuth>
                        }
                    />

                    <Route
                        path="/admin"
                        element={
                            <RequireAuth>
                                <RequireAdmin>
                                    <AdminDashboard />
                                </RequireAdmin>
                            </RequireAuth>
                        }
                    />

                    <Route
                        path="/transports"
                        element={
                            <RequireAuth>
                                <TransportPage />
                            </RequireAuth>
                        }
                    />

                    <Route
                        path="/accommodations"
                        element={
                            <RequireAuth>
                                <AccommodationPage />
                            </RequireAuth>
                        }
                    />

                    <Route
                        path="/trips"
                        element={
                            <RequireAuth>
                                <TripPage />
                            </RequireAuth>
                        }
                    />

                    <Route
                        path="/bookings"
                        element={
                            <RequireAuth>
                                <BookingPage />
                            </RequireAuth>
                        }
                    />

                    {/* Authenticated home */}
                    <Route
                        path="/main"
                        element={
                            <RequireAuth>
                                <Main />
                            </RequireAuth>
                        }
                    />

                    <Route
                        path="/trips/:tripId/booking"
                        element={
                            <RequireAuth>
                                <Main />
                            </RequireAuth>
                        }
                    />
                </Route>

                {/* Catch-all */}
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}