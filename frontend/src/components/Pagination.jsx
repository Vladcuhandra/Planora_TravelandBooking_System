import React from "react";

const Pagination = ({
                        currentPage,
                        totalPages,
                        onPageChange
                    }) => {
    if (totalPages <= 1) return null;

    const getPageNumbers = () => {
        const pages = [];
        const maxVisible = 5;
        const start = Math.max(0, currentPage - 2);
        const end = Math.min(totalPages, start + maxVisible);

        for (let i = start; i < end; i++) {
            pages.push(i);
        }

        return pages;
    };

    return (
        <div className="d-flex justify-content-center align-items-center mt-3 gap-2 flex-wrap">

            {/* Previous */}
            <button
                className="btn btn-sm btn-soft"
                disabled={currentPage === 0}
                onClick={() => onPageChange(currentPage - 1)}
            >
                Previous
            </button>

            {/* First page shortcut */}
            {currentPage > 2 && (
                <>
                    <button
                        className="btn btn-sm btn-soft"
                        onClick={() => onPageChange(0)}
                    >
                        1
                    </button>
                    <span>...</span>
                </>
            )}

            {/* Page numbers */}
            {getPageNumbers().map((page) => (
                <button
                    key={page}
                    className={`btn btn-sm ${
                        currentPage === page
                            ? "btn-planora"
                            : "btn-soft"
                    }`}
                    onClick={() => onPageChange(page)}
                >
                    {page + 1}
                </button>
            ))}

            {/* Last page shortcut */}
            {currentPage < totalPages - 3 && (
                <>
                    <span>...</span>
                    <button
                        className="btn btn-sm btn-soft"
                        onClick={() => onPageChange(totalPages - 1)}
                    >
                        {totalPages}
                    </button>
                </>
            )}

            {/* Next */}
            <button
                className="btn btn-sm btn-soft"
                disabled={currentPage === totalPages - 1}
                onClick={() => onPageChange(currentPage + 1)}
            >
                Next
            </button>
        </div>
    );
};

export default Pagination;