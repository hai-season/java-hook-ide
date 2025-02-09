import axios from 'axios'

const instance = axios.create({
  baseURL: 'http://127.0.0.1:8080/',
  timeout: 1000,
  headers: {}
})

const http = async (url: string, data: any = {}) => {
  let resp = await instance.post(url, data)
  return resp.data
}

const listJvm = async () => {
  return await http('/listJvm')
}

const attach = async (pid: string) => {
  return await http(`/attach/${pid}`)
}

export default {
  listJvm,
  attach
}
