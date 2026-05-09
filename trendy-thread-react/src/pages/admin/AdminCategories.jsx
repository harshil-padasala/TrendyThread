import React, { useState, useEffect } from 'react';
import { categoryService } from '../../services/categoryService';
import { Star, RefreshCw, Save, TrendingUp, ChevronUp, ChevronDown, AlertCircle } from 'lucide-react';

export default function AdminCategories() {
  const [allCategories, setAllCategories] = useState([]);
  const [featuredCategories, setFeaturedCategories] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [allCats, featured, suggested] = await Promise.all([
        categoryService.getAllCategories(0, 100),
        categoryService.getFeaturedCategories(),
        categoryService.getSuggestedCategories(15)
      ]);
      
      setAllCategories(allCats.content);
      setFeaturedCategories(featured);
      setSuggestions(suggested);
      setMessage(null);
    } catch (error) {
      console.error('Failed to load data:', error);
      setMessage({ type: 'error', text: 'Failed to load categories' });
    } finally {
      setLoading(false);
    }
  };

  const handleToggleFeatured = async (category) => {
    const isFeatured = featuredCategories.some(c => c.categoryId === category.categoryId);
    const displayOrder = isFeatured ? 0 : featuredCategories.length + 1;

    try {
      await categoryService.updateFeaturedStatus(category.categoryId, {
        featured: !isFeatured,
        displayOrder: displayOrder
      });
      
      await loadData(); // Reload all data
      setMessage({ 
        type: 'success', 
        text: `${category.categoryName} ${isFeatured ? 'removed from' : 'added to'} featured` 
      });
      
      // Clear message after 3 seconds
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      console.error('Failed to update featured status:', error);
      setMessage({ type: 'error', text: 'Failed to update category' });
    }
  };

  const handleReorderFeatured = async (categoryId, direction) => {
    const currentIndex = featuredCategories.findIndex(c => c.categoryId === categoryId);
    if (currentIndex === -1) return;

    const newIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;
    if (newIndex < 0 || newIndex >= featuredCategories.length) return;

    const reordered = [...featuredCategories];
    [reordered[currentIndex], reordered[newIndex]] = [reordered[newIndex], reordered[currentIndex]];

    try {
      const categoryIds = reordered.map(c => c.categoryId);
      await categoryService.bulkUpdateFeaturedCategories(categoryIds);
      await loadData();
      setMessage({ type: 'success', text: 'Order updated successfully' });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      console.error('Failed to reorder:', error);
      setMessage({ type: 'error', text: 'Failed to update order' });
    }
  };

  const handleAutoUpdate = async () => {
    setSaving(true);
    try {
      await categoryService.triggerAutoUpdate();
      await loadData();
      setMessage({ type: 'success', text: 'Auto-update completed successfully' });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      console.error('Failed to auto-update:', error);
      setMessage({ type: 'error', text: 'Failed to run auto-update' });
    } finally {
      setSaving(false);
    }
  };

  const handleRecalculatePostCounts = async () => {
    setSaving(true);
    try {
      await categoryService.recalculatePostCounts();
      await loadData();
      setMessage({ type: 'success', text: 'Post counts recalculated successfully' });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      console.error('Failed to recalculate post counts:', error);
      setMessage({ type: 'error', text: 'Failed to recalculate post counts' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
        <p className="mt-4 text-slate-600 dark:text-slate-400">Loading...</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2 text-slate-900 dark:text-slate-100">
          Category Management
        </h1>
        <p className="text-slate-600 dark:text-slate-400">
          Manage featured categories and their display order
        </p>
      </div>

      {/* Message */}
      {message && (
        <div className={`mb-6 px-4 py-3 rounded-lg flex items-center gap-2 ${
          message.type === 'success' ? 
            'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800' : 
            'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-800'
        }`}>
          <AlertCircle className="w-5 h-5" />
          {message.text}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Featured Categories */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-800 rounded-lg shadow-lg p-6 border border-slate-200 dark:border-slate-700">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold flex items-center gap-2 text-slate-900 dark:text-slate-100">
              <Star className="w-5 h-5 text-yellow-500" />
              Featured Categories ({featuredCategories.length})
            </h2>
            <div className="flex gap-2">
              <button
                onClick={handleRecalculatePostCounts}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors font-semibold"
                title="Recalculate all category post counts from database"
              >
                <RefreshCw className={`w-4 h-4 ${saving ? 'animate-spin' : ''}`} />
                Sync Counts
              </button>
              <button
                onClick={handleAutoUpdate}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors font-semibold"
              >
                <RefreshCw className={`w-4 h-4 ${saving ? 'animate-spin' : ''}`} />
                Auto-Update
              </button>
            </div>
          </div>

          <div className="space-y-2">
            {featuredCategories.length > 0 ? (
              featuredCategories.map((category, index) => (
                <div 
                  key={category.categoryId}
                  className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-700/50 rounded-lg border border-slate-200 dark:border-slate-600"
                >
                  <div className="flex items-center gap-3">
                    <span className="font-semibold text-slate-500 dark:text-slate-400">
                      #{index + 1}
                    </span>
                    <div>
                      <h3 className="font-semibold text-slate-900 dark:text-slate-100">
                        {category.categoryName}
                      </h3>
                      <p className="text-sm text-slate-600 dark:text-slate-400">
                        {category.postCount || 0} posts
                      </p>
                    </div>
                    {category.autoSuggested && (
                      <span className="bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-400 text-xs px-2 py-1 rounded font-semibold">
                        Auto
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleReorderFeatured(category.categoryId, 'up')}
                      disabled={index === 0}
                      className="p-1 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                      title="Move up"
                    >
                      <ChevronUp className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleReorderFeatured(category.categoryId, 'down')}
                      disabled={index === featuredCategories.length - 1}
                      className="p-1 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                      title="Move down"
                    >
                      <ChevronDown className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleToggleFeatured(category)}
                      className="px-3 py-1 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors font-semibold text-sm"
                    >
                      Remove
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-center text-slate-500 dark:text-slate-400 py-8">
                No featured categories yet. Add some from suggestions!
              </p>
            )}
          </div>
        </div>

        {/* Suggestions */}
        <div className="bg-white dark:bg-slate-800 rounded-lg shadow-lg p-6 border border-slate-200 dark:border-slate-700">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-slate-900 dark:text-slate-100">
            <TrendingUp className="w-5 h-5 text-green-500" />
            Suggestions
          </h2>
          <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">
            Based on post count and activity
          </p>
          <div className="space-y-2 max-h-[600px] overflow-y-auto">
            {suggestions.map((stat) => {
              const isFeatured = featuredCategories.some(c => c.categoryId === stat.categoryId);
              return (
                <div 
                  key={stat.categoryId}
                  className="p-3 bg-slate-50 dark:bg-slate-700/50 rounded-lg border border-slate-200 dark:border-slate-600"
                >
                  <div className="flex items-center justify-between mb-1">
                    <h3 className="font-medium text-sm text-slate-900 dark:text-slate-100">
                      {stat.categoryName}
                    </h3>
                    {!isFeatured && (
                      <button
                        onClick={() => handleToggleFeatured({ 
                          categoryId: stat.categoryId, 
                          categoryName: stat.categoryName 
                        })}
                        className="text-xs text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-semibold transition-colors"
                      >
                        + Add
                      </button>
                    )}
                    {isFeatured && (
                      <span className="text-xs text-green-600 dark:text-green-400 font-semibold">
                        ✓ Featured
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-600 dark:text-slate-400">
                    {stat.postCount} posts
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
