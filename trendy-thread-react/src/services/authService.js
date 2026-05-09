import api from './api';

const login = async (credentials) => {
    // credentials contain email and password
    console.log('🔐 Calling login API with credentials');
    const response = await api.post('/auth/login', credentials);
    console.log('🔐 Login API response:', response.data);
    console.log('🔐 Response data keys:', Object.keys(response.data || {}));
    return response.data;
};

const signup = async (userData) => {
    // userData contains userName, firstName, lastName, email, password
    // Backend expects BloggerDto with all required fields
    const bloggerDto = {
        userName: userData.userName,
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
    };
    const response = await api.post('/auth/signup', bloggerDto);
    return response.data;
};

const logout = async () => {
    // The backend logout expects the Authorization header which is handled by our api interceptor
    try {
        const user = JSON.parse(localStorage.getItem('user'));
        if (user && user.token) {
            await api.post('/auth/logout');
        }
    } catch (error) {
        console.error('Logout failed', error);
    }
};

export const authService = {
    login,
    signup,
    logout
};

