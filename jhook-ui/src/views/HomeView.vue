<template>
  <div class="jhook-home-view">
    <el-card style="max-width: 480px">
      <template #header>
        <div class="card-header">
          <span>操作</span>
        </div>
      </template>
      <div>
        <el-radio-group v-model="type">
          <el-radio value="attach">attch Java 进程，启动 hook 服务并连接</el-radio>
          <el-radio value="connect">连接到 hook 服务</el-radio>
        </el-radio-group>
      </div>
      <div v-if="type === 'attach'">
        Java 进程列表：
        <el-button type="primary" @click="refreshJavaProcess">Refresh</el-button>
        <el-table
          highlight-current-row
          :data="processData"
          border
          style="width: 100%"
          @current-change="handleCurrentChange"
        >
          <el-table-column prop="id" label="进程ID" width="100" />
          <el-table-column prop="displayName" label="名称" />
        </el-table>
        <el-button type="primary" @click="attach">Attach</el-button>
      </div>
      <div v-else>
        <el-form :model="server" label-width="auto">
          <el-form-item label="IP" prop="ip">
            <el-input v-model="server.ip" type="text" autocomplete="off" />
          </el-form-item>
          <el-form-item label="Port" prop="port">
            <el-input v-model="server.port" type="text" autocomplete="off" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="connect">Attach</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/api'

interface JavaProcess {
  pid: string
  displayName: string
}

const router = useRouter()

const type = ref('attach')
const processData = ref([])
const processId = ref()
const server = reactive({
  ip: '127.0.0.1',
  port: '7788',
})

const refreshJavaProcess = async () => {
  let resp = await api.listJvm()
  processData.value = resp.data
}

const handleCurrentChange = (val: JavaProcess | undefined) => {
  processId.value = val?.pid
}

const attach = async () => {
  let resp = await api.attach(processId.value)
  if (resp.success) {
    router.push({
      name: 'function'
    })
  }
}

const connect = async () => {
  let resp = await api.connect(server)
  if (resp.success) {
    router.push({
      name: 'function'
    })
  }
}

refreshJavaProcess()

</script>

<style scoped>
.jhook-home-view {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
