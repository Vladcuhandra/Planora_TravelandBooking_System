import Topbar from "../components/Topbar";
import Sidebar from "../components/Sidebar";
import { Outlet } from "react-router-dom";

function Layout() {
    const isAdmin = true;

    return (
        <>
            <Topbar />
            <Sidebar isAdmin={isAdmin} />
            <div className="container-fluid mt-3">
                <Outlet />
            </div>
        </>
    );
}

export default Layout;