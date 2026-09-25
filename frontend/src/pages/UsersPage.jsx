import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import api, { getApiError } from '../api/client'
import Skeleton from '../components/Skeleton'

const initialUser = {
  name: '',
  email: '',
  phoneNumber: '',
  password: '',
}

const UsersPage = () => {
  const [users, setUsers] = useState([])
  const [form, setForm] = useState(initialUser)
  const [loading, setLoading] = useState(true)

  const loadUsers = async () => {
    setLoading(true)
    try {
      const { data } = await api.get('/users')
      setUsers(data)
    } catch (error) {
      toast.error(getApiError(error, 'Failed to fetch users'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
    const refreshTimer = window.setInterval(loadUsers, 30000)
    return () => window.clearInterval(refreshTimer)
  }, [])

  const onRegister = async (event) => {
    event.preventDefault()
    
    // Client-side email validation
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
    if (!emailRegex.test(form.email)) {
      toast.error('Please enter a valid email address')
      return
    }
    
    // Client-side phone validation
    if (form.phoneNumber && form.phoneNumber.trim()) {
      const phoneDigits = form.phoneNumber.replace(/[^0-9]/g, '')
      if (phoneDigits.length < 10 || phoneDigits.length > 15) {
        toast.error('Phone number must be between 10-15 digits')
        return
      }
    }
    
    try {
      await api.post('/users', form)
      toast.success('User registered successfully')
      setForm(initialUser)
      loadUsers()
    } catch (error) {
      toast.error(getApiError(error, 'Failed to register user'))
    }
  }

  return (
    <div className="space-y-5">
      <h2 className="text-3xl font-bold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent flex items-center gap-2">
        <span>👥</span>
        Users
      </h2>

      <form onSubmit={onRegister} className="grid grid-cols-1 gap-3 rounded-xl border border-slate-200 bg-white p-4 sm:grid-cols-4">
        <input
          placeholder="Name"
          value={form.name}
          onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
          required
          className="rounded-lg border border-slate-300 px-3 py-2"
        />
        <input
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
          required
          className="rounded-lg border border-slate-300 px-3 py-2"
        />
        <input
          type="tel"
          placeholder="Phone Number (optional)"
          value={form.phoneNumber}
          onChange={(e) => setForm((prev) => ({ ...prev, phoneNumber: e.target.value }))}
          className="rounded-lg border border-slate-300 px-3 py-2"
        />
        <input
          type="password"
          placeholder="Password (min 8 characters)"
          value={form.password}
          onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
          minLength={8}
          required
          className="rounded-lg border border-slate-300 px-3 py-2"
        />
        <button className="rounded-lg bg-slate-900 px-4 py-2 text-white">Register User</button>
      </form>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-100 text-left">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Phone</th>
              <th className="px-4 py-3">Borrowed Books</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array.from({ length: 4 }).map((_, index) => (
                <tr key={index} className="border-t border-slate-100">
                  <td colSpan={4} className="px-4 py-3">
                    <Skeleton className="h-7 w-full" />
                  </td>
                </tr>
              ))
            ) : users.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-slate-500">
                  No users registered
                </td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.id} className="border-t border-slate-100">
                  <td className="px-4 py-3">{user.name}</td>
                  <td className="px-4 py-3">{user.email}</td>
                  <td className="px-4 py-3">{user.phoneNumber || '-'}</td>
                  <td className="px-4 py-3">{user.borrowedBooks?.length || 0}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default UsersPage
