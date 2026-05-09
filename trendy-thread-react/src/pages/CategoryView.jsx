import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { categoryService } from '../services/categoryService';
import { postService } from '../services/postService';
import PostCard from '../components/PostCard';
import { Loader2, Hash, ArrowLeft } from 'lucide-react';

export default function CategoryView() {
    const { categoryId } = useParams();
    const [category, setCategory] = useState(null);
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCategoryAndPosts = async () => {
            try {
                const catData = await categoryService.fetchById(categoryId);
                setCategory(catData);

                try {
                    const catPosts = await postService.fetchByCategory(categoryId);
                    setPosts(catPosts.content || catPosts || []);
                } catch (postErr) {
                    console.error("Failed to fetch category posts", postErr);
                }

                setLoading(false);
            } catch (err) {
                setError('Failed to load category details. Please try again later.');
                setLoading(false);
            }
        };
        fetchCategoryAndPosts();
    }, [categoryId]);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[60vh]">
                <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
            </div>
        );
    }

    if (error || !category) {
        return (
            <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-6 rounded-2xl text-center font-medium max-w-2xl mx-auto shadow-sm border border-red-100 dark:border-red-800">
                <p className="text-xl mb-4">{error || 'Category not found'}</p>
                <Link to="/" className="inline-flex items-center text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 transition-colors">
                    <ArrowLeft className="w-4 h-4 mr-2" /> Back to Home
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto space-y-12">
            {/* Category Header — indigo bg with white text, no dark change needed */}
            <div className="bg-indigo-600 rounded-3xl p-12 shadow-lg relative overflow-hidden group">
                <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/50 to-purple-600/50 mix-blend-multiply opacity-50 transition-opacity group-hover:opacity-75"></div>
                <div className="relative z-10 text-center">
                    <div className="inline-flex items-center justify-center p-3 bg-white/10 backdrop-blur-md rounded-2xl mb-6 shadow-sm border border-white/20">
                        <Hash className="h-8 w-8 text-white" />
                    </div>
                    <h1 className="text-4xl md:text-5xl font-black text-white tracking-tight mb-4 drop-shadow-md">
                        {category.categoryName}
                    </h1>
                    <p className="text-xl text-indigo-100 max-w-2xl mx-auto font-medium leading-relaxed">
                        {category.description}
                    </p>
                </div>
            </div>

            {/* Posts Grid */}
            <section>
                <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-700 pb-4 mb-8">
                    <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
                        Articles in this Category
                        <span className="bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 text-sm py-1 px-3 rounded-full font-semibold">
                            {posts.length}
                        </span>
                    </h2>
                </div>

                {posts.length === 0 ? (
                    <div className="text-center py-16 bg-white dark:bg-slate-800 rounded-3xl border border-dashed border-slate-300 dark:border-slate-600">
                        <div className="h-16 w-16 bg-slate-50 dark:bg-slate-700 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-4">
                            <Hash className="w-8 h-8" />
                        </div>
                        <p className="text-lg text-slate-500 dark:text-slate-400 font-medium tracking-tight">There are no articles in this category yet.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                        {posts.map((post) => (
                            <PostCard key={post.id} post={post} />
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}
