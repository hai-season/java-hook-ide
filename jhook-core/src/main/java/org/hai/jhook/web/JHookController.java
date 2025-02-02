package org.hai.jhook.web;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.hai.jhook.bean.Result;
import org.hai.jhook.client.JHookClient;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
public class JHookController {

    @RequestMapping("/listJvm")
    public List<Map<String, String>> listJvm() {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        return list.stream().map(v -> {
            Map<String, String> data = new HashMap<>();
            data.put("id", v.id());
            data.put("displayName", v.displayName());
            return data;
        }).collect(Collectors.toList());
    }

    @RequestMapping("/attach/{pid}")
    public Result attach(@PathVariable("pid") String pid) throws Exception {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        Optional<VirtualMachineDescriptor> currentVmd =
                list.stream().filter(v -> v.id().equals(pid)).findFirst();
        if (!currentVmd.isPresent()) {
            throw new RuntimeException("unknown error");
        }
        VirtualMachineDescriptor desc = currentVmd.get();
        System.out.println("current desc: " + desc.displayName());
        VirtualMachine machine = VirtualMachine.attach(desc);
        machine.loadAgent("E:\\DemoProject\\java-hook-ide\\agent\\target\\agent-1.0-jar-with-dependencies.jar");
        return Result.success();
    }

    @RequestMapping("/class/{className}")
    public Result getClassByteCode(@PathVariable("className") String className) throws Exception {
        JHookClient connect = JHookClient.connect();
        byte[] data = connect.getClassByteCode(className);
        CtClass ctClass = ClassPool.getDefault().makeClass(new ByteArrayInputStream(data));
        CtMethod[] methods = ctClass.getDeclaredMethods();
        StringBuilder sb = new StringBuilder();
        for (CtMethod method : methods) {
            sb.append(method.getName()).append("\n");
        }
        connect.disconnect();
        return Result.success().setData(sb.toString());
    }
}
