import axios from 'axios'

const instance = axios.create({
  baseURL: 'http://127.0.0.1:8080/',
  timeout: 1000,
  headers: {}
})

instance.interceptors.response.use(response => {
  return response
}, error => {
  alert(error)
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

const listClass = async () => {
  return await http('/listClass')
}

const listMethod = async (className: string) => {
  return await http(`/listMethod/${className}`)
}

const decompileClass = async (className: string) => {
  return await http(`/decompile/${className}`)
}

export default {
  listJvm,
  listClass,
  listMethod,
  decompileClass,
  attach
}
