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
import {apiFetch} from "./api/http.js";
import {useEffect, useState} from "react";
import Main from "./pages/Main";

// Protect routes that require authentication
function RequireAuth({ children }) {
    const token = localStorage.getItem("accessToken");
    return token ? children : <Navigate to="/login" replace />;
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
        return <div>Loading...</div>; // Optionally, show a loading spinner
    }

    return isAdmin ? children : <Navigate to="/login" replace />;
}

export default function App() {
    const [error, setError] = useState(null);
    const [user, setUser] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        if (token) fetchUserProfile();
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
                {/* Default route redirects to login */}
                <Route path="/" element={<Navigate to="/login" replace />} />

                {/* Authentication routes */}
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route element={<Layout />}>
                    <Route path="/profile" element={<Profile />} />
                    <Route path="/admin" element={
                        <RequireAuth> <RequireAdmin> <AdminDashboard /> </RequireAdmin> </RequireAuth>}
                    />
                    <Route path="/transports" element={<RequireAuth><TransportPage /></RequireAuth>}/>
                    <Route path="/accommodations" element={<RequireAuth><AccommodationPage /></RequireAuth>}/>
                    <Route path="/trips" element={<RequireAuth><TripPage /></RequireAuth>}/>
                    <Route path="/bookings" element={<RequireAuth><BookingPage /></RequireAuth>}/>
                    <Route path="/main" element={<Main />} />
                </Route>

                {/* Catch-all redirects to login */}
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}