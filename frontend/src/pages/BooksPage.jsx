import { useEffect, useMemo, useState } from 'react'
import toast from 'react-hot-toast'
import api, { getApiError } from '../api/client'
import Skeleton from '../components/Skeleton'

const initialForm = {
  id: '',
  title: '',
  author: '',
  genre: '',
  isbn: '',
  type: 'Book',
  fileFormat: 'PDF',
  description: '',
  publisher: '',
  publicationYear: '',
}

const BooksPage = () => {
  const [books, setBooks] = useState([])
  const [search, setSearch] = useState('')
  const [genreFilter, setGenreFilter] = useState('all')
  const [availabilityFilter, setAvailabilityFilter] = useState('all')
  const [loading, setLoading] = useState(true)
  const [isModalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(initialForm)
  const [ebookFile, setEbookFile] = useState(null)

  const loadBooks = async () => {
    setLoading(true)
    try {
      const { data } = await api.get('/books')
      setBooks(data)
    } catch (error) {
      toast.error(getApiError(error, 'Failed to fetch books'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadBooks()
    const refreshTimer = window.setInterval(loadBooks, 30000)
    return () => window.clearInterval(refreshTimer)
  }, [])

  const filteredBooks = useMemo(() => {
    let filtered = books

    // Apply search filter
    const q = search.trim().toLowerCase()
    if (q) {
      filtered = filtered.filter(
        (book) =>
          book.title.toLowerCase().includes(q) ||
          book.author.toLowerCase().includes(q) ||
          book.genre.toLowerCase().includes(q),
      )
    }

    // Apply genre filter
    if (genreFilter !== 'all') {
      filtered = filtered.filter((book) => book.genre === genreFilter)
    }

    // Apply availability filter
    if (availabilityFilter !== 'all') {
      filtered = filtered.filter((book) => 
        availabilityFilter === 'available' ? book.available : !book.available
      )
    }

    return filtered
  }, [books, search, genreFilter, availabilityFilter])

  const uniqueGenres = useMemo(() => {
    const genres = [...new Set(books.map(book => book.genre))].filter(Boolean)
    return genres.sort()
  }, [books])

  const onChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const onAddBook = async (event) => {
    event.preventDefault()
    
    // Client-side ISBN validation
    if (form.isbn && form.isbn.trim()) {
      const isbnDigits = form.isbn.replace(/[^0-9X]/gi, '')
      if (isbnDigits.length !== 10 && isbnDigits.length !== 13) {
        toast.error('ISBN must be 10 or 13 digits')
        return
      }
    }
    
    try {
      const { data: createdBook } = await api.post('/books', {
        ...form,
        publicationYear: form.publicationYear ? Number(form.publicationYear) : null,
      })
      if (form.type === 'EBook' && ebookFile) {
        const fileData = new FormData()
        fileData.append('file', ebookFile)
        await api.post(`/books/${createdBook.id}/file`, fileData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
      }
      toast.success('Book added successfully')
      setForm(initialForm)
      setEbookFile(null)
      setModalOpen(false)
      loadBooks()
    } catch (error) {
      toast.error(getApiError(error, 'Failed to add book'))
    }
  }

  const onDeleteBook = async (id) => {
    try {
      await api.delete(`/books/${id}`)
      toast.success('Book deleted')
      loadBooks()
    } catch (error) {
      toast.error(getApiError(error, 'Failed to delete book'))
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-3xl font-bold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent flex items-center gap-2">
          <span>📚</span>
          Books
        </h2>
        <button
          onClick={() => setModalOpen(true)}
          className="rounded-full bg-gradient-to-r from-pink-400 to-purple-400 px-6 py-3 text-sm font-bold text-white shadow-lg hover:shadow-xl hover:scale-105 transition-all duration-300"
        >
          ✨ Add Book
        </button>
      </div>

      <div className="flex flex-col gap-3">
        <input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="🔍 Search by title, author, or genre..."
          className="w-full rounded-2xl border-2 border-pink-200 bg-white/70 backdrop-blur-sm px-5 py-3.5 outline-none ring-pink-300 focus:border-pink-400 focus:ring-2 focus:bg-white/90 shadow-md transition-all duration-300"
        />

        <div className="flex flex-wrap gap-3">
          <select
            value={genreFilter}
            onChange={(e) => setGenreFilter(e.target.value)}
            className="rounded-2xl border-2 border-purple-200 bg-white/70 backdrop-blur-sm px-4 py-2.5 outline-none ring-purple-300 focus:border-purple-400 focus:ring-2 focus:bg-white/90 shadow-md transition-all duration-300"
          >
            <option value="all">📚 All Genres</option>
            {uniqueGenres.map((genre) => (
              <option key={genre} value={genre}>
                {genre}
              </option>
            ))}
          </select>

          <select
            value={availabilityFilter}
            onChange={(e) => setAvailabilityFilter(e.target.value)}
            className="rounded-2xl border-2 border-blue-200 bg-white/70 backdrop-blur-sm px-4 py-2.5 outline-none ring-blue-300 focus:border-blue-400 focus:ring-2 focus:bg-white/90 shadow-md transition-all duration-300"
          >
            <option value="all">📎 All Books</option>
            <option value="available">✨ Available Only</option>
            <option value="issued">📝 Issued Only</option>
          </select>

          {(search || genreFilter !== 'all' || availabilityFilter !== 'all') && (
            <button
              onClick={() => {
                setSearch('')
                setGenreFilter('all')
                setAvailabilityFilter('all')
              }}
              className="rounded-full border-2 border-gray-300 bg-white/70 backdrop-blur-sm px-4 py-2 text-sm text-gray-700 hover:bg-white hover:scale-105 shadow-md transition-all duration-300"
            >
              🗑️ Clear
            </button>
          )}

          <div className="ml-auto text-sm font-bold text-purple-600 py-2.5 bg-white/70 backdrop-blur-sm px-4 rounded-full shadow-md">
            💫 {filteredBooks.length} of {books.length} books
          </div>
        </div>
      </div>

      <div className="overflow-hidden rounded-3xl border-2 border-white/50 backdrop-blur-lg bg-white/60 shadow-xl">
        <table className="min-w-full text-sm">
          <thead className="bg-gradient-to-r from-pink-400 to-purple-400 text-left text-white">
            <tr>
              <th className="px-4 py-3">Title</th>
              <th className="px-4 py-3">Author</th>
              <th className="px-4 py-3">Genre</th>
              <th className="px-4 py-3">ISBN</th>
              <th className="px-4 py-3">Type</th>
              <th className="px-4 py-3">Availability</th>
              <th className="px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array.from({ length: 5 }).map((_, index) => (
                <tr key={index} className="border-t border-slate-100">
                  <td className="px-4 py-3" colSpan={7}>
                    <Skeleton className="h-7 w-full" />
                  </td>
                </tr>
              ))
            ) : filteredBooks.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-slate-500">
                  No books found
                </td>
              </tr>
            ) : (
              filteredBooks.map((book) => (
                <tr key={book.id} className="border-t border-slate-100">
                  <td className="px-4 py-3">{book.title}</td>
                  <td className="px-4 py-3">{book.author}</td>
                  <td className="px-4 py-3">{book.genre}</td>
                  <td className="px-4 py-3 text-slate-500 text-xs">{book.isbn || '-'}</td>
                  <td className="px-4 py-3">{book.type}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-3 py-1.5 text-xs font-bold shadow-md backdrop-blur-sm ${
                        book.available
                          ? 'bg-gradient-to-r from-green-300 to-emerald-300 text-green-800'
                          : 'bg-gradient-to-r from-pink-300 to-rose-300 text-pink-800'
                      }`}
                    >
                      {book.available ? '✨ Available' : '📖 Issued'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => onDeleteBook(book.id)}
                      className="rounded-full bg-gradient-to-r from-red-400 to-pink-400 px-3 py-1.5 text-xs font-bold text-white hover:scale-110 shadow-md transition-all duration-300"
                    >
                      🗑️ Delete
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 grid place-items-center backdrop-blur-sm bg-black/20 p-4">
          <div className="w-full max-w-lg rounded-3xl border-2 border-white/50 backdrop-blur-xl bg-white/90 p-6 shadow-2xl">
            <div className="mb-5 flex items-center justify-between">
              <h3 className="text-xl font-bold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent flex items-center gap-2">
                <span>✨</span>
                Add Book
              </h3>
              <button onClick={() => setModalOpen(false)} className="text-gray-500 hover:text-pink-500 transition-colors text-xl hover:scale-110">
                ✕
              </button>
            </div>

            <form onSubmit={onAddBook} className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <input name="id" value={form.id} onChange={onChange} placeholder="📌 Book ID" required className="rounded-2xl border-2 border-pink-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-pink-300 focus:border-pink-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300" />
              <input name="title" value={form.title} onChange={onChange} placeholder="📖 Title" required className="rounded-2xl border-2 border-purple-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-purple-300 focus:border-purple-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300" />
              <input name="author" value={form.author} onChange={onChange} placeholder="✍️ Author" required className="rounded-2xl border-2 border-blue-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-blue-300 focus:border-blue-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300" />
              <input name="genre" value={form.genre} onChange={onChange} placeholder="🎭 Genre" required className="rounded-2xl border-2 border-indigo-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-indigo-300 focus:border-indigo-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300" />
              <input name="isbn" value={form.isbn} onChange={onChange} placeholder="🔢 ISBN (optional, 10 or 13 digits)" className="rounded-2xl border-2 border-purple-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-purple-300 focus:border-purple-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300 sm:col-span-2" />
              <input name="publisher" value={form.publisher} onChange={onChange} placeholder="🏢 Publisher (optional)" className="rounded-2xl border-2 border-blue-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-blue-300 focus:border-blue-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300" />
              <input name="publicationYear" type="number" min="1000" max={new Date().getFullYear()} value={form.publicationYear} onChange={onChange} placeholder="📅 Publication year" className="rounded-2xl border-2 border-indigo-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-indigo-300 focus:border-indigo-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300" />
              <textarea name="description" value={form.description} onChange={onChange} placeholder="📝 Description (optional)" className="min-h-20 rounded-2xl border-2 border-pink-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-pink-300 focus:border-pink-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300 sm:col-span-2" />

              <select name="type" value={form.type} onChange={onChange} className="rounded-2xl border-2 border-pink-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-pink-300 focus:border-pink-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300">
                <option value="Book">📕 Book</option>
                <option value="EBook">💾 EBook</option>
              </select>

              {form.type === 'EBook' ? (
                <>
                  <select name="fileFormat" value={form.fileFormat} onChange={onChange} className="rounded-2xl border-2 border-purple-200 bg-white/70 backdrop-blur-sm px-4 py-3 outline-none ring-purple-300 focus:border-purple-400 focus:ring-2 focus:bg-white shadow-md transition-all duration-300">
                    <option value="PDF">📄 PDF</option>
                    <option value="EPUB">📱 EPUB</option>
                  </select>
                  <input type="file" accept={form.fileFormat === 'PDF' ? '.pdf,application/pdf' : '.epub,application/epub+zip'} onChange={(event) => setEbookFile(event.target.files?.[0] || null)} className="rounded-2xl border-2 border-purple-200 bg-white/70 px-4 py-3 text-sm shadow-md sm:col-span-2" />
                </>
              ) : (
                <div />
              )}

              <div className="sm:col-span-2 mt-3 flex justify-end gap-3">
                <button type="button" onClick={() => setModalOpen(false)} className="rounded-full border-2 border-gray-300 bg-white/70 backdrop-blur-sm px-5 py-2.5 text-sm font-semibold text-gray-700 hover:bg-white hover:scale-105 shadow-md transition-all duration-300">
                  Cancel
                </button>
                <button type="submit" className="rounded-full bg-gradient-to-r from-pink-400 to-purple-400 px-6 py-2.5 text-sm font-bold text-white hover:scale-105 shadow-lg transition-all duration-300">
                  ✨ Save
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default BooksPage
