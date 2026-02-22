import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Layout from "./layout/Layout";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import Profile from "./pages/Profile";
import AdminDashboard from "./pages/AdminDashboard";
import TransportPage from "./pages/TransportPage";
import AccommodationPage from "./pages/AccommodationPage";
import TripPage from "./pages/TripPage";
import BookingPage from "./pages/BookingPage";

// Protect routes that require authentication
function RequireAuth({ children }) {
    const token = localStorage.getItem("accessToken");
    return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Default route redirects to login */}
                <Route path="/" element={<Navigate to="/login" replace />} />

                {/* Authentication routes */}
                <Route path="/login" element={<Login />} />
                <Route element={<Layout />}>
                    <Route path="/signup" element={<Signup />} />
                    <Route path="/profile" element={<Profile />} />
                    <Route path="/dashboard" element={<RequireAuth><Dashboard /></RequireAuth>}/>
                    <Route path="/admin" element={<RequireAuth><AdminDashboard /></RequireAuth>}/>
                    <Route path="/transports" element={<RequireAuth><TransportPage /></RequireAuth>}/>
                    <Route path="/accommodations" element={<RequireAuth><AccommodationPage /></RequireAuth>}/>
                    <Route path="/trips" element={<RequireAuth><TripPage /></RequireAuth>}/>
                    <Route path="/bookings" element={<RequireAuth><BookingPage /></RequireAuth>}/>
                </Route>

                {/* Catch-all redirects to login */}
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}