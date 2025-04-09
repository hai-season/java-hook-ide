<template>
  <div class="jhook-func-view">
    <div class="jhook-func-view-left">
      <div>类列表</div>
      <el-scrollbar>
        <div class="scrollbar-flex-content">
          <el-input
            v-model="filterClass"
            style="width: 200px"
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

      </el-scrollbar>
    </div>
    <div class="jhook-func-view-right">
      <!-- <el-scrollbar> -->
        <div class="jhook-func-view-right-area">
          <div>
            反编译：
            <div id="depcompile-code"></div>
          </div>
          <div class="redefine">
            插入点:
            <div class="redefine-point">
              <div>方法开始前：</div>
              <div class="redefine-editor" id="redefine-point-before-content"></div>
              <el-button type="primary" plain @click="applyRedefine('before')">应用</el-button>
            </div>
            <div class="redefine-point">
              <div>方法结束后：</div>
              <div class="redefine-editor" id="redefine-point-after-content"></div>
              <el-button type="primary" plain @click="applyRedefine('after')">应用</el-button>
            </div>
            <div class="redefine-point">
              <div>出现异常：</div>
              <div class="redefine-editor" id="redefine-point-exception-content"></div>
              <el-button type="primary" plain @click="applyRedefine('exception')">应用</el-button>
            </div>
            <div class="redefine-point">
              <div>整体替换：</div>
              <div class="redefine-editor" id="redefine-point-total-content"></div>
              <el-button type="primary" plain @click="applyRedefine('total')">应用</el-button>
            </div>
            <div class="redefine-point">
              <div>指定行：<el-input v-model="redefineData.line" /></div>
              <div class="redefine-editor" id="redefine-point-line-content"></div>
              <el-button type="primary" plain @click="applyRedefine('line')">应用</el-button>
            </div>
          </div>
        </div>
      <!-- </el-scrollbar> -->
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue'
import api from '@/api/api'
import type Node from 'element-plus/es/components/tree/src/model/node'
import * as monaco from 'monaco-editor'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'
import { ElMessage } from 'element-plus'

self.MonacoEnvironment = {
  getWorker(_, label) {
    if (label === 'json') {
      return new jsonWorker()
    }
    if (label === 'css' || label === 'scss' || label === 'less') {
      return new cssWorker()
    }
    if (label === 'html' || label === 'handlebars' || label === 'razor') {
      return new htmlWorker()
    }
    if (label === 'typescript' || label === 'javascript') {
      return new tsWorker()
    }
    return new editorWorker()
  }
}

let depcompileEditor: any = null
let beforeEditor = null
let afterEditor = null
let exceptionEditor = null
let totalEditor = null
let lineEditor = null
setTimeout(() => {
  depcompileEditor = monaco.editor.create(document.getElementById('depcompile-code'), {
    value: "function hello() {\n\talert('Hello world!');\n}",
    language: 'java',
    theme: 'vs-dark',
    minimap: {
      enabled: false
    }
  })

  beforeEditor = monaco.editor.create(document.getElementById('redefine-point-before-content'), {
    value: "",
    language: 'java',
    theme: 'vs-dark',
    minimap: {
      enabled: false
    }
  })

  afterEditor = monaco.editor.create(document.getElementById('redefine-point-after-content'), {
    value: "",
    language: 'java',
    theme: 'vs-dark',
    minimap: {
      enabled: false
    }
  })

  exceptionEditor = monaco.editor.create(document.getElementById('redefine-point-exception-content'), {
    value: "",
    language: 'java',
    theme: 'vs-dark',
    minimap: {
      enabled: false
    }
  })

  totalEditor = monaco.editor.create(document.getElementById('redefine-point-total-content'), {
    value: "",
    language: 'java',
    theme: 'vs-dark',
    minimap: {
      enabled: false
    }
  })

  lineEditor = monaco.editor.create(document.getElementById('redefine-point-line-content'), {
    value: "",
    language: 'java',
    theme: 'vs-dark',
    minimap: {
      enabled: false
    }
  })
}, 2000)


interface Clazz {
  name: string
  methods?: Clazz[]
}

const filterClass = ref('')
const decompileCode = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()
const redefineData = reactive({
  className: '',
  methodName: '',
  position: '',
  line: '0',
  code: ''
})

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
    redefineData.className = name
    let source = await api.decompileClass(name)
    decompileCode.value = source.data
    depcompileEditor.setValue(decompileCode.value)
  } else if (node.level === 2) {
    redefineData.methodName = data.name
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

const applyRedefine = (type: string) => {
  redefineData.position = type
  switch (type) {
    case 'before':
      redefineData.code = beforeEditor.getValue()
      break;
    case 'after':
      redefineData.code = afterEditor.getValue()
      break;
    case 'exception':
      redefineData.code = exceptionEditor.getValue()
      break;
    case 'total':
      redefineData.code = totalEditor.getValue()
      break;
    case 'line':
      redefineData.code = lineEditor.getValue()
      break;
    default:
      break;
  }
  console.log(redefineData)
  api.redefineClass(redefineData).then(()  => {
    ElMessage({
      message: '应用成功',
      type: 'success',
    })
  }).catch(() => {
    ElMessage.error('应用失败')
  })
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

#depcompile-code {
  margin-top: 10px;
  width: 1000px;
  height: 1000px;
}

.redefine {
  margin: 0 10px;
}

.scrollbar-flex-content {
  display: flex;
  flex-direction: column;
  width: 500px;
}

.jhook-func-view-right-area {
  display: flex;
  height: 1000px;
}

.redefine-point {
  display: flex;
  flex-direction: column;
  margin: 10px 0;
}

.redefine-editor {
  width: 500px;
  height: 500px;
}
</style>
