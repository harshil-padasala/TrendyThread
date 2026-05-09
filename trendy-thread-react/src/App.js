import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Theme & Auth Context
import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider, useAuth } from './context/AuthContext';

// Components
import Navbar from './components/Navbar';

// Pages
import Home from './pages/Home';
import PostDetail from './pages/PostDetail';
import UserProfile from './pages/UserProfile';
import CategoryView from './pages/CategoryView';
import Login from './pages/Login';
import Landing from './pages/Landing';
import CreatePost from './pages/CreatePost';
import EditProfile from './pages/EditProfile';
import SearchResults from './pages/SearchResults';
import Categories from './pages/Categories';
import AdminCategories from './pages/admin/AdminCategories';

function AppRoutes() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-900">
      <div className="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
    </div>;
  }

  return (
    <Routes>
      <Route path="/" element={isAuthenticated ? <Home /> : <Landing />} />
      <Route path="/login" element={!isAuthenticated ? <Login /> : <Navigate to="/" />} />
      <Route path="/signup" element={!isAuthenticated ? <Login /> : <Navigate to="/" />} />
      <Route path="/post/:id" element={<PostDetail />} />
      <Route path="/create-post" element={isAuthenticated ? <CreatePost /> : <Navigate to="/login" />} />
      <Route path="/edit-profile" element={isAuthenticated ? <EditProfile /> : <Navigate to="/login" />} />
      <Route path="/profile" element={isAuthenticated ? <UserProfile /> : <Navigate to="/login" />} />
      <Route path="/user/:id" element={<UserProfile />} />
      <Route path="/category/:categoryId" element={<CategoryView />} />
      <Route path="/categories" element={<Categories />} />
      <Route path="/search" element={<SearchResults />} />
      <Route path="/admin/categories" element={isAuthenticated ? <AdminCategories /> : <Navigate to="/login" />} />
    </Routes>
  );
}

function App() {
  return (
    <ThemeProvider>
      <Router>
        <AuthProvider>
          <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-900 font-sans text-slate-900 dark:text-slate-100 selection:bg-indigo-100 selection:text-indigo-900 transition-colors duration-300">
            <Navbar />

            <main className="flex-grow w-full pt-8 pb-16">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <AppRoutes />
              </div>
            </main>

            <footer className="bg-white dark:bg-slate-800 border-t border-slate-200 dark:border-slate-700 mt-auto shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.02)] transition-colors duration-300">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 md:py-12 flex flex-col md:flex-row justify-between items-center gap-4">
                <div className="flex items-center gap-2">
                  <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 to-purple-600">TrendyThread</span>
                </div>
                <p className="text-center text-sm text-slate-500 dark:text-slate-400 font-medium">
                  © {new Date().getFullYear()} TrendyThread. Designed for sharing ideas.
                </p>
              </div>
            </footer>
          </div>
        </AuthProvider>
      </Router>
    </ThemeProvider>
  );
}

export default App;
