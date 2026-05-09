import api from './api';

const fetchAll = async () => {
    const response = await api.get('/bloggers');
    return response.data;
};

const fetchCurrentUser = async () => {
    const response = await api.get('/bloggers/me');
    return response.data;
};

const fetchById = async (id) => {
    const response = await api.get(`/bloggers/${id}`);
    return response.data;
};

const create = async (userData) => {
    const response = await api.post('/bloggers', userData);
    return response.data;
};

const update = async (id, userData) => {
    const response = await api.put(`/bloggers/${id}`, userData);
    return response.data;
};

const updateProfile = async (profileData) => {
    const response = await api.put('/bloggers/me', profileData);
    return response.data;
};

const remove = async (id) => {
    const response = await api.delete(`/bloggers/${id}`);
    return response.data;
};

export const userService = {
    fetchAll,
    fetchCurrentUser,
    fetchById,
    create,
    update,
    updateProfile,
    remove
};

