const Footer = () => {
    return (
        <footer className="bg-white shadow-sm text-center text-gray-600 py-3 mt-auto h-[8vh]">
            <div className="container mx-auto px-4">
                <small>&copy; {new Date().getFullYear()} Grade Management</small>
            </div>
        </footer>
    );
};

export default Footer;
