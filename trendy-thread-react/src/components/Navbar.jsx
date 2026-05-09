import React, { useEffect, useState } from 'react';
import { BookOpen, Sun, Moon, Menu, X, User, LogOut, Plus, Settings, Search } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { categoryService } from '../services/categoryService';
import { useTheme } from '../context/ThemeContext';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
    const [isOpen, setIsOpen] = useState(false);
    const [categories, setCategories] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const location = useLocation();
    const navigate = useNavigate();
    const { isDark, toggleTheme } = useTheme();
    const { user, isAuthenticated, logout } = useAuth();

    useEffect(() => {
        const fetchCategories = async () => {
            if (!isAuthenticated) {
                return; // Don't fetch categories if not authenticated
            }
            try {
                const data = await categoryService.getFeaturedCategories();
                setCategories(data);
            } catch (error) {
                console.error('Failed to fetch featured categories:', error);
            }
        };
        fetchCategories();
    }, [isAuthenticated]);

    const isActive = (path) => location.pathname === path;

    const handleSearch = (e) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
            setSearchQuery('');
            setIsOpen(false); // Close mobile menu after search
        }
    };

    return (
        <nav className="bg-white/90 dark:bg-slate-800/90 backdrop-blur-md border-b border-slate-200 dark:border-slate-700 sticky top-0 z-50 transition-colors duration-300">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex items-center">
                        <Link to="/" className="flex-shrink-0 flex items-center gap-2 group">
                            <div className="bg-indigo-600 p-2 rounded-lg group-hover:bg-indigo-700 transition-colors">
                                <BookOpen className="h-6 w-6 text-white" />
                            </div>
                            <span className="text-2xl font-black bg-clip-text text-transparent bg-gradient-to-r from-indigo-600 to-violet-600 tracking-tight">
                                TrendyThread
                            </span>
                        </Link>
                    </div>

                    {/* Search Bar - Desktop */}
                    <div className="hidden md:flex items-center flex-1 max-w-md mx-6">
                        <form onSubmit={handleSearch} className="w-full">
                            <div className="relative">
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="Search posts..."
                                    className="w-full px-4 py-2 pl-10 pr-4 text-sm bg-slate-100 dark:bg-slate-700 text-slate-900 dark:text-white rounded-lg border border-slate-200 dark:border-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors"
                                />
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                            </div>
                        </form>
                    </div>

                    {/* Desktop nav */}
                    <div className="hidden sm:ml-6 sm:flex sm:items-center space-x-6">
                        <Link to="/" className={`px-1 py-2 text-sm font-medium transition-colors border-b-2 ${isActive('/') ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400' : 'border-transparent text-slate-500 dark:text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:border-indigo-300'}`}>
                            Home
                        </Link>

                        {isAuthenticated && (
                            <>
                                <Link to="/create-post" className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg transition-all hover:scale-105 shadow-md">
                                    <Plus className="w-4 h-4" />
                                    Create Post
                                </Link>
                                <div className="relative group">
                                    <button className="px-1 py-2 text-sm font-medium text-slate-500 dark:text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors border-b-2 border-transparent focus:outline-none">
                                        Categories
                                    </button>
                                    {/* Dropdown menu */}
                                    <div className="absolute left-0 mt-2 w-48 rounded-xl shadow-lg bg-white dark:bg-slate-700 ring-1 ring-black ring-opacity-5 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 transform origin-top-left z-50">
                                        <div className="py-1 rounded-xl overflow-hidden">
                                            {categories.map((category) => (
                                                <Link
                                                    key={category.categoryId}
                                                    to={`/category/${category.categoryId}`}
                                                    className="block px-4 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-600 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                                                >
                                                    {category.categoryName}
                                                </Link>
                                            ))}
                                            {categories.length > 0 && (
                                                <hr className="border-slate-200 dark:border-slate-600 my-1" />
                                            )}
                                            <Link
                                                to="/categories"
                                                className="block px-4 py-2 text-sm text-indigo-600 dark:text-indigo-400 hover:bg-slate-50 dark:hover:bg-slate-600 font-semibold transition-colors"
                                            >
                                                View All Categories →
                                            </Link>
                                        </div>
                                    </div>
                                </div>
                            </>
                        )}

                        {/* Auth Buttons / User Profile */}
                        {isAuthenticated ? (
                            <div className="flex items-center gap-4">
                                <div className="relative group">
                                    <button className="flex items-center gap-2 group/btn focus:outline-none">
                                        <div className="w-8 h-8 rounded-full bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center text-indigo-600 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800 transition-all group-hover/btn:scale-110">
                                            <User className="w-4 h-4" />
                                        </div>
                                        <span className="text-sm font-bold text-slate-700 dark:text-slate-200">{user.name}</span>
                                    </button>
                                    
                                    {/* Profile Dropdown */}
                                    <div className="absolute right-0 mt-2 w-48 rounded-xl shadow-lg bg-white dark:bg-slate-700 ring-1 ring-black ring-opacity-5 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 transform origin-top-right z-50">
                                        <div className="py-1 rounded-xl overflow-hidden">
                                            <Link
                                                to="/profile"
                                                className="block px-4 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-600 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                                            >
                                                <div className="flex items-center gap-2">
                                                    <User className="w-4 h-4" />
                                                    My Profile
                                                </div>
                                            </Link>
                                            <Link
                                                to="/edit-profile"
                                                className="block px-4 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-600 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                                            >
                                                <div className="flex items-center gap-2">
                                                    <Settings className="w-4 h-4" />
                                                    Edit Profile
                                                </div>
                                            </Link>
                                            {user?.role === 'ROLE_ADMIN' && (
                                                <Link
                                                    to="/admin/categories"
                                                    className="block px-4 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-600 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                                                >
                                                    <div className="flex items-center gap-2">
                                                        <Settings className="w-4 h-4" />
                                                        Manage Categories
                                                    </div>
                                                </Link>
                                            )}
                                            <hr className="border-slate-200 dark:border-slate-600 my-1" />
                                            <button
                                                onClick={logout}
                                                className="w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                                            >
                                                <div className="flex items-center gap-2">
                                                    <LogOut className="w-4 h-4" />
                                                    Logout
                                                </div>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="flex items-center gap-3">
                                <Link to="/login" className="px-5 py-2 text-sm font-bold text-slate-700 dark:text-slate-200 hover:text-indigo-600 transition-colors">
                                    Sign in
                                </Link>
                                <Link to="/signup" className="px-5 py-2 text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl transition-all hover:scale-105 shadow-lg shadow-indigo-100 dark:shadow-none">
                                    Sign up
                                </Link>
                            </div>
                        )}

                        {/* Dark mode toggle */}
                        <button
                            onClick={toggleTheme}
                            aria-label="Toggle dark mode"
                            className="p-2 rounded-full text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                        >
                            {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
                        </button>
                    </div>

                    {/* Mobile: dark toggle + hamburger */}
                    <div className="-mr-2 flex items-center gap-1 sm:hidden">
                        <button
                            onClick={toggleTheme}
                            aria-label="Toggle dark mode"
                            className="p-2 rounded-md text-slate-400 dark:text-slate-400 hover:text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                        >
                            {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
                        </button>
                        <button
                            onClick={() => setIsOpen(!isOpen)}
                            className="inline-flex items-center justify-center p-2 rounded-md text-slate-400 hover:text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500 transition-colors"
                        >
                            <span className="sr-only">Open main menu</span>
                            {isOpen ? <X className="block h-6 w-6" /> : <Menu className="block h-6 w-6" />}
                        </button>
                    </div>
                </div>
            </div>

            {/* Mobile menu */}
            <div className={`${isOpen ? 'block' : 'hidden'} sm:hidden bg-white dark:bg-slate-800 border-t border-slate-200 dark:border-slate-700`}>
                {/* Mobile Search */}
                <div className="px-4 pt-4 pb-2">
                    <form onSubmit={handleSearch}>
                        <div className="relative">
                            <input
                                type="text"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                placeholder="Search posts..."
                                className="w-full px-4 py-2 pl-10 pr-4 text-sm bg-slate-100 dark:bg-slate-700 text-slate-900 dark:text-white rounded-lg border border-slate-200 dark:border-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors"
                            />
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        </div>
                    </form>
                </div>
                
                <div className="pt-2 pb-3 space-y-1">
                    <Link to="/" className={`block pl-3 pr-4 py-2 border-l-4 text-base font-medium ${isActive('/') ? 'bg-indigo-50 dark:bg-indigo-900/30 border-indigo-500 text-indigo-700 dark:text-indigo-400' : 'border-transparent text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300 hover:text-slate-700 dark:hover:text-slate-200'}`}>
                        Home
                    </Link>
                    {!isAuthenticated ? (
                        <>
                            <Link to="/login" className="block pl-3 pr-4 py-2 border-l-4 border-transparent text-base font-medium text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300">
                                Sign in
                            </Link>
                            <Link to="/signup" className="block pl-3 pr-4 py-2 border-l-4 border-transparent text-base font-medium text-indigo-600 dark:text-indigo-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300">
                                Sign up
                            </Link>
                        </>
                    ) : (
                        <>
                            <Link to="/create-post" className="block pl-3 pr-4 py-2 border-l-4 border-transparent text-base font-medium text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300">
                                Create Post
                            </Link>
                            <Link to="/categories" className={`block pl-3 pr-4 py-2 border-l-4 text-base font-medium ${isActive('/categories') ? 'bg-indigo-50 dark:bg-indigo-900/30 border-indigo-500 text-indigo-700 dark:text-indigo-400' : 'border-transparent text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300 hover:text-slate-700 dark:hover:text-slate-200'}`}>
                                Categories
                            </Link>
                            <Link to="/profile" className="block pl-3 pr-4 py-2 border-l-4 border-transparent text-base font-medium text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300">
                                Profile ({user.name})
                            </Link>
                            <Link to="/edit-profile" className="block pl-3 pr-4 py-2 border-l-4 border-transparent text-base font-medium text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300">
                                Edit Profile
                            </Link>
                            <button
                                onClick={logout}
                                className="w-full text-left block pl-3 pr-4 py-2 border-l-4 border-transparent text-base font-medium text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 hover:border-red-300 transition-colors"
                            >
                                Sign out
                            </button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
}
