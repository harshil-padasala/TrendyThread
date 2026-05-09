import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

const AuthContext = createContext();

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        // Check local storage for existing session
        const savedUser = localStorage.getItem('user');
        if (savedUser) {
            setUser(JSON.parse(savedUser));
        }
        setLoading(false);

        // Listen for unauthorized events from API interceptor
        const handleUnauthorized = () => {
            setUser(null);
            localStorage.removeItem('user');
            navigate('/', { replace: true });
        };
        
        window.addEventListener('unauthorized', handleUnauthorized);
        return () => window.removeEventListener('unauthorized', handleUnauthorized);
    }, [navigate]);

    const login = (userData) => {
        console.log('🔐 Login called with userData:', userData);
        console.log('🔐 userData keys:', Object.keys(userData));
        console.log('🔐 Has token?', !!userData.token);
        
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
        console.log('✅ User stored in localStorage');
    };

    const logout = async () => {
        try {
            await authService.logout();
        } catch (error) {
            console.error("Logout error:", error);
        } finally {
            setUser(null);
            localStorage.removeItem('user');
            navigate('/', { replace: true }); // Replace history entry so back button won't return to protected pages
        }
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, loading, isAuthenticated: !!user }}>
            {children}
        </AuthContext.Provider>
    );
}


export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
