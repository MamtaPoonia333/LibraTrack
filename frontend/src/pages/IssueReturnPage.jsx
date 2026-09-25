import { useEffect, useMemo, useState } from 'react'
import toast from 'react-hot-toast'
import api, { getApiError } from '../api/client'
import Skeleton from '../components/Skeleton'

const IssueReturnPage = () => {
  const [books, setBooks] = useState([])
  const [users, setUsers] = useState([])
  const [issuedBooks, setIssuedBooks] = useState([])
  const [selectedBookId, setSelectedBookId] = useState('')
  const [selectedUserId, setSelectedUserId] = useState('')
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
      toast.error(getApiError(error, 'Failed to fetch issue/return data'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
    const refreshTimer = window.setInterval(loadData, 30000)
    return () => window.clearInterval(refreshTimer)
  }, [])

  const availableBooks = useMemo(() => books.filter((book) => book.available), [books])

  const onIssue = async () => {
    if (!selectedBookId || !selectedUserId) {
      toast.error('Please select both user and book')
      return
    }

    const user = users.find(u => u.id === selectedUserId)
    if (user && user.borrowedBooks && user.borrowedBooks.length >= 3) {
      toast.error('User has reached maximum book limit (3 books)')
      return
    }

    try {
      await api.post(`/users/${selectedUserId}/issue/${selectedBookId}`)
      toast.success('Book issued successfully')
      setSelectedBookId('')
      loadData()
    } catch (error) {
      toast.error(getApiError(error, 'Failed to issue book'))
    }
  }

  const onReturn = async (userId, bookId) => {
    try {
      await api.post(`/users/${userId}/return/${bookId}`)
      toast.success('Book returned successfully')
      loadData()
    } catch (error) {
      toast.error(getApiError(error, 'Failed to return book'))
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h2 className="text-3xl font-bold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent flex items-center gap-2">
          <span>📚</span>
          Issue / Return Books
        </h2>
        <p className="text-sm text-gray-600 mt-2 flex items-center gap-2">
          <span>✨</span>
          Early returns are always welcome! You can return books anytime before the due date.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-3 rounded-3xl border-2 border-white/50 backdrop-blur-lg bg-white/60 p-6 shadow-xl sm:grid-cols-3">
        <select
          value={selectedBookId}
          onChange={(e) => setSelectedBookId(e.target.value)}
          className="rounded-2xl border-2 border-purple-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-purple-300 focus:border-purple-400 focus:ring-2 focus:bg-white/90 shadow-md transition-all duration-300"
        >
          <option value="">📖 Select Available Book</option>
          {availableBooks.map((book) => (
            <option key={book.id} value={book.id}>
              {book.title}
            </option>
          ))}
        </select>

        <select
          value={selectedUserId}
          onChange={(e) => setSelectedUserId(e.target.value)}
          className="rounded-2xl border-2 border-pink-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-pink-300 focus:border-pink-400 focus:ring-2 focus:bg-white/90 shadow-md transition-all duration-300"
        >
          <option value="">👤 Select User</option>
          {users.map((user) => {
            const bookCount = user.borrowedBooks?.length || 0
            const isAtLimit = bookCount >= 3
            return (
              <option key={user.id} value={user.id} disabled={isAtLimit}>
                {user.name} ({bookCount}/3 books){isAtLimit ? ' - LIMIT REACHED' : ''}
              </option>
            )
          })}
        </select>

        <button onClick={onIssue} className="rounded-full bg-gradient-to-r from-pink-400 to-purple-400 px-6 py-3 text-sm font-bold text-white hover:scale-105 shadow-lg transition-all duration-300">
          ✨ Issue
        </button>
      </div>

      <div className="overflow-hidden rounded-3xl border-2 border-white/50 backdrop-blur-lg bg-white/60 shadow-xl">
        <table className="min-w-full text-sm">
          <thead className="bg-gradient-to-r from-pink-400 to-purple-400 text-left text-white">
            <tr>
              <th className="px-4 py-3 font-bold">User</th>
              <th className="px-4 py-3 font-bold">Book</th>
              <th className="px-4 py-3 font-bold">Due Date</th>
              <th className="px-4 py-3 font-bold">Status</th>
              <th className="px-4 py-3 font-bold">Action</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array.from({ length: 4 }).map((_, index) => (
                <tr key={index} className="border-t border-purple-100">
                  <td colSpan={5} className="px-4 py-3">
                    <Skeleton className="h-7 w-full" />
                  </td>
                </tr>
              ))
            ) : issuedBooks.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                  🎈 No issued books
                </td>
              </tr>
            ) : (
              issuedBooks.map((item) => {
                const isOverdue = item.isOverdue
                const daysRemaining = item.daysRemaining || 0
                
                return (
                  <tr key={`${item.userId}-${item.bookId}`} className={`border-t border-purple-100 transition-all duration-300 ${
                    isOverdue ? 'bg-pink-50/50 backdrop-blur-sm' : 'hover:bg-white/50'
                  }`}>
                    <td className="px-4 py-3">{item.userName}</td>
                    <td className="px-4 py-3">{item.bookTitle}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-col gap-1">
                        <span className={isOverdue ? 'font-bold text-pink-600' : 'text-gray-700'}>
                          {item.dueDate || '-'}
                        </span>
                        {!isOverdue && daysRemaining > 0 && (
                          <span className="text-xs text-gray-500">
                            {daysRemaining} {daysRemaining === 1 ? 'day' : 'days'} left
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      {isOverdue ? (
                        <span className="rounded-full bg-gradient-to-r from-red-300 to-pink-300 backdrop-blur-sm px-3 py-1.5 text-xs font-bold text-red-800 shadow-md">
                          ⏰ OVERDUE ({item.daysOverdue}d)
                        </span>
                      ) : (
                        <span className="rounded-full bg-gradient-to-r from-green-300 to-emerald-300 backdrop-blur-sm px-3 py-1.5 text-xs font-bold text-green-800 shadow-md">
                          ✨ Can Return Anytime
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <button
                        onClick={() => onReturn(item.userId, item.bookId)}
                        className="rounded-full bg-gradient-to-r from-emerald-400 to-teal-400 px-4 py-2 text-xs font-bold text-white hover:scale-110 shadow-md transition-all duration-300 flex items-center gap-1"
                        title="Return this book now"
                      >
                        🔙 Return Now
                      </button>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default IssueReturnPage
