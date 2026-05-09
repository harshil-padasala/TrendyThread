import api from './api';

// Existing methods
const fetchAll = async () => {
    const response = await api.get('/category');
    return response.data;
};

const fetchById = async (id) => {
    const response = await api.get(`/category/${id}`);
    return response.data;
};

const create = async (categoryData) => {
    const response = await api.post('/category', categoryData);
    return response.data;
};

const update = async (id, categoryData) => {
    const response = await api.put(`/category/${id}`, categoryData);
    return response.data;
};

const remove = async (id) => {
    const response = await api.delete(`/category/${id}`);
    return response.data;
};

// NEW METHODS for Featured Categories

// Get featured categories for navbar (10-15 items)
const getFeaturedCategories = async () => {
    const response = await api.get('/category/featured');
    return response.data;
};

// Get all categories with pagination
const getAllCategories = async (page = 0, size = 20, sortBy = 'name') => {
    const response = await api.get('/category/paginated', {
        params: { page, size, sortBy }
    });
    return response.data;
};

// Search categories by keyword
const searchCategories = async (keyword, page = 0, size = 20) => {
    const response = await api.get('/category/search', {
        params: { keyword, page, size }
    });
    return response.data;
};

// ADMIN METHODS

// Admin: Update featured status and display order
const updateFeaturedStatus = async (categoryId, data) => {
    const response = await api.put(`/admin/category/${categoryId}/featured`, data);
    return response.data;
};

// Admin: Get suggested categories based on algorithm
const getSuggestedCategories = async (limit = 15) => {
    const response = await api.get('/admin/category/suggestions', {
        params: { limit }
    });
    return response.data;
};

// Admin: Bulk update featured categories
const bulkUpdateFeaturedCategories = async (categoryIds) => {
    const response = await api.put('/admin/category/featured/bulk', categoryIds);
    return response.data;
};

// Admin: Trigger auto-update algorithm
const triggerAutoUpdate = async () => {
    const response = await api.post('/admin/category/auto-update');
    return response.data;
};

// Admin: Recalculate all post counts
const recalculatePostCounts = async () => {
    const response = await api.post('/admin/category/recalculate-post-counts');
    return response.data;
};

export const categoryService = {
    fetchAll,
    fetchById,
    create,
    update,
    remove,
    // New public methods
    getFeaturedCategories,
    getAllCategories,
    searchCategories,
    // Admin methods
    updateFeaturedStatus,
    getSuggestedCategories,
    bulkUpdateFeaturedCategories,
    triggerAutoUpdate,
    recalculatePostCounts
};

