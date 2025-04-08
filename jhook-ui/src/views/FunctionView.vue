<template>
  <div class="jhook-func-view">
    <div class="jhook-func-view-left">
      <div>类列表</div>
      <div>
        <el-input
          v-model="filterClass"
          style="width: 240px"
          placeholder="filter class"
        />
        <el-tree
          ref="treeRef"
          lazy
          :data="classData"
          :props="classProps"
          :filter-node-method="filterNode"
          :load="loadNode"
          @node-click="handleNodeClick"
        />
      </div>
    </div>
    <div class="jhook-func-view-right">
      <pre>
        {{ decompileCode }}
      </pre>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue'
import api from '@/api/api'
import type Node from 'element-plus/es/components/tree/src/model/node'

interface Clazz {
  name: string
  methods?: Clazz[]
}

const filterClass = ref('')
const decompileCode = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(filterClass, (val) => {
  treeRef.value!.filter(val)
})

const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name.includes(value)
}

const handleNodeClick = async (data: Clazz, node: Node) => {
  if (node.level === 1) {
    let name = data.name
    if (name.indexOf('[') > 0) { // TODO
      name = name.substring(0, name.indexOf('['))
    }
    let source = await api.decompileClass(name)
    console.log(source.data)
    decompileCode.value = source.data
  }
}

const classData: Clazz[] = reactive([])

const init = async () => {
  const result = await api.listClass()
  let classList = result.data
  classList.sort()
  for (let clazz of classList) {
    classData.push({
      name: clazz
    })
  }
  setTimeout(() => {
    filterClass.value = 'jhook'
  }, 2000)
}

const loadNode = (node: Node, resolve: (data: Tree[]) => void) => {
  let name = node.data.name
  if (node.level === 1) {
    if (!name) {
      return resolve([])
    }
    if (name.indexOf('[') > 0) { // TODO
      name = name.substring(0, name.indexOf('['))
    }
    api.listMethod(name)
      .then(res => {
        let nodes = res.data.map((name: string) => {
          return { name, leaf: true }
        })
        resolve(nodes)
      })
      return
  }
  return resolve([])
}

const classProps = {
  children: 'methods',
  label: 'name',
}

init()

</script>

<style scoped>
.jhook-func-view {
  display: flex;
}

.hook-func-view-left {
  display: flex;
  flex-direction: column;
}
</style>
