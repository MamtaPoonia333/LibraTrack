const navItems = [
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'books', label: 'Books' },
  { id: 'users', label: 'Users' },
  { id: 'issue-return', label: 'Issue / Return' },
]

const Layout = ({ activePage, onPageChange, onLogout, children }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50 text-slate-900 relative overflow-hidden">
      {/* Cute floating bubbles background */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 left-20 w-32 h-32 bg-pink-300/20 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute top-40 right-32 w-40 h-40 bg-purple-300/20 rounded-full blur-3xl animate-pulse" style={{animationDelay: '1s'}}></div>
        <div className="absolute bottom-20 left-40 w-36 h-36 bg-blue-300/20 rounded-full blur-3xl animate-pulse" style={{animationDelay: '2s'}}></div>
      </div>
      
      <div className="mx-auto flex min-h-screen max-w-7xl relative z-10">
        <aside className="w-64 border-r border-white/50 backdrop-blur-xl bg-white/40 p-5 shadow-2xl">
          <h1 className="mb-8 text-xl font-bold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent flex items-center gap-2">
            <span className="text-2xl filter drop-shadow-md">🎀</span>
            Library System
          </h1>
          <nav className="space-y-3">
            {navItems.map((item) => (
              <button
                key={item.id}
                onClick={() => onPageChange(item.id)}
                className={`w-full rounded-2xl px-4 py-3 text-left text-sm font-semibold transition-all duration-300 ${
                  activePage === item.id
                    ? 'bg-gradient-to-r from-pink-400 to-purple-400 text-white shadow-lg scale-105'
                    : 'text-gray-700 hover:bg-white/40 hover:backdrop-blur-sm hover:scale-102'
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>
          <button
            onClick={onLogout}
            className="mt-8 w-full rounded-xl border border-slate-300 bg-white/60 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-white"
          >
            Sign out
          </button>
        </aside>

        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  )
}

export default Layout
