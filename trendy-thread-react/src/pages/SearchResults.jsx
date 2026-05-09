import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { postService } from '../services/postService';
import PostCard from '../components/PostCard';
import { Loader2, Search, ArrowLeft } from 'lucide-react';

export default function SearchResults() {
    const [searchParams] = useSearchParams();
    const query = searchParams.get('q') || '';
    
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchSearchResults = async () => {
            if (!query.trim()) {
                setLoading(false);
                return;
            }

            setLoading(true);
            setError(null);
            
            try {
                const data = await postService.searchPosts(query);
                setPosts(data.content || data || []);
            } catch (err) {
                console.error('Search failed:', err);
                setError('Failed to search posts. Please try again.');
            } finally {
                setLoading(false);
            }
        };

        fetchSearchResults();
    }, [query]);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[60vh]">
                <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto py-8">
            <Link to="/" className="inline-flex items-center text-slate-500 dark:text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 mb-8 transition-colors group">
                <ArrowLeft className="w-4 h-4 mr-2 group-hover:-translate-x-1 transition-transform" />
                Back to Home
            </Link>

            <div className="mb-8">
                <div className="flex items-center gap-3 mb-4">
                    <div className="p-3 bg-indigo-100 dark:bg-indigo-900/30 rounded-xl">
                        <Search className="w-6 h-6 text-indigo-600 dark:text-indigo-400" />
                    </div>
                    <div>
                        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                            Search Results
                        </h1>
                        <p className="text-slate-500 dark:text-slate-400">
                            {query ? (
                                <>
                                    Found <span className="font-bold text-indigo-600 dark:text-indigo-400">{posts.length}</span> {posts.length === 1 ? 'post' : 'posts'} for "<span className="font-semibold">{query}</span>"
                                </>
                            ) : (
                                'Enter a search term to find posts'
                            )}
                        </p>
                    </div>
                </div>
            </div>

            {error && (
                <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-6 rounded-2xl text-center font-medium border border-red-100 dark:border-red-800 mb-8">
                    {error}
                </div>
            )}

            {!query.trim() ? (
                <div className="bg-slate-100 dark:bg-slate-800 p-12 rounded-3xl text-center border border-slate-200 dark:border-slate-700">
                    <Search className="w-16 h-16 text-slate-400 mx-auto mb-4" />
                    <h2 className="text-2xl font-bold text-slate-700 dark:text-slate-300 mb-2">
                        Start Searching
                    </h2>
                    <p className="text-slate-500 dark:text-slate-400">
                        Use the search bar above to find posts by title or keywords
                    </p>
                </div>
            ) : posts.length === 0 ? (
                <div className="bg-slate-100 dark:bg-slate-800 p-12 rounded-3xl text-center border border-slate-200 dark:border-slate-700">
                    <Search className="w-16 h-16 text-slate-400 mx-auto mb-4" />
                    <h2 className="text-2xl font-bold text-slate-700 dark:text-slate-300 mb-2">
                        No Results Found
                    </h2>
                    <p className="text-slate-500 dark:text-slate-400 mb-4">
                        We couldn't find any posts matching "<span className="font-semibold">{query}</span>"
                    </p>
                    <Link 
                        to="/"
                        className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl transition-colors"
                    >
                        Browse All Posts
                    </Link>
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
