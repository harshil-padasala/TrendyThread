import React, { useEffect, useState } from 'react';
import { postService } from '../services/postService';
import PostCard from '../components/PostCard';
import { Loader2 } from 'lucide-react';

export default function Home() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const data = await postService.fetchLatest(6);
                setPosts(data.content || data || []);
                setLoading(false);
            } catch (err) {
                setError('Failed to load posts. Please try again later.');
                setLoading(false);
            }
        };
        fetchPosts();
    }, []);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-4 rounded-lg text-center font-medium">
                {error}
            </div>
        );
    }

    return (
        <div className="space-y-8">
            <div className="text-center py-12 md:py-20 px-4 sm:px-6 lg:px-8 bg-gradient-to-b from-indigo-50 to-white dark:from-indigo-950/50 dark:to-slate-900 rounded-3xl mb-12">
                <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white sm:text-5xl md:text-6xl mb-6">
                    <span className="block text-indigo-600 dark:text-indigo-400">Discover</span>
                    <span className="block mt-2">Trendy Ideas &amp; Threads</span>
                </h1>
                <p className="mt-3 max-w-md mx-auto text-base text-slate-500 dark:text-slate-400 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
                    Dive into our collection of amazing articles, insights, and stories from our creative community.
                </p>
            </div>

            <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-700 pb-4 mb-8">
                <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
                    Latest Posts
                    <span className="bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 text-xs py-1 px-2.5 rounded-full font-semibold">
                        {posts.length}
                    </span>
                </h2>
            </div>

            {posts.length === 0 ? (
                <div className="text-center py-12 text-slate-500 dark:text-slate-400 bg-slate-50 dark:bg-slate-800 rounded-2xl border border-dashed border-slate-300 dark:border-slate-700">
                    No posts available right now. Check back later!
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {posts.map((post) => (
                        <PostCard key={post.id} post={post} />
                    ))}
                </div>
            )}
        </div>
    );
}
