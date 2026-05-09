import React, { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import { userService } from '../services/userService';
import { postService } from '../services/postService';
import PostCard from '../components/PostCard';
import { Loader2, User as UserIcon, Mail, Info, ArrowLeft, BookOpen } from 'lucide-react';

export default function UserProfile() {
    const { id } = useParams();
    const location = useLocation();
    const isOwnProfile = location.pathname === '/profile';
    const [user, setUser] = useState(null);
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchUserAndPosts = async () => {
            try {
                let userData;
                if (isOwnProfile) {
                    // Fetch current authenticated user's profile
                    userData = await userService.fetchCurrentUser();
                    
                    // Fetch own posts using /posts/blogger endpoint
                    try {
                        const userPosts = await postService.fetchMyPosts();
                        setPosts(userPosts.content || userPosts || []);
                    } catch (postErr) {
                        console.error("Failed to fetch user posts", postErr);
                        setPosts([]);
                    }
                } else {
                    // Fetch specific user by ID
                    userData = await userService.fetchById(id);
                    
                    // Fetch posts by user ID using /posts/user/{userId} endpoint
                    try {
                        const userPosts = await postService.fetchByUser(id);
                        setPosts(userPosts.content || userPosts || []);
                    } catch (postErr) {
                        console.error("Failed to fetch user posts", postErr);
                        setPosts([]);
                    }
                }
                setUser(userData);
                setLoading(false);
            } catch (err) {
                console.error("Profile fetch error:", err);
                setError('Failed to load user profile. Please try again later.');
                setLoading(false);
            }
        };
        fetchUserAndPosts();
    }, [id, isOwnProfile]);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[60vh]">
                <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
            </div>
        );
    }

    if (error || !user) {
        return (
            <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-6 rounded-2xl text-center font-medium max-w-2xl mx-auto shadow-sm border border-red-100 dark:border-red-800">
                <p className="text-xl mb-4">{error || 'User not found'}</p>
                <Link to="/" className="inline-flex items-center text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 transition-colors">
                    <ArrowLeft className="w-4 h-4 mr-2" /> Back to Home
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-5xl mx-auto space-y-12">
            {/* Profile Header */}
            <div className="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden relative">
                <div className="h-32 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 w-full absolute top-0 left-0"></div>
                <div className="pt-24 px-8 pb-8 md:px-12 flex flex-col md:flex-row items-center md:items-end gap-6 relative z-10">
                    <div className="h-32 w-32 bg-white dark:bg-slate-700 rounded-full p-2 shadow-lg backdrop-blur-sm border border-slate-200 dark:border-slate-600">
                        <div className="h-full w-full bg-indigo-50 dark:bg-indigo-900/50 rounded-full flex items-center justify-center">
                            <UserIcon className="h-12 w-12 text-indigo-600 dark:text-indigo-400" />
                        </div>
                    </div>
                    <div className="text-center md:text-left flex-grow">
                        <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight mb-2">
                            {user.userName}
                        </h1>
                        <div className="flex flex-col md:flex-row gap-4 text-slate-600 dark:text-slate-400 pb-2">
                            <span className="flex items-center justify-center md:justify-start gap-2 bg-slate-50 dark:bg-slate-700 px-4 py-2 rounded-full text-sm font-medium border border-slate-200 dark:border-slate-600">
                                <Mail className="w-4 h-4 text-indigo-400" />
                                {user.email}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            {/* About Section */}
            {user.about && (
                <section className="bg-indigo-50/50 dark:bg-indigo-950/30 rounded-3xl p-8 border border-indigo-100/50 dark:border-indigo-900/50">
                    <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                        <Info className="w-5 h-5 text-indigo-500" />
                        About
                    </h2>
                    <p className="text-slate-700 dark:text-slate-300 leading-relaxed max-w-3xl text-lg">
                        {user.about}
                    </p>
                </section>
            )}

            {/* User's Posts */}
            <section>
                <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-700 pb-4 mb-8">
                    <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-3">
                        Articles by {user.userName.split(' ')[0]}
                        <span className="bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 text-sm py-1 px-3 rounded-full font-semibold">
                            {posts.length}
                        </span>
                    </h2>
                </div>

                {posts.length === 0 ? (
                    <div className="text-center py-16 bg-white dark:bg-slate-800 rounded-3xl border border-dashed border-slate-300 dark:border-slate-600">
                        <div className="h-16 w-16 bg-slate-50 dark:bg-slate-700 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-4">
                            <BookOpen className="w-8 h-8" />
                        </div>
                        <p className="text-lg text-slate-500 dark:text-slate-400 font-medium tracking-tight">This user hasn't published any articles yet.</p>
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
