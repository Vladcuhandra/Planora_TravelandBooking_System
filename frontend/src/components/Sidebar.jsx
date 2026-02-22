import { Link } from "react-router-dom";
import profileIcon from "../assets/icons/profile.png";
import usersIcon from "../assets/icons/users.png";
import transportIcon from "../assets/icons/transport.png";
import accommodationIcon from "../assets/icons/accommodation.png";
import tripIcon from "../assets/icons/trip.png";
import bookingIcon from "../assets/icons/booking.png";

function Sidebar({ isAdmin = false }) {
    const navItems = [
        { label: "Profile", to: "/profile", icon: profileIcon },
        { label: "Users", to: "/admin", icon: usersIcon, adminOnly: true },
        { label: "Transports", to: "/transports", icon: transportIcon },
        { label: "Accommodations", to: "/accommodations", icon: accommodationIcon },
        { label: "Trips", to: "/trips", icon: tripIcon },
        { label: "Bookings", to: "/bookings", icon: bookingIcon },
    ];

    return (
        <>
            {/* Mini Sidebar (desktop) */}
            <aside className="mini-sidebar d-none d-lg-flex" id="miniSidebar">
                {navItems.map(
                    ({ label, to, icon, adminOnly }) =>
                        (!adminOnly || isAdmin) && (
                            <Link key={to} to={to} className="mini-sidebar-item p-nav-link">
                                <img className="sidebar-icon-img" src={icon} alt={label} />
                                <span className="mini-sidebar-label">{label}</span>
                            </Link>
                        )
                )}
            </aside>

            {/* Drawer Sidebar (mobile / offcanvas) */}
            <div
                className="offcanvas offcanvas-start planora-dark sidebar-drawer"
                tabIndex="-1"
                id="sidebar"
                aria-labelledby="sidebarLabel"
            >
                <div className="offcanvas-header">
                    <h5 className="offcanvas-title" id="sidebarLabel">
                        Navigation
                    </h5>
                    <button
                        type="button"
                        className="btn-close btn-close-white"
                        data-bs-dismiss="offcanvas"
                    ></button>
                </div>

                <div className="offcanvas-body">
                    <nav className="sidebar-nav" id="drawerSidebarNav">
                        {navItems.map(
                            ({ label, to, icon, adminOnly }) =>
                                (!adminOnly || isAdmin) && (
                                    <Link key={to} to={to} className="sidebar-link p-nav-link">
                                        <img className="sidebar-icon-img" src={icon} alt={label} />
                                        <span>{label}</span>
                                    </Link>
                                )
                        )}
                    </nav>
                </div>
            </div>
        </>
    );
}

export default Sidebar;