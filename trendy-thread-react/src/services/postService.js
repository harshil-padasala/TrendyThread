import api from './api';

const fetchAll = async () => {
    const response = await api.get('/posts');
    return response.data;
};

const fetchLatest = async (limit = 6) => {
    const response = await api.get(`/posts/latest?limit=${limit}`);
    return response.data;
};

const fetchById = async (id) => {
    const response = await api.get(`/posts/${id}`);
    return response.data;
};

const fetchByCategory = async (categoryId) => {
    const response = await api.get(`/posts/category/${categoryId}`);
    return response.data;
};

const fetchByUser = async (userId) => {
    const response = await api.get(`/posts/user/${userId}`);
    return response.data;
};

const fetchMyPosts = async () => {
    // Fetches posts of the currently authenticated user
    const response = await api.get('/posts/blogger');
    return response.data;
};

const searchPosts = async (keyword) => {
    const response = await api.get(`/posts/search/${keyword}`);
    return response.data;
};

const create = async (postData, categoryId) => {
    const response = await api.post(`/posts/category/${categoryId}`, postData);
    return response.data;
};

const update = async (id, postData) => {
    const response = await api.put(`/posts/${id}`, postData);
    return response.data;
};

const remove = async (id) => {
    const response = await api.delete(`/posts/${id}`);
    return response.data;
};

export const postService = {
    fetchAll,
    fetchLatest,
    fetchById,
    fetchByCategory,
    fetchByUser,
    fetchMyPosts,
    searchPosts,
    create,
    update,
    remove
};

