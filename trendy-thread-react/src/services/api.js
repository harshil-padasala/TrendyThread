import axios from 'axios';

const API_URL = 'http://localhost:8079/api/v1';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to include the JWT token in all requests
api.interceptors.request.use(
    (config) => {
        const userStr = localStorage.getItem('user');
        console.log('📤 API Request:', config.url);
        console.log('📦 LocalStorage user:', userStr);
        
        if (userStr) {
            try {
                const user = JSON.parse(userStr);
                console.log('👤 Parsed user object:', user);
                console.log('🔑 Token present?', !!user.token);
                
                if (user && user.token) {
                    config.headers.Authorization = `Bearer ${user.token}`;
                    console.log('✅ Added Authorization header to request:', config.url);
                } else {
                    console.warn('⚠️ User object exists but no token found. Keys:', Object.keys(user));
                }
            } catch (e) {
                console.error('❌ Error parsing user from localStorage:', e);
            }
        } else {
            console.log('⚠️ No user in localStorage for request:', config.url);
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add a response interceptor to handle token expiration or other errors
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        console.error('API Error:', {
            url: originalRequest?.url,
            status: error.response?.status,
            message: error.response?.data,
            hasToken: originalRequest?.headers?.Authorization ? 'Yes' : 'No'
        });

        // If the error is 401 and not a retry, we could potentially handle refresh token logic here
        // For now, let's just log out if we get a 401 on a protected route
        if (error.response?.status === 401 && !originalRequest._retry) {
            // Check if it's the login request itself
            if (originalRequest.url.includes('/auth/login') || originalRequest.url.includes('/auth/signup')) {
                return Promise.reject(error);
            }

            console.warn('401 Unauthorized - logging out user');
            // Clear user data on unauthorized access
            // Note: Don't use window.location.href as it causes hard refresh
            // Let React Router and AuthContext handle navigation
            localStorage.removeItem('user');
            
            // Trigger a custom event that AuthContext can listen to
            window.dispatchEvent(new Event('unauthorized'));
        }

        return Promise.reject(error);
    }
);

export default api;
