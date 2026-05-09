import React from 'react';
import { Link } from 'react-router-dom';
import { Clock, User, ArrowUpRight, Edit3 } from 'lucide-react';

export default function PostCard({ post }) {
    const { id, title, description, createdAt, updatedAt, blogger, category } = post;

    // Use updatedAt if it exists and is different from createdAt, otherwise use createdAt
    const displayDate = updatedAt && updatedAt !== createdAt ? updatedAt : createdAt;
    const isUpdated = updatedAt && updatedAt !== createdAt;
    
    const formattedDate = displayDate
        ? new Date(displayDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
        : 'Recently';
    
    const datePrefix = isUpdated ? 'Updated' : 'Posted';
    
    const authorName = blogger ? `${blogger.firstName} ${blogger.lastName}`.trim() || blogger.userName : 'Anonymous';

    // Generate random gradient for cards
    const gradients = [
        'from-indigo-500 to-purple-600',
        'from-violet-500 to-fuchsia-600',
        'from-blue-500 to-cyan-600',
        'from-rose-500 to-pink-600',
        'from-emerald-500 to-teal-600',
        'from-amber-500 to-orange-600'
    ];
    const gradient = gradients[id % gradients.length];

    return (
        <article className="group relative bg-white dark:bg-slate-800 rounded-2xl shadow-md hover:shadow-2xl transition-all duration-500 border border-slate-200 dark:border-slate-700 overflow-hidden h-full flex flex-col hover:-translate-y-2">
            {/* Gradient accent bar */}
            <div className={`h-1.5 bg-gradient-to-r ${gradient}`}></div>
            
            {/* Card content */}
            <div className="p-6 flex flex-col flex-grow relative">
                {/* Category badge */}
                <div className="flex items-center justify-between mb-4">
                    <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 bg-gradient-to-r ${gradient} text-white text-xs font-bold rounded-lg shadow-sm`}>
                        {category?.categoryName || 'General'}
                    </span>
                    {isUpdated && (
                        <span className="inline-flex items-center gap-1 text-xs text-amber-600 dark:text-amber-400 font-semibold">
                            <Edit3 className="w-3 h-3" />
                            Updated
                        </span>
                    )}
                </div>

                {/* Post title and description */}
                <Link to={`/post/${id}`} className="flex-grow group/link">
                    <h3 className="text-xl font-extrabold text-slate-900 dark:text-slate-100 mb-3 line-clamp-2 leading-tight group-hover:text-transparent group-hover:bg-clip-text group-hover:bg-gradient-to-r group-hover:from-indigo-600 group-hover:to-purple-600 dark:group-hover:from-indigo-400 dark:group-hover:to-purple-400 transition-all duration-300">
                        {title}
                    </h3>
                    <p className="text-sm text-slate-600 dark:text-slate-400 line-clamp-3 leading-relaxed mb-4">
                        {description}
                    </p>
                </Link>

                {/* Footer */}
                <div className="mt-auto pt-4 border-t border-slate-100 dark:border-slate-700/50">
                    <div className="flex items-center justify-between">
                        {/* Author info */}
                        <Link to={`/user/${blogger?.id}`} className="flex items-center gap-3 group/author">
                            <div className={`relative w-10 h-10 rounded-full bg-gradient-to-br ${gradient} p-0.5 transition-transform group-hover/author:scale-110`}>
                                <div className="w-full h-full bg-white dark:bg-slate-800 rounded-full flex items-center justify-center">
                                    <User className="w-5 h-5 text-slate-700 dark:text-slate-300" />
                                </div>
                            </div>
                            <div className="flex flex-col">
                                <span className="text-sm font-bold text-slate-900 dark:text-slate-100 group-hover/author:text-indigo-600 dark:group-hover/author:text-indigo-400 transition-colors">
                                    {authorName}
                                </span>
                                <div className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
                                    <Clock className="w-3 h-3" />
                                    <span>{datePrefix} {formattedDate}</span>
                                </div>
                            </div>
                        </Link>

                        {/* Read more arrow */}
                        <Link 
                            to={`/post/${id}`}
                            className="flex items-center justify-center w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 group-hover:bg-indigo-600 group-hover:text-white dark:group-hover:bg-indigo-600 transition-all duration-300 group-hover:rotate-45"
                        >
                            <ArrowUpRight className="w-5 h-5" />
                        </Link>
                    </div>
                </div>
            </div>

            {/* Hover effect overlay */}
            <div className={`absolute inset-0 bg-gradient-to-br ${gradient} opacity-0 group-hover:opacity-5 transition-opacity duration-500 pointer-events-none`}></div>
        </article>
    );
}
