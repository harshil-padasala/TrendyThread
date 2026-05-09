import api from './api';

const fetchByPostId = async (postId) => {
    const response = await api.get(`/posts/${postId}/comments`);
    return response.data;
};

const create = async (postId, commentData) => {
    const response = await api.post(`/post/${postId}/comments`, commentData);
    return response.data;
};

const update = async (postId, commentId, commentData) => {
    const response = await api.put(`/posts/${postId}/comments/${commentId}`, commentData);
    return response.data;
};

const remove = async (postId, commentId) => {
    const response = await api.delete(`/posts/${postId}/comments/${commentId}`);
    return response.data;
};

export const commentService = {
    fetchByPostId,
    create,
    update,
    remove
};

