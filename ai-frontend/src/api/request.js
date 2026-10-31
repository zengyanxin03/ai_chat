import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:5173/api',
  timeout: 0,
})

export default request
