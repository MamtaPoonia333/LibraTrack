import { useEffect, useMemo, useState } from 'react'
import toast from 'react-hot-toast'
import api, { getApiError } from '../api/client'
import Skeleton from '../components/Skeleton'

const isDueWithin3Days = (dueDate) => {
  if (!dueDate) return false
  const now = new Date()
  const due = new Date(dueDate)
  const diff = due.getTime() - now.getTime()
  const days = diff / (1000 * 60 * 60 * 24)
  return days >= 0 && days <= 3
}

const DashboardPage = () => {
  const [books, setBooks] = useState([])
  const [users, setUsers] = useState([])
  const [issuedBooks, setIssuedBooks] = useState([])
  const [loading, setLoading] = useState(true)

  const loadData = async () => {
    setLoading(true)
    try {
      const [booksRes, usersRes, issuedRes] = await Promise.all([
        api.get('/books'),
        api.get('/users'),
        api.get('/books/issued'),
      ])
      setBooks(booksRes.data)
      setUsers(usersRes.data)
      setIssuedBooks(issuedRes.data)
    } catch (error) {
      toast.error(getApiError(error, 'Failed to fetch dashboard data'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
    const refreshTimer = window.setInterval(loadData, 30000)
    return () => window.clearInterval(refreshTimer)
  }, [])

  const stats = useMemo(() => {
    const totalBooks = books.length
    const availableBooks = books.filter((book) => book.available).length
    const issuedCount = books.filter((book) => !book.available).length
    const totalUsers = users.length
    const overdueCount = issuedBooks.filter((item) => item.isOverdue).length
    return { totalBooks, availableBooks, issuedCount, totalUsers, overdueCount }
  }, [books, users, issuedBooks])

  const dueSoon = useMemo(
    () => issuedBooks.filter((item) => isDueWithin3Days(item.dueDate)),
    [issuedBooks],
  )

  const cards = [
    { label: 'Total Books', value: stats.totalBooks, gradient: 'from-blue-400 to-indigo-500', icon: '📚' },
    { label: 'Available Books', value: stats.availableBooks, gradient: 'from-green-400 to-emerald-500', icon: '✨' },
    { label: 'Issued Books', value: stats.issuedCount, gradient: 'from-purple-400 to-pink-500', icon: '📖' },
    { label: 'Total Users', value: stats.totalUsers, gradient: 'from-orange-400 to-amber-500', icon: '💝' },
    { label: 'Overdue Books', value: stats.overdueCount, gradient: stats.overdueCount > 0 ? 'from-red-400 to-rose-500' : 'from-gray-300 to-gray-400', icon: '⏰', highlight: stats.overdueCount > 0 },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <h2 className="text-3xl font-bold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent">Dashboard</h2>
        <span className="text-2xl filter drop-shadow-md">✨</span>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5">
        {loading
          ? Array.from({ length: 5 }).map((_, index) => (
              <div key={index} className="rounded-3xl backdrop-blur-lg bg-white/70 border-2 border-white/50 p-5 shadow-xl">
                <Skeleton className="mb-3 h-5 w-24" />
                <Skeleton className="h-8 w-14" />
              </div>
            ))
          : cards.map((card) => (
              <div
                key={card.label}
                className={`rounded-3xl backdrop-blur-lg bg-white/60 border-2 border-white/50 p-6 shadow-xl hover:shadow-2xl transition-all duration-300 hover:scale-105 hover:-translate-y-1 ${
                  card.highlight ? 'ring-4 ring-pink-300 animate-pulse' : ''
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm font-semibold text-gray-700">{card.label}</p>
                  <span className="text-3xl filter drop-shadow-md">{card.icon}</span>
                </div>
                <p className={`text-4xl font-bold bg-gradient-to-br ${card.gradient} bg-clip-text text-transparent`}>{card.value}</p>
              </div>
            ))}
      </div>

      <div className="rounded-3xl border-2 border-white/50 backdrop-blur-lg bg-white/60 p-6 shadow-xl">
        <h3 className="mb-4 text-lg font-bold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent flex items-center gap-2">
          <span>⏰</span>
          Due Within 3 Days
        </h3>

        {loading ? (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, index) => (
              <Skeleton key={index} className="h-10 w-full rounded-2xl" />
            ))}
          </div>
        ) : dueSoon.length === 0 ? (
          <p className="text-sm text-gray-500 flex items-center gap-2">
            <span>🎉</span>
            No books due in the next 3 days.
          </p>
        ) : (
          <div className="space-y-3">
            {dueSoon.map((item) => {
              const isOverdue = item.isOverdue
              return (
                <div
                  key={`${item.userId}-${item.bookId}`}
                  className={`rounded-2xl border-2 backdrop-blur-sm px-4 py-3 text-sm shadow-md transition-all duration-300 hover:scale-102 ${
                    isOverdue
                      ? 'border-red-300 bg-gradient-to-r from-red-200/60 to-pink-200/60 text-red-900'
                      : 'border-amber-300 bg-gradient-to-r from-amber-200/60 to-yellow-200/60 text-amber-900'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-semibold">{item.bookTitle} — {item.userName}</span>
                    {isOverdue ? (
                      <span className="font-bold text-red-700 flex items-center gap-1">
                        <span>⚠️</span>
                        OVERDUE ({item.daysOverdue} days)
                      </span>
                    ) : (
                      <span className="flex items-center gap-1">
                        <span>📅</span>
                        Due: {item.dueDate}
                      </span>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}

export default DashboardPage
